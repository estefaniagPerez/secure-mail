/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */

package com.github.estegp.secure.mail.exceptions;

/** Exception implementation for errors during the encryption of emails. */
public class EncryptMailException extends Exception {

  /** Constructor Exception for errors during the encryption of emails. */
  public EncryptMailException() {
    super();
  }

  /**
   * Constructor Exception for errors during the encryption of emails.
   *
   * @param message String message describing the error.
   */
  public EncryptMailException(final String message) {
    super(message);
  }

  /**
   * Constructor Exception for errors during the encryption of emails.
   *
   * @param cause the cause of the exception.
   */
  public EncryptMailException(final Throwable cause) {
    super(cause);
  }

  /**
   * Constructor Exception for errors during the encryption of emails.
   *
   * @param message String message describing the error.
   * @param cause the cause of the exception.
   */
  public EncryptMailException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor Exception for errors during the encryption of emails.
   *
   * @param ex the exception object.
   */
  public EncryptMailException(final Exception ex) {
    super(ex);
  }
}
