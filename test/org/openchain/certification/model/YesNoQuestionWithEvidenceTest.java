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

import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openchain.certification.model.YesNoQuestion.YesNo;

/**
 * @author gary
 *
 */
public class YesNoQuestionWithEvidenceTest {

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
	public void testEquivalent() throws QuestionException {
		String question = "This is my question";
		String sectionName = "G1";
		String specVersion = "1.0";
		String number = "12";
		YesNo answer = YesNo.Yes;
		String language = "ang";
		String[] specRefs = new String[] {"A", "B", "C"};
		String evidence = "Evidence";
		Pattern evidencePattern = Pattern.compile(".*");
		YesNoQuestionWithEvidence ynqwithevidence = new YesNoQuestionWithEvidence(question, sectionName, 
				number, specVersion, specRefs, language, answer, evidence, evidencePattern);
		YesNoQuestionWithEvidence compare = ynqwithevidence.clone();
		assertTrue(ynqwithevidence.equivalent(compare));
		compare.setQuestion("Different");
		assertFalse(ynqwithevidence.equivalent(compare));
		compare = ynqwithevidence.clone();
		compare.setSectionName("G2");
		assertFalse(ynqwithevidence.equivalent(compare));
		compare = ynqwithevidence.clone();
		compare.setSpecVersion("1.1");
		assertFalse(ynqwithevidence.equivalent(compare));
		compare = ynqwithevidence.clone();
		compare.setNumber("12.1");
		assertFalse(ynqwithevidence.equivalent(compare));
		compare = ynqwithevidence.clone();
		compare.setCorrectAnswer(YesNo.No);
		assertFalse(ynqwithevidence.equivalent(compare));
		compare = ynqwithevidence.clone();
		compare.setLanguage("en");
		assertTrue(ynqwithevidence.equivalent(compare));
		compare = ynqwithevidence.clone();
		compare.setSpecReference(new String[] {"A", "B"});
		assertFalse(ynqwithevidence.equivalent(compare));
		compare = ynqwithevidence.clone();
		compare.setEvidencePrompt("New prompt");
		assertFalse(ynqwithevidence.equivalent(compare));
		compare = ynqwithevidence.clone();
		compare.setEvidenceValidation(Pattern.compile("match"));
		assertFalse(ynqwithevidence.equivalent(compare));
		compare = ynqwithevidence.clone();
		compare.setEvidenceValidation(Pattern.compile(".*"));
		assertTrue(ynqwithevidence.equivalent(compare));
	}

}
