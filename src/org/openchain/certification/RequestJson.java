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

import org.openchain.certification.model.Survey;

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
	private boolean namePermission;
	private boolean emailPermission;
	private boolean create;
	private String language;
	private Survey survey;
	private String tag;
	private String commit;
	private String locale;
	private String reCaptchaResponse;
	
	/**
	 * @return the create
	 */
	public boolean isCreate() {
		return create;
	}
	/**
	 * @param create the create to set
	 */
	public void setCreate(boolean create) {
		this.create = create;
	}
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
	
	public boolean getNamePermission() {
		return this.namePermission;
	}
	
	public boolean getEmailPermission() {
		return this.emailPermission;
	}
	/**
	 * @param namePermission the namePermission to set
	 */
	public void setNamePermission(boolean namePermission) {
		this.namePermission = namePermission;
	}
	/**
	 * @param emailPermission the emailPermission to set
	 */
	public void setEmailPermission(boolean emailPermission) {
		this.emailPermission = emailPermission;
	}
	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}
	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}
	/**
	 * @return the survey
	 */
	public Survey getSurvey() {
		return survey;
	}
	/**
	 * @param survey the survey to set
	 */
	public void setSurvey(Survey survey) {
		this.survey = survey;
	}
	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}
	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}
	/**
	 * @return the commit
	 */
	public String getCommit() {
		return commit;
	}
	/**
	 * @param commit the commit to set
	 */
	public void setCommit(String commit) {
		this.commit = commit;
	}
	/**
	 * @return the locale
	 */
	public String getLocale() {
		return locale;
	}
	/**
	 * @param locale the locale to set
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}
	/**
	 * @return the reCaptchaResponse
	 */
	public String getReCaptchaResponse() {
		return reCaptchaResponse;
	}
	/**
	 * @param reCaptchaResponse the reCaptchaResponse to set
	 */
	public void setReCaptchaResponse(String reCaptchaResponse) {
		this.reCaptchaResponse = reCaptchaResponse;
	}
}
