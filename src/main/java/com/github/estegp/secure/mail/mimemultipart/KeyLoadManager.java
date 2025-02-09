package com.github.estegp.secure.mail.mimemultipart;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

import java.util.Arrays;
import java.util.Iterator;

public enum KeyLoadManager {
    INSTANCE;
    private JceKeyTransRecipientInfoGenerator jceInfoGenerator = null;
    private PGPEncryptedDataGenerator pgpEncryptor = null;
    private byte[] puk = null;

    KeyLoadManager() {
        loadProvider();
    }

    /**
     * Loads the public key used to encrypt the emails and initializes the encryptor
     * @return the encryptor used to encrypt the email.
     */
    private void loadProvider() {
        // Add Bouncy castles as a security provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        if (Security.getProvider("BC") == null)
        {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Loads the public key used to encrypt the emails and initializes the encryptor
     * @param puk the public key used to encrypt the emails.
     * @return the encryptor used to encrypt the email.
     */
    public JceKeyTransRecipientInfoGenerator loadMimeEncKey(byte[] puk) throws CertificateException, IOException {
        if(this.puk == null || !Arrays.equals(this.puk,puk) || this.jceInfoGenerator == null){
            this.puk = puk;
            this.jceInfoGenerator = this.loadMimeEncKey();
        }
        return this.jceInfoGenerator;
    }

     /**
     * Loads the public key used to encrypt the emails and initializes the encryptor
     * @return the encryptor used to encrypt the email.
     */
    private JceKeyTransRecipientInfoGenerator loadMimeEncKey() throws CertificateException, IOException {
        // Gets certificate chain from byte array
        InputStream bis = new ByteArrayInputStream(this.puk);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        ArrayList<Certificate> certs = new ArrayList<>();
        
        while(bis.available() > 0){
            certs.add(certFactory.generateCertificate(bis));
        }        

        // Build certificate
        X509Certificate certX = (X509Certificate) certs.get(0);
        return new JceKeyTransRecipientInfoGenerator(certX).setProvider("BC");
    }

    /**
     * Initializes the PGP encryptor.
     * @param key the public key that will be used to encrypt the data.
     * @return the encryptor
     */
    public PGPEncryptedDataGenerator iniEncryptorPGP(byte[] puk) throws IOException, PGPException{
        if(this.puk == null || !Arrays.equals(this.puk,puk) || this.pgpEncryptor == null){
            this.puk = puk;
            this.pgpEncryptor = iniEncryptorPGP(loadPGPKey());
        }
        return this.pgpEncryptor;
    }

    /**
     * Initializes the PGP encryptor.
     * @param key the public key that will be used to encrypt the data.
     * @return the encryptor
     */
    private PGPEncryptedDataGenerator iniEncryptorPGP(PGPPublicKey key){
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

    /**
     * Reads the public key / certificate and saves it into a PGPPublicKey so it can
     * be used to encrypt the emails.
     * @param puk the public key used to encrypt the emails.
     * @return the PGPPublicKey object
     */
    public PGPPublicKey loadPGPKey() throws IOException, PGPException {
        InputStream keyIn = new ByteArrayInputStream(this.puk);    // ByteArrayOutputStream Close() method does nothing, no need to call it
         // Initializes reader
        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(
        PGPUtil.getDecoderStream(keyIn), new JcaKeyFingerprintCalculator());

        // Reads each key in the input stream until it finds the encryption key
        Iterator<PGPPublicKeyRing> keyRingIter = pgpPub.getKeyRings();
        while (keyRingIter.hasNext())
        {
            PGPPublicKeyRing keyRing = (PGPPublicKeyRing)keyRingIter.next();

            Iterator<PGPPublicKey> keyIter = keyRing.getPublicKeys();
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
}
