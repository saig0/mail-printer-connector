package org.camunda.bpm.printer.tasks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.extension.mail.dto.Attachment;
import org.camunda.bpm.printer.PrintJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeleteFileTask implements JavaDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFileTask.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		PrintJob printJob = (PrintJob) execution.getVariable("printJob");

		Path pathToAttachment = null;
		
		// delete files first
		for (Attachment attachment : printJob.getMail().getAttachments()) {
			pathToAttachment = Paths.get(attachment.getPath());
			Files.delete(pathToAttachment);
		}
		
		// then delete the empty directory		
		Path directory = pathToAttachment.getParent();

		LOGGER.debug("delete directory: " + directory);

		Files.delete(directory);
	}

}
