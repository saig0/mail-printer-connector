package org.camunda.bpm.printer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.extension.mail.MailConnectors;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.camunda.bpm.extension.mail.notification.MailNotificationService;
import org.camunda.bpm.extension.mail.service.MailService;
import org.camunda.bpm.extension.mail.service.MailServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	@Autowired
	private RuntimeService runtimeService;

	private MailNotificationService notificationService;
	private MailConfiguration configuration;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@PostConstruct
	public void connectToMailServer() throws Exception {
		configuration = MailConfigurationFactory.getConfiguration();
		notificationService = new MailNotificationService(configuration);

		notificationService.registerMailHandler(this::startProcessInstance);
		notificationService.start();

		MailConnectors.pollMails()
			.createRequest()
			.execute()
			.getMails()
			.forEach(this::startProcessInstance);
	}

	private void startProcessInstance(Mail mail) {
		runtimeService.startProcessInstanceByKey("mailDispatching", 
				Variables.createVariables().putValue("mail", mail));
	}

	@PreDestroy
	public void closeConnection() throws Exception {
		notificationService.stop();

		MailService mailService = MailServiceFactory.getService(configuration);
		mailService.close();
	}

}
