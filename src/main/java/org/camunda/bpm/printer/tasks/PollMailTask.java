package org.camunda.bpm.printer.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.Folder;
import javax.mail.Message;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.printer.PrintJob;
import org.camunda.bpm.printer.mail.MailService;
import org.camunda.bpm.printer.mail.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PollMailTask implements JavaDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(PollMailTask.class);

	@Autowired
	private MailService mailService;

	@Autowired
	private MessageProcessor messageProcessor;

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.debug("poll mails from server");

		List<PrintJob> printJobs = poll();

		execution.setVariable("printJobs", printJobs);
	}

	public List<PrintJob> poll() throws Exception {
		List<PrintJob> printJobs = new ArrayList<PrintJob>();

		// re-connect to mail server - otherwise it poll already deleted mails
		mailService.close();

		Folder folder = mailService.connect();

		List<Message> messages = Arrays.asList(folder.getMessages());
		LOGGER.debug("{} mails in folder '{}'", messages.size(), folder.getName());

		for (Message message : messages) {
			messageProcessor.processMessage(message).ifPresent(printJobs::add);
		}

		return printJobs;
	}

}
