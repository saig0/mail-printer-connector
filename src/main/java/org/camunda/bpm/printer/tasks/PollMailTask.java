package org.camunda.bpm.printer.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.mail.Address;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.printer.Configuration;
import org.camunda.bpm.printer.MailService;
import org.camunda.bpm.printer.PrintJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollMailTask implements JavaDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(PollMailTask.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.debug("poll mails from server");

		List<PrintJob> printJobs = poll();

		execution.setVariable("printJobs", printJobs);
	}

	public List<PrintJob> poll() throws Exception {
		List<PrintJob> printJobs = new ArrayList<PrintJob>();

		Folder folder = MailService.connect();

		List<Message> messages = Arrays.asList(folder.getMessages());
		LOGGER.debug("{} mails in folder '{}'", messages.size(), folder.getName());

		for (Message message : messages) {
			processMessage(message).ifPresent(printJobs::add);
		}

		return printJobs;
	}

	private Optional<PrintJob> processMessage(Message message) throws MessagingException, IOException {
		Address[] fromAddress = message.getFrom();
		String from = fromAddress[0].toString();
		String subject = message.getSubject();
		String sentDate = message.getSentDate().toString();

		LOGGER.debug("process message '{}' from '{}' received '{}'", subject, from, sentDate);

		if (matchSubject(subject)) {

			List<String> attachedFiles = new ArrayList<String>();

			if (message.getContentType().contains("multipart")) {
				// content may contain attachments
				Multipart multiPart = (Multipart) message.getContent();
				int numberOfParts = multiPart.getCount();
				for (int partCount = 0; partCount < numberOfParts; partCount++) {

					MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
					if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {

						File file = saveAttachement(subject, part);
						attachedFiles.add(file.getAbsolutePath());
					}
				}
			}

			message.setFlag(Flag.SEEN, true);

			if (!attachedFiles.isEmpty()) {
				return Optional.of(new PrintJob(message.getMessageNumber(), from, subject, attachedFiles));
			} else {
				LOGGER.warn("ignore message because it doesn't have an attachement");
			}
		} else {
			LOGGER.debug("ignore message because it doesn't match the subject '{}'", Configuration.getSubject());
		}
		return Optional.empty();
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
