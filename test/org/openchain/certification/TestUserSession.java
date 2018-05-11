package org.openchain.certification;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
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
import org.openchain.certification.dbdao.SurveyDbDao;
import org.openchain.certification.dbdao.SurveyResponseDao;
import org.openchain.certification.dbdao.UserDb;
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
import org.openchain.certification.utility.EmailUtilException;
import org.openchain.certification.utility.PasswordUtil;

public class TestUserSession {
	static final String USER_PASSWORD = "userpass";
	
	Connection con;
	
	String language = "eng";
	String primarySpecVersion = "1.0";
	String specVersion = primarySpecVersion + ".2";
	String section1Name = "section1Name";
	String section1Title = "section1Title";
	String s1q1Question="s1q1question";
	String s1q1Number = "1.a";
	YesNo s1q1Answer = YesNo.Yes;
	String s1q1SpecRef = "s1q1SpecRef";
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
	String s2q1SpecRef = "s2q1SpecRef";
	String s2q2Question="s2q2question";
	String s2q2Number = "2.b.ii";
	YesNo s2q2Answer = YesNo.No;
	String s2q2Prompt = "s2q2prompt";
	String s2q2validate = "dd";
	YesNoQuestionWithEvidence s2q2;
	String s2q2SpecRef = "s2q2SpecRef";
	String s2q3Question="s2q3question";
	String s2q3Number = "2.b.iii";
	YesNo s2q3Answer = YesNo.NotAnswered;
	YesNoQuestion s2q3;
	String s2q3SpecRef = "s2q3SpecRef";
	
	String primarySpecVersion2 = "2.2";
	String specVersion2 = primarySpecVersion2+".2";
	String section1Name2 = "section1Name2";
	String section1Title2 = "section1Title2";
	String s1q1Question2="s1q1question2";
	String s1q1Number2 = "1.a";
	YesNo s1q1Answer2 = YesNo.Yes;
	String s1q1SpecRef2 = "s1q1SpecRef2";
	YesNoQuestion s1q12;
	Section section12;
	Survey survey2;
	
	String language2 = "fra";
	Survey survey1lang2;
	Survey survey2lang2;
	
	SurveyDbDao surveyDao;
	User user;

	@Before
	public void setUp() throws Exception {
		con = TestHelper.getConnection();
		TestHelper.truncateDatabase(con);
		survey = new Survey(specVersion, language);
		List<Section> sections = new ArrayList<Section>();
		section1 = new Section(language);
		section1.setName(section1Name);
		section1.setTitle(section1Title);
		List<Question> section1Questions = new ArrayList<Question>();
		s1q1 = new YesNoQuestion(s1q1Question, 
				section1Name, s1q1Number, specVersion, language, s1q1Answer);
		s1q1.setSpecReference(s1q1SpecRef);
		section1Questions.add(s1q1);

		s1q2 = new YesNoNotApplicableQuestion(s1q2Question, 
				section1Name, s1q2Number, specVersion, language, s1q2Answer, s1q2Prompt);
		String s1q2SpecRef = "s1q2SpecRef";
		s1q2.setSpecReference(s1q2SpecRef);
		section1Questions.add(s1q2);
		section1.setQuestions(section1Questions);
		sections.add(section1);
		section2 = new Section(language);
		section2.setName(section2Name);
		section2.setTitle(section2Title);
		List<Question> section2Questions = new ArrayList<Question>();
		s2q1 = new SubQuestion(s2q1Question, 
				section2Name, s2q1Number, specVersion, language, s2q1MinCorrect);
		s2q1.setSpecReference(s2q1SpecRef);
		section2Questions.add(s2q1);

		s2q2 = new YesNoQuestionWithEvidence(s2q2Question, 
				section2Name, s2q2Number, specVersion, language, s2q2Answer, s2q2Prompt, Pattern.compile(s2q2validate));
		s2q2.setSpecReference(s2q2SpecRef);
		s2q2.setSubQuestionOfNumber(s2q1Number);
		section2Questions.add(s2q2);

		s2q3 = new YesNoQuestion(s2q3Question, 
				section2Name, s2q3Number, specVersion, language, s2q3Answer);
		s2q3.setSpecReference(s2q3SpecRef);
		s2q3.setSubQuestionOfNumber(s2q1Number);
		section2Questions.add(s2q3);
		section2.setQuestions(section2Questions);
		sections.add(section2);
		survey.setSections(sections);
		surveyDao = new SurveyDbDao(con);
		surveyDao.addSurvey(survey);
		
		survey2 = new Survey(specVersion2, language);
		List<Section> sections2 = new ArrayList<Section>();
		section12 = new Section(language);
		section12.setName(section1Name2);
		section12.setTitle(section1Title2);
		List<Question> section1Questions2 = new ArrayList<Question>();
		s1q12 = new YesNoQuestion(s1q1Question2, 
				section1Name2, s1q1Number2, specVersion2, language, s1q1Answer2);
		s1q12.setSpecReference(s1q1SpecRef2);
		section1Questions2.add(s1q12);
		section12.setQuestions(section1Questions2);
		survey2.setSections(sections2);
		surveyDao.addSurvey(survey2);
		
		survey1lang2 = survey.clone();
		survey1lang2.setLanguage(language2);
		surveyDao.addSurvey(survey1lang2);
		
		survey2lang2 = survey2.clone();
		survey2lang2.setLanguage(language2);
		surveyDao.addSurvey(survey2lang2);
		
		user = new User();
		user.setAddress("Address");
		user.setAdmin(false);
		user.setEmail("test@openchain.com");
		user.setName("Test User");
		user.setOrganization("Test Og.");
		user.setPasswordReset(false);
		user.setPasswordToken(PasswordUtil.getToken(USER_PASSWORD));
		user.setUsername("testuser");
		user.setUuid(UUID.randomUUID().toString());
		user.setVerificationExpirationDate(new Date());
		user.setVerified(true);
		user.setNamePermission(false);
		user.setEmailPermission(true);
		user.setLanguage(language);
		UserDb.getUserDb(TestHelper.getTestServletConfig()).addUser(user);	
	}

	@After
	public void tearDown() throws Exception {
		con.close();
	}

	@Test
	public void testLogin() {
		UserSession userSession = new UserSession(
				user.getUsername(), USER_PASSWORD, TestHelper.getTestServletConfig());
		assertFalse(userSession.isLoggedIn());
		assertTrue(userSession.login());
		assertTrue(userSession.isLoggedIn());
		assertEquals(user.hasEmailPermission(), userSession.hasEmailPermission());
		assertEquals(user.hasNamePermission(), userSession.hasNamePermission());
		userSession.logout();
		assertFalse(userSession.isLoggedIn());
		UserSession badUserSession = new UserSession(
				user.getUsername(), "wrongpass", TestHelper.getTestServletConfig());
		assertFalse(badUserSession.isLoggedIn());
		assertFalse(badUserSession.login());
		assertTrue(badUserSession.getLastError().contains("assword"));
	}

	@Test
	public void testUpdateAnswers() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse(specVersion, language);
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(language, YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(language, YesNo.NotAnswered);

		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		response.setResponses(responses);
		response.setSubmitted(false);
		response.setSurvey(survey);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response, language);
		
		UserSession userSession = new UserSession(
				user.getUsername(), USER_PASSWORD, TestHelper.getTestServletConfig());
		assertTrue(userSession.login());
		
		SurveyResponse result = userSession.getSurveyResponse();
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
		
		List<ResponseAnswer> answerUpdates = new ArrayList<ResponseAnswer>();
		answerUpdates.add(new ResponseAnswer(s1q1Number, "YES", true, null));
		answerUpdates.add(new ResponseAnswer(s2q2Number, "NO", true, "Evidence"));
		// The following should not be added since it is not checkd
		answerUpdates.add(new ResponseAnswer(s2q3Number, "NA", false, null));
		userSession.updateAnswers(answerUpdates);
		
		result = userSession.getSurveyResponse();
		assertEquals(user.getUsername(), result.getResponder().getUsername());
		assertEquals(specVersion, result.getSurvey().getSpecVersion());
		resultResponses = result.getResponses();
		assertEquals(4, resultResponses.size());
		resultAnswer1 = resultResponses.get(s1q1Number);
		assertTrue(resultAnswer1 instanceof YesNoAnswer);
		assertEquals(YesNo.Yes, ((YesNoAnswer)resultAnswer1).getAnswer());
		resultAnswer2 = resultResponses.get(s1q2Number);
		assertTrue(resultAnswer2 instanceof YesNoAnswer);
		assertEquals(s1q2answer.getAnswer(), ((YesNoAnswer)resultAnswer2).getAnswer());
		assertTrue(resultResponses.get(s2q1Number) instanceof SubQuestionAnswers);
		Answer resultAnswer4 = resultResponses.get(s2q2Number);
		assertTrue(resultAnswer4 instanceof YesNoAnswerWithEvidence);
		assertEquals(YesNo.No, ((YesNoAnswerWithEvidence)resultAnswer4).getAnswer());
		assertEquals("Evidence", ((YesNoAnswerWithEvidence)resultAnswer4).getEvidence());
		assertTrue(resultResponses.get(s2q3Number) == null);
		
		userSession.logout();
		try {
			result = userSession.getSurveyResponse();
			fail("This should have failed since the user is not logged in");
		} catch(Exception ex) {
			assertTrue(ex.getMessage().contains("ogged"));
		}
		
		// Check to make sure it persists
		UserSession newUserSession = new UserSession(
				user.getUsername(), USER_PASSWORD, TestHelper.getTestServletConfig());
		newUserSession.login();
		result = newUserSession.getSurveyResponse();
		assertEquals(user.getUsername(), result.getResponder().getUsername());
		assertEquals(specVersion, result.getSurvey().getSpecVersion());
		resultResponses = result.getResponses();
		assertEquals(4, resultResponses.size());
		resultAnswer1 = resultResponses.get(s1q1Number);
		assertTrue(resultAnswer1 instanceof YesNoAnswer);
		assertEquals(YesNo.Yes, ((YesNoAnswer)resultAnswer1).getAnswer());
		resultAnswer2 = resultResponses.get(s1q2Number);
		assertTrue(resultAnswer2 instanceof YesNoAnswer);
		assertEquals(s1q2answer.getAnswer(), ((YesNoAnswer)resultAnswer2).getAnswer());
		assertTrue(resultResponses.get(s2q1Number) instanceof SubQuestionAnswers);
		resultAnswer4 = resultResponses.get(s2q2Number);
		assertTrue(resultAnswer4 instanceof YesNoAnswerWithEvidence);
		assertEquals(YesNo.No, ((YesNoAnswerWithEvidence)resultAnswer4).getAnswer());
		assertEquals("Evidence", ((YesNoAnswerWithEvidence)resultAnswer4).getEvidence());
		assertTrue(resultResponses.get(s2q3Number) == null);
	}
	
	@Test
	public void testUpdateUser() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidUserException, QuestionException, SurveyResponseException, SQLException, EmailUtilException {
		UserSession userSession = new UserSession(
				user.getUsername(), USER_PASSWORD, TestHelper.getTestServletConfig());
		assertTrue(userSession.login());
		assertTrue(userSession.isLoggedIn());
		assertEquals(user.getAddress(), userSession.getAddress());
		assertEquals(user.getEmail(), userSession.getEmail());
		assertEquals(user.getName(), userSession.getName());
		assertEquals(user.getOrganization(), userSession.getOrganization());
		assertEquals(user.isAdmin(), userSession.isAdmin());
		assertEquals(user.hasEmailPermission(), userSession.hasEmailPermission());
		assertEquals(user.hasNamePermission(), userSession.hasNamePermission());
		assertEquals(user.getLanguage(), userSession.getLanguage());
		
		String newName = "new Name";
		String newEmail = "newemail@email.com";
		String newOrganization = "new Organization";
		String newAddress = "new Address";
		String newPassword = "newPassword!";
		String newLanguage = "abc";
		boolean newNamePermission = !user.hasNamePermission();
		boolean newEmailPermission = !user.hasEmailPermission();
		userSession.updateUser(newName, newEmail, newOrganization, newAddress, 
				newPassword, newNamePermission, newEmailPermission, newLanguage);
		assertEquals(newAddress, userSession.getAddress());
		assertEquals(newEmail, userSession.getEmail());
		assertEquals(newName, userSession.getName());
		assertEquals(newOrganization, userSession.getOrganization());
		assertEquals(user.isAdmin(), userSession.isAdmin());
		assertEquals(newEmailPermission, userSession.hasEmailPermission());
		assertEquals(newNamePermission, userSession.hasNamePermission());
		assertEquals(newLanguage, userSession.getLanguage());
		UserSession newUserSession = new UserSession(
				user.getUsername(), newPassword, TestHelper.getTestServletConfig());
		newUserSession.login();
		assertEquals(newAddress, newUserSession.getAddress());
		assertEquals(newEmail, newUserSession.getEmail());
		assertEquals(newName, newUserSession.getName());
		assertEquals(newOrganization, newUserSession.getOrganization());
		assertEquals(user.isAdmin(), newUserSession.isAdmin());
		assertEquals(newEmailPermission, newUserSession.hasEmailPermission());
		assertEquals(newNamePermission, newUserSession.hasNamePermission());
		assertEquals(newLanguage, newUserSession.getLanguage());
	}

	@Test
	public void testResetAnswer() throws SQLException, QuestionException, SurveyResponseException {
		SurveyResponse response = new SurveyResponse(specVersion, language);
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(language, YesNo.No);
		YesNoAnswer s1q2answer = new YesNoAnswer(language, YesNo.NotAnswered);

		responses.put(s1q1Number, s1q1answer);
		responses.put(s1q2Number, s1q2answer);
		response.setResponses(responses);
		response.setSubmitted(false);
		response.setSurvey(survey);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response, language);
		
		UserSession userSession = new UserSession(
				user.getUsername(), USER_PASSWORD, TestHelper.getTestServletConfig());
		assertTrue(userSession.login());
		
		SurveyResponse result = userSession.getSurveyResponse();
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
		
		userSession.resetAnswers(primarySpecVersion);
		result = userSession.getSurveyResponse();
		assertEquals(user.getUsername(), result.getResponder().getUsername());
		assertEquals(specVersion, result.getSurvey().getSpecVersion());
		resultResponses = result.getResponses();
		assertEquals(0, resultResponses.size());
		
		userSession.resetAnswers(primarySpecVersion2);
		result = userSession.getSurveyResponse();
		assertEquals(user.getUsername(), result.getResponder().getUsername());
		assertEquals(specVersion2, result.getSurvey().getSpecVersion());
		resultResponses = result.getResponses();
		assertEquals(0, resultResponses.size());
		
		userSession.logout();
		
		// Check to make sure it persists
		UserSession newUserSession = new UserSession(
				user.getUsername(), USER_PASSWORD, TestHelper.getTestServletConfig());
		newUserSession.login();
		assertTrue(newUserSession.isLoggedIn());
		result = newUserSession.getSurveyResponse();
		assertEquals(user.getUsername(), result.getResponder().getUsername());
		newUserSession.resetAnswers(primarySpecVersion2);
		result = newUserSession.getSurveyResponse();
		assertEquals(user.getUsername(), result.getResponder().getUsername());
		assertEquals(specVersion2, result.getSurvey().getSpecVersion());
		resultResponses = result.getResponses();
		assertEquals(0, resultResponses.size());
	}
	
	@Test
	public void testGetSupportedSpecVersions() throws SQLException {
		// Note - this got moved to the Certification Servlet - but keeping the unit test
		List<String> result = CertificationServlet.getSupportedSpecVersions(TestHelper.getTestServletConfig());
		assertEquals(2, result.size());
		assertEquals(primarySpecVersion, result.get(0));
		assertEquals(primarySpecVersion2, result.get(1));
	}
	
	@Test
	public void testGetSurveyResponseSpecVersions() throws SQLException, QuestionException, SurveyResponseException {
		SurveyResponse response = new SurveyResponse(specVersion, language);
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();

		response.setResponses(responses);
		response.setSubmitted(false);
		response.setSurvey(survey);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response, language);

		SurveyResponse response2 = new SurveyResponse(specVersion2, language);
		response2.setResponder(user);

		response2.setResponses(responses);
		response2.setSubmitted(false);
		response2.setSurvey(survey2);
		dao.addSurveyResponse(response2, language);
		
		UserSession userSession = new UserSession(
				user.getUsername(), USER_PASSWORD, TestHelper.getTestServletConfig());
		assertTrue(userSession.login());
		assertTrue(userSession.isLoggedIn());
		List<String> result = userSession.getSurveyResponseSpecVersions();
		assertEquals(2, result.size());
		assertEquals(primarySpecVersion, result.get(0));
		assertEquals(primarySpecVersion2, result.get(1));
		// the current spec version should default to the largest value
		assertEquals(specVersion2, userSession.getSurveyResponse().getSpecVersion());
		userSession.logout();
	}
	
	@Test
	public void testSetCurrentSurveyResponse() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse(specVersion, language);
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(language, YesNo.No);
		responses.put(s1q1Number, s1q1answer);
		response.setResponses(responses);
		response.setSubmitted(false);
		response.setSurvey(survey);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response, language);
		
		UserSession userSession = new UserSession(
				user.getUsername(), USER_PASSWORD, TestHelper.getTestServletConfig());
		assertTrue(userSession.login());
		assertTrue(userSession.isLoggedIn());
		SurveyResponse result = userSession.getSurveyResponse();
		assertEquals(specVersion, result.getSpecVersion());
		
		userSession.setCurrentSurveyResponse(primarySpecVersion2, true);
		result = userSession.getSurveyResponse();
		assertEquals(specVersion2, result.getSpecVersion());
		
		userSession.setCurrentSurveyResponse(primarySpecVersion, false);
		result = userSession.getSurveyResponse();
		assertEquals(specVersion, result.getSpecVersion());
		assertEquals(1,result.getResponses().size());
		assertEquals(s1q1answer, result.getResponses().get(s1q1Number));

		userSession.logout();
	}
	
	@Test
	public void testSetLanguage() throws SQLException, SurveyResponseException, QuestionException {
		SurveyResponse response = new SurveyResponse(specVersion, language);
		response.setResponder(user);
		Map<String, Answer> responses = new HashMap<String, Answer>();
		YesNoAnswer s1q1answer = new YesNoAnswer(language, YesNo.No);
		responses.put(s1q1Number, s1q1answer);
		response.setResponses(responses);
		response.setSubmitted(false);
		response.setSurvey(survey);
		SurveyResponseDao dao = new SurveyResponseDao(con);
		dao.addSurveyResponse(response, language);
		
		UserSession userSession = new UserSession(
				user.getUsername(), USER_PASSWORD, TestHelper.getTestServletConfig());
		assertTrue(userSession.login());
		assertTrue(userSession.isLoggedIn());
		userSession.setCurrentSurveyResponse(primarySpecVersion2, true);
		List<SurveyResponse> result = userSession.getAllResponses();
		assertEquals(2, result.size());
		for (SurveyResponse res:result) {
			assertEquals(language, res.getLanguage());
		}
		assertEquals(language, userSession.getLanguage());
		
		userSession.setLanguage(language2);
		result = userSession.getAllResponses();
		assertEquals(2, result.size());
		for (SurveyResponse res:result) {
			assertEquals(language2, res.getLanguage());
		}

		userSession.logout();
	}
}
