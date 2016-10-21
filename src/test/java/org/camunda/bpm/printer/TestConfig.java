package org.camunda.bpm.printer;

import java.io.IOException;

import javax.sql.DataSource;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.printer.cups.PrinterService;
import org.camunda.connect.plugin.impl.ConnectProcessEnginePlugin;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

@Configuration
@ComponentScan("org.camunda.bpm.printer.tasks")
@PropertySource("classpath:/printer-application.properties")
public class TestConfig {

	@Autowired
	private ResourcePatternResolver resourceLoader;
	
	@Bean(destroyMethod = "stop")
	public GreenMail greenMail() {
		GreenMail greenMail = new GreenMail(ServerSetupTest.ALL);
				
		greenMail.setUser("test@camunda.com", "bpmn");
		
		greenMail.start();
		
		return greenMail;
	}
	
	@Bean(destroyMethod = "closeConnection")
	@DependsOn("greenMail")
	public AppStarter appStarter() throws Exception {
		AppStarter starter = new AppStarter();
		
		starter.connectToMailServer();
		
		return starter;
	}
	
	@Bean
	public PrinterService printerService() {
		return Mockito.mock(PrinterService.class);
	}
	
	@Bean
	public DataSource dataSource() {
		SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
		dataSource.setDriverClass(org.h2.Driver.class);
		dataSource.setUrl("jdbc:h2:mem:camunda-test;DB_CLOSE_DELAY=-1");
		dataSource.setUsername("sa");
		dataSource.setPassword("");
		return dataSource;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}

	@Bean
	public SpringProcessEngineConfiguration processEngineConfiguration() throws IOException {
		SpringProcessEngineConfiguration config = new SpringProcessEngineConfiguration();
	
		config.setDataSource(dataSource());
		config.setDatabaseSchemaUpdate("true");

		config.setTransactionManager(transactionManager());

		config.setHistory("audit");
		config.setJobExecutorActivate(true);
		config.setDbMetricsReporterActivate(false);

		Resource[] resources = resourceLoader.getResources("classpath*:/*.bpmn");
		config.setDeploymentResources(resources);

		config.getProcessEnginePlugins().add(new ConnectProcessEnginePlugin());

		return config;
	}

	@Bean
	public ProcessEngineFactoryBean processEngine() throws IOException {
		ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
		factoryBean.setProcessEngineConfiguration(processEngineConfiguration());
		return factoryBean;
	}

	@Bean
	public RepositoryService repositoryService(ProcessEngine processEngine) {
		return processEngine.getRepositoryService();
	}

	@Bean
	public RuntimeService runtimeService(ProcessEngine processEngine) {
		return processEngine.getRuntimeService();
	}

	@Bean
	public ManagementService managementService(ProcessEngine processEngine) {
		return processEngine.getManagementService();
	}
	
}
