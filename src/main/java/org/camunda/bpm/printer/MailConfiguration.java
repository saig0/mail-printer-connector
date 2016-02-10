package org.camunda.bpm.printer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MailConfiguration {

	private static final String PROPERTIES_FILE = "/mail.properties";

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

	public static String getUserName() throws IOException {
		ensureInitialized();
		return (String) config.get("user");
	}

	public static String getPassword() throws IOException {
		ensureInitialized();
		return (String) config.get("password");
	}

	public static String getHost() throws IOException {
		ensureInitialized();
		return (String) config.get("host");
	}

	public static Properties getProperties() throws IOException {
		ensureInitialized();
		return config;
	}

}
