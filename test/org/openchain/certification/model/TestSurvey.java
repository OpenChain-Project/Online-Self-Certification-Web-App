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
package org.openchain.certification.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openchain.certification.model.YesNoQuestion.YesNo;

/**
 * @author gary
 *
 */
public class TestSurvey {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSetLanguage() throws QuestionException {
		String specVersion = "1.0";
		String language = "ang";
		Survey survey = new Survey(specVersion, language);
		String sectionName = "G1";
		String sectionTitle = "stitle";
		Section section = new Section(sectionName, sectionTitle, language);
		String question = "This is my question";
		String number = "12";
		YesNo answer = YesNo.Yes;
		String evidence = "evidence";
		Pattern pattern = Pattern.compile(".*");
		String[] specRefs = new String[] {"1.0", "1.1"};
		YesNoQuestionWithEvidence ynq = new YesNoQuestionWithEvidence(question, sectionName, 
				number, specVersion, specRefs, language, answer, evidence, pattern);
		List<Question> questions = new ArrayList<Question>();
		questions.add(ynq);
		section.setQuestions(questions);
		List<Section> sections = new ArrayList<Section>();
		sections.add(section);
		survey.setSections(sections);
		assertEquals(language, survey.getLanguage());
		assertEquals(language, survey.getSections().get(0).getLanguage());
		assertEquals(language, survey.getSections().get(0).getQuestions().get(0).getLanguage());
		
		String language2 = "ger";
		survey.setLanguage(language2);
		assertEquals(language2, survey.getLanguage());
		assertEquals(language2, survey.getSections().get(0).getLanguage());
		assertEquals(language2, survey.getSections().get(0).getQuestions().get(0).getLanguage());
	}

	@Test
	public void testClone() throws QuestionException {
		String specVersion = "1.0";
		String language = "ang";
		Survey survey = new Survey(specVersion, language);
		String sectionName = "G1";
		String sectionTitle = "stitle";
		Section section = new Section(sectionName, sectionTitle, sectionTitle);
		String question = "This is my question";
		String number = "12";
		YesNo answer = YesNo.Yes;
		String evidence = "evidence";
		Pattern pattern = Pattern.compile(".*");
		String[] specRefs = new String[] {"1.0", "1.1"};
		YesNoQuestionWithEvidence ynq = new YesNoQuestionWithEvidence(question, sectionName, 
				number, specVersion, specRefs, language, answer, evidence, pattern);
		List<Question> questions = new ArrayList<Question>();
		questions.add(ynq);
		section.setQuestions(questions);
		List<Section> sections = new ArrayList<Section>();
		sections.add(section);
		survey.setSections(sections);
		
		assertEquals(specVersion, survey.getSpecVersion());
		assertEquals(language, survey.getLanguage());
		assertEquals(1, survey.getSections().size());
		Section sclone = survey.getSections().get(0);
		assertEquals(sectionName, sclone.getName());
		assertEquals(sectionTitle, sclone.getTitle());
		List<Question> cloneQuestions = sclone.getQuestions();
		assertEquals(1, cloneQuestions.size());
		
		YesNoQuestionWithEvidence qclone = (YesNoQuestionWithEvidence)cloneQuestions.get(0);
		assertEquals(question, qclone.getQuestion());
		assertEquals(sectionName, qclone.getSectionName());
		assertEquals(specVersion, qclone.getSpecVersion());
		assertEquals(number, qclone.getNumber());
		assertEquals(answer, qclone.getCorrectAnswer());
		assertEquals(language, qclone.getLanguage());
		assertEquals(evidence, qclone.getEvidencePrompt());
		assertEquals(pattern.toString(), qclone.getEvidenceValidation().toString());
	}

}
