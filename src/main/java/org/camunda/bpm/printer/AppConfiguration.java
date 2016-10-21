package org.camunda.bpm.printer;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("org.camunda.bpm.printer")
@PropertySource("file:${CONFIG_DIR}/printer-application.properties")
public class AppConfiguration {

	@Bean
	public ProcessEngineService processEngineService() {
		return BpmPlatform.getProcessEngineService();
	}

	@Bean
	  public ProcessEngine processEngine(ProcessEngineService processEngineService) {
	    return processEngineService.getDefaultProcessEngine();
	  }

	@Bean
	public RepositoryService repositoryService(ProcessEngine processEngine) {
		return processEngine.getRepositoryService();
	}

	@Bean
	public RuntimeService runtimeService(ProcessEngine processEngine) {
		return processEngine.getRuntimeService();
	}

}
