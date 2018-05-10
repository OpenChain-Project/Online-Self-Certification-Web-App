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

import java.util.List;
import java.util.ArrayList;;

/**
 * Section of the OpenChain specification  A section groups questions for the 
 * certification based on the spec.
 * @author Gary O'Neall
 *
 */
public class Section implements Comparable<Section> {
	
	private String name;
	private String title;
	private List<Question> questions;
	private String language;

	public Section(String language) {
		this.language = language;
	}
	
	/**
	 * @param name
	 * @param title
	 * @param language ISO 639 alpha-2 or alpha-3 language code
	 */
	public Section(String name, String title, String language) {
		this(language);
		this.name = name;
		this.title = title;
		questions = new ArrayList<Question>();
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
		for (Question question:questions) {
			question.setLanguage(language);
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the questions
	 */
	public List<Question> getQuestions() {
		return questions;
	}
	/**
	 * @param questions the questions to set
	 */
	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}
	@Override
	public int compareTo(Section o) {
		if (o.getName() == null) {
			if (this.getName() == null) {
				return 0;
			} else {
				return -1;
			}
		}
		if (this.getName() == null) {
			return 1;
		}
		return this.getName().compareToIgnoreCase(o.getName());
	}
	
	public Section clone() {
		Section retval = new Section(name, title, language);
		if (questions != null) {
			List<Question> clonedQuestions = new ArrayList<Question>();
			for (Question question:questions) {
				clonedQuestions.add(question.clone());
			}
			retval.setQuestions(clonedQuestions);
		}
		return retval;
	}
}
