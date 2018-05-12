package org.openchain.certification.utility;

/* This file was copied from the AWS code at http://docs.aws.amazon.com/ses/latest/DeveloperGuide/smtp-credentials.html
 * The terms of service states that it may only be used for personal use unless covered by a separate license
 * 
 * This file is not intended to be used as part of the main service - it is a utility for personal use only per the terms of the Amazon license
 */
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class SesSmtpCredentialGenerator {
       private static final String KEY_ENV_VARIABLE = "AWS_SECRET_ACCESS_KEY"; // Put your AWS secret access key in this environment variable. //$NON-NLS-1$
       private static final String MESSAGE = "SendRawEmail"; // Used to generate the HMAC signature. Do not modify. //$NON-NLS-1$
       private static final byte VERSION =  0x02; // Version number. Do not modify.

       public static void main(String[] args) {
    	       	   	
              // Get the AWS secret access key from environment variable AWS_SECRET_ACCESS_KEY.
              String key = System.getenv(KEY_ENV_VARIABLE);         	  
              if (key == null)
              {
                 System.out.println("Error: Cannot find environment variable AWS_SECRET_ACCESS_KEY.");   //$NON-NLS-1$
                 System.exit(0);
              }
   	    	       	   
              // Create an HMAC-SHA256 key from the raw bytes of the AWS secret access key.
              SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256"); //$NON-NLS-1$

              try {         	  
                     // Get an HMAC-SHA256 Mac instance and initialize it with the AWS secret access key.
                     Mac mac = Mac.getInstance("HmacSHA256"); //$NON-NLS-1$
                     mac.init(secretKey);

                     // Compute the HMAC signature on the input data bytes.
                     byte[] rawSignature = mac.doFinal(MESSAGE.getBytes());

                     // Prepend the version number to the signature.
                     byte[] rawSignatureWithVersion = new byte[rawSignature.length + 1];               
                     byte[] versionArray = {VERSION};                
                     System.arraycopy(versionArray, 0, rawSignatureWithVersion, 0, 1);
                     System.arraycopy(rawSignature, 0, rawSignatureWithVersion, 1, rawSignature.length);

                     // To get the final SMTP password, convert the HMAC signature to base 64.
                     String smtpPassword = DatatypeConverter.printBase64Binary(rawSignatureWithVersion);       
                     System.out.println(smtpPassword);
              } 
              catch (Exception ex) {
                     System.out.println("Error generating SMTP password: " + ex.getMessage()); //$NON-NLS-1$
              }             
       }
}