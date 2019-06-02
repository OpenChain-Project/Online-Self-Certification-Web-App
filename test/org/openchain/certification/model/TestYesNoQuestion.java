package org.openchain.certification.model;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openchain.certification.model.YesNoQuestion.YesNo;

public class TestYesNoQuestion {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSetCorrectAnswer() throws QuestionException {
		String question = "This is my question";
		String sectionName = "G1";
		String specVersion = "1.0";
		String number = "12";
		YesNo answer = YesNo.Yes;
		String language = "eng";
		String[] specRefs = new String[] {"1.0", "1.1"};
		YesNoQuestion ynq = new YesNoQuestion(question, sectionName, 
				number, specVersion, specRefs, language, answer);
		assertEquals(YesNo.Yes, ynq.getCorrectAnswer());
		ynq.setCorrectAnswer(YesNo.No);
		assertEquals(YesNo.No, ynq.getCorrectAnswer());
	}

	@Test
	public void testCompareTo() throws QuestionException {
		String question = "This is my question";
		String sectionName = "G1";
		String specVersion = "1.0";
		String language = "eng";
		String number = "14";
		YesNo answer = YesNo.Yes;
		String[] specRefs = new String[] {"1.0", "1.1"};
		YesNoQuestion ynq = new YesNoQuestion(question, sectionName, 
				number, specVersion, specRefs, language, answer);
		YesNoQuestion ynqSame = new YesNoQuestion(question, sectionName, 
				number, specVersion, specRefs, language, YesNo.No);
		assertEquals(0, ynq.compareTo(ynqSame));
		assertEquals(0, ynqSame.compareTo(ynq));
		
		// Greater spec version
		
		YesNoQuestion ynqVersionGreater = new YesNoQuestion(question, sectionName, 
				"1", "1.1", specRefs, language, answer);
		assertTrue(ynq.compareTo(ynqVersionGreater) < 0);
		assertTrue(ynqVersionGreater.compareTo(ynq) > 0);
		
		// Question number greater (first digit)
		YesNoQuestion ynqNumberGreater = new YesNoQuestion(question, sectionName, 
				"15", specVersion, specRefs, language, answer);
		assertTrue(ynq.compareTo(ynqNumberGreater) < 0);
		assertTrue(ynqNumberGreater.compareTo(ynq) > 0);
		
		YesNoQuestion ynqSubOne = new YesNoQuestion(question, sectionName, 
				"14.1", specVersion, specRefs, language, answer);
		assertTrue(ynq.compareTo(ynqSubOne) < 0);
		assertTrue(ynqSubOne.compareTo(ynq) > 0);
		
		YesNoQuestion ynqSubTwo = new YesNoQuestion(question, sectionName, 
				"14.10", specVersion, specRefs, language, answer);
		assertTrue(ynqSubOne.compareTo(ynqSubTwo) < 0);
		assertTrue(ynqSubTwo.compareTo(ynqSubOne) > 0);
		
		YesNoQuestion ynqSubSubOne = new YesNoQuestion(question, sectionName, 
				"14.1.5", specVersion, specRefs, language, answer);
		assertTrue(ynqSubOne.compareTo(ynqSubSubOne) < 0);
		assertTrue(ynqSubSubOne.compareTo(ynqSubOne) > 0);
		
		YesNoQuestion ynqSubSubTwo = new YesNoQuestion(question, sectionName, 
				"14.1.100", specVersion, specRefs, language, answer);
		assertTrue(ynqSubSubOne.compareTo(ynqSubSubTwo) < 0);
		assertTrue(ynqSubSubTwo.compareTo(ynqSubSubOne) > 0);
	}
	
	@Test
	public void testValidate() throws QuestionException {
		String question = "This is my question";
		String sectionName = "G1";
		String specVersion = "1.0";
		String number = "12";
		YesNo answer = YesNo.Yes;
		String language = "ang";
		String[] specRefs = new String[] {"1.0", "1.1"};
		YesNoQuestion ynq = new YesNoQuestion(question, sectionName, 
				number, specVersion, specRefs, language, answer);
		assertTrue(!ynq.validate(YesNo.Any));
		assertTrue(ynq.validate(YesNo.Yes));
		assertTrue(!ynq.validate(YesNo.No));
		assertTrue(!ynq.validate(YesNo.NoNotApplicable));
		assertTrue(!ynq.validate(YesNo.NotAnswered));
		assertTrue(!ynq.validate(YesNo.NotApplicable));
		assertTrue(!ynq.validate(YesNo.YesNotApplicable));
		
		YesNo answer2 = YesNo.NoNotApplicable;
		YesNoNotApplicableQuestion ynq2 = new YesNoNotApplicableQuestion(question, sectionName, 
				number, specVersion, specRefs, language, answer2, "Prompt");
		assertTrue(!ynq2.validate(YesNo.Any));
		assertTrue(!ynq2.validate(YesNo.Yes));
		assertTrue(ynq2.validate(YesNo.No));
		assertTrue(!ynq2.validate(YesNo.NoNotApplicable));
		assertTrue(!ynq2.validate(YesNo.NotAnswered));
		assertTrue(ynq2.validate(YesNo.NotApplicable));
		assertTrue(!ynq2.validate(YesNo.YesNotApplicable));
		
		YesNo answer3 = YesNo.Any;
		YesNoNotApplicableQuestion ynq3 = new YesNoNotApplicableQuestion(question, sectionName, 
				number, specVersion, specRefs, language, answer3, "Prompt");
		assertTrue(ynq3.validate(YesNo.Any));
		assertTrue(ynq3.validate(YesNo.Yes));
		assertTrue(ynq3.validate(YesNo.No));
		assertTrue(ynq3.validate(YesNo.NoNotApplicable));
		assertTrue(ynq3.validate(YesNo.NotAnswered));
		assertTrue(ynq3.validate(YesNo.NotApplicable));
		assertTrue(ynq3.validate(YesNo.YesNotApplicable));
		
		YesNo answer4 = YesNo.YesNotApplicable;
		YesNoNotApplicableQuestion ynq4 = new YesNoNotApplicableQuestion(question, sectionName, 
				number, specVersion, specRefs, language, answer4, "Prompt");
		assertTrue(!ynq4.validate(YesNo.Any));
		assertTrue(ynq4.validate(YesNo.Yes));
		assertTrue(!ynq4.validate(YesNo.No));
		assertTrue(!ynq4.validate(YesNo.NoNotApplicable));
		assertTrue(!ynq4.validate(YesNo.NotAnswered));
		assertTrue(ynq4.validate(YesNo.NotApplicable));
		assertTrue(!ynq4.validate(YesNo.YesNotApplicable));
	}
	
	@Test
	public void testLanguage() throws QuestionException {
		String question = "This is my question";
		String sectionName = "G1";
		String specVersion = "1.0";
		String number = "12";
		YesNo answer = YesNo.Yes;
		String language = "ang";
		String[] specRefs = new String[] {"1.0", "1.1"};
		YesNoQuestion ynq = new YesNoQuestion(question, sectionName, 
				number, specVersion, specRefs, language, answer);
		assertEquals(language, ynq.getLanguage());
		String language2 = "fra";
		ynq.setLanguage(language2);
		assertEquals(language2, ynq.getLanguage());
	}
	
	@Test
	public void testClone() throws QuestionException {
		String question = "This is my question";
		String sectionName = "G1";
		String specVersion = "1.0";
		String number = "12";
		YesNo answer = YesNo.Yes;
		String language = "ang";
		String[] specRefs = new String[] {"1.0", "1.1"};
		YesNoQuestion ynq = new YesNoQuestion(question, sectionName, 
				number, specVersion, specRefs, language, answer);
		YesNoQuestion clone = ynq.clone();
		assertEquals(question, clone.getQuestion());
		assertEquals(sectionName, clone.getSectionName());
		assertEquals(specVersion, clone.getSpecVersion());
		assertEquals(number, clone.getNumber());
		assertEquals(answer, clone.getCorrectAnswer());
		assertEquals(language, clone.getLanguage());
		assertTrue(Arrays.deepEquals(specRefs, clone.getSpecReference()));
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
		YesNoQuestion ynq = new YesNoQuestion(question, sectionName, 
				number, specVersion, specRefs, language, answer);
		YesNoQuestion compare = ynq.clone();
		assertTrue(ynq.equivalent(compare));
		compare.setQuestion("Different");
		assertFalse(ynq.equivalent(compare));
		compare = ynq.clone();
		compare.setSectionName("G2");
		assertFalse(ynq.equivalent(compare));
		compare = ynq.clone();
		compare.setSpecVersion("1.1");
		assertFalse(ynq.equivalent(compare));
		compare = ynq.clone();
		compare.setNumber("12.1");
		assertFalse(ynq.equivalent(compare));
		compare = ynq.clone();
		compare.setCorrectAnswer(YesNo.No);
		assertFalse(ynq.equivalent(compare));
		compare = ynq.clone();
		compare.setLanguage("en");
		assertTrue(ynq.equivalent(compare));
		compare = ynq.clone();
		compare.setSpecReference(new String[] {"A", "B"});
		assertFalse(ynq.equivalent(compare));
	}

}
