# SecureMail
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

(This repository was moved from my old repository on Feb 2025)

This is a Java library for encrypting multipart emails with `PGP` or `S/MIME`. This library was conceived to 
address email encryption using asymmetric keys. While most of the popular mail providers secure 
the mail transmission with SSL (HTTPS), this kind of security does not provide end to end encryption - the
server will see the unencrypted mail -. By using these standards it is possible to generate encrypted mails where only 
the end user will be able to read.

The library supports:

- `S/MIME`: supported by the majority of email clients, like Outlook. 
In this case, the _BouncyCastle_ implementation of `S/MIME` has been reused.

- `PGP`: less wildly used, but supported by some open source mail clients like Thunderbird. In this case, the implementation is done following the 
standard described in the [RFC3156](https://tools.ietf.org/html/rfc3156) document.

## Usage
To use the library, import the package into your project and then add the package as a _Maven_ 
dependency.

The library is quite easy to use, and only two steps are neccesary:

1. Use the factory to get the mail encryptor instance. 
    - By type: the type of encryptor to be used will be explicitly indicated by the _type_ field.
        ```
        // SMIME encryptor
        EncryptMailFactory factory = new EncryptMailFactory(EncryptMailFactory.SMIME, key_smime);
        EncryptMail encryptor_smime = factory.getEncryptor();
      
        // PGP encryptor
        EncryptMailFactory factory = new EncryptMailFactory(EncryptMailFactory.PGP, key_pgp);
        EncryptMail encryptor_smime = factory.getEncryptor();
        ```
    
    - By Certificate: the type of encryptor to be used will be deduced by the format of the certificate.
        ```
        // SMIME encryptor
        EncryptMailFactory factory = new EncryptSMIME(key_smime);
        EncryptMail encryptor_smime = factory.getEncryptor();
      
        // PGP encryptor
        EncryptMailFactory factory = new EncryptSMIME(key_pgp); 
        EncryptMail encryptor_pgp = factory.getEncryptor(); 
        ```

2. Build the encrypted email with the data to be encrypted.
    ``` 
    // Generate mail
    MimeBodyPart mail = encryptor.encryptMultiPart(msg, message);        
    ```
    Where _msg_ is the object to be sent, which will contain the final encrypted data.
    And _message_ is the object that contains the data to be encrypted.
    
    
#### Certificates

In order to use asymetric encryption - used in `PGP` and `S/MIME` - we will need,
at least, one key and one certificate. This is known as a public/privated key pair. The idea is to use the public key - that
can be known by anyone - to encrypt the email. This email cannot be decrypted back into plain text with the public
key, the email can only be decrypted with the privated key, which only the recipient has - this private key needs to be kept in a secure location, and if leaked, anyone will be able to decypt the emails -.


Depending on the standard used to encrypt the email, the key pair is generated in one of the following ways:


- `S/MIME`: the keypair can be generated using OpenSSL [ [REF](https://security.stackexchange.com/questions/17583/how-do-i-create-a-valid-email-certificate-for-outlook-s-mime-with-openssl) ]
The private key needs to be installed on the recipient computer, making it possible for the mail client to decrypt it.

- `PGP`: the keypair can be generated using the OpenPGP or GNUPGP software; or in some instances like ThunderBird 
using the mail client itself [ [REF](https://support.mozilla.org/en-US/kb/digitally-signing-and-encrypting-messages) ].



#### Logging
Logging has been left to the end user of the library. When an error occurs the library will return an `EncryptMailException`.
The decision to leave the logging task to the end-user was made to reduce dependencies and incompatibilities with other logging libraries or versions.

## Minimal Required JDK
`SecureMail` is known to work with:

- Java 1.21 and higher
## License

This library and all the contents in this repository are licenced under the MIT license. 

See the LICENSE file for more information.
