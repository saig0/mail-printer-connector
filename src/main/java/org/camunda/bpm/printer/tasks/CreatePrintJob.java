package org.camunda.bpm.printer.tasks;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.camunda.bpm.printer.PrintJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreatePrintJob implements JavaDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreatePrintJob.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		Mail mail = (Mail) execution.getVariable("mail");

		PrintJob printJob = new PrintJob(mail);

		String options = mail.getSubject().substring("print".length());
		parsePrintOptions(printJob, options);

		LOGGER.debug("created print job: {}", printJob);

		execution.setVariable("printJob", printJob);
	}

	private void parsePrintOptions(PrintJob printJob, String options) {
		for (String option : options.toLowerCase().split(" ")) {

			if (option.startsWith("-p=")) {
				String pagesToPrint = option.substring(3);
				printJob.setPagesToPrint(pagesToPrint);

			} else if (option.startsWith("-c=")) {
				printJob.setColorPrint(true);
			}
		}
	}

}
