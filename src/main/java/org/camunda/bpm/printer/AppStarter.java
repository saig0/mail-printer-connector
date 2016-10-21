package org.camunda.bpm.printer;

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
import org.springframework.stereotype.Component;

@Component
public class AppStarter {

	@Autowired
	private RuntimeService runtimeService;

	private MailNotificationService notificationService;
	private MailConfiguration configuration;

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
		runtimeService.startProcessInstanceByKey("printerMailDispatching", 
				Variables.createVariables().putValue("mail", mail));
	}

	public void closeConnection() throws Exception {
		notificationService.stop();

		MailService mailService = MailServiceFactory.getService(configuration);
		mailService.close();
	}
	
}
