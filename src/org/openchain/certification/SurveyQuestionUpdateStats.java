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
import org.openchain.certification.model.User;

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
	
	/**
	 * @param list List of strings
	 * @param startIndex index of the list to start
	 * @param language to use for localization
	 * @return a localized comma separated list of strings
	 */
	public String buildCommaList(List<String> list, int startIndex, String language) {
		// See http://cldr.unicode.org/translation/lists for patterns used in this algorithm
		if (startIndex >= list.size()) {
			return ""; //$NON-NLS-1$
		} else if (list.size() == 2) {
			return I18N.getMessage("SurveyQuestionUpdateStats.2list", language, list.get(0), list.get(1)); //$NON-NLS-1$
		} else if (startIndex == list.size()-1) {
			return I18N.getMessage("SurveyQuestionUpdateStats.1list", language, list.get(0));
		}else if (startIndex == 0) {	// must be size > 2
			return I18N.getMessage("SurveyQuestionUpdateStats.start", language, list.get(0), buildCommaList(list, 1, language)); //$NON-NLS-1$
		} else if (startIndex == list.size()-2) { // end of the list
			return I18N.getMessage("SurveyQuestionUpdateStats.end", language, list.get(startIndex), list.get(startIndex+1)); //$NON-NLS-1$
		} else { // middle of a list of 4 or more items
			return I18N.getMessage("SurveyQuestionUpdateStats.middle", language, list.get(startIndex), buildCommaList(list, startIndex+1, language)); //$NON-NLS-1$
		}
	}
	
	/**
	 * @param questions
	 * @param language to use for localization
	 * @return Comma separated list of question numbers
	 */
	public String buildQuestionNumberList(List<Question> questions, String language) {
		List<String> questionNumbers = new ArrayList<String>();
		for (Question question:questions) {
			questionNumbers.add(question.getNumber());
		}
		return buildCommaList(questionNumbers, 0, language);
	}
	
	
	/**
	 * @param language Local language for the logged in user
	 * @return
	 */
	public String toString(String language) {
		StringBuilder sb = new StringBuilder();
		if (this.addedQuestions.size() > 0) {
			sb.append(I18N.getMessage("SurveyQuestionUpdateStats.0", language, Integer.toString(this.addedQuestions.size()))); //$NON-NLS-1$
			sb.append(buildQuestionNumberList(this.addedQuestions, language));
		}
		if (this.updatedQuestions.size() > 0) {
			sb.append(I18N.getMessage("SurveyQuestionUpdateStats.1", language, Integer.toString(this.updatedQuestions.size()))); //$NON-NLS-1$
			sb.append(buildQuestionNumberList(this.updatedQuestions, language));
		}
		if (sb.length() == 0) {
			sb.append(I18N.getMessage("SurveyQuestionUpdateStats.2",language)); //$NON-NLS-1$
		}
		return sb.toString().trim();
	}
	
	@Override
	public String toString() {
		return toString(User.DEFAULT_LANGUAGE);
	}
	
	public int getNumChanges() {
		return this.addedQuestions.size() + this.updatedQuestions.size();
	}

}
