/**
 * Copyright (c) 2016 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
*/
package org.openchain.certification.utility;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;
/**
 * A static utility class for managing passwords including the hashing
 * @author Gary O'Neall
 *
 */
public class PasswordUtil {
	private static final int iterations = 20*1000;
	 private static final int saltLenght = 32;
	 private static final int desiredKeyLength = 256;
	 
	 public static String getToken(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] salt;
		salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLenght); //$NON-NLS-1$
		return Base64.encodeBase64String(salt) + "$" + hash(password, salt); //$NON-NLS-1$
	 }
	 
	 public static boolean validate(String password, String token) throws NoSuchAlgorithmException, InvalidKeySpecException {
		String[] saltPassword = token.split("\\$"); //$NON-NLS-1$
       if (saltPassword.length != 2) {
           throw new IllegalStateException("Invalid format for the stored password"); //$NON-NLS-1$
       }
       String hashOfToken = hash(password, Base64.decodeBase64(saltPassword[0]));
       return hashOfToken.equals(saltPassword[1]);
	 }
	 
	 private static String hash(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		 SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); //$NON-NLS-1$
		 SecretKey key = factory.generateSecret(new PBEKeySpec(password.toCharArray(), 
				 salt, iterations, desiredKeyLength));
		 return Base64.encodeBase64String(key.getEncoded());
	 }
}
