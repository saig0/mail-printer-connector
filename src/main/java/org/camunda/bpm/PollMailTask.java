package org.camunda.bpm;

import java.util.Collections;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class PollMailTask implements JavaDelegate {

	public void execute(DelegateExecution execution) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("poll...");

		execution.setVariable("mails", Collections.emptyList());
	}

}
