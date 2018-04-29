package org.openchain.certification.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openchain.certification.model.YesNoQuestion.YesNo;

public class TestSection {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSetLanguage() throws QuestionException {
		String sectionName = "G1";
		String sectionTitle = "stitle";
		String language = "ang";
		Section section = new Section(sectionName, sectionTitle, language);
		String question = "This is my question";
		String specVersion = "1.0";
		String number = "12";
		YesNo answer = YesNo.Yes;
		String evidence = "evidence";
		Pattern pattern = Pattern.compile(".*");
		YesNoQuestionWithEvidence ynq = new YesNoQuestionWithEvidence(question, sectionName, 
				number, specVersion, language, answer, evidence, pattern);
		List<Question> questions = new ArrayList<Question>();
		questions.add(ynq);
		section.setQuestions(questions);
		assertEquals(language, section.getLanguage());
		assertEquals(language, section.getQuestions().get(0).getLanguage());
		
		String language2 = "ger";
		section.setLanguage(language2);
		assertEquals(language2, section.getLanguage());
		assertEquals(language2, section.getQuestions().get(0).getLanguage());
	}

	@Test
	public void testClone() throws QuestionException {
		String sectionName = "G1";
		String sectionTitle = "stitle";
		String language = "ang";
		Section section = new Section(sectionName, sectionTitle, language);
		String question = "This is my question";
		String specVersion = "1.0";
		String number = "12";
		YesNo answer = YesNo.Yes;
		String evidence = "evidence";
		Pattern pattern = Pattern.compile(".*");
		YesNoQuestionWithEvidence ynq = new YesNoQuestionWithEvidence(question, sectionName, 
				number, specVersion, language, answer, evidence, pattern);
		List<Question> questions = new ArrayList<Question>();
		questions.add(ynq);
		section.setQuestions(questions);
		
		Section clone = section.clone();
		assertEquals(sectionName, clone.getName());
		assertEquals(sectionTitle, clone.getTitle());
		List<Question> cloneQuestions = clone.getQuestions();
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
