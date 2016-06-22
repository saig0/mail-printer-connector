package org.camunda.bpm.printer.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.extension.mail.dto.Attachment;
import org.camunda.bpm.printer.PrintJob;
import org.camunda.bpm.printer.cups.PrinterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CupsPrintTask implements JavaDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(CupsPrintTask.class);

	@Value("${printer.name:MyPrinter}")
	private String printerName;
	
	@Autowired
	private PrinterService service;

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		PrintJob printJob = (PrintJob) execution.getVariable("printJob");

		List<String> printerNames = service.getPrinterNames();

		if (printerNames.contains(printerName)) {
			List<String> jobIds = printFiles(printJob);
			execution.setVariable("jobIds", jobIds);

		} else {
			LOGGER.error("no printer found with name '{}'. Avaiable printers: {}", printerName, printerNames);
		}
	}

	private List<String> printFiles(PrintJob printJob) throws Exception {
		List<String> jobIds = new ArrayList<>();

		for (Attachment attachment : printJob.getMail().getAttachments()) {

			File file = new File(attachment.getPath());
			if (file.exists()) {
				String jobId = printFile(file, printJob);
				jobIds.add(jobId);

			} else {
				throw new IllegalStateException("file not found: " + file);
			}
		}
		return jobIds;
	}

	private String printFile(File file, PrintJob printJob) throws Exception {
		LOGGER.debug("print file '{}' on printer '{}'", file, printerName);

		String jobId = service.printFile(printerName, file, printJob);
		LOGGER.debug("created print job with id '{}'", jobId);

		return jobId;
	}

}
