package org.openchain.certification.dbdao;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openchain.certification.TestHelper;
import org.openchain.certification.model.Answer;
import org.openchain.certification.model.Question;
import org.openchain.certification.model.QuestionException;
import org.openchain.certification.model.Section;
import org.openchain.certification.model.SubQuestion;
import org.openchain.certification.model.SubQuestionAnswers;
import org.openchain.certification.model.Survey;
import org.openchain.certification.model.SurveyResponse;
import org.openchain.certification.model.SurveyResponseException;
import org.openchain.certification.model.User;
import org.openchain.certification.model.YesNoAnswer;
import org.openchain.certification.model.YesNoAnswerWithEvidence;
import org.openchain.certification.model.YesNoNotApplicableQuestion;
import org.openchain.certification.model.YesNoQuestion;
import org.openchain.certification.model.YesNoQuestionWithEvidence;
import org.openchain.certification.model.YesNoQuestion.YesNo;

public class TestSurveyResponseDao {

	Connection con;
	String specVersion = "test-spec-version";
	String section1Name = "section1Name";
	String section1Title = "section1Title";
	String s1q1Question="s1q1question";
	String s1q1Number = "1.0";
	YesNo s1q1Answer = YesNo.Yes;
	String s1q1SpecRef = "s1q1SpecRef";
	YesNoQuestion s1q1;
	Section section1;
	Survey survey;
	String s1q2Question="s1q2question";
	String s1q2Number = "1.1";
	YesNo s1q2Answer = YesNo.NotApplicable;
	String s1q2Prompt = "s1q2prompt";
	YesNoNotApplicableQuestion s1q2;
	Section section2;
	String section2Name = "section2Name";
	String section2Title = "section2Title";
	String s2q1Question="s2q1question";
	String s2q1Number = "2.1";
	int s2q1MinCorrect = 4;
	SubQuestion s2q1;
	String s2q1SpecRef = "s2q1SpecRef";
	String s2q2Question="s2q2question";
	String s2q2Number = "2.1.1";
	YesNo s2q2Answer = YesNo.No;
	String s2q2Prompt = "s2q2prompt";
	String s2q2validate = "dd";
	YesNoQuestionWithEvidence s2q2;
	String s2q2SpecRef = "s2q2SpecRef";
	String s2q3Question="s2q3question";
	String s2q3Number = "2.1.2";
	YesNo s2q3Answer = YesNo.NotAnswered;
	YesNoQuestion s2q3;
	String s2q3SpecRef = "s2q3SpecRef";
	SurveyDbDao surveyDao;
	User user;
	
	@Before
	public void setUp() throws Exception {
		con = TestHelper.getConnection();
		TestHelper.truncateDatabase(con);
		survey = new Survey(specVersion);
		List<Section> sections = new ArrayList<Section>();
		section1 = new Section();
		section1.setName(section1Name);
		section1.setTitle(section1Title);
		List<Question> section1Questions = new ArrayList<Question>();
		s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, specVersion, s1q1Answer);
		s1q1.setSpecReference(s1q1SpecRef);
		section1Questions.add(s1q1);

		s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, specVersion, s1q2Answer, s1q2Prompt);
		String s1q2SpecRef = "s1q2SpecRef";
		s1q2.setSpecReference(s1q2SpecRef);
		section1Questions.add(s1q2);
		section1.setQuestions(section1Questions);
		sections.add(section1);
		section2 = new Section();
		section2.setName(section2Name);
		section2.setTitle(section2Title);
		List<Question> section2Questions = new ArrayList<Question>();
		s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, specVersion, s2q1MinCorrect);
		s2q1.setSpecReference(s2q1SpecRef);
		section2Questions.add(s2q1);

		s2q2 = new YesNoQuestionWithEvidence(s2q2Question, 
				section2Name, s2q2Number, specVersion, s2q2Answer, s2q2Prompt, Pattern.compile(s2q2validate));
		s2q2.setSpecReference(s2q2SpecRef);
		s2q2.addSubQuestionOf(s2q1Number);
		section2Questions.add(s2q2);

		s2q3 = new YesNoQuestion(s2q3Question, 
				section2Name, s2q3Number, specVersion, s2q3Answer);
		s2q3.setSpecReference(s2q3SpecRef);
		s2q3.addSubQuestionOf(s2q1Number);
		section2Questions.add(s2q3);
		section2.setQuestions(section2Questions);
		sections.add(section2);
		survey.setSections(sections);
		surveyDao = new SurveyDbDao(con);
		surveyDao.addSurvey(survey);
		
		user = new User();
		user.setAddress("Address");
		user.setAdmin(false);
		user.setEmail("test@openchain.com");
		user.setName("Test User");
		user.setOrganization("Test Og.");
		user.setPasswordReset(false);
		user.setPasswordToken("TOKEN");
		user.setUsername("testuser");
		user.setUuid(UUID.randomUUID().toString());
		user.setVerificationExpirationDate(new Date());
		user.setVerified(true);
		UserDb.getUserDb(TestHelper.getTestServletConfig()).addUser(user);	
	}

	@After
	public void tearDown() throws Exception {
		con.close();
	}
	@Test
	public void testGetLatestSpecVersion() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponseDao dao = new SurveyResponseDao(con);
		assertEquals(this.specVersion, dao.getLatestSpecVersion());
		String laterSpecVersion = specVersion + ".1";
		Survey laterSurvey = new Survey(laterSpecVersion);
		laterSurvey.setSections(new ArrayList<Section>());
		surveyDao.addSurvey(laterSurvey);
		assertEquals(laterSpecVersion, dao.getLatestSpecVersion());
	}

	@Test
	public void testAddSurveyResponse() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse();
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(YesNo.NotAnswered);

		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		response.setResponses(responses);
		response.setSpecVersion(specVersion);
		response.setSubmitted(false);
		response.setSurvey(survey);
		response.setApproved(true);
		response.setRejected(false);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response);
		String laterSpecVersion = specVersion + ".1";
		Survey laterSurvey = new Survey(laterSpecVersion);
		List<Section> laterSections = new ArrayList<Section>();
		Section newSection1 = new Section();
		newSection1.setName(section1.getName());
		newSection1.setTitle(section1.getTitle());
		List<Question> newQuestions1 = new ArrayList<Question>();
		s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, laterSpecVersion, s1q1Answer);
		s1q1.setSpecReference(s1q1SpecRef);
		newQuestions1.add(s1q1);

		s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, laterSpecVersion, s1q2Answer, s1q2Prompt);
		String s1q2SpecRef = "s1q2SpecRef";
		s1q2.setSpecReference(s1q2SpecRef);
		newQuestions1.add(s1q2);
		newSection1.setQuestions(newQuestions1);
		laterSections.add(newSection1);
		Section newSection2 = new Section();
		newSection2.setName(section2.getName());
		newSection2.setTitle(section2.getTitle());
		List<Question> newQuestions2 = new ArrayList<Question>();
		s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, laterSpecVersion, s2q1MinCorrect);
		s2q1.setSpecReference(s2q1SpecRef);
		newQuestions2.add(s2q1);

		s2q2 = new YesNoQuestionWithEvidence(s2q2Question, 
				section2Name, s2q2Number, laterSpecVersion, s2q2Answer, s2q2Prompt, Pattern.compile(s2q2validate));
		s2q2.setSpecReference(s2q2SpecRef);
		s2q2.addSubQuestionOf(s2q1Number);
		newQuestions2.add(s2q2);

		s2q3 = new YesNoQuestion(s2q3Question, 
				section2Name, s2q3Number, laterSpecVersion, s2q3Answer);
		s2q3.setSpecReference(s2q3SpecRef);
		s2q3.addSubQuestionOf(s2q1Number);
		newQuestions2.add(s2q3);
		newSection2.setQuestions(newQuestions2);
		laterSections.add(newSection2);
		laterSurvey.setSections(laterSections);
		surveyDao.addSurvey(laterSurvey);
		SurveyResponse response2 = new SurveyResponse();
		response2.setResponder(user);
		Map<String, Answer> responses2 = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer2 = new YesNoAnswer(YesNo.Yes);
		
		responses2.put(s1q1Number, s1q1answer2);

		YesNoAnswerWithEvidence s2q2Answer = new YesNoAnswerWithEvidence(YesNo.No, "s2q2evidence");
		responses2.put(s2q2Number, s2q2Answer);
		YesNoAnswer s2q3answer = new YesNoAnswer(YesNo.Yes);
		responses2.put(s2q3Number, s2q3answer);
		
		response2.setResponses(responses2);
		response2.setSpecVersion(laterSpecVersion);
		response2.setSubmitted(false);
		response2.setSurvey(laterSurvey);
		dao.addSurveyResponse(response2);
		
		// test the first version
		SurveyResponse result = dao.getSurveyResponse(user.getUsername(), specVersion);
		assertEquals(user.getUsername(), result.getResponder().getUsername());
		assertEquals(specVersion, result.getSurvey().getSpecVersion());
		Map<String, Answer> resultResponses = result.getResponses();
		assertEquals(2, resultResponses.size());
		Answer resultAnswer1 = resultResponses.get(s1q1Number);
		assertTrue(resultAnswer1 instanceof YesNoAnswer);
		assertEquals(s1q1answer.getAnswer(), ((YesNoAnswer)resultAnswer1).getAnswer());
		Answer resultAnswer2 = resultResponses.get(s1q2Number);
		assertTrue(resultAnswer2 instanceof YesNoAnswer);
		assertEquals(s1q2answer.getAnswer(), ((YesNoAnswer)resultAnswer2).getAnswer());

		// test latest version
		result = dao.getSurveyResponse(user.getUsername(), null);
		assertEquals(user.getUsername(), result.getResponder().getUsername());
		assertEquals(laterSpecVersion, result.getSurvey().getSpecVersion());
		resultResponses = result.getResponses();
		assertEquals(4, resultResponses.size());
		resultAnswer1 = resultResponses.get(s1q1Number);
		assertTrue(resultAnswer1 instanceof YesNoAnswer);
		assertEquals(s1q1answer2.getAnswer(), ((YesNoAnswer)resultAnswer1).getAnswer());
		resultAnswer2 = resultResponses.get(s2q2Number);
		assertTrue(resultAnswer2 instanceof YesNoAnswerWithEvidence);
		assertEquals(s2q2Answer.getAnswer(), ((YesNoAnswerWithEvidence)resultAnswer2).getAnswer());
		assertEquals(s2q2Answer.getEvidence(), ((YesNoAnswerWithEvidence)resultAnswer2).getEvidence());
		Answer resultAnswer3 = resultResponses.get(s2q3Number);
		assertTrue(resultAnswer3 instanceof YesNoAnswer);
		assertEquals(s2q3answer.getAnswer(), ((YesNoAnswer)resultAnswer1).getAnswer());
		Answer resultSubAnswer = resultResponses.get(s2q1Number);
		Answer subAnswer = ((SubQuestionAnswers)resultSubAnswer).getSubAnswers().get(s2q3Number);
		assertEquals(s2q3answer.getAnswer(), ((YesNoAnswer)subAnswer).getAnswer());	
	}

	@Test
	public void testSetSubmitted() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse();
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(YesNo.NotAnswered);

		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		response.setResponses(responses);
		response.setSpecVersion(specVersion);
		response.setSubmitted(false);
		response.setSurvey(survey);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response);
		SurveyResponse result = dao.getSurveyResponse(user.getUsername(), null);
		assertFalse(result.isSubmitted());
		dao.setSubmitted(user.getUsername(), specVersion, true);
		result = dao.getSurveyResponse(user.getUsername(), null);
		assertTrue(result.isSubmitted());
	}
	
	@Test
	public void testSetApproved() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse();
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(YesNo.NotAnswered);

		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		response.setResponses(responses);
		response.setSpecVersion(specVersion);
		response.setApproved(false);
		response.setSurvey(survey);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response);
		SurveyResponse result = dao.getSurveyResponse(user.getUsername(), null);
		assertFalse(result.isApproved());
		dao.setApproved(user.getUsername(), specVersion, true);
		result = dao.getSurveyResponse(user.getUsername(), null);
		assertTrue(result.isApproved());
	}
	
	@Test
	public void testSetRejected() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse();
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(YesNo.NotAnswered);

		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		response.setResponses(responses);
		response.setSpecVersion(specVersion);
		response.setRejected(false);
		response.setSurvey(survey);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response);
		SurveyResponse result = dao.getSurveyResponse(user.getUsername(), null);
		assertFalse(result.isApproved());
		dao.setRejected(user.getUsername(), specVersion, true);
		result = dao.getSurveyResponse(user.getUsername(), null);
		assertTrue(result.isRejected());
	}
	
	@Test
	public void testUpdateSurveyResponseAnswers() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse();
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(YesNo.NotAnswered);
		YesNoAnswerWithEvidence s2q2Answer = new YesNoAnswerWithEvidence(YesNo.No, "s2q2evidence");
		YesNoAnswer s2q3answer = new YesNoAnswer(YesNo.Yes);
		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		responses.put(s2q2Number, s2q2Answer);
		responses.put(s2q3Number, s2q3answer);
		response.setResponses(responses);
		response.setSpecVersion(specVersion);
		response.setSubmitted(false);
		response.setSurvey(survey);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response);
		@SuppressWarnings("unused")
		SurveyResponse beforeresult = dao.getSurveyResponse(user.getUsername(), null);
		
		Map<String, Answer> updated = new HashMap<String, Answer>();
		updated.put(s1q1Number, s1q1answer);	// unchanged
		// delete s1q2Number
		YesNoAnswerWithEvidence updatedS2q2Answer = new YesNoAnswerWithEvidence(YesNo.No, "different");
		updated.put(s2q2Number, updatedS2q2Answer);	// changed Evidence
		YesNoAnswer updatedS2q3Answer = new YesNoAnswer(YesNo.No);	// changed answer
		updated.put(s2q3Number, updatedS2q3Answer);
		SurveyResponse updatedResponse = new SurveyResponse();
		updatedResponse.setResponder(user);
		updatedResponse.setResponses(updated);
		updatedResponse.setSpecVersion(specVersion);
		updatedResponse.setSubmitted(false);
		updatedResponse.setSurvey(survey);
		dao.updateSurveyResponseAnswers(updatedResponse);
		
		SurveyResponse result = dao.getSurveyResponse(user.getUsername(), null);
		assertEquals(user.getUsername(), result.getResponder().getUsername());
		assertEquals(specVersion, result.getSurvey().getSpecVersion());
		Map<String, Answer> resultResponses = result.getResponses();
		assertEquals(4, resultResponses.size());
		Answer resultAnswer1 = resultResponses.get(s1q1Number);
		assertTrue(resultAnswer1 instanceof YesNoAnswer);
		assertEquals(s1q1answer.getAnswer(), ((YesNoAnswer)resultAnswer1).getAnswer());
		assertTrue(resultResponses.get(s1q2Number) == null);
		Answer resultAnswer2 = resultResponses.get(s2q2Number);
		assertTrue(resultAnswer2 instanceof YesNoAnswerWithEvidence);
		assertEquals(updatedS2q2Answer.getAnswer(), ((YesNoAnswerWithEvidence)resultAnswer2).getAnswer());
		assertEquals(updatedS2q2Answer.getEvidence(), ((YesNoAnswerWithEvidence)resultAnswer2).getEvidence());
		Answer resultAnswer3 = resultResponses.get(s2q3Number);
		assertTrue(resultAnswer3 instanceof YesNoAnswer);
		assertEquals(updatedS2q3Answer.getAnswer(), ((YesNoAnswer)resultAnswer3).getAnswer());
	}

}
