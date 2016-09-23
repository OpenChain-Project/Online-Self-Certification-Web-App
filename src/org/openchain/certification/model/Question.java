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

/**
 * A question asked in the self certification
 * @author Gary O'Neall
 *
 */
public abstract class Question {
	protected String question;
	protected String sectionName;
	protected String number;

	public Question(String question, String sectionName, String number) {
		this.question = question;
		this.sectionName = sectionName;
		this.number = number;
	}

	/**
	 * @return the question
	 */
	public String getQuestion() {
		return question;
	}

	/**
	 * @param question the question to set
	 */
	public void setQuestion(String question) {
		this.question = question;
	}
	
	/**
	 * @return true if the answer is the correct answer
	 */
	public abstract boolean validate(Object answer);

	/**
	 * @return the section
	 */
	public String getSectionName() {
		return sectionName;
	}

	/**
	 * @param section the section to set
	 */
	public void setSection(String sectionName) {
		this.sectionName = sectionName;
	}

	/**
	 * @return the number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * @param number the number to set
	 */
	public void setNumber(String number) {
		this.number = number;
	}
	
}
