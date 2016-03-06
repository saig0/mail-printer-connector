package org.camunda.bpm.printer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.camunda.bpm.printer.mail.ImapMailConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	@Autowired
	private ImapMailConnector mailConnector;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@PostConstruct
	public void connectToMailServer() throws Exception {
		mailConnector.connect();
	}

	@PreDestroy
	public void closeConnection() throws Exception {
		mailConnector.close();
	}

}
