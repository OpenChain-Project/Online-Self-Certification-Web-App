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

/**
 * Holds the response answers from the UI
 * @author Gary O'Neall
 *
 */
public class ResponseAnswer {
	
	private String questionNumber;
	private String value;
	private boolean checked;
	private String evidence;
	
	public ResponseAnswer() {
		questionNumber = null;
		value = null;
		checked = false;
		evidence = null;
	}
	
	public ResponseAnswer(String questionNumber, String value, boolean checked, String evidence) {
		this.questionNumber = questionNumber;
		this.value = value;
		this.checked = checked;
		this.evidence = evidence;
	}
	
	/**
	 * @return the questionNumber
	 */
	public String getQuestionNumber() {
		return questionNumber;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @return the checked
	 */
	public boolean isChecked() {
		return checked;
	}
	/**
	 * @return the evidence
	 */
	public String getEvidence() {
		return evidence;
	}
	/**
	 * @param evidence the evidence to set
	 */
	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}
	/**
	 * @param questionNumber the questionNumber to set
	 */
	public void setQuestionNumber(String questionNumber) {
		this.questionNumber = questionNumber;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @param checked the checked to set
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
}
