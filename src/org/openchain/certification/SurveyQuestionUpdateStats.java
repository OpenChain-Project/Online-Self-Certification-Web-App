/**
 * Copyright (c) 2018 Source Auditor Inc.
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

import java.util.ArrayList;
import java.util.List;

import org.openchain.certification.model.Question;

/**
 * Statistics on updates to survey questions
 * @author Gary O'Neall
 *
 */
public class SurveyQuestionUpdateStats {
	
	private List<Question> updatedQuestions = new ArrayList<Question>();
	private List<Question> addedQuestions = new ArrayList<Question>();
	
	public SurveyQuestionUpdateStats() {
		// empty constructor
	}

	public void addUpdateQuestions(List<Question> updatedQuestions) {
		this.updatedQuestions.addAll(updatedQuestions);
	}

	public void addAddedQuestions(List<Question> addedQuestions) {
		this.addedQuestions.addAll(addedQuestions);
	}
	
	public List<Question> getAddedQuestions() {
		return this.addedQuestions;
	}
	
	public List<Question> getUpdatedQuestions() {
		return this.updatedQuestions;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.addedQuestions.size() > 0) {
			sb.append("Added ");
			sb.append(this.addedQuestions.size());
			sb.append(" questions: ");
			sb.append(this.addedQuestions.get(0).getNumber());
			for (int i = 1; i < this.addedQuestions.size(); i++) {
				sb.append(", ");
				sb.append(this.addedQuestions.get(i).getNumber());
			}
			sb.append("). ");
		}
		if (this.updatedQuestions.size() > 0) {
			sb.append("Updated ");
			sb.append(this.updatedQuestions.size());
			sb.append(" questions: ");
			sb.append(this.updatedQuestions.get(0).getNumber());
			for (int i = 1; i < this.updatedQuestions.size(); i++) {
				sb.append(", ");
				sb.append(this.updatedQuestions.get(i).getNumber());
			}
			sb.append("). ");
		}
		if (sb.length() == 0) {
			sb.append("No Changes");
		}
		return sb.toString();
	}
	
	public int getNumChanges() {
		return this.addedQuestions.size() + this.updatedQuestions.size();
	}

}
