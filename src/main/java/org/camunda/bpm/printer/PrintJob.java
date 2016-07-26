package org.camunda.bpm.printer;

import java.io.Serializable;

import org.camunda.bpm.extension.mail.dto.Mail;

public class PrintJob implements Serializable {

	public enum Orientation {
		PORTRAIT, LANDSCAPE
	}
	
	private static final long serialVersionUID = -2522993700692476834L;
	
	private final Mail mail;

	// print options
	private String pagesToPrint;
	private boolean colorPrint = false;
	private int numberOfCopies = 1;
	private Orientation orientation = Orientation.PORTRAIT;
	
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

	public int getNumberOfCopies() {
		return numberOfCopies;
	}

	public void setNumberOfCopies(int numberOfCopies) {
		this.numberOfCopies = numberOfCopies;
	}

	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}

	@Override
	public String toString() {
		return "PrintJob [mail=" + mail + ", pagesToPrint=" + pagesToPrint + ", colorPrint=" + colorPrint
				+ ", numberOfCopies=" + numberOfCopies + ", orientation=" + orientation + "]";
	}

}
