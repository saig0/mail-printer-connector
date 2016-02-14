package org.camunda.bpm.printer.tasks;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.printer.MailService;
import org.camunda.bpm.printer.PrintJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteMessageTask implements JavaDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteMessageTask.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		PrintJob printJob = (PrintJob) execution.getVariable("printJob");

		deleteMail(printJob);
	}

	private void deleteMail(PrintJob printJob) throws Exception {
		LOGGER.debug("delete message '{}'", printJob.getSubject());

		Folder folder = MailService.connect();

		Message message = folder.getMessage(printJob.getMessageNumber());
		message.setFlag(Flag.DELETED, true);
	}

}
