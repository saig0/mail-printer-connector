package org.camunda.bpm.printer.mail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;

import org.apache.commons.mail.EmailException;
import org.camunda.bpm.printer.PrintJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);

	@Value("${mail.output:print}")
	private String printDirectory;

	@Value("${mail.subject:print}")
	private String mailSubject;

	@Autowired
	private MailService mailService;

	public Optional<PrintJob> processMessage(Message message) throws MessagingException, IOException, EmailException {
		Address[] fromAddress = message.getFrom();
		String from = fromAddress[0].toString();
		String subject = message.getSubject();
		String sentDate = message.getSentDate().toString();

		LOGGER.debug("process message '{}' from '{}' received '{}'", subject, from, sentDate);

		if (!alreayProcessed(message)) {

			if (matchSubject(subject)) {

				List<String> attachedFiles = processMessageAttachments(message, subject);

				message.setFlag(Flag.SEEN, true);

				if (!attachedFiles.isEmpty()) {

					PrintJob printJob = createPrintJob(message, from, subject, attachedFiles);
					return Optional.of(printJob);

				} else {
					LOGGER.warn("ignore message because it doesn't have an attachement");
				}

			} else if ("help".equalsIgnoreCase(subject)) {
				mailService.sendMessage(from, "Re: " + subject,
						"To print a file, send a mail with the subject '" + mailSubject + "'. "
								+ "You can add the following options:\n"
								+ "\t-p prints only the given pages (e.g. '-p=1', '-p=1,2', '-p=1-3')\n"
								+ "\t-c prints with colors instead of gray scale\n\n" + "Example:\n" + "\t"
								+ mailSubject + " -p=1 -c");

				message.setFlag(Flag.DELETED, true);

			} else {
				LOGGER.debug("ignore message because it doesn't match the subject '{}'", mailSubject);
			}
		} else {
			LOGGER.debug("ignore message because it is already processed '{}'", mailSubject);
		}
		return Optional.empty();
	}

	private List<String> processMessageAttachments(Message message, String subject)
			throws MessagingException, IOException {
		List<String> attachedFiles = new ArrayList<String>();

		if (message.getContentType().contains("multipart")) {
			// content may contain attachments
			Multipart multiPart = (Multipart) message.getContent();
			int numberOfParts = multiPart.getCount();
			for (int partCount = 0; partCount < numberOfParts; partCount++) {

				MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
				if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {

					Path file = saveAttachement(part.getFileName(), part.getDataHandler());
					attachedFiles.add(file.toAbsolutePath().toString());
				}
			}
		} else {
			// assuming a single file (e.g. a PDF)
			if (Part.ATTACHMENT.equalsIgnoreCase(message.getDisposition())) {

				Path file = saveAttachement(message.getFileName(), message.getDataHandler());
				attachedFiles.add(file.toAbsolutePath().toString());
			}
		}
		return attachedFiles;
	}

	private boolean alreayProcessed(Message message) throws MessagingException {
		return message.isSet(Flag.SEEN);
	}

	private boolean matchSubject(String subject) throws IOException {
		return subject.toLowerCase().startsWith(mailSubject.toLowerCase());
	}

	private Path saveAttachement(String fileName, DataHandler dataHandler) throws MessagingException, IOException {

		Path directory = Paths.get(printDirectory);
		if (!Files.exists(directory)) {
			Files.createDirectories(directory);
		}

		Path newFile = directory.resolve(fileName);

		Files.copy(dataHandler.getInputStream(), newFile);

		LOGGER.debug("downloaded attachment '{}' and saved in folder '{}'", newFile.getFileName(),
				directory.toAbsolutePath());
		return newFile;
	}

	private PrintJob createPrintJob(Message message, String from, String subject, List<String> attachedFiles) {
		PrintJob printJob = new PrintJob(message.getMessageNumber(), from, subject, attachedFiles);

		String options = subject.substring(mailSubject.length());
		parsePrintOptions(printJob, options);
		return printJob;
	}

	private void parsePrintOptions(PrintJob printJob, String options) {
		for (String option : options.toLowerCase().split(" ")) {

			if (option.startsWith("-p=")) {
				String pagesToPrint = option.substring(3);
				printJob.setPagesToPrint(pagesToPrint);

				LOGGER.debug("print option: page rage = '{}'", pagesToPrint);

			} else if (option.startsWith("-c=")) {
				printJob.setColorPrint(true);
				LOGGER.debug("print option: color print");

			}
		}
	}

}
