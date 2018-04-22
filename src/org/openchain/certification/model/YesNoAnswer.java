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

import java.util.Objects;

import org.openchain.certification.I18N;
import org.openchain.certification.model.YesNoQuestion.YesNo;

/**
 * Response to a Yes/No question
 * @author Gary O'Neall
 *
 */
public class YesNoAnswer extends Answer {

	private static final String ANSWER_MESSAGE_KEY_PREFIX = "YesNo";
	protected YesNo answer;
	
	/**
	 * @param language ISO 639 alpha-2 or alpha-3 language code
	 * @param answer answer
	 */
	public YesNoAnswer(String language, YesNo answer) {
		super(language);
		this.answer = answer;
	}

	/**
	 * @return the answer
	 */
	public YesNo getAnswer() {
		return answer;
	}

	/**
	 * @param answer the answer to set
	 */
	public void setAnswer(YesNo answer) {
		this.answer = answer;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof YesNoAnswer)) {
			return false;
		}
		if (this instanceof YesNoAnswerWithEvidence) {
			if (!(o instanceof YesNoAnswerWithEvidence)) {
				return false;
			}
			if (!Objects.equals(((YesNoAnswerWithEvidence)this).getEvidence(), ((YesNoAnswerWithEvidence)o).getEvidence())) {
				return false;
			}
		}
		return Objects.equals(this.answer, ((YesNoAnswer)o).getAnswer());
	}

	@Override
	public int hashCode() {
		int prime = 977;
		if (this.answer != null) {
			return this.answer.hashCode();
		} else {
			return prime;
		}
	}

	@Override
	public String getAnswerString() {
		if (answer == null) {
			return "";
		} else {
			String key = ANSWER_MESSAGE_KEY_PREFIX + "." + answer.toString();
			String retval = I18N.getMessage(key, language);
			if (retval == null) {
				retval = answer.toString();
			}
			return retval;
		}
	}
}
