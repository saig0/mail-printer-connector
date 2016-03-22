package org.camunda.bpm.printer.cups;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GetJobStatusCommand {

	private final String printerName;

	public GetJobStatusCommand(String printerName) {
		this.printerName = printerName;
	}

	public List<String> getOpenJobs() throws IOException {
		List<String> jobIds = new ArrayList<>();

		ProcessBuilder processBuilder = new ProcessBuilder("lpstat", "-P", printerName);

		Process process = processBuilder.start();

		InputStream inputStream = process.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader reader = new BufferedReader(inputStreamReader);

		String line = null;
		while ((line = reader.readLine()) != null) {

			if (line.startsWith(printerName)) {

				String jobId = line.split("\\s")[0];

				jobIds.add(jobId);
			}
		}

		return jobIds;
	}

}
