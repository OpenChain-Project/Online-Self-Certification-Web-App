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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

/**
 * List of answers associated with a subquestion
 * @author Gary O'Neall
 *
 */
public class SubQuestionAnswers extends Answer {
	
	/**
	 * @param language tag in IETF RFC 5646 format
	 */
	public SubQuestionAnswers(String language) {
		super(language);
	}
	
	/**
	 * Map of the question number to an answer
	 */
	private Map<String, Answer> subAnswers = new HashMap<String, Answer>();
	

	public Map<String, Answer> getSubAnswers() {
		return subAnswers;
	}
	
	public void addSubAnswer(String questionNumber, Answer answer) {
		this.subAnswers.put(questionNumber, answer);
	}
	
	public void removeSubAnswer(String questionNumber) {
		this.subAnswers.remove(questionNumber);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SubQuestionAnswers)) {
			return false;
		}
		SubQuestionAnswers compare = (SubQuestionAnswers)o;
		Map<String,Answer> compareAnswers = compare.getSubAnswers();
		Iterator<Entry<String, Answer>> iter = subAnswers.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, Answer> entry = iter.next();
			Answer compAnswer = compareAnswers.get(entry.getKey());
			if (compAnswer == null) {
				return false;
			}
			if (!compAnswer.equals(entry.getValue())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		TreeSet<Integer> hashCodes = new TreeSet<Integer>();
		int retval = 773;
		Iterator<Entry<String, Answer>> iter = subAnswers.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, Answer> entry = iter.next();
			hashCodes.add(entry.getKey().hashCode() ^ entry.getValue().hashCode());
		}
		// this is to sort the hashcodes
		for (Integer hashCode:hashCodes) {
			retval = retval ^ hashCode;
		}
		return retval;
	}

	@Override
	public String getAnswerString() {
		return "";	// no answer for a subquestionanswer
	}
}
