package com.github.estegp.secure.mail.mimemultipart;

import static org.junit.jupiter.api.Assertions.*;

import com.github.estegp.secure.mail.exceptions.EncryptMailException;
import com.github.estegp.secure.mail.mimemultipart.helper.MailBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import javax.mail.MessagingException;
import javax.mail.internet.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class EncryptMailPGPTest {

  private static byte[] pgpKey = null;
  private static EncryptMailPgp encryptor = null;
  private static MimeMessage message = null;
  private static MimeMultipart msg = null;
  private static MimeBodyPart bodyPart = null;

  @BeforeAll
  public static void SetUp() throws IOException, URISyntaxException, MessagingException {
    URL keyFileURL = EncryptMailPGPTest.class.getClassLoader().getResource("for_testing_only.pgp");
    if (keyFileURL == null) return;
    File dir = new File(keyFileURL.toURI());
    try (InputStream stream = new FileInputStream(dir)) {
      EncryptMailPGPTest.pgpKey = stream.readAllBytes();
    }
    EncryptMailPGPTest.encryptor = new EncryptMailPgp(EncryptMailPGPTest.pgpKey);
    EncryptMailPGPTest.message = MailBuilder.setGeneralData();
    EncryptMailPGPTest.msg = MailBuilder.buildContent();
    EncryptMailPGPTest.bodyPart = MailBuilder.buildPart();
  }

  @Test
  public void encryptMultiPart() {
    try {
      MimeBodyPart body =
          EncryptMailPGPTest.encryptor.encryptMultiPart(
              EncryptMailPGPTest.msg, EncryptMailPGPTest.message);
      assertNotNull(body);
    } catch (EncryptMailException ex) {
      fail("Expected Exception: 'EncryptMailException'");
    } catch (Exception ex) {
      fail("Unexpected Exception");
    }
  }

  @Test
  public void encryptData() {
    try {
      MimeBodyPart body =
          EncryptMailPGPTest.encryptor.encryptData(
              EncryptMailPGPTest.bodyPart, EncryptMailPGPTest.message);
      assertNotNull(body);
    } catch (EncryptMailException ex) {
      fail("Expected Exception: 'EncryptMailException'");
    } catch (Exception ex) {
      fail("Unexpected Exception");
    }
  }
}
