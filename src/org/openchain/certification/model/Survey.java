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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

import com.opencsv.CSVWriter;

/**
 * Top level class for the survey itself.  The survey consists of sections.  
 * Sections contains questions.  Questions have correct answers and can be validated
 * against those questions.
 * @author Gary O'Neall
 *
 */
public class Survey {
	public static final String[] CSV_COLUMNS = new String[] {
		"Section Name", "Question Number", "Spec Reference Number", "Question Text", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		"Answer Type", "Correct Answer", "Evidence Prompt", "Evidence Validation", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		"Sub-Question Of Number" //$NON-NLS-1$
	};
	private String specVersion;
	private String language;
	private List<Section> sections;
	transient private Boolean compactAndPretty = null;	// Null indicates it has not been explicitly set
	
	public Survey(String specVersion, String language) {
		this.specVersion = specVersion;
		this.language = language;
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}
	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
		if (sections != null) {
			for (Section section:sections) {
				section.setLanguage(language);
			}
		}
	}
	/**
	 * @return the sections
	 */
	public List<Section> getSections() {
		return sections;
	}

	/**
	 * @return the specVersion
	 */
	public String getSpecVersion() {
		return specVersion;
	}
	/**
	 * @param specVersion the specVersion to set
	 */
	public void setSpecVersion(String specVersion) {
		this.specVersion = specVersion;
	}
	/**
	 * @param sections the sections to set
	 */
	public void setSections(List<Section> sections) {
		this.sections = sections;
	}

	/**
	 * @return a set of all question numbers in the survey
	 */
	public Set<String> getQuestionNumbers() {
		HashSet<String> retval = new HashSet<String>();
		if (sections != null) {
			for (Section section:sections) {
				List<Question> questions = section.getQuestions();
				for (Question question:questions) {
					retval.add(question.getNumber());
				}
			}
		}
		return retval;
	}

	public Question getQuestion(String questionNumber) {
		// TODO this is not the fastest, but it is accurate
		if (sections == null) {
			return null;
		}
		for (Section section:sections) {
			List<Question> questions = section.getQuestions();
			for (Question question:questions) {
				if (Objects.equals(question.getNumber(), questionNumber)) {
					return question;
				}
			}
		}
		return null;
	}

	/**
	 * Prints the survey questions in a CSV file format
	 * @param out
	 * @throws IOException 
	 */
	public void printCsv(PrintWriter out) throws IOException {
		CSVWriter csv = new CSVWriter(out);
		try {
			csv.writeNext(CSV_COLUMNS);
			for (Section section:sections) {
				for (Question question:section.getQuestions()) {
					String[] questionRow = question.toCsvRow();
					csv.writeNext(questionRow);
				}
			}
		} finally {
			csv.close();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Survey clone() {
		Survey retval = new Survey(this.specVersion, this.language);
		if (sections != null) {
			List<Section> clonedSections = new ArrayList<Section>();
			for (Section section:sections) {
				clonedSections.add(section.clone());
			}
			retval.setSections(clonedSections);
		}
		return retval;
	}
	
	/**
	 * @return true if all redundant information in the sections and questions have been set to null for 
	 * pretty printing the survey.  Null return values are likely if the Survey has not been specifically
	 * prettified or expanded
	 */
	public Boolean isCompactAndPretty() {
		return this.compactAndPretty;
	}
	
	/**
	 * Remove all redundant information such as language and specversion in the included sections and questions
	 */
	public void prettify() {
		if (this.compactAndPretty != null && this.compactAndPretty) {
			return;	// already pretty
		}
		for (Section section:getSections()) {
			section.setLanguage(null);
			Map<String, SubQuestion> questionsWithSubquestions = new HashMap<String, SubQuestion>();
			for (Question question:section.getQuestions()) {
				question.setSpecVersion(null);
				question.setLanguage(null);
				question.setSectionName(null);
				if (question instanceof SubQuestion) {
					questionsWithSubquestions.put(question.getNumber(), (SubQuestion)question);
				}
			}
			for (SubQuestion questionsWithSubquestion:questionsWithSubquestions.values()) {
				for (Question subQuestion:questionsWithSubquestion.getAllSubquestions()) {
					// remove any redundant copys of the subquestion
					section.getQuestions().remove(subQuestion);
					// Clear the redundant subquestion of number
					subQuestion.setSubQuestionOfNumber(null);
				}
			}
		}
		this.compactAndPretty = true;
	}
	
	/**
	 * Add information back to questions essentially reversing <code>prettify()</code>
	 */
	public void addInfoToSectionQuestions() {
		if (this.compactAndPretty != null && !this.compactAndPretty) {
			return;	// already done
		}
		String lang = getLanguage();
		String specV = getSpecVersion();
		for (Section section:getSections()) {
			String sectionName = section.getName();
			section.setLanguage(lang);
			Map<String, SubQuestion> questionsWithSubquestions = new HashMap<String, SubQuestion>();
			for (Question question:section.getQuestions()) {
				question.setSpecVersion(specV);
				question.setLanguage(lang);
				question.setSectionName(sectionName);
				if (question instanceof SubQuestion) {
					questionsWithSubquestions.put(question.getNumber(), (SubQuestion)question);
				}
			}
			for (SubQuestion questionsWithSubquestion:questionsWithSubquestions.values()) {
				for (Question subQuestion:questionsWithSubquestion.getAllSubquestions()) {
					subQuestion.setSpecVersion(specV);
					subQuestion.setLanguage(lang);
					subQuestion.setSectionName(sectionName);
					// Add the redundant subquestion of number
					subQuestion.setSubQuestionOfNumber(questionsWithSubquestion.getNumber());
					// Add the redundant copy of the subquestion to the section
					section.getQuestions().add(subQuestion);
				}
			}
		}
		this.compactAndPretty = false;
	}
}
