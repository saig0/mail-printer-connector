package org.camunda.bpm.printer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollMailTask implements JavaDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(PollMailTask.class);

	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.debug("poll mails from server");

		poll();

		execution.setVariable("mails", Collections.emptyList());
	}

	public void poll() throws Exception {

		Store store = connectToServer();
		Folder folder = openFolder(store);

		List<Message> messages = Arrays.asList(folder.getMessages());
		LOGGER.debug("{} mails in folder '{}'", messages.size(), folder.getName());

		for (Message message : messages) {
			processMessage(message);
		}

		folder.close(true);
		store.close();
	}

	private Store connectToServer() throws IOException, NoSuchProviderException, MessagingException {
		Session session = createSession();

		Store store = session.getStore("imaps");
		store.connect(MailConfiguration.getHost(), MailConfiguration.getUserName(), MailConfiguration.getPassword());
		return store;
	}

	private Session createSession() throws IOException {
		Properties props = MailConfiguration.getProperties();
		Session session = Session.getDefaultInstance(props, null);
		return session;
	}

	private Folder openFolder(Store store) throws MessagingException, IOException {
		Folder folder = store.getFolder(Configuration.getFolder());
		folder.open(Folder.READ_ONLY);
		return folder;
	}

	private void processMessage(Message message) throws MessagingException, IOException {
		Address[] fromAddress = message.getFrom();
		String from = fromAddress[0].toString();
		String subject = message.getSubject();
		String sentDate = message.getSentDate().toString();

		LOGGER.debug("process message '{}' from '{}' received '{}'", subject, from, sentDate);

		if (matchSubject(subject)) {

			List<File> attachFiles = new ArrayList<File>();

			if (message.getContentType().contains("multipart")) {
				// content may contain attachments
				Multipart multiPart = (Multipart) message.getContent();
				int numberOfParts = multiPart.getCount();
				for (int partCount = 0; partCount < numberOfParts; partCount++) {

					MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
					if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {

						File file = saveAttachement(subject, part);
						attachFiles.add(file);
					}
				}
			}

			if (attachFiles.isEmpty()) {
				LOGGER.warn("ignore message because it doesn't have an attachement");
			}
		} else {
			LOGGER.debug("ignore message because it doesn't match the subject '{}'", Configuration.getSubject());
		}
	}

	private boolean matchSubject(String subject) throws IOException {
		return subject.startsWith(Configuration.getSubject());
	}

	private File saveAttachement(String subject, MimeBodyPart part) throws MessagingException, IOException {
		String fileName = part.getFileName();

		Path directory = Paths.get(Configuration.getPrintDirectory());
		if (!Files.exists(directory)) {
			Files.createDirectories(directory);
		}

		File newFile = new File(directory.toFile(), fileName);
		part.saveFile(newFile);

		LOGGER.debug("downloaded attachment '{}' and saved in folder '{}'", newFile.getName(),
				directory.toAbsolutePath());
		return newFile;
	}

}
