package org.camunda.bpm.printer;

import org.camunda.bpm.printer.cups.PrinterService;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.FilterType;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

@Configuration
@EnableAutoConfiguration
@ComponentScan( excludeFilters = {
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.camunda.bpm.printer.Application"),
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.camunda.bpm.printer.cups.*")})
public class TestConfig {

	@Bean
	public GreenMail greenMail() {
		GreenMail greenMail = new GreenMail(ServerSetupTest.ALL);
		greenMail.setUser("test@camunda.com", "bpmn");
				
		greenMail.start();
		
		return greenMail;
	}
	
	@Bean
	@DependsOn("greenMail")
	public Application application() {
		return new Application();
	}
	
	@Bean
	public PrinterService printerService() {
		return Mockito.mock(PrinterService.class);
	}
	
}
