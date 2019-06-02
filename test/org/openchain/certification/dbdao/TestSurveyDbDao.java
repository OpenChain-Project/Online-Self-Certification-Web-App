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
import org.openchain.certification.model.User;
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
		String language = "abc";
		Survey survey = new Survey(specVersion, language);
		List<Section> sections = new ArrayList<Section>();
		Section section1 = new Section(language);
		String section1Name = "section1Name";
		section1.setName(section1Name);
		String section1Title = "section1Title";
		section1.setTitle(section1Title);
		List<Question> section1Questions = new ArrayList<Question>();
		String s1q1Question="s1q1question";
		String s1q1Number = "1.a";
		YesNo s1q1Answer = YesNo.Yes;
		String[] s1q1SpecRef = new String[] {"s1q1SpecRef"};
		Question s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, specVersion, s1q1SpecRef, language, s1q1Answer);

		section1Questions.add(s1q1);
		String s1q2Question="s1q2question";
		String s1q2Number = "1.b";
		YesNo s1q2Answer = YesNo.NotApplicable;
		String s1q2Prompt = "s1q2prompt";
		String[] s1q2SpecRef = new String[] {"s1q2SpecRef"};
		Question s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, specVersion, s1q2SpecRef, language, s1q2Answer, s1q2Prompt);
		section1Questions.add(s1q2);
		section1.setQuestions(section1Questions);
		sections.add(section1);
		Section section2 = new Section(language);
		String section2Name = "section2Name";
		section2.setName(section2Name);
		String section2Title = "section2Title";
		section2.setTitle(section2Title);
		List<Question> section2Questions = new ArrayList<Question>();
		String s2q1Question="s2q1question";
		String s2q1Number = "2.b";
		int s2q1MinCorrect = 4;
		String[] s2q1SpecRef = new String[] {"s2q1SpecRef"};
		Question s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, specVersion, s2q1SpecRef, language, s2q1MinCorrect);
		section2Questions.add(s2q1);
		String s2q2Question="s2q2question";
		String s2q2Number = "2.b.ii";
		YesNo s2q2Answer = YesNo.No;
		String s2q2Prompt = "s2q2prompt";
		String s2q2validate = "dd";
		String[] s2q2SpecRef = new String[] {"s2q2SpecRef"};
		Question s2q2 = new YesNoQuestionWithEvidence(s2q2Question, 
				section2Name, s2q2Number, specVersion, s2q2SpecRef, language, s2q2Answer, s2q2Prompt, Pattern.compile(s2q2validate));
		s2q2.setSubQuestionOfNumber(s2q1Number);
		section2Questions.add(s2q2);
		String s2q3Question="s2q3question";
		String s2q3Number = "1.b.iii";
		YesNo s2q3Answer = YesNo.NotAnswered;
		String[] s2q3SpecRef = new String[] {"s2q3SpecRef"};
		Question s2q3 = new YesNoQuestion(s2q3Question, 
				section2Name, s2q3Number, specVersion, s2q3SpecRef, language, s2q3Answer);
		s2q3.setSubQuestionOfNumber(s2q1Number);
		section2Questions.add(s2q3);
		section2.setQuestions(section2Questions);
		sections.add(section2);
		survey.setSections(sections);
		SurveyDbDao dao = new SurveyDbDao(con);
		dao.addSurvey(survey);
		Survey result = dao.getSurvey(specVersion, language);
		assertEquals(specVersion, result.getSpecVersion());
		for (Section rs:result.getSections()) {
			if (rs.getName().equals(section1Name)) {
				assertEquals(section1Title, rs.getTitle());
				assertEquals(language, rs.getLanguage());
				for (Question rq:rs.getQuestions()) {
					if (rq.getNumber().equals(s1q1Number)) {
						YesNoQuestion yn = (YesNoQuestion)rq;
						assertEquals(s1q1Question, yn.getQuestion());
						assertEquals(s1q1Answer, yn.getCorrectAnswer());
						assertEquals(section1Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(s1q1SpecRef), Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertTrue(yn.getSubQuestionOfNumber() == null);
						assertEquals(language, yn.getLanguage());
					} else if (rq.getNumber().equals(s1q2Number)) {
						YesNoNotApplicableQuestion yn = (YesNoNotApplicableQuestion)rq;
						assertEquals(s1q2Question, yn.getQuestion());
						assertEquals(s1q2Answer, yn.getCorrectAnswer());
						assertEquals(section1Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(s1q2SpecRef), Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertTrue(yn.getSubQuestionOfNumber() == null);
						assertEquals(language, yn.getLanguage());
					} else {
						fail("no matching question numbers");
					}
				}
			} else if (rs.getName().equals(section2Name)) {
				assertEquals(section2Title, rs.getTitle());
				assertEquals(language, rs.getLanguage());
				for (Question rq:rs.getQuestions()) {
					assertEquals(language, rq.getLanguage());
					if (rq.getNumber().equals(s2q1Number)) {
						SubQuestion yn = (SubQuestion)rq;
						assertEquals(s2q1Question, yn.getQuestion());
						assertEquals(s2q1MinCorrect, yn.getMinNumberValidatedAnswers());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(s2q1SpecRef), Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertTrue(yn.getSubQuestionOfNumber() == null);
					} else if (rq.getNumber().equals(s2q2Number)) {
						YesNoQuestionWithEvidence yn = (YesNoQuestionWithEvidence)rq;
						assertEquals(s2q2Question, yn.getQuestion());
						assertEquals(s2q2Answer, yn.getCorrectAnswer());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(s2q2SpecRef), Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertEquals(s2q2Prompt, yn.getEvidencePrompt());
						assertEquals(s2q2validate, yn.getEvidenceValidation().toString());
						assertEquals(s2q1Number, yn.getSubQuestionOfNumber());
					} else if (rq.getNumber().equals(s2q3Number)) {
						YesNoQuestion yn = (YesNoQuestion)rq;
						assertEquals(s2q3Question, yn.getQuestion());
						assertEquals(s2q3Answer, yn.getCorrectAnswer());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(s2q3SpecRef), Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertEquals(s2q1Number, yn.getSubQuestionOfNumber());
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
		String language = "fra";
		Survey survey = new Survey(specVersion, language);
		List<Section> sections = new ArrayList<Section>();
		Section section1 = new Section(language);
		String section1Name = "section1Name";
		section1.setName(section1Name);
		String section1Title = "section1Title";
		section1.setTitle(section1Title);
		List<Question> section1Questions = new ArrayList<Question>();
		String s1q1Question="s1q1question";
		String s1q1Number = "1.a";
		YesNo s1q1Answer = YesNo.Yes;
		String[]  s1q1SpecRef = new String[] {"s1q1SpecRef"};
		Question s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, specVersion, s1q1SpecRef, language, s1q1Answer);
		section1Questions.add(s1q1);
		String s1q2Question="s1q2question";
		String s1q2Number = "1.b";
		YesNo s1q2Answer = YesNo.NotApplicable;
		String s1q2Prompt = "s1q2prompt";
		String[] s1q2SpecRef = new String[] {"s1q2SpecRef"};
		Question s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, specVersion, s1q2SpecRef, language, s1q2Answer, s1q2Prompt);
		section1Questions.add(s1q2);
		section1.setQuestions(section1Questions);
		sections.add(section1);
		Section section2 = new Section(language);
		String section2Name = "section2Name";
		section2.setName(section2Name);
		String section2Title = "section2Title";
		section2.setTitle(section2Title);
		List<Question> section2Questions = new ArrayList<Question>();
		String s2q1Question="s2q1question";
		String s2q1Number = "2.b";
		int s2q1MinCorrect = 4;
		String[] s2q1SpecRef = new String[] {"s2q1SpecRef"};
		Question s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, specVersion, s2q1SpecRef, language, s2q1MinCorrect);
		section2Questions.add(s2q1);
		String s2q2Question="s2q2question";
		String s2q2Number = "2.b.ii";
		YesNo s2q2Answer = YesNo.No;
		String s2q2Prompt = "s2q2prompt";
		String s2q2validate = "dd";
		String[] s2q2SpecRef = new String[] {"s2q2SpecRef"};
		Question s2q2 = new YesNoQuestionWithEvidence(s2q2Question, 
				section2Name, s2q2Number, specVersion, s2q2SpecRef, language, 
				s2q2Answer, s2q2Prompt, Pattern.compile(s2q2validate));
		s2q2.setSubQuestionOfNumber(s2q1Number);
		section2Questions.add(s2q2);
		String s2q3Question="s2q3question";
		String s2q3Number = "1.b.iii";
		YesNo s2q3Answer = YesNo.NotAnswered;
		String[] s2q3SpecRef = new String[] {"s2q3SpecRef"};
		Question s2q3 = new YesNoQuestion(s2q3Question, 
				section2Name, s2q3Number, specVersion, s2q3SpecRef, language, s2q3Answer);
		s2q3.setSubQuestionOfNumber(s2q1Number);
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
		String[] us1q2specRef = new String[] {"UpdatedRef"};
		Question Updateds1q2 = new YesNoQuestionWithEvidence(us1q2Question, 
				section1Name, s1q2Number, specVersion, us1q2specRef, language, 
				updates1q2Answer, us1q2Prompt, Pattern.compile(us1q2valid));
		
		String us2q3Question = "Updated " +s2q3Question;	
		String[] us2q3SpecRef = new String[] {"us2q3SpecRef"};
		Question updateds2q3 = new YesNoQuestion(us2q3Question, 
				section2Name, s2q3Number, specVersion, us2q3SpecRef, language, s2q3Answer);
		updateds2q3.setSubQuestionOfNumber(s2q1Number);
		List<Question> questionsToUpdate = new ArrayList<Question>();
		questionsToUpdate.add(Updateds1q2);
		questionsToUpdate.add(updateds2q3);
		dao.updateQuestions(questionsToUpdate);
		Survey result = dao.getSurvey(specVersion, language);
		assertEquals(specVersion, result.getSpecVersion());
		for (Section rs:result.getSections()) {
			assertEquals(language, rs.getLanguage());
			if (rs.getName().equals(section1Name)) {
				assertEquals(section1Title, rs.getTitle());
				for (Question rq:rs.getQuestions()) {
					assertEquals(language, rq.getLanguage());
					if (rq.getNumber().equals(s1q1Number)) {
						YesNoQuestion yn = (YesNoQuestion)rq;
						assertEquals(s1q1Question, yn.getQuestion());
						assertEquals(s1q1Answer, yn.getCorrectAnswer());
						assertEquals(section1Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(s1q1SpecRef), 
								Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertTrue(yn.getSubQuestionOfNumber() == null);
					} else if (rq.getNumber().equals(s1q2Number)) {
						YesNoQuestionWithEvidence yn = (YesNoQuestionWithEvidence)rq;
						assertEquals(us1q2Question, yn.getQuestion());
						assertEquals(updates1q2Answer, yn.getCorrectAnswer());
						assertEquals(section1Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(us1q2specRef), 
								Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertEquals(us1q2Prompt, yn.getEvidencePrompt());
						assertEquals(us1q2valid, yn.getEvidenceValidation().toString());
						assertTrue(yn.getSubQuestionOfNumber() == null);
					} else {
						fail("no matching question numbers");
					}
				}
			} else if (rs.getName().equals(section2Name)) {
				assertEquals(section2Title, rs.getTitle());
				for (Question rq:rs.getQuestions()) {
					assertEquals(language, rq.getLanguage());
					if (rq.getNumber().equals(s2q1Number)) {
						SubQuestion yn = (SubQuestion)rq;
						assertEquals(s2q1Question, yn.getQuestion());
						assertEquals(s2q1MinCorrect, yn.getMinNumberValidatedAnswers());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(s2q1SpecRef), 
								Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertTrue(yn.getSubQuestionOfNumber() == null);
					} else if (rq.getNumber().equals(s2q2Number)) {
						YesNoQuestionWithEvidence yn = (YesNoQuestionWithEvidence)rq;
						assertEquals(s2q2Question, yn.getQuestion());
						assertEquals(s2q2Answer, yn.getCorrectAnswer());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(s2q2SpecRef),
								Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertEquals(s2q2Prompt, yn.getEvidencePrompt());
						assertEquals(s2q2validate, yn.getEvidenceValidation().toString());
						assertEquals(s2q1Number, yn.getSubQuestionOfNumber());
					} else if (rq.getNumber().equals(s2q3Number)) {
						YesNoQuestion yn = (YesNoQuestion)rq;
						assertEquals(us2q3Question, yn.getQuestion());
						assertEquals(s2q3Answer, yn.getCorrectAnswer());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(us2q3SpecRef),
								Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertEquals(s2q1Number, yn.getSubQuestionOfNumber());
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
		String language = "ang";
		Survey survey = new Survey(specVersion, language);
		List<Section> sections = new ArrayList<Section>();
		Section section1 = new Section(language);
		String section1Name = "section1Name";
		section1.setName(section1Name);
		String section1Title = "section1Title";
		section1.setTitle(section1Title);
		List<Question> section1Questions = new ArrayList<Question>();
		String s1q1Question="s1q1question";
		String s1q1Number = "1.a";
		YesNo s1q1Answer = YesNo.Yes;
		String[] s1q1SpecRef = new String[] {"s1q1SpecRef"};
		Question s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, specVersion, s1q1SpecRef, language, s1q1Answer);
		section1Questions.add(s1q1);
		String s1q2Question="s1q2question";
		String s1q2Number = "1.b";
		YesNo s1q2Answer = YesNo.NotApplicable;
		String s1q2Prompt = "s1q2prompt";
		String[] s1q2SpecRef = new String[] {"s1q2SpecRef"};
		Question s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, specVersion, s1q2SpecRef, language, s1q2Answer, s1q2Prompt);
		section1Questions.add(s1q2);
		section1.setQuestions(section1Questions);
		sections.add(section1);
		Section section2 = new Section(language);
		String section2Name = "section2Name";
		section2.setName(section2Name);
		String section2Title = "section2Title";
		section2.setTitle(section2Title);
		List<Question> section2Questions = new ArrayList<Question>();
		String s2q1Question="s2q1question";
		String s2q1Number = "2.b";
		int s2q1MinCorrect = 4;
		String[] s2q1SpecRef = new String[] {"s2q1SpecRef"};
		Question s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, specVersion, s2q1SpecRef, language, s2q1MinCorrect);
		section2Questions.add(s2q1);
		String s2q2Question="s2q2question";
		String s2q2Number = "2.b.ii";
		YesNo s2q2Answer = YesNo.No;
		String s2q2Prompt = "s2q2prompt";
		String s2q2validate = "dd";
		String[] s2q2SpecRef = new String[] {"s2q2SpecRef"};
		Question s2q2 = new YesNoQuestionWithEvidence(s2q2Question, 
				section2Name, s2q2Number, specVersion, s2q2SpecRef, language,
				s2q2Answer, s2q2Prompt, Pattern.compile(s2q2validate));
		s2q2.setSubQuestionOfNumber(s2q1Number);
		section2Questions.add(s2q2);
		String s2q3Question="s2q3question";
		String s2q3Number = "1.b.iii";
		YesNo s2q3Answer = YesNo.NotAnswered;
		String[] s2q3SpecRef = new String[] {"s2q3SpecRef"};
		Question s2q3 = new YesNoQuestion(s2q3Question, 
				section2Name, s2q3Number, specVersion, s2q3SpecRef, language, s2q3Answer);
		s2q3.setSubQuestionOfNumber(s2q1Number);
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
		String[] s2q4SpecRef = new String[] {"s2q4SpecRef"};
		Question s2q4 = new YesNoQuestion(s2q4Question, 
				section2Name, s2q4Number, specVersion, s2q4SpecRef, language, s2q4Answer);
		s2q4.setSubQuestionOfNumber(s2q1Number);
		
		String s2q5Question="s2q5question";
		String s2q5Number = "1.c";
		YesNo s2q5Answer = YesNo.Yes;
		String[] s2q5SpecRef = new String[] {"s2q5SpecRef"};
		Question s2q5 = new YesNoQuestion(s2q5Question, 
				section2Name, s2q5Number, specVersion, s2q5SpecRef, language, s2q5Answer);

		List<Question> added = new ArrayList<Question>();
		added.add(s2q4);
		added.add(s2q5);
		dao.addQuestions(added);
		
		Survey result = dao.getSurvey(specVersion, language);
		assertEquals(specVersion, result.getSpecVersion());
		for (Section rs:result.getSections()) {
			assertEquals(language, rs.getLanguage());
			if (rs.getName().equals(section1Name)) {
				assertEquals(section1Title, rs.getTitle());
				for (Question rq:rs.getQuestions()) {
					assertEquals(language, rq.getLanguage());
					if (rq.getNumber().equals(s1q1Number)) {
						YesNoQuestion yn = (YesNoQuestion)rq;
						assertEquals(s1q1Question, yn.getQuestion());
						assertEquals(s1q1Answer, yn.getCorrectAnswer());
						assertEquals(section1Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(s1q1SpecRef),
								Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertTrue(yn.getSubQuestionOfNumber() == null);
					} else if (rq.getNumber().equals(s1q2Number)) {
						YesNoNotApplicableQuestion yn = (YesNoNotApplicableQuestion)rq;
						assertEquals(s1q2Question, yn.getQuestion());
						assertEquals(s1q2Answer, yn.getCorrectAnswer());
						assertEquals(section1Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(s1q2SpecRef),
								Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertTrue(yn.getSubQuestionOfNumber() == null);
					} else {
						fail("no matching question numbers");
					}
				}
			} else if (rs.getName().equals(section2Name)) {
				assertEquals(section2Title, rs.getTitle());
				for (Question rq:rs.getQuestions()) {
					assertEquals(language, rq.getLanguage());
					if (rq.getNumber().equals(s2q1Number)) {
						SubQuestion yn = (SubQuestion)rq;
						assertEquals(s2q1Question, yn.getQuestion());
						assertEquals(s2q1MinCorrect, yn.getMinNumberValidatedAnswers());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(s2q1SpecRef),
								Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertTrue(yn.getSubQuestionOfNumber() == null);
					} else if (rq.getNumber().equals(s2q2Number)) {
						YesNoQuestionWithEvidence yn = (YesNoQuestionWithEvidence)rq;
						assertEquals(s2q2Question, yn.getQuestion());
						assertEquals(s2q2Answer, yn.getCorrectAnswer());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(s2q2SpecRef),
								Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertEquals(s2q2Prompt, yn.getEvidencePrompt());
						assertEquals(s2q2validate, yn.getEvidenceValidation().toString());
						assertEquals(s2q1Number, yn.getSubQuestionOfNumber());
					} else if (rq.getNumber().equals(s2q3Number)) {
						YesNoQuestion yn = (YesNoQuestion)rq;
						assertEquals(s2q3Question, yn.getQuestion());
						assertEquals(s2q3Answer, yn.getCorrectAnswer());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(s2q3SpecRef),
								Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertEquals(s2q1Number, yn.getSubQuestionOfNumber());
					} else if (rq.getNumber().equals(s2q4Number)) {
						YesNoQuestion yn = (YesNoQuestion)rq;
						assertEquals(s2q4Question, yn.getQuestion());
						assertEquals(s2q4Answer, yn.getCorrectAnswer());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(s2q4SpecRef),
								Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertEquals(s2q1Number, yn.getSubQuestionOfNumber());
					} else if (rq.getNumber().equals(s2q5Number)) {
						YesNoQuestion yn = (YesNoQuestion)rq;
						assertEquals(s2q5Question, yn.getQuestion());
						assertEquals(s2q5Answer, yn.getCorrectAnswer());
						assertEquals(section2Name, yn.getSectionName());
						assertEquals(specVersion, yn.getSpecVersion());
						assertEquals(Question.specReferenceArrayToStr(s2q5SpecRef),
								Question.specReferenceArrayToStr(yn.getSpecReference()));
						assertTrue(yn.getSubQuestionOfNumber() == null);
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
		String language = "alr";
		Survey survey = new Survey(specVersion, language);
		List<Section> sections = new ArrayList<Section>();
		Section section1 = new Section(language);
		String section1Name = "section1Name";
		section1.setName(section1Name);
		String section1Title = "section1Title";
		section1.setTitle(section1Title);
		List<Question> section1Questions = new ArrayList<Question>();
		String s1q1Question="s1q1question";
		String s1q1Number = "1.a";
		YesNo s1q1Answer = YesNo.Yes;
		String[] s1q1SpecRef = new String[] {"s1q1SpecRef"};
		Question s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, specVersion, s1q1SpecRef, language, s1q1Answer);
		section1Questions.add(s1q1);
		String s1q2Question="s1q2question";
		String s1q2Number = "1.b";
		YesNo s1q2Answer = YesNo.NotApplicable;
		String s1q2Prompt = "s1q2prompt";
		String[] s1q2SpecRef = new String[] {"s1q2SpecRef"};
		Question s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, specVersion, s1q2SpecRef, language, s1q2Answer, s1q2Prompt);
		section1Questions.add(s1q2);
		section1.setQuestions(section1Questions);
		sections.add(section1);
		Section section2 = new Section(language);
		String section2Name = "section2Name";
		section2.setName(section2Name);
		String section2Title = "section2Title";
		section2.setTitle(section2Title);
		List<Question> section2Questions = new ArrayList<Question>();
		String s2q1Question="s2q1question";
		String s2q1Number = "2.b";
		int s2q1MinCorrect = 4;
		String[] s2q1SpecRef = new String[] {"s2q1SpecRef"};
		Question s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, specVersion, s2q1SpecRef, language, s2q1MinCorrect);
		section2Questions.add(s2q1);
		String s2q2Question="s2q2question";
		String s2q2Number = "2.b.ii";
		YesNo s2q2Answer = YesNo.No;
		String s2q2Prompt = "s2q2prompt";
		String s2q2validate = "dd";
		String[] s2q2SpecRef = new String[] {"s2q2SpecRef"};
		Question s2q2 = new YesNoQuestionWithEvidence(s2q2Question, 
				section2Name, s2q2Number, specVersion, s2q2SpecRef, language,
				s2q2Answer, s2q2Prompt, Pattern.compile(s2q2validate));
		s2q2.setSubQuestionOfNumber("3.a");	// this should cause an exception
		section2Questions.add(s2q2);
		String s2q3Question="s2q3question";
		String s2q3Number = "1.b.iii";
		YesNo s2q3Answer = YesNo.NotAnswered;
		String[] s2q3SpecRef = new String[] {"s2q3SpecRef"};
		Question s2q3 = new YesNoQuestion(s2q3Question, 
				section2Name, s2q3Number, specVersion, s2q3SpecRef, language, s2q3Answer);
		s2q3.setSubQuestionOfNumber(s2q1Number);
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
	
	@Test
	public void testGetSurveyVersions() throws SQLException, SurveyResponseException, QuestionException {
		SurveyDbDao dao = new SurveyDbDao(con);
		String language = "asd";
		
		List<Section> sections = new ArrayList<Section>();
		Section section1 = new Section(language);
		String section1Name = "section1Name";
		section1.setName(section1Name);
		String section1Title = "section1Title";
		section1.setTitle(section1Title);
		List<Question> section1Questions = new ArrayList<Question>();
		String s1q1Question="s1q1question";
		String s1q1Number = "1.a";
		YesNo s1q1Answer = YesNo.Yes;
		String[] s1q1SpecRef = new String[] {"s1q1SpecRef"};
		Question s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, "v", s1q1SpecRef, language, s1q1Answer);
		section1Questions.add(s1q1);
		String s1q2Question="s1q2question";
		String s1q2Number = "1.b";
		YesNo s1q2Answer = YesNo.NotApplicable;
		String s1q2Prompt = "s1q2prompt";
		String[] s1q2SpecRef = new String[] {"s1q2SpecRef"};
		Question s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, "v", s1q2SpecRef, language, s1q2Answer, s1q2Prompt);
		section1Questions.add(s1q2);
		section1.setQuestions(section1Questions);
		sections.add(section1);
		String v1 = "1.0.0";
		String v2 = "1.0.1";
		String v3 = "2.1.1";
		String v4 = "2.2.2";
		Survey surveyV1 = new Survey(v1, language);
		surveyV1.setSections(sections);
		Survey surveyV2 = new Survey(v2, language);
		surveyV2.setSections(sections);
		Survey surveyV3 = new Survey(v3, language);
		surveyV3.setSections(sections);
		Survey surveyV4 = new Survey(v4, language);
		surveyV4.setSections(sections);
		dao.addSurvey(surveyV1);
		dao.addSurvey(surveyV3);
		dao.addSurvey(surveyV2);
		dao.addSurvey(surveyV4);
		List<String> result = dao.getSurveyVesions();
		assertEquals(4, result.size());
		assertEquals(v1, result.get(0));
		assertEquals(v2, result.get(1));
		assertEquals(v3, result.get(2));
		assertEquals(v4, result.get(3));
	}
	
	@Test
	public void testLanguage()  throws SQLException, SurveyResponseException, QuestionException {
		String specVersion = "test-spec-version";
		String language1 = "abc";
		Survey survey1 = new Survey(specVersion, language1);
		List<Section> sections1 = new ArrayList<Section>();
		Section section1_1 = new Section(language1);
		String section1Name = "section1Name";
		section1_1.setName(section1Name);
		String section1Title = "section1Title";
		section1_1.setTitle(section1Title);
		List<Question> section1_1Questions = new ArrayList<Question>();
		String s1q1_1Question="s1q1question";
		String s1q1Number = "1.a";
		YesNo s1q1Answer = YesNo.Yes;
		Question s1q1_1 = new YesNoQuestion(s1q1_1Question, 
				section1Name, s1q1Number, specVersion, new String[0], language1, s1q1Answer);
		section1_1Questions.add(s1q1_1);
		section1_1.setQuestions(section1_1Questions);
		sections1.add(section1_1);
		survey1.setSections(sections1);
		SurveyDbDao dao = new SurveyDbDao(con);
		dao.addSurvey(survey1);
		
		String language2 = "def";
		Survey survey2 = new Survey(specVersion, language2);
		List<Section> sections2 = new ArrayList<Section>();
		Section section1_2 = new Section(language2);
		String section2Name = "section2Name";
		section1_2.setName(section2Name);
		section1_2.setTitle(section1Title);
		List<Question> section1_2Questions = new ArrayList<Question>();
		String s1q1_2Question="s1q1-2question";
		Question s1q1_2 = new YesNoQuestion(s1q1_2Question, 
				section2Name, s1q1Number, specVersion, new String[0], language2, s1q1Answer);
		section1_2Questions.add(s1q1_2);
		section1_2.setQuestions(section1_2Questions);
		sections2.add(section1_2);
		survey2.setSections(sections2);
		dao.addSurvey(survey2);
		
		Survey result = dao.getSurvey(specVersion, language1);
		assertEquals(language1, result.getLanguage());
		assertEquals(language1, result.getSections().get(0).getLanguage());
		assertEquals(section1Name, result.getSections().get(0).getName());
		assertEquals(language1, result.getSections().get(0).getQuestions().get(0).getLanguage());
		assertEquals(s1q1_1Question, result.getSections().get(0).getQuestions().get(0).getQuestion());
		
		result = dao.getSurvey(specVersion, language2);
		assertEquals(language2, result.getLanguage());
		assertEquals(language2, result.getSections().get(0).getLanguage());
		assertEquals(section2Name, result.getSections().get(0).getName());
		assertEquals(language2, result.getSections().get(0).getQuestions().get(0).getLanguage());
		assertEquals(s1q1_2Question, result.getSections().get(0).getQuestions().get(0).getQuestion());
	}
	
	@Test
	public void testGetSpecVersion() throws SQLException, SurveyResponseException, QuestionException {
		List<Section> sections = new ArrayList<Section>();
		Section section1 = new Section("abc");
		String section1Name = "section1Name";
		section1.setName(section1Name);
		String section1Title = "section1Title";
		section1.setTitle(section1Title);
		List<Question> section1Questions = new ArrayList<Question>();
		String s1q1Question="s1q1question";
		String s1q1Number = "1.a";
		YesNo s1q1Answer = YesNo.Yes;
		Question s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, "X", new String[0], "abc", s1q1Answer);
		String[] s1q1SpecRef = new String[] {"s1q1SpecRef"};
		s1q1.setSpecReference(s1q1SpecRef);
		section1Questions.add(s1q1);
	
		String version1 = "0.1.2";
		String lang1 = "fra";
		String version2 = "1.1.1";
		String lang2 = "abc";
		String lang3 = "ejk";
		Survey survey1 = new Survey(version1, lang1);
		survey1.setSections(sections);
		Survey survey2 = new Survey(version1, User.DEFAULT_LANGUAGE);
		survey2.setSections(sections);
		Survey survey3 = new Survey(version1, lang2);
		survey3.setSections(sections);
		Survey survey4 = new Survey(version2, null);
		survey4.setSections(sections);
		Survey survey5 = new Survey(version2, lang1);
		survey5.setSections(sections);
		SurveyDbDao dao = new SurveyDbDao(con);
		dao.addSurvey(survey1);
		dao.addSurvey(survey2);
		dao.addSurvey(survey3);		
		dao.addSurvey(survey4);
		dao.addSurvey(survey5);
		// Test exact version language
		long lang1v1result = SurveyDbDao.getSpecId(con, version1, lang1, true);
		assertTrue(lang1v1result > 0);
		long defaultLangv1result = SurveyDbDao.getSpecId(con, version1, User.DEFAULT_LANGUAGE, true);
		assertTrue(defaultLangv1result > 0);
		assertTrue(defaultLangv1result != lang1v1result);
		long lang2v1result = SurveyDbDao.getSpecId(con, version1, lang2, true);
		assertTrue(lang2v1result > 0);
		assertTrue(lang2v1result != lang1v1result);
		assertTrue(lang2v1result != defaultLangv1result);
		long lang1v2result = SurveyDbDao.getSpecId(con, version2, lang1, true);
		assertTrue(lang1v2result > 0);
		assertTrue(lang1v2result != lang1v1result);
		assertTrue(lang1v2result != defaultLangv1result);
		// Test default language
		long defV1result = SurveyDbDao.getSpecId(con, version1, lang3, true);
		assertEquals(defaultLangv1result, defV1result);
		// Test no default
		long nullV2result = SurveyDbDao.getSpecId(con, version2, lang3, true);
		assertTrue(nullV2result > 0);
		assertTrue(nullV2result != lang1v1result);
		assertTrue(nullV2result != defaultLangv1result);
	}
}
