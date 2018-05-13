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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * A question that contains subquestions.  The answer is the number of suquestions
 * which must have a validated answer.
 * @author Gary O'Neall
 *
 */
public class SubQuestion extends Question {
	
	public static final String TYPE_NAME = "SUBQUESTIONS"; //$NON-NLS-1$
	private int minNumberValidatedAnswers;
	private Map<String, Question> subQuestions;
	
	/**
	 * @param question Text for the question
	 * @param sectionName Name of the section
	 * @param number Question number
	 * @param specVersion Version for the spec
	 * @param language tag in IETF RFC 5646 format
	 * @param minNumberValidatedAnswers Minimum number of valid answers
	 * @throws QuestionException
	 */
	public SubQuestion(String question, String sectionName, String number,
			String specVersion, String language, int minNumberValidatedAnswers) throws QuestionException {
		super(question, sectionName, number, specVersion, language);
		this.minNumberValidatedAnswers = minNumberValidatedAnswers;
		this.subQuestions = new TreeMap<String,Question>();
		this.type = TYPE_NAME;
	}

	/* (non-Javadoc)
	 * @see org.openchain.certification.model.Question#validate(java.lang.Object)
	 */
	@Override
	public boolean validate(Object answer) {
		if (!(answer instanceof SubQuestionAnswers)) {
			return false;
		}
		int numValid = 0;
		SubQuestionAnswers answers = (SubQuestionAnswers)answer;
		Map<String,Answer> subAnswers = answers.getSubAnswers();
		Iterator<Entry<String,Answer>> iter = subAnswers.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, Answer> entry = iter.next();
			Question subQuestion = subQuestions.get(entry.getKey());
			if (subQuestion != null && subQuestion.validate(entry.getValue())) {
				numValid++;
			}
		}
		return numValid >= this.minNumberValidatedAnswers;
	}
	
	public void addSubQuestion(Question subQuestion) {
		this.subQuestions.put(subQuestion.getNumber(), subQuestion);
	}
	
	public void removeSubQuestion(Question subQuestion) {
		this.subQuestions.remove(subQuestion.getNumber());
	}

	@Override
	protected String getCorrectAnswer() {
		return String.valueOf(this.minNumberValidatedAnswers);
	}

	/**
	 * @return the minNumberValidatedAnswers
	 */
	public int getMinNumberValidatedAnswers() {
		return minNumberValidatedAnswers;
	}

	/**
	 * @param minNumberValidatedAnswers the minNumberValidatedAnswers to set
	 */
	public void setMinNumberValidatedAnswers(int minNumberValidatedAnswers) {
		this.minNumberValidatedAnswers = minNumberValidatedAnswers;
	}
	
	public Collection<Question> getAllSubquestions() {
		return this.subQuestions.values();
	}
	
	public SubQuestion clone() {
		try {
			return new SubQuestion(question, sectionName, getNumber(),
					specVersion, getLanguage(), minNumberValidatedAnswers);
		} catch (QuestionException e) {
			throw new RuntimeException(e);
		}
	}
}
