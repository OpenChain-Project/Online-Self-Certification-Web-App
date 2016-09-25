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

import java.util.Map;

/**
 * Responses to a survey from a responder
 * @author Gary O'Neall
 *
 */
public class SurveyResponse {

	User responder;
	/**
	 * Map of the question number to answer
	 */
	Map<String, Answer> responses;
	String specVersion;
	Survey survey;
	boolean submitted;
	
	/**
	 * @return the responder
	 */
	public User getResponder() {
		return responder;
	}
	/**
	 * @param responder the responder to set
	 */
	public void setResponder(User responder) {
		this.responder = responder;
	}
	/**
	 * @return the responses
	 */
	public Map<String, Answer> getResponses() {
		return responses;
	}
	/**
	 * @param responses the responses to set
	 */
	public void setResponses(Map<String, Answer> responses) {
		this.responses = responses;
	}
	/**
	 * @return the surveyVersion
	 */
	public String getSpecVersion() {
		return specVersion;
	}
	/**
	 * @param surveyVersion the surveyVersion to set
	 */
	public void setSpecVersion(String surveyVersion) {
		this.specVersion = surveyVersion;
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
	 * @return the submitted
	 */
	public boolean isSubmitted() {
		return submitted;
	}
	/**
	 * @param submitted the submitted to set
	 */
	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}
	
	
}
