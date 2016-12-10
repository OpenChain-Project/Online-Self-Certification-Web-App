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
package org.openchain.certification.model;

import java.util.Date;

/**
 * User of the OpenChain site
 * @author Gary O'Neall
 *
 */
public class User {

	private String username;
	private String passwordToken;
	private String name;
	private String address;
	private String email;
	private boolean verified;
	private boolean passwordReset;
	private boolean admin;	// admin privileges
	private Date verificationExpirationDate;
	private String organization;
	/**
	 * This is a hashed version of the uuid used for the email verification
	 */
	private String uuid;
	private boolean namePermission;
	private boolean emailPermission;
	
	/**
	 * Copies all of the non-private information from a user into a new instance of a user
	 * @param user
	 * @return
	 */
	public static User createNonPrivateInfo(User user) {
		User retval = new User();
		if (user.hasEmailPermission()) {
			retval.setEmail(user.getEmail());
		}
		if (user.hasNamePermission()) {
			retval.setName(user.getName());
		}
		retval.setOrganization(user.getOrganization());
		retval.setNamePermission(user.hasNamePermission());
		retval.setEmailPermission(user.hasEmailPermission());
		return retval;
	}
	
	/**
	 * @return the verificationDate
	 */
	public Date getVerificationExpirationDate() {
		return verificationExpirationDate;
	}
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return the passwordToken
	 */
	public String getPasswordToken() {
		return passwordToken;
	}
	/**
	 * @param passwordToken the passwordToken to set
	 */
	public void setPasswordToken(String passwordToken) {
		this.passwordToken = passwordToken;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}
	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @return the verified
	 */
	public boolean isVerified() {
		return verified;
	}
	/**
	 * @param verified the verified to set
	 */
	public void setVerified(boolean verified) {
		this.verified = verified;
	}
	/**
	 * @return the passwordReset
	 */
	public boolean isPasswordReset() {
		return passwordReset;
	}
	/**
	 * @param passwordReset the passwordReset to set
	 */
	public void setPasswordReset(boolean passwordReset) {
		this.passwordReset = passwordReset;
	}
	/**
	 * @return the admin
	 */
	public boolean isAdmin() {
		return admin;
	}
	/**
	 * @param admin the admin to set
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	public void setVerificationExpirationDate(Date date) {
		this.verificationExpirationDate = date;
	}
	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}
	/**
	 * @param hashedUuid the uuid to set
	 */
	public void setUuid(String hashedUuid) {
		this.uuid = hashedUuid;
	}
	/**
	 * @return the organization
	 */
	public String getOrganization() {
		return organization;
	}
	
	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	/**
	 * @param namePermission true if the user has given permission to use their name on the public website
	 */
	public void setNamePermission(boolean namePermission) {
		this.namePermission = namePermission;
	}
	
	/**
	 * @return true if the user has given permission to use their name on the public website
	 */
	public boolean hasNamePermission() {
		return this.namePermission;
	}

	/**
	 * @param emailPermission true if the user has given permission to use their email address on the public website
	 */
	public void setEmailPermission(boolean emailPermission) {
		this.emailPermission = emailPermission;
	}
	
	/**
	 * @return true if the user has given permission to use their email address on the public website
	 */
	public boolean hasEmailPermission() {
		return this.emailPermission;
	}
}
