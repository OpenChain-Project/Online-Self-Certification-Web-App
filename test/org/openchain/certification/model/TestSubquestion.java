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
import org.openchain.certification.model.YesNoQuestion.YesNo;

/**
 * @author gary
 *
 */
public class TestSubquestion {

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
		String number = "12.1";
		YesNo answer = YesNo.Yes;
		String language = "ang";
		String[] specRefs = new String[] {"A", "B", "C"};
		YesNoQuestion ynq = new YesNoQuestion(question, sectionName, 
				number, specVersion, specRefs, language, answer);
		String question2 = "This is my second question";
		String number2 = "12.2";
		YesNo answer2 = YesNo.No;
		String[] specRefs2 = new String[] {"A", "B"};
		YesNoQuestion ynq2 = new YesNoQuestion(question2, sectionName, 
				number2, specVersion, specRefs2, language, answer2);
		String questionSub = "Overall question";
		String numberSub = "12";
		String[] specRefsSub = new String[] {"A"};
		int minValidSub = 2;
		SubQuestion subQuestion = new SubQuestion(questionSub, sectionName, numberSub, specVersion, 
				specRefsSub, language, minValidSub);
		subQuestion.addSubQuestion(ynq);
		subQuestion.addSubQuestion(ynq2);
		SubQuestion compare = subQuestion.clone();
		assertTrue(subQuestion.equivalent(compare));
		compare.setQuestion("Different");
		assertFalse(subQuestion.equivalent(compare));
		compare = subQuestion.clone();
		compare.setSectionName("G2");
		assertFalse(subQuestion.equivalent(compare));
		compare = subQuestion.clone();
		compare.setSpecVersion("1.1");
		assertFalse(subQuestion.equivalent(compare));
		compare = subQuestion.clone();
		compare.setNumber("12.1");
		assertFalse(subQuestion.equivalent(compare));
		compare = subQuestion.clone();
		compare.setMinNumberValidatedAnswers(minValidSub-1);
		assertFalse(subQuestion.equivalent(compare));
		compare = subQuestion.clone();
		compare.setLanguage("en");
		assertTrue(subQuestion.equivalent(compare));
		compare = subQuestion.clone();
		compare.setSpecReference(new String[] {"A", "B"});
		assertFalse(subQuestion.equivalent(compare));
		compare = subQuestion.clone();
		compare.removeSubQuestion(ynq2);
		assertFalse(subQuestion.equivalent(compare));
	}

}
