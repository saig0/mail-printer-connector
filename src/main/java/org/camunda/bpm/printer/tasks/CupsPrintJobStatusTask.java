package org.camunda.bpm.printer.tasks;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.printer.cups.PrinterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CupsPrintJobStatusTask implements JavaDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(CupsPrintJobStatusTask.class);

	@Autowired
	private PrinterService service;

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		String jobId = (String) execution.getVariable("jobId");

		LOGGER.debug("check status of print job with id '{}'", jobId);

		boolean completed = service.isCompletedJob(jobId);

		LOGGER.debug("job '{}' is {}", jobId, completed ? "completed" : "open");

		execution.setVariableLocal("isPrinted", completed);
	}

}
