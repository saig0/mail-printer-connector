package org.camunda.bpm.printer.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.printer.PrintJob;
import org.camunda.bpm.printer.cups.GetPrintersCommand;
import org.camunda.bpm.printer.cups.PrintFileCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CupsPrintTask implements JavaDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(CupsPrintTask.class);

	@Value("${printer.name:MyPrinter}")
	private String printerName;

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		PrintJob printJob = (PrintJob) execution.getVariable("printJob");

		List<String> printerNames = new GetPrintersCommand().getPrinters();

		if (printerNames.contains(printerName)) {
			List<String> jobIds = printFiles(printJob);
			execution.setVariable("jobIds", jobIds);

		} else
			LOGGER.error("no printer found with name '{}'. Avaiable printers: {}", printerName, printerNames);
	}

	private List<String> printFiles(PrintJob printJob) throws IOException {
		List<String> jobIds = new ArrayList<>();

		for (String filePath : printJob.getFiles()) {

			File file = new File(filePath);
			if (file.exists()) {
				String jobId = printFile(file, printJob);
				jobIds.add(jobId);

			} else {
				throw new IllegalStateException("file not found: " + file);
			}
		}
		return jobIds;
	}

	private String printFile(File file, PrintJob printJob) throws IOException {
		LOGGER.debug("print file '{}' on printer '{}'", file, printerName);

		String jobId = new PrintFileCommand(printerName, file).printFile(printJob);
		LOGGER.debug("created print job with id '{}'", jobId);

		return jobId;
	}

}
