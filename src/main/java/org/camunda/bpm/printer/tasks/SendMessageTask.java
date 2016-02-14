package org.camunda.bpm.printer.tasks;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.EmailException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.printer.Configuration;
import org.camunda.bpm.printer.MailService;
import org.camunda.bpm.printer.PrintJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendMessageTask implements JavaDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageTask.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		PrintJob printJob = (PrintJob) execution.getVariable("printJob");

		sendMessage(printJob);
	}

	private void sendMessage(PrintJob printJob) throws IOException, EmailException, MessagingException {
		Message message = new MimeMessage(MailService.getSession());
		message.setFrom(new InternetAddress(Configuration.getFrom()));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(printJob.getFrom()));
		message.setSubject("Re: " + printJob.getSubject());
		message.setText("...printed!");

		LOGGER.debug("send notification mail '{}' to '{}'", printJob.getSubject(), printJob.getFrom());
		Transport.send(message);
	}

}
