package org.camunda.bpm.printer;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {

		LOGGER.debug("init process engine");

		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

		LOGGER.debug("deploy process");

		processEngine.getRepositoryService().createDeployment().addClasspathResource("mail-to-printer-process.bpmn")
				.deploy();

		LOGGER.debug("start process");

		processEngine.getRuntimeService().startProcessInstanceByKey("printerProcess");

		LOGGER.debug("shutdown");

		MailService.close();
	}

}
