package org.camunda.bpm.printer.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.printer.Configuration;
import org.camunda.bpm.printer.PrintJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintTask implements JavaDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(PrintTask.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		PrintJob printJob = (PrintJob) execution.getVariable("printJob");

		PrintService printService = getPrintService();

		for (String filePath : printJob.getFiles()) {

			File file = new File(filePath);
			if (file.exists()) {
				printFile(printService, file);
			} else {
				throw new IllegalStateException("file not found: " + file);
			}
		}
	}

	private PrintService getPrintService() throws IOException {

		DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		PrintRequestAttributeSet patts = new HashPrintRequestAttributeSet();
		// patts.add(Sides.DUPLEX);

		List<PrintService> printServices = Arrays.asList(PrintServiceLookup.lookupPrintServices(flavor, patts));
		LOGGER.debug("avaiable printers: " + printServices);

		if (printServices.size() == 0) {
			throw new IllegalStateException("no printers found");
		}

		for (PrintService printService : printServices) {
			if (Configuration.getPrinterName().equals(printService.getName())) {
				LOGGER.debug("select printer: " + printService.getName());
				return printService;
			}
		}
		throw new IllegalStateException(
				"No printer selected. Looking for printer with name: " + Configuration.getPrinterName());
	}

	private void printFile(PrintService printService, File file)
			throws FileNotFoundException, IOException, PrintException {

		LOGGER.debug("print file: " + file);

		FileInputStream inputStream = new FileInputStream(file);
		Doc doc = new SimpleDoc(inputStream, DocFlavor.INPUT_STREAM.AUTOSENSE, null);

		DocPrintJob printJob = printService.createPrintJob();

		printJob.addPrintJobListener(new PrintJobListener() {

			@Override
			public void printJobRequiresAttention(PrintJobEvent job) {
				System.out.println("?");
			}

			@Override
			public void printJobNoMoreEvents(PrintJobEvent job) {
				System.out.println("-");
			}

			@Override
			public void printJobFailed(PrintJobEvent job) {
				LOGGER.error("failed to print: " + job.getPrintJob());
			}

			@Override
			public void printJobCompleted(PrintJobEvent job) {
				LOGGER.debug("successful printed: " + job.getPrintJob());
				// TODO continue process
			}

			@Override
			public void printJobCanceled(PrintJobEvent job) {
				LOGGER.warn("canceled print job: " + job.getPrintJob());
			}

			@Override
			public void printDataTransferCompleted(PrintJobEvent job) {
				System.out.println(".");
			}
		});

		printJob.print(doc, new HashPrintRequestAttributeSet());

		inputStream.close();
	}

}
