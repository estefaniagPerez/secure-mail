package com.github.estegp.secure.mail.mimemultipart;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
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

/** Enum singleton that manages the loading of keys and initialization of encryptors. */
public enum KeyLoadManager {
  INSTANCE;

  KeyLoadManager() {
    loadProvider();
  }

  /**
   * Loads the public key used to encrypt the emails and initializes the encryptor.
   *
   * @return the encryptor used to encrypt the email.
   */
  private void loadProvider() {
    // Add Bouncy castles as a security provider
    Security.addProvider(new BouncyCastleProvider());

    if (Security.getProvider("BC") == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  /**
   * Loads the public key used to encrypt the emails and initializes the encryptor.
   *
   * @param puk the public key used to encrypt the emails.
   * @return the encryptor used to encrypt the email.
   */
  public JceKeyTransRecipientInfoGenerator loadMimeEncKey(byte[] puk)
      throws CertificateException, IOException {

    // Gets certificate chain from byte array
    InputStream bis = new ByteArrayInputStream(puk);
    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    ArrayList<Certificate> certs = new ArrayList<>();

    while (bis.available() > 0) {
      certs.add(certFactory.generateCertificate(bis));
    }

    // Build certificate
    X509Certificate certX = (X509Certificate) certs.get(0);
    return new JceKeyTransRecipientInfoGenerator(certX).setProvider("BC");
  }

  /**
   * Initializes the 'PGP' encryptor.
   *
   * @param puk the public key that will be used to encrypt the data.
   * @return the encryptor
   */
  public PGPEncryptedDataGenerator iniEncryptorPgp(byte[] puk) throws IOException, PGPException {
    return iniEncryptorPgp(loadPgpKey(puk));
  }

  /**
   * Initializes the 'PGP' encryptor.
   *
   * @param key the public key that will be used to encrypt the data.
   * @return the encryptor
   */
  private PGPEncryptedDataGenerator iniEncryptorPgp(PGPPublicKey key) {

    PGPEncryptedDataGenerator encGen =
        new PGPEncryptedDataGenerator(new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5)
            .setSecureRandom(new SecureRandom()).setProvider("BC"));
    encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(key).setProvider("BC"));

    return encGen;
  }

  /**
   * Reads the public key / certificate and saves it into a 'PGPPublicKey' so it can be used to
   * encrypt the emails.
   *
   * @param puk the public key used to encrypt the emails.
   * @return the PGPPublicKey object
   */
  public PGPPublicKey loadPgpKey(byte[] puk) throws IOException, PGPException {

    InputStream keyIn = new ByteArrayInputStream(puk);
    // ByteArrayOutputStream Close() method does nothing, no need to call it

    // Initializes reader
    PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(
        PGPUtil.getDecoderStream(keyIn), new JcaKeyFingerprintCalculator());

    // Reads each key in the input stream until it finds the encryption key
    Iterator<PGPPublicKeyRing> keyRingIter = pgpPub.getKeyRings();
    while (keyRingIter.hasNext()) {
      PGPPublicKeyRing keyRing = (PGPPublicKeyRing) keyRingIter.next();

      Iterator<PGPPublicKey> keyIter = keyRing.getPublicKeys();
      while (keyIter.hasNext()) {

        PGPPublicKey key = (PGPPublicKey) keyIter.next();

        if (key.isEncryptionKey()) {
          return key;
        }
      }
    }

    throw new IllegalArgumentException("Can't find encryption key in key ring.");
  }
}
