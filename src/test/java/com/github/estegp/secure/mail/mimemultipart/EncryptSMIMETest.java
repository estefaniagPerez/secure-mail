package com.github.estegp.secure.mail.mimemultipart;

import com.github.estegp.secure.mail.exceptions.EncryptMailException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.github.estegp.secure.mail.mimemultipart.helper.MailBuilder;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class EncryptSMIMETest {

    private static byte[] mimeKey = null;
    private static EncryptSMIME encryptor = null;
    private static MimeMessage message = null;
    private static MimeMultipart msg = null;
    private static MimeBodyPart bodyPart = null;

    @BeforeAll
    public static void SetUp() throws IOException, URISyntaxException, MessagingException {
        URL keyFileURL = EncryptSMIMETest.class.getClassLoader().getResource("for_testing_only.smime");
        if(keyFileURL == null) return;
        File dir = new File(keyFileURL.toURI());
        try (InputStream stream = new FileInputStream(dir)){
            EncryptSMIMETest.mimeKey = stream.readAllBytes();
        }
        EncryptSMIMETest.encryptor = new EncryptSMIME(EncryptSMIMETest.mimeKey);
        EncryptSMIMETest.message = MailBuilder.setGeneralData();
        EncryptSMIMETest.msg = MailBuilder.buildContent();
        EncryptSMIMETest.bodyPart = MailBuilder.buildPart();
    }

    @Test
    public void encryptMultiPart() {
        try{
            MimeBodyPart body = EncryptSMIMETest.encryptor.encryptMultiPart(EncryptSMIMETest.msg, EncryptSMIMETest.message);
            assertNotNull(body);
        }catch (EncryptMailException ex){
            fail("Expected Exception: 'EncryptMailException'");
        }catch (Exception ex){
            fail("Unexpected Exception");
        }
    }

    @Test
    public void encryptData() {
        try{
            MimeBodyPart body = EncryptSMIMETest.encryptor.encryptData(EncryptSMIMETest.bodyPart, EncryptSMIMETest.message);
            assertNotNull(body);
        }catch (EncryptMailException ex){
            fail("Expected Exception: 'EncryptMailException'");
        }catch (Exception ex){
            fail("Unexpected Exception");
        }
    }
}