package com.github.estegp.secure.mail.mimemultipart.helper;

import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public final class MailBuilder {
  /**
   * Builds the message with the session and the content
   *
   * @return the email to be sent
   */
  public static MimeMessage setGeneralData() throws MessagingException, MessagingException {
    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.host", "smtp.server");
    props.put("mail.smtp.port", "smtp.port");
    props.put("mail.smtp.submitter", "from@mail.com");
    Session session = Session.getDefaultInstance(props);
    MimeMessage message = new MimeMessage(session);
    message.setSubject("subject");
    message.setFrom(new InternetAddress("from@mail.com"));
    message.addRecipient(Message.RecipientType.TO, new InternetAddress("to@mail.com"));
    return message;
  }

  /**
   * Builds the content of the message
   *
   * @return the multipart content of the email to be sent
   */
  public static MimeMultipart buildContent() throws MessagingException {
    MimeMultipart multipart = new MimeMultipart("related");
    BodyPart messageBodyPart = buildPart();
    multipart.addBodyPart(messageBodyPart);

    return multipart;
  }

  /**
   * Builds a body part
   *
   * @return the body part content of the email to be sent
   */
  public static MimeBodyPart buildPart() throws MessagingException {
    MimeBodyPart messageBodyPart = new MimeBodyPart();
    String htmlText = "<H1>Email</H1>";
    messageBodyPart.setContent(htmlText, "text/html");
    return messageBodyPart;
  }
}
