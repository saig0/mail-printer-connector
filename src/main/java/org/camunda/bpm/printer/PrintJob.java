package org.camunda.bpm.printer;

import java.io.Serializable;
import java.util.List;

public class PrintJob implements Serializable {

	private static final long serialVersionUID = -2522993700692476834L;

	private final int messageNumber;
	private final String from;
	private final String subject;
	private final List<String> files;

	public PrintJob(int messageNumber, String from, String subject, List<String> files) {
		this.messageNumber = messageNumber;
		this.from = from;
		this.subject = subject;
		this.files = files;
	}

	public List<String> getFiles() {
		return files;
	}

	public int getMessageNumber() {
		return messageNumber;
	}

	public String getFrom() {
		return from;
	}

	public String getSubject() {
		return subject;
	}

}
