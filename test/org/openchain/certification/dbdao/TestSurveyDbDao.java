package org.openchain.certification.dbdao;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openchain.certification.TestHelper;
import org.openchain.certification.model.Question;
import org.openchain.certification.model.QuestionException;
import org.openchain.certification.model.Section;
import org.openchain.certification.model.SubQuestion;
import org.openchain.certification.model.Survey;
import org.openchain.certification.model.SurveyResponseException;
import org.openchain.certification.model.YesNoNotApplicableQuestion;
import org.openchain.certification.model.YesNoQuestion;
import org.openchain.certification.model.YesNoQuestion.YesNo;
import org.openchain.certification.model.YesNoQuestionWithEvidence;

public class TestSurveyDbDao {
	
	private Connection con;

	@Before
	public void setUp() throws Exception {
		con = TestHelper.getConnection();
		TestHelper.truncateDatabase(con);
	}

	@After
	public void tearDown() throws Exception {
		con.close();
	}

	@Test
	public void testGetSurveyString() throws QuestionException, SQLException, SurveyResponseException {
		String specVersion = "test-spec-version";
		Survey survey = new Survey(specVersion);
		List<Section> sections = new ArrayList<Section>();
		Section section1 = new Section();
		String section1Name = "section1Name";
		section1.setName(section1Name);
		String section1Title = "section1Title";
		section1.setTitle(section1Title);
		List<Question> section1Questions = new ArrayList<Question>();
		String s1q1Question="s1q1question";
		String s1q1Number = "1.a";
		YesNo s1q1Answer = YesNo.Yes;
		Question s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, specVersion, s1q1Answer);
		String s1q1SpecRef = "s1q1SpecRef";
		s1q1.setSpecReference(s1q1SpecRef);
		section1Questions.add(s1q1);
		String s1q2Question="s1q2question";
		String s1q2Number = "1.b";
		YesNo s1q2Answer = YesNo.NotApplicable;
		String s1q2Prompt = "s1q2prompt";
		Question s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, specVersion, s1q2Answer, s1q2Prompt);
		String s1q2SpecRef = "s1q2SpecRef";
		s1q2.setSpecReference(s1q2SpecRef);
		section1Questions.add(s1q2);
		section1.setQuestions(section1Questions);
		sections.add(section1);
		Section section2 = new Section();
		String section2Name = "section2Name";
		section2.setName(section2Name);
		String section2Title = "section2Title";
		section2.setTitle(section2Title);
		List<Question> section2Questions = new ArrayList<Question>();
		String s2q1Question="s2q1question";
		String s2q1Number = "2.b";
		int s2q1MinCorrect = 4;
		Question s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, specVersion, s2q1MinCorrect);
		String s2q1SpecRef = "s2q1SpecRef";
		s2q1.setSpecReference(s2q1SpecRef);
		section2Questions.add(s2q1);
		String s2q2Question="s2q2question";
		String s2q2Number = "2.b.ii";
		YesNo s2q2Answer = YesNo.No;
		String s2q2Prompt = "s2q2prompt";
		String s2q2validate = "dd";
		Question s2q2 = new YesNoQuestionWithEvidence(s2q2Question, 
				section2Name, s2q2Number, specVersion, s2q2Answer, s2q2Prompt, Pattern.compile(s2q2validate));
		String s2q2SpecRef = "s2q2SpecRef";
		s2q2.setSpecReference(s2q2SpecRef);
		s2q2.addSubQuestionOf(s2q1Number);
		section2Questions.add(s2q2);
		String s2q3Question="s2q3question";
		String s2q3Number = "1.b.iii";
		YesNo s2q3Answer = YesNo.NotAnswered;
		Question s2q3 = new YesNoQuestion(s2q3Question, 
				section2Name, s2q3Number, specVersion, s2q3Answer);
		String s2q3SpecRef = "s2q3SpecRef";
		s2q3.setSpecReference(s2q3SpecRef);
		s2q3.addSubQuestionOf(s2q1Number);
		section2Questions.add(s2q3);
		section2.setQuestions(section2Questions);
		sections.add(section2);
		survey.setSections(sections);
		SurveyDbDao dao = new SurveyDbDao(con);
		dao.addSurvey(survey);
		Survey result = dao.getSurvey();
		assertEquals(specVersion, result.getSpecVersion());
		for (Section rs:result.getSections()) {
			if (rs.getName().equals(section1Name)) {
				assertEquals(section1Title, rs.getTitle());
				for (Question rq:rs.getQuestions()) {
					if (rq.getNumber().equals(s1q1Number)) {
						YesNoQuestion yn = (YesNoQuestion)rq;
						assertEquals(s1q1Question, yn.getQuestion());
						assertEquals(s1q1Answer, yn.getCorrectAnswer());
						assertEquals(section1Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(s1q1SpecRef, yn.getSpecReference());
						assertTrue(yn.getSubQuestionNumber() == null);
					} else if (rq.getNumber().equals(s1q2Number)) {
						YesNoNotApplicableQuestion yn = (YesNoNotApplicableQuestion)rq;
						assertEquals(s1q2Question, yn.getQuestion());
						assertEquals(s1q2Answer, yn.getCorrectAnswer());
						assertEquals(section1Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(s1q2SpecRef, yn.getSpecReference());
						assertTrue(yn.getSubQuestionNumber() == null);
					} else {
						fail("no matching question numbers");
					}
				}
			} else if (rs.getName().equals(section2Name)) {
				assertEquals(section2Title, rs.getTitle());
				for (Question rq:rs.getQuestions()) {
					if (rq.getNumber().equals(s2q1Number)) {
						SubQuestion yn = (SubQuestion)rq;
						assertEquals(s2q1Question, yn.getQuestion());
						assertEquals(s2q1MinCorrect, yn.getMinNumberValidatedAnswers());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(s2q1SpecRef, yn.getSpecReference());
						assertTrue(yn.getSubQuestionNumber() == null);
					} else if (rq.getNumber().equals(s2q2Number)) {
						YesNoQuestionWithEvidence yn = (YesNoQuestionWithEvidence)rq;
						assertEquals(s2q2Question, yn.getQuestion());
						assertEquals(s2q2Answer, yn.getCorrectAnswer());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(s2q2SpecRef, yn.getSpecReference());
						assertEquals(s2q2Prompt, yn.getEvidencePrompt());
						assertEquals(s2q2validate, yn.getEvidenceValidation().toString());
						assertEquals(s2q1Number, yn.getSubQuestionNumber());
					} else if (rq.getNumber().equals(s2q3Number)) {
						YesNoQuestion yn = (YesNoQuestion)rq;
						assertEquals(s2q3Question, yn.getQuestion());
						assertEquals(s2q3Answer, yn.getCorrectAnswer());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(s2q3SpecRef, yn.getSpecReference());
						assertEquals(s2q1Number, yn.getSubQuestionNumber());
					} else {
						fail("no matching question numbers");
					}
				}
			} else {
				fail("No matching section names");
			}
		}
	}

	@Test
	public void testUpdateQuestions() throws QuestionException, SQLException, SurveyResponseException {
		String specVersion = "test-spec-version";
		Survey survey = new Survey(specVersion);
		List<Section> sections = new ArrayList<Section>();
		Section section1 = new Section();
		String section1Name = "section1Name";
		section1.setName(section1Name);
		String section1Title = "section1Title";
		section1.setTitle(section1Title);
		List<Question> section1Questions = new ArrayList<Question>();
		String s1q1Question="s1q1question";
		String s1q1Number = "1.a";
		YesNo s1q1Answer = YesNo.Yes;
		Question s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, specVersion, s1q1Answer);
		String s1q1SpecRef = "s1q1SpecRef";
		s1q1.setSpecReference(s1q1SpecRef);
		section1Questions.add(s1q1);
		String s1q2Question="s1q2question";
		String s1q2Number = "1.b";
		YesNo s1q2Answer = YesNo.NotApplicable;
		String s1q2Prompt = "s1q2prompt";
		Question s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, specVersion, s1q2Answer, s1q2Prompt);
		String s1q2SpecRef = "s1q2SpecRef";
		s1q2.setSpecReference(s1q2SpecRef);
		section1Questions.add(s1q2);
		section1.setQuestions(section1Questions);
		sections.add(section1);
		Section section2 = new Section();
		String section2Name = "section2Name";
		section2.setName(section2Name);
		String section2Title = "section2Title";
		section2.setTitle(section2Title);
		List<Question> section2Questions = new ArrayList<Question>();
		String s2q1Question="s2q1question";
		String s2q1Number = "2.b";
		int s2q1MinCorrect = 4;
		Question s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, specVersion, s2q1MinCorrect);
		String s2q1SpecRef = "s2q1SpecRef";
		s2q1.setSpecReference(s2q1SpecRef);
		section2Questions.add(s2q1);
		String s2q2Question="s2q2question";
		String s2q2Number = "2.b.ii";
		YesNo s2q2Answer = YesNo.No;
		String s2q2Prompt = "s2q2prompt";
		String s2q2validate = "dd";
		Question s2q2 = new YesNoQuestionWithEvidence(s2q2Question, 
				section2Name, s2q2Number, specVersion, s2q2Answer, s2q2Prompt, Pattern.compile(s2q2validate));
		String s2q2SpecRef = "s2q2SpecRef";
		s2q2.setSpecReference(s2q2SpecRef);
		s2q2.addSubQuestionOf(s2q1Number);
		section2Questions.add(s2q2);
		String s2q3Question="s2q3question";
		String s2q3Number = "1.b.iii";
		YesNo s2q3Answer = YesNo.NotAnswered;
		Question s2q3 = new YesNoQuestion(s2q3Question, 
				section2Name, s2q3Number, specVersion, s2q3Answer);
		String s2q3SpecRef = "s2q3SpecRef";
		s2q3.setSpecReference(s2q3SpecRef);
		s2q3.addSubQuestionOf(s2q1Number);
		section2Questions.add(s2q3);
		section2.setQuestions(section2Questions);
		sections.add(section2);
		survey.setSections(sections);
		SurveyDbDao dao = new SurveyDbDao(con);
		dao.addSurvey(survey);
		
		// make updates
		String us1q2Question = "Updated s1q2Question";
		YesNo updates1q2Answer = YesNo.Any;
		String us1q2Prompt = "Updated "+s1q2Prompt;
		String us1q2valid = "abc";
		Question Updateds1q2 = new YesNoQuestionWithEvidence(us1q2Question, 
				section1Name, s1q2Number, specVersion, updates1q2Answer, us1q2Prompt, Pattern.compile(us1q2valid));
		String us1q2specRef = "UpdatedRef";
		Updateds1q2.setSpecReference(us1q2specRef);
		
		String us2q3Question = "Updated " +s2q3Question;		
		Question updateds2q3 = new YesNoQuestion(us2q3Question, 
				section2Name, s2q3Number, specVersion, s2q3Answer);
		String us2q3SpecRef = "us2q3SpecRef";
		updateds2q3.setSpecReference(us2q3SpecRef);
		updateds2q3.addSubQuestionOf(s2q1Number);
		List<Question> questionsToUpdate = new ArrayList<Question>();
		questionsToUpdate.add(Updateds1q2);
		questionsToUpdate.add(updateds2q3);
		dao.updateQuestions(questionsToUpdate);
		Survey result = dao.getSurvey();
		assertEquals(specVersion, result.getSpecVersion());
		for (Section rs:result.getSections()) {
			if (rs.getName().equals(section1Name)) {
				assertEquals(section1Title, rs.getTitle());
				for (Question rq:rs.getQuestions()) {
					if (rq.getNumber().equals(s1q1Number)) {
						YesNoQuestion yn = (YesNoQuestion)rq;
						assertEquals(s1q1Question, yn.getQuestion());
						assertEquals(s1q1Answer, yn.getCorrectAnswer());
						assertEquals(section1Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(s1q1SpecRef, yn.getSpecReference());
						assertTrue(yn.getSubQuestionNumber() == null);
					} else if (rq.getNumber().equals(s1q2Number)) {
						YesNoQuestionWithEvidence yn = (YesNoQuestionWithEvidence)rq;
						assertEquals(us1q2Question, yn.getQuestion());
						assertEquals(updates1q2Answer, yn.getCorrectAnswer());
						assertEquals(section1Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(us1q2specRef, yn.getSpecReference());
						assertEquals(us1q2Prompt, yn.getEvidencePrompt());
						assertEquals(us1q2valid, yn.getEvidenceValidation().toString());
						assertTrue(yn.getSubQuestionNumber() == null);
					} else {
						fail("no matching question numbers");
					}
				}
			} else if (rs.getName().equals(section2Name)) {
				assertEquals(section2Title, rs.getTitle());
				for (Question rq:rs.getQuestions()) {
					if (rq.getNumber().equals(s2q1Number)) {
						SubQuestion yn = (SubQuestion)rq;
						assertEquals(s2q1Question, yn.getQuestion());
						assertEquals(s2q1MinCorrect, yn.getMinNumberValidatedAnswers());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(s2q1SpecRef, yn.getSpecReference());
						assertTrue(yn.getSubQuestionNumber() == null);
					} else if (rq.getNumber().equals(s2q2Number)) {
						YesNoQuestionWithEvidence yn = (YesNoQuestionWithEvidence)rq;
						assertEquals(s2q2Question, yn.getQuestion());
						assertEquals(s2q2Answer, yn.getCorrectAnswer());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(s2q2SpecRef, yn.getSpecReference());
						assertEquals(s2q2Prompt, yn.getEvidencePrompt());
						assertEquals(s2q2validate, yn.getEvidenceValidation().toString());
						assertEquals(s2q1Number, yn.getSubQuestionNumber());
					} else if (rq.getNumber().equals(s2q3Number)) {
						YesNoQuestion yn = (YesNoQuestion)rq;
						assertEquals(us2q3Question, yn.getQuestion());
						assertEquals(s2q3Answer, yn.getCorrectAnswer());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(us2q3SpecRef, yn.getSpecReference());
						assertEquals(s2q1Number, yn.getSubQuestionNumber());
					} else {
						fail("no matching question numbers");
					}
				}
			} else {
				fail("No matching section names");
			}
		}
	}

	@Test
	public void testAddQuestions() throws SQLException, SurveyResponseException, QuestionException {
		String specVersion = "test-spec-version";
		Survey survey = new Survey(specVersion);
		List<Section> sections = new ArrayList<Section>();
		Section section1 = new Section();
		String section1Name = "section1Name";
		section1.setName(section1Name);
		String section1Title = "section1Title";
		section1.setTitle(section1Title);
		List<Question> section1Questions = new ArrayList<Question>();
		String s1q1Question="s1q1question";
		String s1q1Number = "1.a";
		YesNo s1q1Answer = YesNo.Yes;
		Question s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, specVersion, s1q1Answer);
		String s1q1SpecRef = "s1q1SpecRef";
		s1q1.setSpecReference(s1q1SpecRef);
		section1Questions.add(s1q1);
		String s1q2Question="s1q2question";
		String s1q2Number = "1.b";
		YesNo s1q2Answer = YesNo.NotApplicable;
		String s1q2Prompt = "s1q2prompt";
		Question s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, specVersion, s1q2Answer, s1q2Prompt);
		String s1q2SpecRef = "s1q2SpecRef";
		s1q2.setSpecReference(s1q2SpecRef);
		section1Questions.add(s1q2);
		section1.setQuestions(section1Questions);
		sections.add(section1);
		Section section2 = new Section();
		String section2Name = "section2Name";
		section2.setName(section2Name);
		String section2Title = "section2Title";
		section2.setTitle(section2Title);
		List<Question> section2Questions = new ArrayList<Question>();
		String s2q1Question="s2q1question";
		String s2q1Number = "2.b";
		int s2q1MinCorrect = 4;
		Question s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, specVersion, s2q1MinCorrect);
		String s2q1SpecRef = "s2q1SpecRef";
		s2q1.setSpecReference(s2q1SpecRef);
		section2Questions.add(s2q1);
		String s2q2Question="s2q2question";
		String s2q2Number = "2.b.ii";
		YesNo s2q2Answer = YesNo.No;
		String s2q2Prompt = "s2q2prompt";
		String s2q2validate = "dd";
		Question s2q2 = new YesNoQuestionWithEvidence(s2q2Question, 
				section2Name, s2q2Number, specVersion, s2q2Answer, s2q2Prompt, Pattern.compile(s2q2validate));
		String s2q2SpecRef = "s2q2SpecRef";
		s2q2.setSpecReference(s2q2SpecRef);
		s2q2.addSubQuestionOf(s2q1Number);
		section2Questions.add(s2q2);
		String s2q3Question="s2q3question";
		String s2q3Number = "1.b.iii";
		YesNo s2q3Answer = YesNo.NotAnswered;
		Question s2q3 = new YesNoQuestion(s2q3Question, 
				section2Name, s2q3Number, specVersion, s2q3Answer);
		String s2q3SpecRef = "s2q3SpecRef";
		s2q3.setSpecReference(s2q3SpecRef);
		s2q3.addSubQuestionOf(s2q1Number);
		section2Questions.add(s2q3);
		section2.setQuestions(section2Questions);
		sections.add(section2);
		survey.setSections(sections);
		SurveyDbDao dao = new SurveyDbDao(con);
		dao.addSurvey(survey);
		
		// Add questions
		
		String s2q4Question="s2q4question";
		String s2q4Number = "1.b.ix";
		YesNo s2q4Answer = YesNo.No;
		Question s2q4 = new YesNoQuestion(s2q4Question, 
				section2Name, s2q4Number, specVersion, s2q4Answer);
		String s2q4SpecRef = "s2q4SpecRef";
		s2q4.setSpecReference(s2q4SpecRef);
		s2q4.addSubQuestionOf(s2q1Number);
		
		String s2q5Question="s2q5question";
		String s2q5Number = "1.c";
		YesNo s2q5Answer = YesNo.Yes;
		Question s2q5 = new YesNoQuestion(s2q5Question, 
				section2Name, s2q5Number, specVersion, s2q5Answer);
		String s2q5SpecRef = "s2q5SpecRef";
		s2q5.setSpecReference(s2q5SpecRef);

		List<Question> added = new ArrayList<Question>();
		added.add(s2q4);
		added.add(s2q5);
		dao.addQuestions(added);
		
		Survey result = dao.getSurvey();
		assertEquals(specVersion, result.getSpecVersion());
		for (Section rs:result.getSections()) {
			if (rs.getName().equals(section1Name)) {
				assertEquals(section1Title, rs.getTitle());
				for (Question rq:rs.getQuestions()) {
					if (rq.getNumber().equals(s1q1Number)) {
						YesNoQuestion yn = (YesNoQuestion)rq;
						assertEquals(s1q1Question, yn.getQuestion());
						assertEquals(s1q1Answer, yn.getCorrectAnswer());
						assertEquals(section1Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(s1q1SpecRef, yn.getSpecReference());
						assertTrue(yn.getSubQuestionNumber() == null);
					} else if (rq.getNumber().equals(s1q2Number)) {
						YesNoNotApplicableQuestion yn = (YesNoNotApplicableQuestion)rq;
						assertEquals(s1q2Question, yn.getQuestion());
						assertEquals(s1q2Answer, yn.getCorrectAnswer());
						assertEquals(section1Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(s1q2SpecRef, yn.getSpecReference());
						assertTrue(yn.getSubQuestionNumber() == null);
					} else {
						fail("no matching question numbers");
					}
				}
			} else if (rs.getName().equals(section2Name)) {
				assertEquals(section2Title, rs.getTitle());
				for (Question rq:rs.getQuestions()) {
					if (rq.getNumber().equals(s2q1Number)) {
						SubQuestion yn = (SubQuestion)rq;
						assertEquals(s2q1Question, yn.getQuestion());
						assertEquals(s2q1MinCorrect, yn.getMinNumberValidatedAnswers());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(s2q1SpecRef, yn.getSpecReference());
						assertTrue(yn.getSubQuestionNumber() == null);
					} else if (rq.getNumber().equals(s2q2Number)) {
						YesNoQuestionWithEvidence yn = (YesNoQuestionWithEvidence)rq;
						assertEquals(s2q2Question, yn.getQuestion());
						assertEquals(s2q2Answer, yn.getCorrectAnswer());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(s2q2SpecRef, yn.getSpecReference());
						assertEquals(s2q2Prompt, yn.getEvidencePrompt());
						assertEquals(s2q2validate, yn.getEvidenceValidation().toString());
						assertEquals(s2q1Number, yn.getSubQuestionNumber());
					} else if (rq.getNumber().equals(s2q3Number)) {
						YesNoQuestion yn = (YesNoQuestion)rq;
						assertEquals(s2q3Question, yn.getQuestion());
						assertEquals(s2q3Answer, yn.getCorrectAnswer());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(s2q3SpecRef, yn.getSpecReference());
						assertEquals(s2q1Number, yn.getSubQuestionNumber());
					} else if (rq.getNumber().equals(s2q4Number)) {
						YesNoQuestion yn = (YesNoQuestion)rq;
						assertEquals(s2q4Question, yn.getQuestion());
						assertEquals(s2q4Answer, yn.getCorrectAnswer());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(s2q4SpecRef, yn.getSpecReference());
						assertEquals(s2q1Number, yn.getSubQuestionNumber());
					} else if (rq.getNumber().equals(s2q5Number)) {
						YesNoQuestion yn = (YesNoQuestion)rq;
						assertEquals(s2q5Question, yn.getQuestion());
						assertEquals(s2q5Answer, yn.getCorrectAnswer());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(s2q5SpecRef, yn.getSpecReference());
						assertTrue(yn.getSubQuestionNumber() == null);
					} else {
						fail("no matching question numbers");
					}
				}
			} else {
				fail("No matching section names");
			}
		}
	}

	@Test
	public void testSubNumberErrorHandling() throws QuestionException, SQLException, SurveyResponseException {
		String specVersion = "test-spec-version";
		Survey survey = new Survey(specVersion);
		List<Section> sections = new ArrayList<Section>();
		Section section1 = new Section();
		String section1Name = "section1Name";
		section1.setName(section1Name);
		String section1Title = "section1Title";
		section1.setTitle(section1Title);
		List<Question> section1Questions = new ArrayList<Question>();
		String s1q1Question="s1q1question";
		String s1q1Number = "1.a";
		YesNo s1q1Answer = YesNo.Yes;
		Question s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, specVersion, s1q1Answer);
		String s1q1SpecRef = "s1q1SpecRef";
		s1q1.setSpecReference(s1q1SpecRef);
		section1Questions.add(s1q1);
		String s1q2Question="s1q2question";
		String s1q2Number = "1.b";
		YesNo s1q2Answer = YesNo.NotApplicable;
		String s1q2Prompt = "s1q2prompt";
		Question s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, specVersion, s1q2Answer, s1q2Prompt);
		String s1q2SpecRef = "s1q2SpecRef";
		s1q2.setSpecReference(s1q2SpecRef);
		section1Questions.add(s1q2);
		section1.setQuestions(section1Questions);
		sections.add(section1);
		Section section2 = new Section();
		String section2Name = "section2Name";
		section2.setName(section2Name);
		String section2Title = "section2Title";
		section2.setTitle(section2Title);
		List<Question> section2Questions = new ArrayList<Question>();
		String s2q1Question="s2q1question";
		String s2q1Number = "2.b";
		int s2q1MinCorrect = 4;
		Question s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, specVersion, s2q1MinCorrect);
		String s2q1SpecRef = "s2q1SpecRef";
		s2q1.setSpecReference(s2q1SpecRef);
		section2Questions.add(s2q1);
		String s2q2Question="s2q2question";
		String s2q2Number = "2.b.ii";
		YesNo s2q2Answer = YesNo.No;
		String s2q2Prompt = "s2q2prompt";
		String s2q2validate = "dd";
		Question s2q2 = new YesNoQuestionWithEvidence(s2q2Question, 
				section2Name, s2q2Number, specVersion, s2q2Answer, s2q2Prompt, Pattern.compile(s2q2validate));
		String s2q2SpecRef = "s2q2SpecRef";
		s2q2.setSpecReference(s2q2SpecRef);
		s2q2.addSubQuestionOf("3.a");	// this should cause an exception
		section2Questions.add(s2q2);
		String s2q3Question="s2q3question";
		String s2q3Number = "1.b.iii";
		YesNo s2q3Answer = YesNo.NotAnswered;
		Question s2q3 = new YesNoQuestion(s2q3Question, 
				section2Name, s2q3Number, specVersion, s2q3Answer);
		String s2q3SpecRef = "s2q3SpecRef";
		s2q3.setSpecReference(s2q3SpecRef);
		s2q3.addSubQuestionOf(s2q1Number);
		section2Questions.add(s2q3);
		section2.setQuestions(section2Questions);
		sections.add(section2);
		survey.setSections(sections);
		SurveyDbDao dao = new SurveyDbDao(con);
		try {
			dao.addSurvey(survey);
			fail("This should have failed");
		} catch (QuestionException ex) {
			// expected
			assertTrue(ex.getMessage().contains("umber"));
		}
	}
}
