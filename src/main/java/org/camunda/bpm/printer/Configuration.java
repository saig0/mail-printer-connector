package org.camunda.bpm.printer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

	private static final String PROPERTIES_FILE = "/config.properties";

	private static Properties config;

	private static void ensureInitialized() throws IOException {
		if (config == null) {
			config = new Properties();

			InputStream inputStream = MailConfiguration.class.getResourceAsStream(PROPERTIES_FILE);
			if (inputStream == null) {
				throw new RuntimeException("properties not found: " + PROPERTIES_FILE);
			}

			config.load(inputStream);
		}
	}

	public static String getPrintDirectory() throws IOException {
		ensureInitialized();
		return (String) config.get("print.directory");
	}

	public static String getFolder() throws IOException {
		ensureInitialized();
		return (String) config.get("folder");
	}

	public static String getSubject() throws IOException {
		ensureInitialized();
		return (String) config.get("subject");
	}

	public static String getFrom() throws IOException {
		ensureInitialized();
		return (String) config.get("from");
	}

}
