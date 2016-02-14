package org.camunda.bpm.printer;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

	private static Session session = null;
	private static Store store = null;
	private static Folder folder = null;

	public static Session getSession() throws IOException {
		if (session == null) {
			LOGGER.debug("open session");

			Properties props = MailConfiguration.getProperties();
			session = Session.getDefaultInstance(props, new MailAuthenticatorExtension());
		}
		return session;
	}

	public static Folder connect() throws Exception {
		if (store == null && folder == null) {
			LOGGER.debug("connect to mail server...");

			store = connectToServer();
			folder = openFolder(store);

			LOGGER.debug("connected.");
		}

		return folder;
	}

	private static Store connectToServer() throws IOException, NoSuchProviderException, MessagingException {
		Session session = getSession();

		Store store = session.getStore("imaps");
		store.connect(MailConfiguration.getHost(), MailConfiguration.getUserName(), MailConfiguration.getPassword());
		return store;
	}

	private static Folder openFolder(Store store) throws MessagingException, IOException {
		Folder folder = store.getFolder(Configuration.getFolder());
		folder.open(Folder.READ_WRITE);
		return folder;
	}

	public static void close() throws Exception {
		if (folder != null && store != null) {
			LOGGER.debug("close connection to mail server");

			folder.close(true);
			store.close();
		} else {
			throw new IllegalStateException("not connected yet");
		}
	}

	private static final class MailAuthenticatorExtension extends javax.mail.Authenticator {
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			try {
				return new PasswordAuthentication(MailConfiguration.getUserName(), MailConfiguration.getPassword());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
