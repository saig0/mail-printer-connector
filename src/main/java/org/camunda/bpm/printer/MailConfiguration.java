package org.camunda.bpm.printer;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MailConfiguration {

	@Value("${mail.smtp.host:smtp.gmail.com}")
	private String smtpHost;

	@Value("${mail.smtp.port:465}")
	private String smtpPort;

	@Value("${mail.smtp.auth:true}")
	private String smtpAuth;

	@Value("${mail.smtp.ssl.enable:true}")
	private String smtpSslEnable;

	@Value("${mail.smtp.socketFactory.port:465}")
	private String smtpSocketFactoryPort;

	@Value("${mail.smtp.socketFactory.class:javax.net.ssl.SSLSocketFactory}")
	private String smtpSocketFactoryClass;

	@Value("${mail.imaps.host:imap.gmail.com}")
	private String imapHost;

	@Value("${mail.imaps.port:993}")
	private String imapPort;

	@Value("${mail.imaps.timeout:10000}")
	private String imapsTimeout;

	@Value("${mail.store.protocol:imaps}")
	private String storeProtocol;

	@Value("${mail.user}")
	private String user;

	@Value("${mail.password}")
	private String password;

	public String getUserName() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public Properties getProperties() {
		Properties properties = new Properties();

		// smtp
		properties.put("mail.smtp.host", smtpHost);
		properties.put("mail.smtp.port", smtpPort);
		properties.put("mail.smtp.auth", smtpAuth);
		properties.put("mail.smtp.ssl.enable", smtpSslEnable);
		properties.put("mail.smtp.socketFactory.port", smtpSocketFactoryPort);
		properties.put("mail.smtp.socketFactory.class", smtpSocketFactoryClass);

		// imap
		properties.put("mail.imaps.host", imapHost);
		properties.put("mail.imaps.port", imapPort);
		properties.put("mail.store.protocol", storeProtocol);
		properties.put("mail.imaps.timeout", imapsTimeout);

		return properties;
	}

}
