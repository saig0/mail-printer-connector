package org.camunda.bpm.printer.tasks;

import java.io.File;
import java.io.IOException;
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
			printFiles(printJob);

		} else {
			LOGGER.error("no printer found with name '{}'. Avaiable printers: {}", printerName, printerNames);
		}
	}

	private void printFiles(PrintJob printJob) throws IOException {
		for (String filePath : printJob.getFiles()) {

			File file = new File(filePath);
			if (file.exists()) {
				printFile(file, printJob);
			} else {
				throw new IllegalStateException("file not found: " + file);
			}
		}
	}

	private void printFile(File file, PrintJob printJob) throws IOException {
		LOGGER.debug("print file '{}' on printer '{}'", file, printerName);

		String jobId = new PrintFileCommand(printerName, file).printFile();
		LOGGER.debug("created print job with id '{}'", jobId);
	}

}
