package org.camunda.bpm.printer.tasks;

import java.io.File;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.printer.PrintJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteFileTask implements JavaDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFileTask.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		PrintJob printJob = (PrintJob) execution.getVariable("printJob");

		for (String file : printJob.getFiles()) {

			LOGGER.debug("delete file: " + file);

			new File(file).delete();
		}
	}

}
