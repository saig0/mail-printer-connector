package org.camunda.bpm.printer.cups;

import java.io.File;
import java.util.List;

import org.camunda.bpm.printer.PrintJob;

public interface PrinterService {

	List<String> getPrinterNames() throws Exception;
	
	String printFile(String prinerName, File file, PrintJob printJob) throws Exception;
	
	boolean isCompletedJob(String jobId) throws Exception;
	
}
