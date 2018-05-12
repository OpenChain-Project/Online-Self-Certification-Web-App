package org.openchain.certification;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openchain.certification.model.Question;
import org.openchain.certification.model.QuestionException;
import org.openchain.certification.model.SubQuestion;
import org.openchain.certification.model.YesNoNotApplicableQuestion;
import org.openchain.certification.model.YesNoQuestion;
import org.openchain.certification.model.YesNoQuestion.YesNo;
import org.openchain.certification.model.YesNoQuestionWithEvidence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestQuestionJsonDeserializer {
	static final String QUESTION = "question";
	static final String SECTION = "sectionName";
	static final String NUMBER = "1.a";
	static final String SPECVERSION = "1.1";
	static final String LANGUAGE = "fr";
	static final YesNo ANSWER = YesNo.Yes;
	private static final String SUBQUESTIONOF = "1";
	private static final String SPECREF = "1.1,2.2";
	private static final String NAPROMPT = "Not applicable";
	private static final String EVIDENCE_PROMPT = "Evidence prompt";
	private static final Pattern VALIDATION = Pattern.compile("Test String");
	private static final int NUMVALID = 7;
	Gson gson;

	@Before
	public void setUp() throws Exception {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Question.class, new QuestionJsonDeserializer());
		gson = builder.create();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDeserializeQuestionYesNo() throws QuestionException {
		YesNoQuestion q = new YesNoQuestion(QUESTION, SECTION, NUMBER, SPECVERSION,
				LANGUAGE, ANSWER);
		q.setSubQuestionOfNumber(SUBQUESTIONOF);
		q.setSpecReference(Question.specReferenceStrToArray(SPECREF));
		String jsonStr = gson.toJson(q);
		Question result = gson.fromJson(jsonStr, Question.class);
		assertTrue(result instanceof YesNoQuestion);
		assertEquals("YES_NO", result.getType());
		assertEquals(QUESTION, result.getQuestion());
		assertEquals(SECTION, result.getSectionName());
		assertEquals(SPECVERSION, result.getSpecVersion());
		assertEquals(SUBQUESTIONOF, q.getSubQuestionOfNumber());
		assertEquals(SPECREF, Question.specReferenceArrayToStr(q.getSpecReference()));
		assertEquals(ANSWER, ((YesNoQuestion)q).getCorrectAnswer());
	}
	
	@Test
	public void testDeserializeQuestionSubquestion() throws QuestionException {
		SubQuestion q = new SubQuestion(QUESTION, SECTION, NUMBER, SPECVERSION,
				LANGUAGE,  NUMVALID);
		q.setSubQuestionOfNumber(SUBQUESTIONOF);
		q.setSpecReference(Question.specReferenceStrToArray(SPECREF));
		YesNoQuestion subq1 = new YesNoQuestion("sq1-q", "sec1", "2.a", "1.1",
				"du", YesNo.No);
		subq1.setSubQuestionOfNumber(NUMBER);
		subq1.setSpecReference(Question.specReferenceStrToArray("3.2"));
		q.addSubQuestion(subq1);
		String jsonStr = gson.toJson(q);
		Question result = gson.fromJson(jsonStr, Question.class);
		assertTrue(result instanceof SubQuestion);
		assertEquals("SUBQUESTIONS", result.getType());
		assertEquals(QUESTION, result.getQuestion());
		assertEquals(SECTION, result.getSectionName());
		assertEquals(SPECVERSION, result.getSpecVersion());
		assertEquals(SUBQUESTIONOF, q.getSubQuestionOfNumber());
		assertEquals(SPECREF, Question.specReferenceArrayToStr(q.getSpecReference()));
		assertEquals(NUMVALID, ((SubQuestion)q).getMinNumberValidatedAnswers());
		Collection<Question> subquestions = ((SubQuestion)q).getAllSubquestions();
		assertEquals(1, subquestions.size());
		Question subresult = subquestions.toArray(new Question[1])[0];
		assertTrue(subresult instanceof YesNoQuestion);
		assertEquals(subq1.getQuestion(), subresult.getQuestion());
		assertEquals(subq1.getSectionName(), subresult.getSectionName());
		assertEquals(subq1.getSectionName(), subresult.getSectionName());
		assertEquals(subq1.getSpecVersion(), subresult.getSpecVersion());
		assertEquals(subq1.getSubQuestionOfNumber(), subresult.getSubQuestionOfNumber());
		assertEquals(Question.specReferenceArrayToStr(subq1.getSpecReference()), Question.specReferenceArrayToStr(subresult.getSpecReference()));
		assertEquals(((YesNoQuestion)subq1).getCorrectAnswer(), ((YesNoQuestion)subresult).getCorrectAnswer());
	}
	
	@Test
	public void testDeserializeQuestionYesNoNa() throws QuestionException {
		YesNoNotApplicableQuestion q = new YesNoNotApplicableQuestion(QUESTION, SECTION, NUMBER, SPECVERSION,
				LANGUAGE, ANSWER, NAPROMPT);
		q.setSubQuestionOfNumber(SUBQUESTIONOF);
		q.setSpecReference(Question.specReferenceStrToArray(SPECREF));
		String jsonStr = gson.toJson(q);
		Question result = gson.fromJson(jsonStr, Question.class);
		assertTrue(result instanceof YesNoNotApplicableQuestion);
		assertEquals("YES_NO_NA", result.getType());
		assertEquals(QUESTION, result.getQuestion());
		assertEquals(SECTION, result.getSectionName());
		assertEquals(SPECVERSION, result.getSpecVersion());
		assertEquals(SUBQUESTIONOF, q.getSubQuestionOfNumber());
		assertEquals(SPECREF, Question.specReferenceArrayToStr(q.getSpecReference()));
		assertEquals(ANSWER, ((YesNoQuestion)q).getCorrectAnswer());
		assertEquals(NAPROMPT, ((YesNoNotApplicableQuestion)q).getNotApplicablePrompt());
	}
	
	@Test
	public void testDeserializeQuestionYesNoEvidence() throws QuestionException {
		YesNoQuestionWithEvidence q = new YesNoQuestionWithEvidence(QUESTION, SECTION, NUMBER, SPECVERSION,
				LANGUAGE, ANSWER, EVIDENCE_PROMPT, VALIDATION);
		q.setSubQuestionOfNumber(SUBQUESTIONOF);
		q.setSpecReference(Question.specReferenceStrToArray(SPECREF));
		String jsonStr = gson.toJson(q);
		Question result = gson.fromJson(jsonStr, Question.class);
		assertTrue(result instanceof YesNoQuestionWithEvidence);
		assertEquals("YES_NO_EVIDENCE", result.getType());
		assertEquals(QUESTION, result.getQuestion());
		assertEquals(SECTION, result.getSectionName());
		assertEquals(SPECVERSION, result.getSpecVersion());
		assertEquals(SUBQUESTIONOF, q.getSubQuestionOfNumber());
		assertEquals(SPECREF, Question.specReferenceArrayToStr(q.getSpecReference()));
		assertEquals(ANSWER, ((YesNoQuestion)q).getCorrectAnswer());
		assertEquals(EVIDENCE_PROMPT, ((YesNoQuestionWithEvidence)q).getEvidencePrompt());
		assertEquals(VALIDATION.pattern(), ((YesNoQuestionWithEvidence)q).getEvidenceValidation().pattern());
	}
}
