package org.camunda.bpm.printer.mail;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.EmailException;
import org.camunda.bpm.printer.MailConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

@Component
public class MailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

	@Autowired
	private MailConfiguration mailConfiguration;

	private static Session session = null;
	private static IMAPStore store = null;
	private static IMAPFolder folder = null;

	@Value("${mail.folder:inbox}")
	private String mailFolder;

	@Value("${mail.sender:MyPrinter}")
	private String mailSender;

	public Session getSession() throws IOException {
		if (session == null) {
			LOGGER.debug("open session");

			Properties props = mailConfiguration.getProperties();
			// session = Session.getDefaultInstance(props);
			session = Session.getInstance(props);
		}
		return session;
	}

	public IMAPFolder connect() throws Exception {
		if (store == null && folder == null) {
			LOGGER.debug("connect to mail server...");

			store = connectToServer();
			folder = openFolder(store);

			LOGGER.debug("connected.");
		}

		return folder;
	}

	private IMAPStore connectToServer() throws IOException, NoSuchProviderException, MessagingException {
		Session session = getSession();

		IMAPStore store = (IMAPStore) session.getStore("imaps");
		store.connect(mailConfiguration.getUserName(), mailConfiguration.getPassword());

		if (!store.hasCapability("IDLE")) {
			throw new IllegalStateException("IDLE not supported");
		}

		return store;
	}

	private IMAPFolder openFolder(IMAPStore store) throws MessagingException, IOException {
		if (folder == null) {
			folder = (IMAPFolder) store.getFolder(mailFolder);
			openFolder(folder);
		}
		return folder;
	}

	private void openFolder(Folder folder) throws MessagingException {
		if (!folder.isOpen()) {
			LOGGER.debug("open folder '{}'", folder.getName());

			folder.open(Folder.READ_WRITE);

			if (!folder.isOpen()) {
				throw new IllegalStateException("folder is not open");
			}
		}
	}

	public void close() throws Exception {
		if (folder != null && store != null) {
			LOGGER.debug("close connection to mail server");

			folder.close(true);
			store.close();

			folder = null;
			store = null;
		} else {
			throw new IllegalStateException("not connected yet");
		}
	}

	public void ensureOpen(final Folder folder) throws MessagingException, IOException {
		if (folder == null) {
			throw new IllegalArgumentException("folder is null");
		}

		Store store = folder.getStore();
		if (store != null && !store.isConnected()) {
			store.connect(mailConfiguration.getUserName(), mailConfiguration.getPassword());
		}

		openFolder(folder);
	}

	public void sendMessage(String to, String subject, String messageContent)
			throws IOException, EmailException, MessagingException {
		Message message = new MimeMessage(getSession());
		message.setFrom(new InternetAddress(mailSender));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		message.setSubject("Re: " + subject);
		message.setText(messageContent);

		LOGGER.debug("send mail '{}' to '{}'", subject, to);
		Transport.send(message, mailConfiguration.getUserName(), mailConfiguration.getPassword());
	}

}
