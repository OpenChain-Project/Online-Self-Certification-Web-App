/**
 * Copyright (c) 2020 Source Auditor Inc.
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
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.UUID;

import org.openchain.certification.UserSession;

/**
 * @author Gary O'Neall
 * 
 * Java commandline utility to create a reset to be manually input into the database and emailed to the user
 * main method prints instructions
 */
public class PasswordManualReset {

	/**
	 * @param args args[0] username - must be exactly as found in the database; args[1] URL of the web application (e.g. https://certification.openchainproject.org)
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Invalid number of arguments.  Expected 2 - username weburs");
			System.exit(1);
		}
		String username = args[0].trim();
		if (username.isEmpty()) {
			System.out.println("Missing required username.");
			System.exit(1);
		}
		String webUrl = args[1].trim();
		if (webUrl.isEmpty()) {
			System.out.println("Missing required web URL.");
			System.exit(1);
		}
		Date expiration = UserSession.generateVerificationExpirationDate();
		UUID uuid = UUID.randomUUID();
		String hashedUuid = null;
		try {
			hashedUuid = PasswordUtil.getToken(uuid.toString());
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			System.out.println("Error hashing UUID: "+e.getMessage());
			System.exit(1);
		}
		String responseServletUrl = webUrl + "/CertificationServlet";
		String resetUrl = EmailUtility.formPasswordResetLink(responseServletUrl, username, uuid, "EN");
		System.out.println("Update the database with the following update:");
		System.out.printf("update openchain_user set verificationexpirationdate='%1$tY-%1$tm-%1$td', uuid='%2$s', passwordreset=true where username='%3$s';%n",
				expiration, hashedUuid, username);
		System.out.println("Following the link on the next line to reset the password:");
		System.out.println(resetUrl);
	}

}
