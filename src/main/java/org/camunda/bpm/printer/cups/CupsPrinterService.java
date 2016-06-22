package org.camunda.bpm.printer.cups;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.camunda.bpm.printer.PrintJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CupsPrinterService implements PrinterService {

	@Autowired
	private CupsJobStatusService statusService;

	@Override
	public List<String> getPrinterNames() throws IOException {
		return new GetPrintersCommand().getPrinters();
	}

	@Override
	public String printFile(String printerName, File file, PrintJob printJob) throws IOException {
		return new PrintFileCommand(printerName, file).printFile(printJob);
	}

	@Override
	public boolean isCompletedJob(String jobId) throws IOException {
		return statusService.isCompleted(jobId);
	}

}
