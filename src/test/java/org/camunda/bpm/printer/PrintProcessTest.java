package org.camunda.bpm.printer;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

public class PrintProcessTest {

	@Rule
	public ProcessEngineRule processEngineRule = new ProcessEngineRule();

	@Test
	@Deployment(resources = "mail-to-printer-process.bpmn")
	public void happyPath() {
		RuntimeService runtimeService = processEngineRule.getRuntimeService();
		runtimeService.startProcessInstanceByKey("printerProcess");

		// runtimeService.correlateMessage("printed");
	}

}
