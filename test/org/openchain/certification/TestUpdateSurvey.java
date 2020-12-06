/**
 * Copyright (c) 2019 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
*/
package org.openchain.certification;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openchain.certification.dbdao.SurveyDbDao;
import org.openchain.certification.model.Question;
import org.openchain.certification.model.QuestionException;
import org.openchain.certification.model.Survey;
import org.openchain.certification.model.SurveyResponseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author gary
 *
 */
public class TestUpdateSurvey {

	private static final String TEST_DIRECTORY = "testresources";
	private static final String VALID_DIRECTORY_PATH = TEST_DIRECTORY + File.separator + "multiplevalidquestionnaires";
	private static final String DUP_LOCAL_DIRECTORY_PATH = TEST_DIRECTORY + File.separator + "duplicatedlocale";
	private static final String MISSMATCH_DIRECTORY_PATH = TEST_DIRECTORY + File.separator + "questionsmissmatched";
	private static final String VALID_FILE_PATH = TEST_DIRECTORY + File.separator + "valid_questionnaire.json";
	private static final String LANGUAGE = "en";
	private static final String NEW_VERSION = "2.0.0";
	private static final int NUM_SECTIONS = 6;
	private static final String VALID_UPDATE_PATH = TEST_DIRECTORY + File.separator + "updated_questionnaire.json";
	private static final String ORIG_QUESTION_1 = "Do you have a documented policy that governs open source license compliance of the Supplied Software distribution (e.g., via training, internal wiki, or other practical communication method)?";
	private static final String UPDATED_QUESTION_1 = "Test updated question?";
	private static final String UPDATED_QUESTION_NUM_1 = "1.a";
	private static final String UPDATED_SECTION_NAME = "G1";
	private static final String UPDATED_SECTION_TITLE = "G1: You Don't Know Your Open Source Responsibilities";
	private static final String INVALID_UPDATE_LESS_QUESTIONS_PATH = TEST_DIRECTORY + File.separator + "oneless_questionnaire.json";
	private static final String INVALID_DUP_QUESTION_PATH = TEST_DIRECTORY + File.separator + "dup_question_questionnaire.json";
	private static final int NUM_SECTION1_QUESTIONS = 14;
	Connection con;
	Gson gson;
	SurveyDbDao surveyDao;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		con = TestHelper.getConnection();
		TestHelper.truncateDatabase(con);
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Question.class, new QuestionJsonDeserializer());
		gson = builder.create();
		surveyDao = new SurveyDbDao(con);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		con.close();
	}

	@Test
	public void testValidUpload() throws SQLException, QuestionException, SurveyResponseException {
		File jsonFile = new File(VALID_FILE_PATH);
		SurveyUpdateResult stats = new SurveyUpdateResult();
		CertificationServlet.updateSurveyFromFile(jsonFile, LANGUAGE, true, stats, surveyDao, gson);
		assertEquals(1, stats.getVersionsAdded().size());
		assertTrue(stats.getVersionsAdded().get(0).contains(NEW_VERSION));
		assertEquals(0, stats.getVersionsUpdated().size());
		assertEquals(0, stats.getWarnings().size());
		List<String> surveyVersions = surveyDao.getSurveyVesions();
		assertEquals(1, surveyVersions.size());
		assertEquals(NEW_VERSION, surveyVersions.get(0));
		Survey survey = surveyDao.getSurvey(NEW_VERSION, LANGUAGE);
		assertEquals(NUM_SECTIONS, survey.getSections().size());
	}
	
	@Test
	public void testInValidDupQuestionUpload() throws SQLException, QuestionException, SurveyResponseException {
		File jsonFile = new File(INVALID_DUP_QUESTION_PATH);
		SurveyUpdateResult stats = new SurveyUpdateResult();
		CertificationServlet.updateSurveyFromFile(jsonFile, LANGUAGE, true, stats, surveyDao, gson);
		assertEquals(0, stats.getVersionsAdded().size());
		assertEquals(0, stats.getVersionsUpdated().size());
		assertEquals(1, stats.getWarnings().size());
		assertTrue(stats.getWarnings().get(0).contains(NEW_VERSION));
		List<String> surveyVersions = surveyDao.getSurveyVesions();
		assertEquals(0, surveyVersions.size());
	}
	
	@Test
	public void testInValidDupQuestionUpdate() throws SQLException, QuestionException, SurveyResponseException {
		File jsonFile = new File(VALID_FILE_PATH);
		SurveyUpdateResult stats = new SurveyUpdateResult();
		CertificationServlet.updateSurveyFromFile(jsonFile, LANGUAGE, true, stats, surveyDao, gson);
		File updateFile = new File(INVALID_DUP_QUESTION_PATH);
		stats = new SurveyUpdateResult();
		CertificationServlet.updateSurveyFromFile(updateFile, LANGUAGE, true, stats, surveyDao, gson);
		assertEquals(0, stats.getVersionsAdded().size());
		assertEquals(0, stats.getVersionsUpdated().size());
		assertEquals(1, stats.getWarnings().size());
		assertTrue(stats.getWarnings().get(0).contains(NEW_VERSION));
		List<String> surveyVersions = surveyDao.getSurveyVesions();
		assertEquals(1, surveyVersions.size());
	}
	
	@Test
	public void testValidUpdate() throws SQLException, QuestionException, SurveyResponseException {
		File jsonFile = new File(VALID_FILE_PATH);
		SurveyUpdateResult stats = new SurveyUpdateResult();
		CertificationServlet.updateSurveyFromFile(jsonFile, LANGUAGE, true, stats, surveyDao, gson);
		Survey survey = surveyDao.getSurvey(NEW_VERSION, LANGUAGE);
		assertEquals(ORIG_QUESTION_1, survey.getSections().get(0).getQuestions().get(0).getQuestion());
		File updateFile = new File(VALID_UPDATE_PATH);
		stats = new SurveyUpdateResult();
		CertificationServlet.updateSurveyFromFile(updateFile, LANGUAGE, true, stats, surveyDao, gson);
		assertEquals(0, stats.getVersionsAdded().size());
		assertEquals(1, stats.getVersionsUpdated().size());
		assertTrue(stats.getVersionsUpdated().get(0).contains(NEW_VERSION));
		assertTrue(stats.getVersionsUpdated().get(0).contains(UPDATED_QUESTION_NUM_1));
		assertTrue(stats.getVersionsUpdated().get(0).contains(UPDATED_SECTION_NAME));
		assertEquals(0, stats.getWarnings().size());
		List<String> surveyVersions = surveyDao.getSurveyVesions();
		assertEquals(1, surveyVersions.size());
		assertEquals(NEW_VERSION, surveyVersions.get(0));
		Survey updatedSurvey = surveyDao.getSurvey(NEW_VERSION, LANGUAGE);
		assertEquals(UPDATED_QUESTION_1, updatedSurvey.getSections().get(0).getQuestions().get(0).getQuestion());
		assertEquals(UPDATED_SECTION_TITLE, updatedSurvey.getSections().get(0).getTitle());
	}
	
	@Test
	public void testUpdateLessQuestions() throws SQLException, QuestionException, SurveyResponseException {
		File jsonFile = new File(VALID_FILE_PATH);
		SurveyUpdateResult stats = new SurveyUpdateResult();
		CertificationServlet.updateSurveyFromFile(jsonFile, LANGUAGE, true, stats, surveyDao, gson);
		Survey survey = surveyDao.getSurvey(NEW_VERSION, LANGUAGE);
		assertEquals(NUM_SECTION1_QUESTIONS, survey.getSections().get(0).getQuestions().size());
		File updateFile = new File(INVALID_UPDATE_LESS_QUESTIONS_PATH);
		stats = new SurveyUpdateResult();
		CertificationServlet.updateSurveyFromFile(updateFile, LANGUAGE, true, stats, surveyDao, gson);
		assertEquals(0, stats.getVersionsAdded().size());
		assertEquals(0, stats.getVersionsUpdated().size());
		assertEquals(1, stats.getWarnings().size());
		List<String> surveyVersions = surveyDao.getSurveyVesions();
		assertEquals(1, surveyVersions.size());
		assertEquals(NEW_VERSION, surveyVersions.get(0));
		survey = surveyDao.getSurvey(NEW_VERSION, LANGUAGE);
		assertEquals(NUM_SECTION1_QUESTIONS, survey.getSections().get(0).getQuestions().size());
	}
	
	@Test
	public void testUpdateMultipleFiles() throws SQLException, QuestionException, SurveyResponseException {
		File dir = new File(VALID_DIRECTORY_PATH);
		SurveyUpdateResult stats = new SurveyUpdateResult();
		for (File file:dir.listFiles()) {
			CertificationServlet.updateSurveyFromFile(file, LANGUAGE, false, stats, surveyDao, gson);
		}
		stats.verify(LANGUAGE);
		assertEquals(0, stats.getWarnings().size());
	}
	
	@Test
	public void testUpdateDupLocale() throws SQLException, QuestionException, SurveyResponseException {
		File dir = new File(DUP_LOCAL_DIRECTORY_PATH);
		SurveyUpdateResult stats = new SurveyUpdateResult();
		for (File file:dir.listFiles()) {
			CertificationServlet.updateSurveyFromFile(file, LANGUAGE, false, stats, surveyDao, gson);
		}
		stats.verify(LANGUAGE);
		assertEquals(1, stats.getWarnings().size());
		assertTrue(stats.getWarnings().get(0).contains("Duplicate"));
		assertTrue(stats.getWarnings().get(0).contains("2.0.0:ja"));
	}

	@Test
	public void testUpdateLocaleQuestionMissmatch() throws SQLException, QuestionException, SurveyResponseException {
		File dir = new File(MISSMATCH_DIRECTORY_PATH);
		SurveyUpdateResult stats = new SurveyUpdateResult();
		for (File file:dir.listFiles()) {
			CertificationServlet.updateSurveyFromFile(file, LANGUAGE, false, stats, surveyDao, gson);
		}
		stats.verify(LANGUAGE);
		assertEquals(1, stats.getWarnings().size());
		assertTrue(stats.getWarnings().get(0).contains("do not match"));
		assertTrue(stats.getWarnings().get(0).contains("2.0.0"));
	}
}
