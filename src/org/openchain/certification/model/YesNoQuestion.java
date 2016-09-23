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
 * @author Gary O'Neall
 *
 */
public class YesNoQuestion extends Question {
	
	public enum YesNo {Yes, No}
	public static final String TYPE_NAME = "YES_NO";;
	protected YesNo correctAnswer;
	
	public YesNoQuestion(String question, String sectionName, String number, YesNo correctAnswer) {
		super(question, sectionName, number);
		this.correctAnswer = correctAnswer;
	}
	
	/**
	 * @return the correctAnswer
	 */
	public YesNo getCorrectAnswer() {
		return correctAnswer;
	}
	/**
	 * @param correctAnswer the correctAnswer to set
	 */
	public void setCorrectAnswer(YesNo correctAnswer) {
		this.correctAnswer = correctAnswer;
	}
	@Override
	public boolean validate(Object answer) {
		if (!(answer instanceof YesNo)) {
			return false;
		}
		return answer.equals(correctAnswer);
	}
	
}
