package org.camunda.bpm.printer.cups;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GetPrintersCommand {

	private static final String PRINTER_PREFIX = "printer";

	public List<String> getPrinters() throws IOException {
		List<String> printerNames = new ArrayList<>();

		ProcessBuilder processBuilder = new ProcessBuilder("lpstat", "-p");

		Process process = processBuilder.start();

		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		String line = null;
		while ((line = reader.readLine()) != null) {

			if (line.startsWith(PRINTER_PREFIX)) {

				String output = line.substring(PRINTER_PREFIX.length()).trim();
				String printerName = output.split("\\s")[0];

				printerNames.add(printerName);
			}
		}

		return printerNames;
	}

}
