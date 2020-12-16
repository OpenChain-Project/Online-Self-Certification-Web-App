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
	String s1q1Number = "1.a";
	YesNo s1q1Answer = YesNo.Yes;
	String[] s1q1SpecRef = new String[] {"s1q1SpecRef"};
	YesNoQuestion s1q1;
	Section section1;
	Survey survey;
	String s1q2Question="s1q2question";
	String s1q2Number = "1.b";
	YesNo s1q2Answer = YesNo.NotApplicable;
	String s1q2Prompt = "s1q2prompt";
	YesNoNotApplicableQuestion s1q2;
	Section section2;
	String section2Name = "section2Name";
	String section2Title = "section2Title";
	String s2q1Question="s2q1question";
	String s2q1Number = "2.b";
	int s2q1MinCorrect = 4;
	SubQuestion s2q1;
	String[] s2q1SpecRef = new String[] {"s2q1SpecRef"};
	String s2q2Question="s2q2question";
	String s2q2Number = "2.b.i";
	YesNo s2q2Answer = YesNo.No;
	String s2q2Prompt = "s2q2prompt";
	String s2q2validate = "dd";
	YesNoQuestionWithEvidence s2q2;
	String[] s2q2SpecRef = new String[] {"s2q2SpecRef"};
	String s2q3Question="s2q3question";
	String s2q3Number = "2.b.ii";
	YesNo s2q3Answer = YesNo.NotAnswered;
	YesNoQuestion s2q3;
	String[] s2q3SpecRef = new String[] {"s2q3SpecRef"};
	SurveyDbDao surveyDao;
	User user;
	User user2;
	String language1 = User.DEFAULT_LANGUAGE;
	String language2 = "def";
	String language3 = "hij";
	
	@Before
	public void setUp() throws Exception {
		con = TestHelper.getConnection();
		TestHelper.truncateDatabase(con);
		survey = new Survey(specVersion, language1);
		List<Section> sections = new ArrayList<Section>();
		section1 = new Section(language1);
		section1.setName(section1Name);
		section1.setTitle(section1Title);
		List<Question> section1Questions = new ArrayList<Question>();
		s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, specVersion, s1q1SpecRef, language1, s1q1Answer);
		section1Questions.add(s1q1);

		String[] s1q2SpecRef = new String[] {"s1q2SpecRef"};
		s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, specVersion, s1q2SpecRef, language1, s1q2Answer, s1q2Prompt);
		section1Questions.add(s1q2);
		section1.setQuestions(section1Questions);
		sections.add(section1);
		section2 = new Section(language1);
		section2.setName(section2Name);
		section2.setTitle(section2Title);
		List<Question> section2Questions = new ArrayList<Question>();
		s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, specVersion, s2q1SpecRef, language1, s2q1MinCorrect);
		section2Questions.add(s2q1);

		s2q2 = new YesNoQuestionWithEvidence(s2q2Question, 
				section2Name, s2q2Number, specVersion, s2q2SpecRef, language1, s2q2Answer, s2q2Prompt, Pattern.compile(s2q2validate));
		s2q2.setSubQuestionOfNumber(s2q1Number);
		section2Questions.add(s2q2);

		s2q3 = new YesNoQuestion(s2q3Question, 
				section2Name, s2q3Number, specVersion, s2q3SpecRef, language1, s2q3Answer);
		s2q3.setSubQuestionOfNumber(s2q1Number);
		section2Questions.add(s2q3);
		section2.setQuestions(section2Questions);
		sections.add(section2);
		survey.setSections(sections);
		surveyDao = new SurveyDbDao(con);
		surveyDao.addSurvey(survey);
		Survey survey2 = new Survey(specVersion, language3);
		survey2.setSections(sections);
		survey2.setLanguage(language3);
		surveyDao.addSurvey(survey2);
		
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
		user2 = new User();
		user2.setAddress("Address");
		user2.setAdmin(false);
		user2.setEmail("test2@openchain.com");
		user2.setName("Test User2");
		user2.setOrganization("Test Og2.");
		user2.setPasswordReset(false);
		user2.setPasswordToken("TOKEN2");
		user2.setUsername("testuser2");
		user2.setUuid(UUID.randomUUID().toString());
		user2.setVerificationExpirationDate(new Date());
		user2.setVerified(true);
		UserDb.getUserDb(TestHelper.getTestServletConfig()).addUser(user2);	
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
		Survey laterSurvey = new Survey(laterSpecVersion, language1);
		laterSurvey.setSections(new ArrayList<Section>());
		surveyDao.addSurvey(laterSurvey);
		assertEquals(laterSpecVersion, dao.getLatestSpecVersion());
	}

	@Test
	public void testAddSurveyResponse() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse(specVersion, language1);
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(language1, YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(language1, YesNo.NotAnswered);

		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		response.setResponses(responses);
		response.setSubmitted(false);
		response.setSurvey(survey);
		response.setApproved(true);
		response.setRejected(false);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response, language1);
		String laterSpecVersion = specVersion + ".1";
		Survey laterSurvey = new Survey(laterSpecVersion, language1);
		List<Section> laterSections = new ArrayList<Section>();
		Section newSection1 = new Section(language1);
		newSection1.setName(section1.getName());
		newSection1.setTitle(section1.getTitle());
		List<Question> newQuestions1 = new ArrayList<Question>();
		s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, laterSpecVersion, s1q1SpecRef, language1, s1q1Answer);
		newQuestions1.add(s1q1);

		String[] s1q2SpecRef = new String[] {"s1q2SpecRef"};
		s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, laterSpecVersion, s1q2SpecRef, language1, s1q2Answer, s1q2Prompt);
		newQuestions1.add(s1q2);
		newSection1.setQuestions(newQuestions1);
		laterSections.add(newSection1);
		Section newSection2 = new Section(language1);
		newSection2.setName(section2.getName());
		newSection2.setTitle(section2.getTitle());
		List<Question> newQuestions2 = new ArrayList<Question>();
		s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, laterSpecVersion, s2q1SpecRef, language1, s2q1MinCorrect);
		newQuestions2.add(s2q1);

		s2q2 = new YesNoQuestionWithEvidence(s2q2Question, 
				section2Name, s2q2Number, laterSpecVersion, s2q2SpecRef, language1, 
				s2q2Answer, s2q2Prompt, Pattern.compile(s2q2validate));
		s2q2.setSubQuestionOfNumber(s2q1Number);
		newQuestions2.add(s2q2);

		s2q3 = new YesNoQuestion(s2q3Question, 
				section2Name, s2q3Number, laterSpecVersion, s2q3SpecRef, language1, s2q3Answer);
		s2q3.setSubQuestionOfNumber(s2q1Number);
		newQuestions2.add(s2q3);
		newSection2.setQuestions(newQuestions2);
		laterSections.add(newSection2);
		laterSurvey.setSections(laterSections);
		surveyDao.addSurvey(laterSurvey);
		SurveyResponse response2 = new SurveyResponse(laterSpecVersion, language1);
		response2.setResponder(user);
		Map<String, Answer> responses2 = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer2 = new YesNoAnswer(language1, YesNo.Yes);
		
		responses2.put(s1q1Number, s1q1answer2);

		YesNoAnswerWithEvidence s2q2Answer = new YesNoAnswerWithEvidence(language1, YesNo.No, "s2q2evidence");
		responses2.put(s2q2Number, s2q2Answer);
		YesNoAnswer s2q3answer = new YesNoAnswer(language1, YesNo.Yes);
		responses2.put(s2q3Number, s2q3answer);
		
		response2.setResponses(responses2);
		response2.setSubmitted(false);
		response2.setSurvey(laterSurvey);
		dao.addSurveyResponse(response2, language1);
		
		// test the first version
		SurveyResponse result = dao.getSurveyResponse(user.getUsername(), specVersion, language1);
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
		assertEquals(response.getId(), result.getId());
		// test latest version
		result = dao.getSurveyResponse(user.getUsername(), null, language1);
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
		assertEquals(response2.getId(), result.getId());
	}

	@Test
	public void testSetSubmitted() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse(specVersion, language1);
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(language1, YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(language1, YesNo.NotAnswered);

		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		response.setResponses(responses);
		response.setSubmitted(false);
		response.setSurvey(survey);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response, language1);
		SurveyResponse result = dao.getSurveyResponse(user.getUsername(), null, language1);
		assertFalse(result.isSubmitted());
		dao.setSubmitted(user.getUsername(), specVersion, true);
		result = dao.getSurveyResponse(user.getUsername(), null, language1);
		assertTrue(result.isSubmitted());
	}
	
	@Test
	public void testSetApproved() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse(specVersion, language1);
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(language1, YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(language1, YesNo.NotAnswered);

		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		response.setResponses(responses);
		response.setApproved(false);
		response.setSurvey(survey);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response, language1);
		SurveyResponse result = dao.getSurveyResponse(user.getUsername(), null, language1);
		assertFalse(result.isApproved());
		dao.setApproved(user.getUsername(), specVersion, true);
		result = dao.getSurveyResponse(user.getUsername(), null, language1);
		assertTrue(result.isApproved());
	}
	
	@Test
	public void testSetApprovedIds() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse(specVersion, language1);
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(language1, YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(language1, YesNo.NotAnswered);

		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		response.setResponses(responses);
		response.setApproved(false);
		response.setSurvey(survey);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response, language1);
		
		SurveyResponse response2 = new SurveyResponse(specVersion, language1);
		response2.setResponder(user2);
		Map<String, Answer> responses2 = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer2 = new YesNoAnswer(language1, YesNo.No);
		YesNoAnswer s1q2answer2 = new YesNoAnswer(language1, YesNo.NotAnswered);

		responses2.put(s1q1Number, s1q1answer2);
		responses2.put(s1q2Number, s1q2answer2);
		response2.setResponses(responses2);
		response2.setApproved(true);
		response2.setSurvey(survey);
		dao.addSurveyResponse(response2, language1);	
		
		SurveyResponse result = dao.getSurveyResponse(user.getUsername(), null, language1);
		assertFalse(result.isApproved());
		result = dao.getSurveyResponse(user2.getUsername(), null, language1);
		assertTrue(result.isApproved());
		String[] ids = new String[] {response.getId(), response2.getId()};
		
		dao.setApproved(ids, true);
		result = dao.getSurveyResponse(user.getUsername(), null, language1);
		assertTrue(result.isApproved());
		result = dao.getSurveyResponse(user2.getUsername(), null, language1);
		assertTrue(result.isApproved());
		dao.setApproved(ids, false);
		result = dao.getSurveyResponse(user.getUsername(), null, language1);
		assertFalse(result.isApproved());
		result = dao.getSurveyResponse(user2.getUsername(), null, language1);
		assertFalse(result.isApproved());
	}
	
	@Test
	public void testSetRejected() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse(specVersion, language1);
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(language1, YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(language1, YesNo.NotAnswered);

		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		response.setResponses(responses);
		response.setRejected(false);
		response.setSurvey(survey);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response, language1);
		SurveyResponse result = dao.getSurveyResponse(user.getUsername(), null, language1);
		assertFalse(result.isApproved());
		dao.setRejected(user.getUsername(), specVersion, true);
		result = dao.getSurveyResponse(user.getUsername(), null, language1);
		assertTrue(result.isRejected());
	}
	
	@Test
	public void testSetRejectedIds() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse(specVersion, language1);
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(language1, YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(language1, YesNo.NotAnswered);

		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		response.setResponses(responses);
		response.setRejected(false);
		response.setSurvey(survey);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response, language1);
		
		SurveyResponse response2 = new SurveyResponse(specVersion, language1);
		response2.setResponder(user2);
		Map<String, Answer> responses2 = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer2 = new YesNoAnswer(language1, YesNo.No);
		YesNoAnswer s1q2answer2 = new YesNoAnswer(language1, YesNo.NotAnswered);

		responses2.put(s1q1Number, s1q1answer2);
		responses2.put(s1q2Number, s1q2answer2);
		response2.setResponses(responses2);
		response2.setRejected(true);
		response2.setSurvey(survey);
		dao.addSurveyResponse(response2, language1);	
		
		SurveyResponse result = dao.getSurveyResponse(user.getUsername(), null, language1);
		assertFalse(result.isRejected());
		result = dao.getSurveyResponse(user2.getUsername(), null, language1);
		assertTrue(result.isRejected());
		String[] ids = new String[] {response.getId(), response2.getId()};
		
		dao.setRejected(ids, true);
		result = dao.getSurveyResponse(user.getUsername(), null, language1);
		assertTrue(result.isRejected());
		result = dao.getSurveyResponse(user2.getUsername(), null, language1);
		assertTrue(result.isRejected());
		dao.setRejected(ids, false);
		result = dao.getSurveyResponse(user.getUsername(), null, language1);
		assertFalse(result.isRejected());
		result = dao.getSurveyResponse(user2.getUsername(), null, language1);
		assertFalse(result.isRejected());
	}
	
	@Test
	public void testUpdateSurveyResponseAnswers() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse(specVersion, language1);
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(language1, YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(language1, YesNo.NotAnswered);
		YesNoAnswerWithEvidence s2q2Answer = new YesNoAnswerWithEvidence(language1, YesNo.No, "s2q2evidence");
		YesNoAnswer s2q3answer = new YesNoAnswer(language1, YesNo.Yes);
		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		responses.put(s2q2Number, s2q2Answer);
		responses.put(s2q3Number, s2q3answer);
		response.setResponses(responses);
		response.setSubmitted(false);
		response.setSurvey(survey);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response, language1);
		@SuppressWarnings("unused")
		SurveyResponse beforeresult = dao.getSurveyResponse(language1, user.getUsername(), null);
		
		Map<String, Answer> updated = new HashMap<String, Answer>();
		updated.put(s1q1Number, s1q1answer);	// unchanged
		// delete s1q2Number
		YesNoAnswerWithEvidence updatedS2q2Answer = new YesNoAnswerWithEvidence(language1, YesNo.No, "different");
		updated.put(s2q2Number, updatedS2q2Answer);	// changed Evidence
		YesNoAnswer updatedS2q3Answer = new YesNoAnswer(language1, YesNo.No);	// changed answer
		updated.put(s2q3Number, updatedS2q3Answer);
		SurveyResponse updatedResponse = new SurveyResponse(specVersion, language1);
		updatedResponse.setResponder(user);
		updatedResponse.setResponses(updated);
		updatedResponse.setSubmitted(false);
		updatedResponse.setSurvey(survey);
		dao.updateSurveyResponseAnswers(updatedResponse, language1);
		
		SurveyResponse result = dao.getSurveyResponse(user.getUsername(), null, language1);
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

	@Test
	public void testDeleteId() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse(specVersion, language1);
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(language1, YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(language1, YesNo.NotAnswered);

		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		response.setResponses(responses);
		response.setSubmitted(false);
		response.setSurvey(survey);
		response.setApproved(true);
		response.setRejected(false);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response, language1);
		String laterSpecVersion = specVersion + ".1";
		Survey laterSurvey = new Survey(laterSpecVersion, language1);
		List<Section> laterSections = new ArrayList<Section>();
		Section newSection1 = new Section(language1);
		newSection1.setName(section1.getName());
		newSection1.setTitle(section1.getTitle());
		List<Question> newQuestions1 = new ArrayList<Question>();
		s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, laterSpecVersion, s1q1SpecRef, language1, s1q1Answer);
		newQuestions1.add(s1q1);
		
		String[] s1q2SpecRef = new String[] {"s1q2SpecRef"};
		s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, laterSpecVersion, s1q2SpecRef, language1, s1q2Answer, s1q2Prompt);
		newQuestions1.add(s1q2);
		newSection1.setQuestions(newQuestions1);
		laterSections.add(newSection1);
		Section newSection2 = new Section(language1);
		newSection2.setName(section2.getName());
		newSection2.setTitle(section2.getTitle());
		List<Question> newQuestions2 = new ArrayList<Question>();
		s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, laterSpecVersion, s2q1SpecRef, language1, s2q1MinCorrect);
		newQuestions2.add(s2q1);

		s2q2 = new YesNoQuestionWithEvidence(s2q2Question, 
				section2Name, s2q2Number, laterSpecVersion, s2q2SpecRef, language1, 
				s2q2Answer, s2q2Prompt, Pattern.compile(s2q2validate));
		s2q2.setSubQuestionOfNumber(s2q1Number);
		newQuestions2.add(s2q2);

		s2q3 = new YesNoQuestion(s2q3Question, 
				section2Name, s2q3Number, laterSpecVersion, s2q3SpecRef, language1, s2q3Answer);
		s2q3.setSubQuestionOfNumber(s2q1Number);
		newQuestions2.add(s2q3);
		newSection2.setQuestions(newQuestions2);
		laterSections.add(newSection2);
		laterSurvey.setSections(laterSections);
		surveyDao.addSurvey(laterSurvey);
		SurveyResponse response2 = new SurveyResponse(laterSpecVersion, language1);
		response2.setResponder(user);
		Map<String, Answer> responses2 = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer2 = new YesNoAnswer(language1, YesNo.Yes);
		
		responses2.put(s1q1Number, s1q1answer2);

		YesNoAnswerWithEvidence s2q2Answer = new YesNoAnswerWithEvidence(language1, YesNo.No, "s2q2evidence");
		responses2.put(s2q2Number, s2q2Answer);
		YesNoAnswer s2q3answer = new YesNoAnswer(language1, YesNo.Yes);
		responses2.put(s2q3Number, s2q3answer);
		
		response2.setResponses(responses2);
		response2.setSubmitted(false);
		response2.setSurvey(laterSurvey);
		dao.addSurveyResponse(response2, language1);
		
		// test the first version
		SurveyResponse result = dao.getSurveyResponse(user.getUsername(), specVersion, language1);
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
		assertEquals(response.getId(), result.getId());
		// test latest version
		result = dao.getSurveyResponse(user.getUsername(), null, laterSpecVersion);
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
		assertEquals(response2.getId(), result.getId());
		
		// delete the first version
		dao.deleteSurveyResponseAnswers(response.getId());
		result = null;
		try {
			result = dao.getSurveyResponse(user.getUsername(), specVersion, language1);
		} catch(Exception ex) {
			
		}
		assertTrue(result == null);
		result = dao.getSurveyResponse(user.getUsername(), laterSpecVersion, language1);
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
		resultAnswer3 = resultResponses.get(s2q3Number);
		assertTrue(resultAnswer3 instanceof YesNoAnswer);
		assertEquals(s2q3answer.getAnswer(), ((YesNoAnswer)resultAnswer1).getAnswer());
		resultSubAnswer = resultResponses.get(s2q1Number);
		subAnswer = ((SubQuestionAnswers)resultSubAnswer).getSubAnswers().get(s2q3Number);
		assertEquals(s2q3answer.getAnswer(), ((YesNoAnswer)subAnswer).getAnswer());	
		assertEquals(response2.getId(), result.getId());
	}
	
	@Test
	public void testDeleteUserVersion() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse(specVersion, language1);
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(language1, YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(language1, YesNo.NotAnswered);

		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		response.setResponses(responses);
		response.setSubmitted(false);
		response.setSurvey(survey);
		response.setApproved(true);
		response.setRejected(false);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response, language1);
		String laterSpecVersion = specVersion + ".1";
		Survey laterSurvey = new Survey(laterSpecVersion, language1);
		List<Section> laterSections = new ArrayList<Section>();
		Section newSection1 = new Section(language1);
		newSection1.setName(section1.getName());
		newSection1.setTitle(section1.getTitle());
		List<Question> newQuestions1 = new ArrayList<Question>();
		s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, laterSpecVersion, s1q1SpecRef, language1, s1q1Answer);
		newQuestions1.add(s1q1);
		
		String[] s1q2SpecRef = new String[] {"s1q2SpecRef"};
		s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, laterSpecVersion, s1q2SpecRef, language1, s1q2Answer, s1q2Prompt);
		newQuestions1.add(s1q2);
		newSection1.setQuestions(newQuestions1);
		laterSections.add(newSection1);
		Section newSection2 = new Section(language1);
		newSection2.setName(section2.getName());
		newSection2.setTitle(section2.getTitle());
		List<Question> newQuestions2 = new ArrayList<Question>();
		s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, laterSpecVersion, s2q1SpecRef, language1, s2q1MinCorrect);
		newQuestions2.add(s2q1);

		s2q2 = new YesNoQuestionWithEvidence(s2q2Question, 
				section2Name, s2q2Number, laterSpecVersion, s2q2SpecRef, language1, 
				s2q2Answer, s2q2Prompt, Pattern.compile(s2q2validate));
		s2q2.setSubQuestionOfNumber(s2q1Number);
		newQuestions2.add(s2q2);

		s2q3 = new YesNoQuestion(s2q3Question, 
				section2Name, s2q3Number, laterSpecVersion, s2q3SpecRef, language1, s2q3Answer);
		s2q3.setSubQuestionOfNumber(s2q1Number);
		newQuestions2.add(s2q3);
		newSection2.setQuestions(newQuestions2);
		laterSections.add(newSection2);
		laterSurvey.setSections(laterSections);
		surveyDao.addSurvey(laterSurvey);
		SurveyResponse response2 = new SurveyResponse(laterSpecVersion, language1);
		response2.setResponder(user);
		Map<String, Answer> responses2 = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer2 = new YesNoAnswer(language1, YesNo.Yes);
		
		responses2.put(s1q1Number, s1q1answer2);

		YesNoAnswerWithEvidence s2q2Answer = new YesNoAnswerWithEvidence(language1, YesNo.No, "s2q2evidence");
		responses2.put(s2q2Number, s2q2Answer);
		YesNoAnswer s2q3answer = new YesNoAnswer(language1, YesNo.Yes);
		responses2.put(s2q3Number, s2q3answer);
		
		response2.setResponses(responses2);
		response2.setSubmitted(false);
		response2.setSurvey(laterSurvey);
		dao.addSurveyResponse(response2, language1);
		
		// test the first version
		SurveyResponse result = dao.getSurveyResponse(user.getUsername(), specVersion, language1);
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
		assertEquals(response.getId(), result.getId());
		// test latest version
		result = dao.getSurveyResponse(user.getUsername(), null, laterSpecVersion);
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
		assertEquals(response2.getId(), result.getId());
		
		// delete the first version
		dao.deleteSurveyResponseAnswers(response.getResponder().getUsername(), response.getSpecVersion());
		result = null;
		try {
			result = dao.getSurveyResponse(user.getUsername(), specVersion, language1);
		} catch(Exception ex) {
			
		}
		assertTrue(result == null);
		result = dao.getSurveyResponse(user.getUsername(), laterSpecVersion, language1);
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
		resultAnswer3 = resultResponses.get(s2q3Number);
		assertTrue(resultAnswer3 instanceof YesNoAnswer);
		assertEquals(s2q3answer.getAnswer(), ((YesNoAnswer)resultAnswer1).getAnswer());
		resultSubAnswer = resultResponses.get(s2q1Number);
		subAnswer = ((SubQuestionAnswers)resultSubAnswer).getSubAnswers().get(s2q3Number);
		assertEquals(s2q3answer.getAnswer(), ((YesNoAnswer)subAnswer).getAnswer());	
		assertEquals(response2.getId(), result.getId());
	}
	
	@Test
	public void testGetSurveyResponsesUser() throws Exception {
		SurveyResponse response = new SurveyResponse(specVersion, language1);
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(language1, YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(language1, YesNo.NotAnswered);

		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		response.setResponses(responses);
		response.setSubmitted(false);
		response.setSurvey(survey);
		response.setApproved(true);
		response.setRejected(false);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response, language1);
		
		// Second survey for the same user
		String laterSpecVersion = specVersion + ".1";
		Survey laterSurvey = new Survey(laterSpecVersion, language1);
		List<Section> laterSections = new ArrayList<Section>();
		Section newSection1 = new Section(language1);
		newSection1.setName(section1.getName());
		newSection1.setTitle(section1.getTitle());
		List<Question> newQuestions1 = new ArrayList<Question>();
		s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, laterSpecVersion, s1q1SpecRef, language1, s1q1Answer);
		newQuestions1.add(s1q1);

		String[] s1q2SpecRef = new String[] {"s1q2SpecRef"};
		s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, laterSpecVersion, s1q2SpecRef, language1, s1q2Answer, s1q2Prompt);
		newQuestions1.add(s1q2);
		newSection1.setQuestions(newQuestions1);
		laterSections.add(newSection1);
		Section newSection2 = new Section(language1);
		newSection2.setName(section2.getName());
		newSection2.setTitle(section2.getTitle());
		List<Question> newQuestions2 = new ArrayList<Question>();
		s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, laterSpecVersion, s2q1SpecRef, language1, s2q1MinCorrect);
		newQuestions2.add(s2q1);

		s2q2 = new YesNoQuestionWithEvidence(s2q2Question, 
				section2Name, s2q2Number, laterSpecVersion, s2q2SpecRef, language1, s2q2Answer, s2q2Prompt, Pattern.compile(s2q2validate));
		s2q2.setSubQuestionOfNumber(s2q1Number);
		newQuestions2.add(s2q2);

		s2q3 = new YesNoQuestion(s2q3Question, 
				section2Name, s2q3Number, laterSpecVersion, s2q3SpecRef, language1, s2q3Answer);
		s2q3.setSubQuestionOfNumber(s2q1Number);
		newQuestions2.add(s2q3);
		newSection2.setQuestions(newQuestions2);
		laterSections.add(newSection2);
		laterSurvey.setSections(laterSections);
		surveyDao.addSurvey(laterSurvey);
		SurveyResponse response2 = new SurveyResponse(laterSpecVersion, language1);
		response2.setResponder(user);
		Map<String, Answer> responses2 = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer2 = new YesNoAnswer(language1, YesNo.Yes);
		
		responses2.put(s1q1Number, s1q1answer2);

		YesNoAnswerWithEvidence s2q2Answer = new YesNoAnswerWithEvidence(language1, YesNo.No, "s2q2evidence");
		responses2.put(s2q2Number, s2q2Answer);
		YesNoAnswer s2q3answer = new YesNoAnswer(language1, YesNo.Yes);
		responses2.put(s2q3Number, s2q3answer);
		
		response2.setResponses(responses2);
		response2.setSubmitted(false);
		response2.setSurvey(laterSurvey);
		dao.addSurveyResponse(response2, language1);
		
		// not the users survey response
		String nonUserSpecVersion = "2.1";
		Survey nonUserSurvey = new Survey(nonUserSpecVersion, language1);
		List<Section> nonUserSections = new ArrayList<Section>();
		Section nonUserSection1 = new Section(language1);
		nonUserSection1.setName(section1.getName());
		nonUserSection1.setTitle(section1.getTitle());
		List<Question> nonUserQuestions1 = new ArrayList<Question>();
		s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, nonUserSpecVersion, s1q1SpecRef, language1, s1q1Answer);
		nonUserQuestions1.add(s1q1);

		String[] s1q2SpecRef3 = new String[] {"s1q2SpecRef3"};
		s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, nonUserSpecVersion, s1q2SpecRef3, language1, s1q2Answer, s1q2Prompt);
		nonUserQuestions1.add(s1q2);
		nonUserSection1.setQuestions(nonUserQuestions1);
		nonUserSections.add(nonUserSection1);
		Section nonUserSection2 = new Section(language1);
		nonUserSection2.setName(section2.getName());
		nonUserSection2.setTitle(section2.getTitle());
		List<Question> nonUserQuestions2 = new ArrayList<Question>();
		s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, nonUserSpecVersion, s2q1SpecRef, language1, s2q1MinCorrect);
		nonUserQuestions2.add(s2q1);

		nonUserSection2.setQuestions(nonUserQuestions2);
		nonUserSections.add(nonUserSection2);
		nonUserSurvey.setSections(nonUserSections);
		surveyDao.addSurvey(nonUserSurvey);
		SurveyResponse response3 = new SurveyResponse(nonUserSpecVersion, language1);
		response3.setResponder(user2);
		Map<String, Answer> responses3 = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer3 = new YesNoAnswer(language1, YesNo.Yes);
		
		responses3.put(s1q1Number, s1q1answer3);
		
		response3.setResponses(responses3);
		response3.setSubmitted(false);
		response3.setSurvey(laterSurvey);
		dao.addSurveyResponse(response3, language1);
		
		List<SurveyResponse> result = dao.getSurveyResponses(user.getUsername(), language1);
		assertEquals(2, result.size());
		
		SurveyResponse firstVersion = null;
		SurveyResponse secondVersion = null;
		if (result.get(0).getSpecVersion().compareTo(result.get(1).getSpecVersion()) > 0) {
			firstVersion = result.get(1);
			secondVersion = result.get(0);
		} else {
			firstVersion = result.get(0);
			secondVersion = result.get(1);
		}
		// test the first version
		assertEquals(user.getUsername(), firstVersion.getResponder().getUsername());
		assertEquals(specVersion, firstVersion.getSurvey().getSpecVersion());
		Map<String, Answer> resultResponses = firstVersion.getResponses();
		assertEquals(2, resultResponses.size());
		Answer resultAnswer1 = resultResponses.get(s1q1Number);
		assertTrue(resultAnswer1 instanceof YesNoAnswer);
		assertEquals(s1q1answer.getAnswer(), ((YesNoAnswer)resultAnswer1).getAnswer());
		Answer resultAnswer2 = resultResponses.get(s1q2Number);
		assertTrue(resultAnswer2 instanceof YesNoAnswer);
		assertEquals(s1q2answer.getAnswer(), ((YesNoAnswer)resultAnswer2).getAnswer());
		assertEquals(response.getId(), firstVersion.getId());
		// test latest version
		assertEquals(user.getUsername(), secondVersion.getResponder().getUsername());
		assertEquals(laterSpecVersion, secondVersion.getSurvey().getSpecVersion());
		resultResponses = secondVersion.getResponses();
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
		assertEquals(response2.getId(), secondVersion.getId());
	}
}
