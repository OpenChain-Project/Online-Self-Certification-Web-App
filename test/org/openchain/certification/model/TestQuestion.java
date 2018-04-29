package org.openchain.certification.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestQuestion {
	
	static final String ROMAN_SPEC_VERSION = "1.0.2";
	static final String NUMERIC_SPEC_STRING = "1.0.1";
	static final String ROMAN_LANGUAGE = "eng";
	static final String NUMERIC_LANGUAGE = "eng";
	
	static final String[] ORDERED_ROMAN = new String[] {
		"1", "1.a", "2", "2.b", "2.aa", "2.aa.i", "2.aa.ii", "2.aa.v","2.aa.c", "11"
	};
	
	static final String[] ORDERED_NUMERIC = new String[] {
		"1", "2", "2.1", "2.1.1", "2.1.2", "2.1.11", "2.11"
	};
	
	
	
	class QuestionForTest extends Question {

		public QuestionForTest(String question, String sectionName,
				String number, String specVersion, String language) throws QuestionException {
			super(question, sectionName, number, specVersion, language);
		}

		@Override
		public boolean validate(Object answer) {
			return false;
		}

		@Override
		protected Object getCorrectAnswer() {
			return null;
		}

		@Override
		public Question clone() {
			try {
				return new QuestionForTest(getQuestion(), getSectionName(), getNumber(), getSpecVersion(), getLanguage());
			} catch (QuestionException e) {
				throw new RuntimeException(e);
			}
		}
		
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSortRoman() throws QuestionException {
		List<QuestionForTest> romanQuestions = new ArrayList<QuestionForTest>();
		for (int i = ORDERED_ROMAN.length-1; i >= 0; i--) {
			romanQuestions.add(new QuestionForTest("Q"+ORDERED_ROMAN[i],
					"section", ORDERED_ROMAN[i], ROMAN_SPEC_VERSION, ROMAN_LANGUAGE));
		}
		Collections.sort(romanQuestions);
		for (int i = 0; i  < ORDERED_ROMAN.length; i++) {
			assertEquals(ORDERED_ROMAN[i],romanQuestions.get(i).getNumber());
		}
	}
	@Test
	public void testSortNumeric() throws QuestionException {
		List<QuestionForTest> romanQuestions = new ArrayList<QuestionForTest>();
		for (int i = ORDERED_NUMERIC.length-1; i >= 0; i--) {
			romanQuestions.add(new QuestionForTest("Q"+ORDERED_NUMERIC[i],
					"section", ORDERED_NUMERIC[i], NUMERIC_SPEC_STRING, NUMERIC_LANGUAGE));
		}
		Collections.sort(romanQuestions);
		for (int i = 0; i  < ORDERED_NUMERIC.length; i++) {
			assertEquals(ORDERED_NUMERIC[i],romanQuestions.get(i).getNumber());
		}
	}
}
