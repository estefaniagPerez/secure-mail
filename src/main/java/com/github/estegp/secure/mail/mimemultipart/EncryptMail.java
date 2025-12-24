/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */

package com.github.estegp.secure.mail.mimemultipart;

import com.github.estegp.secure.mail.exceptions.EncryptMailException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/** This class generalizes the encryption of email. */
public interface EncryptMail {

  /**
   * This method encrypts a multipart email.
   *
   * @param msg the multipart message with the data to encrypt
   * @param message the message that will contain the data
   * @return a mimebody part with the encrypted data
   */
  MimeBodyPart encryptMultiPart(MimeMultipart msg, MimeMessage message) throws EncryptMailException;

  /**
   * This method encrypts the mime bodypart of an email.
   *
   * @param msg the mime multipart with the data to encrypt
   * @param message the message that will contain the data
   * @return a mimebody part with the encrypted data
   */
  MimeBodyPart encryptData(MimeBodyPart msg, MimeMessage message) throws EncryptMailException;
}
