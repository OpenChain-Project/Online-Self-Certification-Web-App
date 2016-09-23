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
package org.openchain.certification;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.UUID;

import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;
import org.openchain.certification.dbdao.UserDb;
import org.openchain.certification.model.User;
import org.openchain.certification.utility.EmailUtilException;
import org.openchain.certification.utility.EmailUtility;
import org.openchain.certification.utility.PasswordUtil;


/**
 * User information for the HTTP session
 * @author Gary O'Neall
 *
 */
public class UserSession {
	static final transient Logger logger = Logger.getLogger(UserSession.class);
	
	private boolean loggedIn = false;
	private String username;
	private transient String password;
	private String lastError;
	private transient ServletConfig config;
	public UserSession(String username, String password, ServletConfig config) {
		this.username = username;
		this.password = password;
		this.config = config;
		this.loggedIn = false;
	}
	public UserSession(ServletConfig config) {
		this.config = config;
		this.loggedIn = false;
		this.username = null;
		this.password = null;
	}
	public void logout() {
		this.loggedIn = false;
		this.username = null;
		this.password = null;
	}
	public boolean login() {
		this.loggedIn = false;
		User user = null;
		try {
			user = UserDb.getUserDb(config).getUser(username);
		} catch (SQLException e) {
			this.lastError = "Unexpected SQL error.  Please report this error to the OpenChain team: "+e.getMessage();
			logger.error("SQL Exception logging in user",e);
			return false;
		}
		if (user == null) {
			this.lastError = "User "+username+" does not exist.  Please review the username or sign up as a new user.";
			return false;
		}
		if (!user.isVerified()) {
			this.lastError = "This use has not been verified.  Please check your email and click on the provided link to verify this user and email address";
			return false;
		}
		try {
			if (!PasswordUtil.validate(password, user.getPasswordToken())) {
				this.lastError = "Passwords do not match.  Please retry or reset your password";
				return false;
			}
		} catch (NoSuchAlgorithmException e) {
			logger.error("Unexpected No Such Algorithm error logging in user",e);
			this.lastError = "Unexpected No Such Algorithm error.  Please report this error to the OpenChain team";
			return false;
		} catch (InvalidKeySpecException e) {
			this.lastError = "Unexpected Invalid Key Spec error.  Please report this error to the OpenChain team";
			logger.error("Unexpected Invalid Key Spec error logging in user",e);
			return false;
		}
		this.loggedIn = true;
		return true;
	}
	public String getLastError() {
		return this.lastError;
	}
	/**
	 * Sign up a new user
	 * @param Name of the user (common name, not the username)
	 * @param address Address of the user
	 * @param organization User organization
	 * @param email Email - must already be validated
	 * @param responseServletUrl The URL of the servlet to handle the email validation link
	 * @return
	 */
	public boolean signUp(String name, String address, String organization,
			String email, String responseServletUrl) {
		User user = null;
		try {
			user = UserDb.getUserDb(config).getUser(username);
			if (user != null) {
				this.lastError = "User "+username+" already exist.  Please select a different unique username.";
				return false;
			}
			user = new User();
			user.setAddress(address);
			user.setAdmin(false);
			user.setEmail(email);
			user.setName(name);
			user.setOrganization(organization);
			user.setPasswordReset(false);
			user.setPasswordToken(PasswordUtil.getToken(this.password));
			user.setUsername(this.username);
			user.setVerified(false);			
			user.setVerificationExpirationDate(EmailUtility.generateVerificationExpirationDate());
			UUID uuid = UUID.randomUUID();
			String hashedUuid = PasswordUtil.getToken(uuid.toString());
			user.setUuid(hashedUuid);
			UserDb.getUserDb(config).addUser(user);
			EmailUtility.emailVerification(name, email, uuid, username, responseServletUrl, config);
	        return true;
		} catch (SQLException e) {
			this.lastError = "Unexpected SQL error.  Please report this error to the OpenChain team: "+e.getMessage();
			logger.error("SQL Exception signing up user",e);
			return false;
		} catch (NoSuchAlgorithmException e) {
			logger.error("Unexpected No Such Algorithm error signing up user",e);
			this.lastError = "Unexpected No Such Algorithm error.  Please report this error to the OpenChain team";
			return false;
		} catch (InvalidKeySpecException e) {
			logger.error("Unexpected Invalid Key Spec error signing up user",e);
			this.lastError = "Unexpected Invalid Key Spec error.  Please report this error to the OpenChain team";
			return false;
		} catch (EmailUtilException e) {
			logger.error("Error emailing invitation",e);
			this.lastError = "Unable to email the invitiation: "+e.getMessage();
			return false;
		}
	}
	public String getUsername() {
		return this.username;
	}
	
	public boolean isLoggedIn() {
		return this.loggedIn;
	}
}
