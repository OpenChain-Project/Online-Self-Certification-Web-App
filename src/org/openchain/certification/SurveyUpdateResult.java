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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * Holds the results for a survey update
 * @author Gary O'Neall
 *
 */
public class SurveyUpdateResult {
	
	/**
	 * Git commit reference used for the survey update
	 */
	private String commit;
	private List<String> versionsUpdated = new ArrayList<String>();
	private List<String> versionsAdded = new ArrayList<String>();
	private List<String> warnings = new ArrayList<String>();
	/**
	 * Map of spec version to a map of local to all question numbers - used to verify all question numbers match for a given version
	 */
	private Map<String, Map<String, List<String>>>  versionQuestions = new HashMap<>();
	
	public SurveyUpdateResult() {
		
	}
	
	/**
	 * @param commit Git commit reference used for the survey update
	 */
	public SurveyUpdateResult(String commit) {
		this.commit = commit;
	}
	
	/**
	 * @param specVersion spec version for the question numbers
	 * @param locale locale for the question numbers
	 * @param questionNumbers all question numbers in the survey
	 * @param language local language
	 */
	public void addVersionQuestions(String specVersion, String locale, 
			Set<String> questionNumbers, String language) {
		Map<String, List<String>> localQuestions = this.versionQuestions.get(specVersion);
		if (Objects.isNull(localQuestions)) {
			localQuestions = new HashMap<>();
			this.versionQuestions.put(specVersion, localQuestions);
		}
		List<String> existingQuestionNumbers = localQuestions.get(locale);
		if (Objects.isNull(existingQuestionNumbers)) {
			existingQuestionNumbers = new ArrayList<>();
			localQuestions.put(locale, existingQuestionNumbers);
		} else {
			this.warnings.add(I18N.getMessage("SurveyUpdateResult.0", language, specVersion, locale)); //$NON-NLS-1$
		}
		existingQuestionNumbers.addAll(questionNumbers);
	}
	
	/**
	 * This should be called after all questions have been added.  Miss-matched question numbers between locales for a specific spec version and adds any found errors to warnings.
	 */
	public void verify(String language) {

		for (Entry<String, Map<String, List<String>>> versionEntry:this.versionQuestions.entrySet()) {
			List<String> versionQuestions = null;
			boolean questionsMatch = true;
			for (Entry<String, List<String>> localeEntry:versionEntry.getValue().entrySet()) {
				if (Objects.isNull(versionQuestions)) {
					versionQuestions = localeEntry.getValue();
				} else {
					if (!versionQuestions.containsAll(localeEntry.getValue()) ||
						!localeEntry.getValue().containsAll(versionQuestions)) {
						questionsMatch = false;
					}
				}
			}
			if (!questionsMatch) {
				warnings.add(I18N.getMessage("SurveyUpdateResult.4", language, versionEntry.getKey())); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * @param version Specification version
	 * @param specLanguage Lanauge for the questionnaire
	 * @param updateStats Statistics for the udpate
	 * @param language Local language for the logged in user
	 */
	public void addVersionUpdated(String version, String specLanguage, SurveyQuestionUpdateStats updateStats, String language) {
		this.versionsUpdated.add(I18N.getMessage("SurveyUpdateResult.1", language, version, specLanguage, updateStats.toString(language))); //$NON-NLS-1$
	}
	
	public List<String> getVersionsUpdated() {
		return this.versionsUpdated;
	}
	
	/**
	 * @param version Specification version
	 * @param specLanguage Lanauge for the questionnaire
	 * @param language Local language for the logged in user
	 */
	public void addVersionAdded(String version, String specLanguage, String language) {
		this.versionsAdded.add(I18N.getMessage("SurveyUpdateResult.2", language, version, specLanguage)); //$NON-NLS-1$
	}
	
	public List<String> getVersionsAdded() {
		return this.versionsAdded;
	}
	
	/**
	 * @return Git commmit reference for this update
	 */
	public String getCommit() {
		return this.commit;
	}
	
	/**
	 * @param commit commmit reference for this update
	 */
	public void setCommit(String commit) {
		this.commit = commit;
	}
	
	public void addWarning(String warning) {
		this.warnings.add(warning);
	}
	
	public List<String> getWarnings() {
		return this.warnings;
	}

}
