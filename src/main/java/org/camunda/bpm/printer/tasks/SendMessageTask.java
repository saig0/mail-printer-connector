package org.camunda.bpm.printer.tasks;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.EmailException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.printer.MailConfiguration;
import org.camunda.bpm.printer.PrintJob;
import org.camunda.bpm.printer.mail.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SendMessageTask implements JavaDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageTask.class);

	@Autowired
	private MailService mailService;

	@Autowired
	private MailConfiguration mailConfiguration;

	@Value("${mail.sender:MyPrinter}")
	private String mailSender;

	private Expression text;

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		PrintJob printJob = (PrintJob) execution.getVariable("printJob");
		String messageContent = (String) text.getValue(execution);

		sendMessage(printJob, messageContent);
	}

	private void sendMessage(PrintJob printJob, String messageContent)
			throws IOException, EmailException, MessagingException {
		Message message = new MimeMessage(mailService.getSession());
		message.setFrom(new InternetAddress(mailSender));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(printJob.getFrom()));
		message.setSubject("Re: " + printJob.getSubject());
		message.setText(messageContent);

		LOGGER.debug("send notification mail '{}' to '{}'", printJob.getSubject(), printJob.getFrom());
		Transport.send(message, mailConfiguration.getUserName(), mailConfiguration.getPassword());
	}

}
