package org.camunda.bpm.printer;

import java.io.Serializable;

import org.camunda.bpm.extension.mail.dto.Mail;

public class PrintJob implements Serializable {

	private static final long serialVersionUID = -2522993700692476834L;

	private final Mail mail;

	// print options
	private String pagesToPrint;
	private boolean colorPrint = false;

	public PrintJob(Mail mail) {
		this.mail = mail;
	}

	public Mail getMail() {
		return mail;
	}

	public String getPagesToPrint() {
		return pagesToPrint;
	}

	public void setPagesToPrint(String pagesToPrint) {
		this.pagesToPrint = pagesToPrint;
	}

	public boolean isColorPrint() {
		return colorPrint;
	}

	public void setColorPrint(boolean colorPrint) {
		this.colorPrint = colorPrint;
	}

	@Override
	public String toString() {
		return "PrintJob [mail=" + mail + ", pagesToPrint=" + pagesToPrint + ", colorPrint=" + colorPrint + "]";
	}

}
