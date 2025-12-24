package com.github.estegp.secure.mail.mimemultipart;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EncryptMailFactoryTest {

  private byte[] genKey = new byte[0];
  private static byte[] pgpKey = null;
  private static byte[] smimeKey = null;

  @BeforeAll
  public static void SetUp() throws IOException, URISyntaxException {
    URL keyFileURL =
        EncryptMailFactoryTest.class.getClassLoader().getResource("for_testing_only.pgp");
    File dir = new File(keyFileURL.toURI());
    try (InputStream stream = new FileInputStream(dir)) {
      EncryptMailFactoryTest.pgpKey = stream.readAllBytes();
    }

    keyFileURL =
        EncryptMailFactoryTest.class.getClassLoader().getResource("for_testing_only.smime");
    dir = new File(keyFileURL.toURI());
    try (InputStream stream = new FileInputStream(dir)) {
      EncryptMailFactoryTest.smimeKey = stream.readAllBytes();
    }
  }

  @Test
  public void TestGetMime() {
    EncryptMail instance =
        new EncryptMailFactory(EncryptMailFactory.SMIME, this.genKey).getEncryptor();
    assertInstanceOf(EncryptSmime.class, instance);
  }

  @Test
  public void TestGetPGP() {
    EncryptMail instance =
        new EncryptMailFactory(EncryptMailFactory.PGP, this.genKey).getEncryptor();
    assertInstanceOf(EncryptMailPgp.class, instance);
  }

  @Test
  public void TestGetMimeByKey() {
    EncryptMail instance = new EncryptMailFactory(EncryptMailFactoryTest.smimeKey).getEncryptor();
    assertInstanceOf(EncryptSmime.class, instance);
  }

  @Test
  public void TestGetPGPByKey() {
    EncryptMail instance = new EncryptMailFactory(EncryptMailFactoryTest.pgpKey).getEncryptor();
    assertInstanceOf(EncryptMailPgp.class, instance);
  }
}
