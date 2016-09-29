package org.openchain.certification.model;

import static org.junit.Assert.*;

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
		YesNoQuestion ynq = new YesNoQuestion(question, sectionName, 
				number, specVersion, answer);
		assertEquals(YesNo.Yes, ynq.getCorrectAnswer());
		ynq.setCorrectAnswer(YesNo.No);
		assertEquals(YesNo.No, ynq.getCorrectAnswer());
	}

	@Test
	public void testCompareTo() throws QuestionException {
		String question = "This is my question";
		String sectionName = "G1";
		String specVersion = "1.0";
		String number = "14";
		YesNo answer = YesNo.Yes;
		YesNoQuestion ynq = new YesNoQuestion(question, sectionName, 
				number, specVersion, answer);
		YesNoQuestion ynqSame = new YesNoQuestion(question, sectionName, 
				number, specVersion, YesNo.No);
		assertEquals(0, ynq.compareTo(ynqSame));
		assertEquals(0, ynqSame.compareTo(ynq));
		
		// Greater spec version
		
		YesNoQuestion ynqVersionGreater = new YesNoQuestion(question, sectionName, 
				"1", "1.1", answer);
		assertTrue(ynq.compareTo(ynqVersionGreater) < 0);
		assertTrue(ynqVersionGreater.compareTo(ynq) > 0);
		
		// Question number greater (first digit)
		YesNoQuestion ynqNumberGreater = new YesNoQuestion(question, sectionName, 
				"15", specVersion, answer);
		assertTrue(ynq.compareTo(ynqNumberGreater) < 0);
		assertTrue(ynqNumberGreater.compareTo(ynq) > 0);
		
		YesNoQuestion ynqSubOne = new YesNoQuestion(question, sectionName, 
				"14.1", specVersion, answer);
		assertTrue(ynq.compareTo(ynqSubOne) < 0);
		assertTrue(ynqSubOne.compareTo(ynq) > 0);
		
		YesNoQuestion ynqSubTwo = new YesNoQuestion(question, sectionName, 
				"14.10", specVersion, answer);
		assertTrue(ynqSubOne.compareTo(ynqSubTwo) < 0);
		assertTrue(ynqSubTwo.compareTo(ynqSubOne) > 0);
		
		YesNoQuestion ynqSubSubOne = new YesNoQuestion(question, sectionName, 
				"14.1.5", specVersion, answer);
		assertTrue(ynqSubOne.compareTo(ynqSubSubOne) < 0);
		assertTrue(ynqSubSubOne.compareTo(ynqSubOne) > 0);
		
		YesNoQuestion ynqSubSubTwo = new YesNoQuestion(question, sectionName, 
				"14.1.100", specVersion, answer);
		assertTrue(ynqSubSubOne.compareTo(ynqSubSubTwo) < 0);
		assertTrue(ynqSubSubTwo.compareTo(ynqSubSubOne) > 0);
	}

}
