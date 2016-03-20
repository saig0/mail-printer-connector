package org.camunda.bpm.printer.cups;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.printer.PrintJob;

public class PrintFileCommand {

	private static final String JOB_PREFIX = "request id is";

	private static final String COMMAND = "lp";

	private final String printerName;
	private final File file;

	public PrintFileCommand(String printer, File file) {
		this.printerName = printer;
		this.file = file;
	}

	public String printFile(PrintJob printJob) throws IOException {
		List<String> command = buildCommand(printJob);

		ProcessBuilder processBuilder = new ProcessBuilder(command);

		Process process = processBuilder.start();

		String jobId = getJobId(process);

		return jobId;
	}

	private List<String> buildCommand(PrintJob printJob) {
		List<String> arguments = new ArrayList<>();
		arguments.add(COMMAND);

		arguments.add("-d");
		arguments.add(printerName);

		getOptions(printJob).stream().forEach(option -> {
			arguments.add("-o");
			arguments.add(option);
		});

		arguments.add(file.toString());
		return arguments;
	}

	private List<String> getOptions(PrintJob printJob) {
		List<String> options = new ArrayList<>();

		if (printJob.isColorPrint()) {
			options.add("ColorMode=RGB");
		} else {
			options.add("ColorMode=KGray");
		}

		String pagesToPrint = printJob.getPagesToPrint();
		if (pagesToPrint != null && !pagesToPrint.isEmpty()) {
			options.add("page-rage=" + pagesToPrint);
		}

		return options;
	}

	private String getJobId(Process process) throws IOException {
		InputStream inputStream = process.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader reader = new BufferedReader(inputStreamReader);

		String line = null;
		while ((line = reader.readLine()) != null) {

			if (line.startsWith(JOB_PREFIX)) {

				String output = line.substring(JOB_PREFIX.length()).trim();
				String jobId = output.split("\\s")[0];

				return jobId;
			}
		}
		return null;
	}

}
