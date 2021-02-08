package com.usermanagement.service;

import com.sun.mail.smtp.SMTPTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

@Service
public class EmailService {

  private final String username;
  private final String password;
  private final String senderEmail;
  private final String carbonCopy;
  private final String emailSubject;
  private final String smtpServer;
  private final String port;
  private final String protocol;
  private final String smtpHost;
  private final String smtpAuth;
  private final String smtpPort;
  private final String smtpStarttlsEnable;
  private final String smtpStarttlsRequired;


  public EmailService(@Value("${email.protocol}") String protocol,
                      @Value("${email.username}") String username,
                      @Value("${email.password}") String password,
                      @Value("${email.sender}") String senderEmail,
                      @Value("${email.carbon.copy}") String carbonCopy,
                      @Value("${email.subject}") String emailSubject,
                      @Value("${email.server}") String smtpServer,
                      @Value("${email.host}") String smtpHost,
                      @Value("${email.auth}") String smtpAuth,
                      @Value("${email.port}") String smtpPort,
                      @Value("${email.starttls.enable}") String smtpStarttlsEnable,
                      @Value("${email.starttls.required}") String smtpStarttlsRequired,
                      @Value("${email.port.number}") String port) {

    this.protocol = protocol;
    this.username = username;
    this.password = password;
    this.senderEmail = senderEmail;
    this.carbonCopy = carbonCopy;
    this.emailSubject = emailSubject;
    this.smtpServer = smtpServer;
    this.smtpHost = smtpHost;
    this.smtpAuth = smtpAuth;
    this.smtpPort = smtpPort;
    this.smtpStarttlsEnable = smtpStarttlsEnable;
    this.smtpStarttlsRequired = smtpStarttlsRequired;
    this.port = port;
  }

  public void sendEmail(String firstName, String password, String email) throws MessagingException {
    Message message = this.createEmail(firstName, password, email);
    SMTPTransport smtpTransport = (SMTPTransport) getEmailSession().getTransport(protocol);
    smtpTransport.connect(smtpServer, username, this.password);
    smtpTransport.sendMessage(message, message.getAllRecipients());
    smtpTransport.close();
  }

  private Message createEmail(String firstName, String password, String email) throws MessagingException {
    String text = "Hello " + firstName + ",\n\n Your new account password is: " + password
        + "\n\nUser Management Application";
    Message message = new MimeMessage(getEmailSession());
    message.setFrom(new InternetAddress(senderEmail));
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
    message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(carbonCopy, false));
    message.setSubject(emailSubject);
    message.setText(text);
    message.setSentDate(new Date());
    message.saveChanges();
    return message;
  }

  private Session getEmailSession() {
    Properties properties = System.getProperties();
    properties.put(smtpHost, smtpServer);
    properties.put(smtpAuth, true);
    properties.put(smtpPort, port);
    properties.put(smtpStarttlsEnable, true);
    properties.put(smtpStarttlsRequired, true);

    return Session.getDefaultInstance(properties);
  }
}
