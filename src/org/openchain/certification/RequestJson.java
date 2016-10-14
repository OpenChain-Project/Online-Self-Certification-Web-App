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

import java.util.List;

/**
 * Request JSON for posts 
 * @author Gary O'Neall
 *
 */
public class RequestJson {
	
	private String request;
	private List<ResponseAnswer> answers;
	private String username;
	private String password;
	private String name;
	private String address;
	private String organization;
	private String email;
	private String specVersion;
	private String[] csvLines;
	private SectionTextJson[] sectionTexts;
	private String[] ids;
	
	/**
	 * @return the request
	 */
	public String getRequest() {
		return request;
	}
	/**
	 * @return the answers
	 */
	public List<ResponseAnswer> getAnswers() {
		return answers;
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
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @param request the request to set
	 */
	public void setRequest(String request) {
		this.request = request;
	}
	/**
	 * @param answers the answers to set
	 */
	public void setAnswers(List<ResponseAnswer> answers) {
		this.answers = answers;
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
	 * @return the specVersion
	 */
	public String getSpecVersion() {
		return specVersion;
	}
	/**
	 * @param specVersion the specVersion to set
	 */
	public void setSpecVersion(String specVersion) {
		this.specVersion = specVersion;
	}
	/**
	 * @return the csvLines
	 */
	public String[] getCsvLines() {
		return csvLines;
	}
	/**
	 * @param csvLines the csvLines to set
	 */
	public void setCsvLines(String[] csvLines) {
		this.csvLines = csvLines;
	}
	/**
	 * @return the sectionText
	 */
	public SectionTextJson[] getSectionTexts() {
		return sectionTexts;
	}
	/**
	 * @param sectionText the sectionText to set
	 */
	public void setSectionTexts(SectionTextJson[] sectionTexts) {
		this.sectionTexts = sectionTexts;
	}
	/**
	 * @return the ids
	 */
	public String[] getIds() {
		return ids;
	}
	/**
	 * @param ids the ids to set
	 */
	public void setIds(String[] ids) {
		this.ids = ids;
	}

}
