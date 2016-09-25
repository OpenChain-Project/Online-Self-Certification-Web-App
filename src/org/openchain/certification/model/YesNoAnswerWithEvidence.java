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

import org.openchain.certification.model.YesNoQuestion.YesNo;

/**
 * Answer for a question which requests evidence
 * @author Gary O'Neall
 *
 */
public class YesNoAnswerWithEvidence extends YesNoAnswer {
	private String evidence;

	public YesNoAnswerWithEvidence(YesNo answer, String evidence) {
		super(answer);
		this.evidence = evidence;
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
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof YesNoAnswerWithEvidence)) {
			return false;
		}
		return Objects.equals(this.answer, ((YesNoAnswerWithEvidence)o).getAnswer()) &&
				Objects.equals(this.evidence, ((YesNoAnswerWithEvidence)o).getEvidence());
	}

	@Override
	public int hashCode() {
		int retval = 349;
		if (this.answer != null) {
			retval = retval ^ this.answer.hashCode();
		} 
		if (this.evidence != null) {
			retval = retval ^ this.evidence.hashCode();
		}
		return retval;
	}
	
}
