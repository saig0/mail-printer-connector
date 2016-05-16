package org.camunda.bpm.printer.tasks;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.printer.PrintJob;
import org.camunda.bpm.printer.mail.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SendMessageTask implements JavaDelegate {

	@Autowired
	private MailService mailService;

	private Expression text;

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		PrintJob printJob = (PrintJob) execution.getVariable("printJob");
		String messageContent = (String) text.getValue(execution);

		mailService.sendMessage(printJob.getFrom(), "Re: " + printJob.getSubject(), messageContent);
	}

}
