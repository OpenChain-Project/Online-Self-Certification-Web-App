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
	
	/**
	 * Yes means yes, no means no, Any means either yes or no can be considered correct
	 * NotApplicable means that answer does not apply and NotAnswered indicates no response was given
	 */
	public enum YesNo {Yes, No, Any, NotApplicable, NotAnswered, YesNotApplicable, NoNotApplicable};
	public static final String TYPE_NAME = "YES_NO";
	protected YesNo correctAnswer;
	
	/**
	 * @param question Text for the question
	 * @param sectionName Name of the section containing the question
	 * @param number Number of the question
	 * @param specVersion Version of the specification
	 * @param language ISO 639 alpha-2 or alpha-3 language code
	 * @param correctAnswer Correct answer
	 * @throws QuestionException
	 */
	public YesNoQuestion(String question, String sectionName, String number, 
			String specVersion, String language, YesNo correctAnswer) throws QuestionException {
		super(question, sectionName, number, specVersion, language);
		this.correctAnswer = correctAnswer;
		this.type = TYPE_NAME;
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
		if (correctAnswer.equals(YesNo.Any)) {
			return true;
		}
		YesNo ynAnswer = null;
		if (answer instanceof YesNo) {
			ynAnswer = (YesNo)answer;
		} else if (answer instanceof YesNoAnswer) {
			ynAnswer = ((YesNoAnswer)answer).getAnswer();
		} else {
			return false;
		}
		if (correctAnswer.equals(YesNo.YesNotApplicable)) {
			return ynAnswer.equals(YesNo.Yes) || ynAnswer.equals(YesNo.NotApplicable);
		}
		if (correctAnswer.equals(YesNo.NoNotApplicable)) {
			return ynAnswer.equals(YesNo.No) || ynAnswer.equals(YesNo.NotApplicable);
		}
		return this.correctAnswer.equals(ynAnswer);
	}
	
	public YesNoQuestion clone() {
		try {
			return new YesNoQuestion(this.getQuestion(), this.getSectionName(), this.getNumber(), 
					this.getSpecVersion(), this.getLanguage(), this.getCorrectAnswer());
		} catch (QuestionException e) {
			throw new RuntimeException(e);
		}
	}
}
