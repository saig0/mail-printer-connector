package org.camunda.bpm.printer.mail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.printer.PrintJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sun.mail.imap.IMAPFolder;

@Component
public class ImapMailConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImapMailConnector.class);

	@Autowired
	private MailService mailService;

	@Autowired
	private MessageProcessor messageProcessor;

	@Autowired
	private RuntimeService runtimeService;

	private IdleThread idleThread;

	public void connect() throws Exception {

		IMAPFolder folder = mailService.connect();

		// poll existing messages
		List<Message> messages = Arrays.asList(folder.getMessages());
		if (!messages.isEmpty()) {
			processMessages(folder, messages);
		}

		folder.addMessageCountListener(new MessageCountAdapter() {

			@Override
			public void messagesAdded(MessageCountEvent event) {
				List<Message> messages = Arrays.asList(event.getMessages());

				processMessages(folder, messages);
			}

		});

		idleThread = new IdleThread(mailService, folder);
		idleThread.setDaemon(false);
		idleThread.start();

		// idleThread.join();
	}

	private void processMessages(IMAPFolder folder, List<Message> messages) {
		try {
			LOGGER.debug("{} mails in folder '{}'", messages.size(), folder.getName());

			for (Message message : messages) {
				messageProcessor.processMessage(message).ifPresent(this::startProcessInstance);
			}

		} catch (MessagingException | IOException e) {
			LOGGER.error("try to process new messages", e);
		}
	}

	private void startProcessInstance(PrintJob printJob) {
		runtimeService.startProcessInstanceByMessage("newPrintJob",
				Variables.createVariables().putValue("printJob", printJob));
	}

	public void close() throws Exception {
		idleThread.kill();

		mailService.close();
	}

	private static class IdleThread extends Thread {
		private final MailService mailService;
		private final IMAPFolder folder;
		private volatile boolean running = true;

		public IdleThread(MailService mailService, IMAPFolder folder) {
			super();
			this.mailService = mailService;
			this.folder = folder;
		}

		public synchronized void kill() {
			this.running = false;
		}

		@Override
		public void run() {
			while (running) {
				waitForMails();
			}
		}

		private void waitForMails() {
			try {
				mailService.ensureOpen(folder);

				LOGGER.debug("waiting for mails");

				folder.idle();

			} catch (Exception e) {
				LOGGER.error("waiting for new messages", e);

				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// ignore
				}
			}
		}
	}

}
