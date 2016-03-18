package org.camunda.bpm.printer.cups;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class PrintFileCommand {

	private static final String JOB_PREFIX = "request id is";

	private static final String COMMAND = "lp";

	private final String printer;
	private final File file;

	public PrintFileCommand(String printer, File file) {
		this.printer = printer;
		this.file = file;
	}

	public String printFile() throws IOException {
		String jobId = null;

		ProcessBuilder processBuilder = new ProcessBuilder(COMMAND, "-d", printer, file.toString());

		Process process = processBuilder.start();

		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		String line = null;
		while ((line = reader.readLine()) != null && jobId == null) {

			if (line.startsWith(JOB_PREFIX)) {

				String output = line.substring(JOB_PREFIX.length()).trim();
				jobId = output.split("\\s")[0];
			}
		}

		return jobId;
	}

}
