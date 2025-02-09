/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.estegp.secure.mail.mimemultipart;
import java.io.IOException;
import java.security.cert.CertificateException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.github.estegp.secure.mail.exceptions.EncryptMailException;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.mail.smime.SMIMEException;

/**
 * This class implements the encryption of emails with SMIME
 */
public class EncryptSMIME implements EncryptMail{
    private byte[] puk;
     /**
     * Constructor
     * @param puk the public key used to encrypt the emails.
     */
    public EncryptSMIME(byte[] puk){
        this.puk = puk;
    }
    
    @Override
    public MimeBodyPart encryptMultiPart( MimeMultipart msg, MimeMessage message) throws EncryptMailException {
        try{
            // 1. Sets the msg inside mimebody part
            MimeBodyPart mp = new MimeBodyPart();
            mp.setContent(msg);
            // 2. Encrypts the body part
            return this.encryptData(mp, message);
        }catch (MessagingException ex){
            throw new EncryptMailException(ex);
        }
    }
    
    @Override
    public MimeBodyPart encryptData( MimeBodyPart msg, MimeMessage message) throws EncryptMailException{
        try {
            // The library Directly encrypts the msg and generates a new body part
            SMIMEEnvelopedGenerator gen = new SMIMEEnvelopedGenerator();
            gen.addRecipientInfoGenerator(KeyLoadManager.INSTANCE.loadMimeEncKey(this.puk).setProvider("BC"));
            return gen.generate(
                    msg, new JceCMSContentEncryptorBuilder(CMSAlgorithm.RC2_CBC
                    ).setProvider("BC").build());
        }catch (CertificateException | SMIMEException | CMSException | IOException ex){
            throw new EncryptMailException(ex);
        }
    }    
}
