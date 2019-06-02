/**
 * Copyright (c) 2019 Source Auditor Inc.
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author gary
 *
 */
public class TestYesNoNotApplicableQuestion {

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
	public void testEquivelant() throws QuestionException {
		String question = "This is my question";
		String sectionName = "G1";
		String specVersion = "1.0";
		String number = "12";
		YesNoQuestion.YesNo answer = YesNoQuestion.YesNo.Yes;
		String language = "ang";
		String[] specRefs = new String[] {"A", "B", "C"};
		String prompt = "Prompt";
		YesNoNotApplicableQuestion ynnaq = new YesNoNotApplicableQuestion(question, sectionName, 
				number, specVersion, specRefs, language, answer, prompt);
		YesNoNotApplicableQuestion compare = ynnaq.clone();
		assertTrue(ynnaq.equivalent(compare));
		compare.setQuestion("Different");
		assertFalse(ynnaq.equivalent(compare));
		compare = ynnaq.clone();
		compare.setSectionName("G2");
		assertFalse(ynnaq.equivalent(compare));
		compare = ynnaq.clone();
		compare.setSpecVersion("1.1");
		assertFalse(ynnaq.equivalent(compare));
		compare = ynnaq.clone();
		compare.setNumber("12.1");
		assertFalse(ynnaq.equivalent(compare));
		compare = ynnaq.clone();
		compare.setCorrectAnswer(YesNoQuestion.YesNo.No);
		assertFalse(ynnaq.equivalent(compare));
		compare = ynnaq.clone();
		compare.setLanguage("en");
		assertTrue(ynnaq.equivalent(compare));
		compare = ynnaq.clone();
		compare.setSpecReference(new String[] {"A", "B"});
		assertFalse(ynnaq.equivalent(compare));
		compare = ynnaq.clone();
		compare.setNotApplicablePrompt("Different");
		assertFalse(ynnaq.equivalent(compare));
	}

}
