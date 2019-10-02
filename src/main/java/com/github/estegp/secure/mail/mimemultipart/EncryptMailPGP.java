/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.estegp.secure.mail.mimemultipart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

/**
 * This class implements the encryption of emails with PGP
 */
public class EncryptMailPGP extends EncryptMail{

    /**
     * Constructor
     * @param puk the public key used to encrypt the emails.
     */
    public EncryptMailPGP (byte[] puk){
        this.puk = puk;
    }

   
    @Override
    public MimeBodyPart encryptMultiPart(MimeMultipart msg, MimeMessage message) {
        try {
            // 1. Puts the msg as the message content
            message.setContent(msg, msg.getContentType());
            // 2. Convert the message to a byte array
            ByteArrayOutputStream out = new ByteArrayOutputStream();    // ByteArrayOutputStream Close() method does nothing, no need to call it
            message.writeTo(out);
            // 3. Encrypt the message
            OutputStream crypt = this.encrypt(this.loadKey(),out.toByteArray());
            // 4. Build the MimeBodypart from the encrypted message
            return this.buildMail(
                new String(((ByteArrayOutputStream)crypt).toByteArray())
            );
        } catch (IOException | PGPException | MessagingException ex) {
            Logger.getLogger(EncryptMailPGP.class.getName()).log(Level.SEVERE, null, ex);
            return  null;
        }
    }

    @Override
    public MimeBodyPart encryptData(MimeBodyPart msg, MimeMessage message) {
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
            OutputStream crypt = this.encrypt( this.loadKey(), out.toByteArray());
            // 4. Build the MimeBodypart from the encrypted message
            return this.buildMail(
                new String(((ByteArrayOutputStream)crypt).toByteArray())
            );
            
        } catch (IOException | PGPException | MessagingException ex) {
            Logger.getLogger(EncryptMailPGP.class.getName()).log(Level.SEVERE, null, ex);
            return null;
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
     * Loads the byte array public key / certificate into a PGPPublicKey so it can
     * be used to encrypt the emails.
     * @return the PGPPublicKey object
     */
    private PGPPublicKey loadKey() throws IOException, PGPException{
        InputStream keyIn = new ByteArrayInputStream(this.puk);    // ByteArrayOutputStream Close() method does nothing, no need to call it
        PGPPublicKey key = readPublicKey(keyIn);
        return key;
    }
    
    /**
     * Reads the public key / certificate and saves it into a PGPPublicKey so it can
     * be used to encrypt the emails.
     * @return the PGPPublicKey object
     */
    private PGPPublicKey readPublicKey(InputStream input) throws IOException, PGPException
    {
        // Initializes reader
        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(
        PGPUtil.getDecoderStream(input), new JcaKeyFingerprintCalculator());

        // Reads each key in the inpout stream until it finds the encryption key
        Iterator keyRingIter = pgpPub.getKeyRings();
        while (keyRingIter.hasNext())
        {
            PGPPublicKeyRing keyRing = (PGPPublicKeyRing)keyRingIter.next();

            Iterator keyIter = keyRing.getPublicKeys();
            while (keyIter.hasNext())
            {
                PGPPublicKey key = (PGPPublicKey)keyIter.next();

                if (key.isEncryptionKey())
                {
                    return key;
                }
            }
        }
        throw new IllegalArgumentException("Can't find encryption key in key ring.");
    }

    /**
     * Encrypts the given data with PGP
     * @param key the public key used to encrypt the data.
     * @param data the data ti be encrypted
     * @return the encrypted data
     */
    private OutputStream encrypt(PGPPublicKey key, byte[] data) throws IOException, PGPException{
        // 1. Sets provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        // 2. Opens the output stream where the encrypted text will be written
        OutputStream outByteStream =  new ByteArrayOutputStream();
            // The armor is a stream that writes AsCii encoded data in the 
            // outByteStream, needed for sending the encryption through email
        OutputStream armoureOut = new ArmoredOutputStream(outByteStream);
        // 3. Creates temporal file with the data to be encrypted
        File temp = this.writeTempFile(data);
        // 4. Initializes the encryptor
        PGPEncryptedDataGenerator encGen = this.iniEncryptor(key);
        // 5. Ini compressor
        PGPCompressedDataGenerator comData =
                new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
        // 6. Initializes compressor
        try (OutputStream cOut = encGen.open(armoureOut, new byte[1 << 16])){
            // 7. Encrypts and compresses the data in the temporal file
            PGPUtil.writeFileToLiteralData(
                    comData.open(cOut), PGPLiteralData.BINARY, temp, new byte[1 << 16]);
        } finally {
            // Closes all the streams
            armoureOut.close();
            comData.close();
        }
        return outByteStream;
    }
    
    /**
     * Writes the data to a temporary file.
     * @param data the data to be write in the file
     * @return the file
     */
    private File writeTempFile(byte[] data) throws IOException{
        File temp = File.createTempFile("pgp", null);;
        try(FileOutputStream fos = new FileOutputStream(temp)){
            fos.write(data);
        }
        catch (IOException ex){
            throw ex;
        }
        return temp;
    }
    
    /**
     * Initializes the PGP encryptor.
     * @param key the public key that will be used to encrypt the data.
     * @return the encryptor
     */
    private PGPEncryptedDataGenerator iniEncryptor(PGPPublicKey key){
        PGPEncryptedDataGenerator encGen = 
            new PGPEncryptedDataGenerator(
                new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5).
                                    setSecureRandom(new SecureRandom()).
                                    setProvider("BC")
            ); 
        encGen.addMethod(
            new JcePublicKeyKeyEncryptionMethodGenerator(key).setProvider("BC"));
        return encGen;
        
    }
}
