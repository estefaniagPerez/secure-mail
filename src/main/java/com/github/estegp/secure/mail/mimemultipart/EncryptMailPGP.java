/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.estegp.secure.mail.mimemultipart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.github.estegp.secure.mail.exceptions.EncryptMailException;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;

/**
 * This class implements the encryption of emails with PGP
 */
public class EncryptMailPGP implements EncryptMail{
    private byte[] puk;
    /**
     * Constructor
     * @param puk the public key used to encrypt the emails.
     */
    public EncryptMailPGP (byte[] puk){
        this.puk = puk;
    }

   
    @Override
    public MimeBodyPart encryptMultiPart(MimeMultipart msg, MimeMessage message) throws EncryptMailException {
        try{
            // 1. Puts the msg as the message content
            message.setContent(msg, msg.getContentType());
            // 2. Convert the message to a byte array
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            message.writeTo(out);
            // 3. Encrypt the message
            byte[] crypt = this.encrypt(out.toByteArray());
            // 4. Build the MimeBodypart from the encrypted message
            return this.buildMail(new String(crypt));
        }catch (IOException | PGPException | MessagingException ex){
            throw new EncryptMailException(ex);
        }
    }

    @Override
    public MimeBodyPart encryptData(MimeBodyPart msg, MimeMessage message) throws EncryptMailException {
        try {
            // 0. Build message
            MimeMultipart multipart = new MimeMultipart("related");
            multipart.addBodyPart(msg);
            // 1. Puts the msg as the message content
            message.setContent(multipart, msg.getContentType());
            // 2. Convert the message to a byte array
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            message.writeTo(out);
            // 3. Encrypt the message
            byte[] crypt = this.encrypt(out.toByteArray());
            // 4. Build the MimeBodypart from the encrypted message
            return this.buildMail(new String(crypt));
        }catch (IOException | PGPException | MessagingException ex)
        {
            throw new EncryptMailException(ex);
        }
    }
    
    /**
     * Builds an email with the PGP format defined in rfc3156, and using
     * the given encrypted data as content .
     * @param content encrypted original message.
     * @return an email with the proper PGP format
     */
    private MimeBodyPart buildMail(String content) throws MessagingException{
        
        // 1. First part, the version and encoding [rfc3156]
        MimeBodyPart part1 = new MimeBodyPart();
        part1.setContent("Version: 1\r\n", "application/pgp-encrypted");
        part1.setDescription("PGP/MIME version identification");
        part1.setHeader("Content-transfer-encoding", "8bit");
        
        // 2. Second part, the encrypted data [rfc3156]
        MimeBodyPart part2 = new MimeBodyPart();
        part2.setContent(content, "application/octet-stream; name = \"encrypted.asc\";");
        part2.setDescription("OpenPGP encrypted message");
        part2.setDisposition("preview; filename=\"encrypted.asc\"");
        part2.setHeader("Content-transfer-encoding", "8bit");
        
        // 3. The two parts are contained in a multipart block [rfc3156] of type
        //    multipart/encrypted
        MimeMultipart multipart = new MimeMultipart(
                "encrypted;\r\n" + " protocol=\"application/pgp-encrypted\"",
                part1,part2
        );
        
        // 4. Wrap it in a mime body so it can be sent with javax.mail
        MimeBodyPart finalPart = new MimeBodyPart();
        finalPart.setContent(multipart);
        return finalPart;
    }

    /**
     * Encrypts the given data with PGP
     * @param key the public key used to encrypt the data.
     * @param data the data ti be encrypted
     * @return the encrypted data
     */
    private byte[] encrypt(byte[] data) throws IOException, PGPException {
        byte[] result = null;
            // 1. Opens the output stream where the encrypted text will be written
        try(OutputStream outByteStream =  new ByteArrayOutputStream();
            // 2. The armor is a stream that writes AsCii encoded data in the outByteStream
            OutputStream armoureOut = new ArmoredOutputStream(outByteStream);) {
                // 3. Initializes the encryptor
                PGPEncryptedDataGenerator encGen = KeyLoadManager.INSTANCE.iniEncryptorPGP(this.puk);
                // 4. Ini compressor
                PGPCompressedDataGenerator comData =
                        new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
                // 5. Initializes compressor
                try (OutputStream cOut = encGen.open(armoureOut, new byte[1 << 16])){
                    // 6. Encrypts and compresses the data using in-memory processing
                    PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
                    try (OutputStream pOut = lData.open(comData.open(cOut), PGPLiteralData.BINARY, PGPLiteralData.CONSOLE, data.length, new Date())) {
                        pOut.write(data);
                    }
                } finally {
                    // Closes all the streams
                    comData.close();
                }
                
                result = ((ByteArrayOutputStream) outByteStream).toByteArray();
        }
        return result;
    }
}
