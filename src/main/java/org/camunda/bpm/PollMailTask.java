package org.camunda.bpm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class PollMailTask implements JavaDelegate {

	private static final String MAIL_PROPERTIES = "/mail.properties";

	public void execute(DelegateExecution execution) throws Exception {
		System.out.println("poll...");

		poll();

		execution.setVariable("mails", Collections.emptyList());
	}

	public void poll() throws Exception {

		Properties props = loadProperties();

		Session session = Session.getDefaultInstance(props, null);

		Store store = session.getStore("imaps");
		store.connect(props.getProperty("host"), props.getProperty("user"), props.getProperty("password"));

		Folder inbox = store.getFolder("inbox");
		inbox.open(Folder.READ_ONLY);
		int messageCount = inbox.getMessageCount();

		System.out.println("Total Messages:- " + messageCount);

		Message[] messages = inbox.getMessages();
		System.out.println("------------------------------");
		for (int i = 0; i < messageCount; i++) {
			System.out.println("Mail Subject:- " + messages[i].getSubject());
		}

		inbox.close(true);
		store.close();
	}

	private Properties loadProperties() throws IOException {
		Properties mailProperties = new Properties();
		InputStream inputStream = getClass().getResourceAsStream(MAIL_PROPERTIES);

		if (inputStream == null) {
			throw new RuntimeException("mail properties not found: " + MAIL_PROPERTIES);
		}

		mailProperties.load(inputStream);

		return mailProperties;
	};

}
