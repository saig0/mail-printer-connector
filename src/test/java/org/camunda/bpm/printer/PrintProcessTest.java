package org.camunda.bpm.printer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;

import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.extension.mail.MailContentType;
import org.camunda.bpm.printer.cups.PrinterService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { TestConfig.class })
public class PrintProcessTest {

	@Autowired
	private ManagementService managementService;

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private GreenMail greenMail;
	
	@Autowired
	private PrinterService printerService;
	
	@Test
	public void printAttachment() throws Exception {

		when(printerService.getPrinterNames()).thenReturn(Collections.singletonList("MyPrinter"));
		when(printerService.printFile(eq("MyPrinter"), any(File.class), any(PrintJob.class))).thenReturn("job-1");
		when(printerService.isCompletedJob("job-1")).thenReturn(true);
		
		sendMessageWithAttachment();

		// print file
		// delete mail
		
		waitForTimerJob();
		
		// check status of print job
		Job timerJob = managementService.createJobQuery().timers().activityId("waitForCheckPrintJobStatus").singleResult();
		managementService.executeJob(timerJob.getId());
				
		// delete files
		// send mail
		
		waitForProcessEnd();
		
		verify(printerService, times(1)).printFile(eq("MyPrinter"), any(File.class), any(PrintJob.class));
		
		MimeMessage[] mails = greenMail.getReceivedMessages();
	    assertThat(mails).hasSize(2);

	    MimeMessage mail = mails[0];
	    assertThat(mail.getSubject()).isEqualTo("print");
	    assertThat(mail.isSet(Flag.DELETED)).isTrue();
	    
	    mail = mails[1];
	    assertThat(mail.getSubject()).isEqualTo("RE: print");
	    assertThat(GreenMailUtil.getBody(mail)).isEqualTo("...printed!");
	}
	
	@Test
	@Ignore("TODO")
	public void noPrinter() throws Exception {

		when(printerService.getPrinterNames()).thenReturn(Collections.emptyList());
		
		// TODO write the test
	}
	
	@Test
	@Ignore("TODO")
	public void noAttachment() throws Exception {

		// TODO write the test
	}
	
	@Test
	@Ignore("TODO")
	public void multipleAttachment() throws Exception {

		// TODO write the test
	}
	
	@Test
	@Ignore("TODO")
	public void help() throws Exception {

		// TODO write the test
	}

	private void sendMessageWithAttachment() throws MessagingException, AddressException, FolderException {
		Session smtpSession = greenMail.getSmtp().createSession();
		MimeMessage message = createMimeMessage(smtpSession);

		message.setContent("", MailContentType.TEXT_PLAIN.getType());
		message.setFileName("attachment.pdf");
		message.setDisposition(Part.ATTACHMENT);

		GreenMailUtil.sendMimeMessage(message);
	}

	private MimeMessage createMimeMessage(Session session) throws MessagingException, AddressException {
		MimeMessage message = new MimeMessage(session);

		message.setFrom(new InternetAddress("from@camunda.com"));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("test@camunda.com"));
		message.setSubject("print");

		return message;
	}

	private void waitForTimerJob() throws Exception {
		JobQuery query = managementService.createJobQuery().timers();
		while(query.count() == 0) {
			Thread.sleep(500);
		}
	}
	

	private void waitForProcessEnd() throws Exception {
		ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
		while(query.count() > 0) {
			Thread.sleep(500);
		}
		
	}
	
}
