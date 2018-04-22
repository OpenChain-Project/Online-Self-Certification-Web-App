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
import java.util.List;
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
		"Section Name", "Question Number", "Spec Reference Number", "Question Text",
		"Answer Type", "Correct Answer", "Evidence Prompt", "Evidence Validation",
		"Sub-Question Of Number"
	};
	private List<Section> sections;
	private String specVersion;
	private String language;
	
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
		for (Section section:sections) {
			List<Question> questions = section.getQuestions();
			for (Question question:questions) {
				retval.add(question.getNumber());
			}
		}
		return retval;
	}

	public Question getQuestion(String questionNumber) {
		// TODO this is not the fastest, but it is accurate
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
	
}
