/**
 * Copyright (c) 2016 Source Auditor Inc.
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
package org.openchain.certification.dbdao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openchain.certification.model.QuestionException;
import org.openchain.certification.model.QuestionTypeException;
import org.openchain.certification.model.SubQuestion;
import org.openchain.certification.model.SubQuestionAnswers;
import org.openchain.certification.model.Survey;
import org.openchain.certification.model.SurveyResponse;
import org.openchain.certification.model.SurveyResponseException;
import org.openchain.certification.model.User;
import org.openchain.certification.model.Answer;
import org.openchain.certification.model.YesNoAnswer;
import org.openchain.certification.model.YesNoAnswerWithEvidence;
import org.openchain.certification.model.YesNoNotApplicableQuestion;
import org.openchain.certification.model.YesNoQuestion;
import org.openchain.certification.model.YesNoQuestionWithEvidence;
import org.openchain.certification.model.YesNoQuestion.YesNo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage data access for Survey Responses
 * NOTE: There is an assumption that there is only one response per user.  If this
 * assumption is changed, some code redesign will be in order
 * @author Gary O'Neall
 *
 */
public class SurveyResponseDao {
	static final Logger logger = LoggerFactory.getLogger(SurveyResponseDao.class);
	Connection con;
	private PreparedStatement getUserQuery;
	private PreparedStatement getLatestSpecVersionForUserQuery;
	private PreparedStatement getAnswersQuery;
	private PreparedStatement getUserIdQuery;
	private PreparedStatement addSurveyResponseQuery;
	private PreparedStatement addAnswerQuery;
	private PreparedStatement updateAnswerQuery;
	private PreparedStatement deleteAnswerQuery;
	private PreparedStatement setSubmittedQuery;
	private PreparedStatement setApprovedQuery;
	private PreparedStatement setRejectedQuery;
	private PreparedStatement getQuestionNameQuery;
	private PreparedStatement getUsersWithResponsesQuery;
	private PreparedStatement getResponsesForUserQuery;
	private PreparedStatement setApprovedIdsQuery;
	private PreparedStatement setRejectedIdsQuery;
	private PreparedStatement getStatusQuery;
	private PreparedStatement deleteAllAnswersForResponseQuery;
	private PreparedStatement deleteAllResponsesForSpecVersionQuery;
	
	public SurveyResponseDao(Connection con) throws SQLException {
		this.con = con;
		this.con.setAutoCommit(false);
		getUserQuery = con.prepareStatement("select password_token, name, address, email," + //$NON-NLS-1$
				"verified, passwordReset, admin, verificationExpirationDate," + //$NON-NLS-1$
				" uuid, organization, id, name_permission, email_permission from openchain_user where username=?", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		getLatestSpecVersionForUserQuery = con.prepareStatement("select max(version) from survey_response join " + //$NON-NLS-1$
				"spec on survey_response.spec_version=spec.id where user_id=?", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		getAnswersQuery = con.prepareStatement("select number, answer, evidence, type, question_id, subquestion_of from " + //$NON-NLS-1$
				"answer join survey_response on answer.response_id=survey_response.id " + //$NON-NLS-1$
				"join question on answer.question_id=question.id " + //$NON-NLS-1$
				"join spec on survey_response.spec_version=spec.id " + //$NON-NLS-1$
				"where user_id=? and version=? order by number", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		getUserIdQuery = con.prepareStatement("select id from openchain_user where username=?", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		addSurveyResponseQuery = con.prepareStatement("insert into survey_response (user_id, spec_version, submitted, approved, rejected) values (?,?,?,?,?)", //$NON-NLS-1$
				Statement.RETURN_GENERATED_KEYS);
		deleteAnswerQuery = con.prepareStatement("delete from answer where response_id in (select id from " + //$NON-NLS-1$
				"survey_response where user_id=? and spec_version=?) and question_id in (select id from question where number=? and section_id in " + //$NON-NLS-1$
				"(select id from section where spec_version=?))", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		updateAnswerQuery = con.prepareStatement("update answer set answer=?, evidence=? where response_id in (select id from " + //$NON-NLS-1$
				"survey_response where user_id=? and spec_version=?) and question_id in (select id from question where number=? and section_id in " + //$NON-NLS-1$
				"(select id from section where spec_version=?))", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		addAnswerQuery = con.prepareStatement("insert into answer (response_id, question_id, answer, evidence) values((select id from " + //$NON-NLS-1$
				"survey_response where user_id=? and spec_version=?), (select id from question where number=? and section_id in " + //$NON-NLS-1$
				"(select id from section where spec_version=?)), ?, ?)", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		setSubmittedQuery = con.prepareStatement("update survey_response set submitted=? where " + //$NON-NLS-1$
				"user_id =(select id from openchain_user where username=?) and spec_version in (select id from spec where version=?)", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		getStatusQuery = con.prepareStatement("select survey_response.id as id, approved, rejected, submitted from survey_response " + //$NON-NLS-1$
				"join spec on survey_response.spec_version=spec.id " + //$NON-NLS-1$
				" where user_id=? and version=?", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		setApprovedQuery = con.prepareStatement("update survey_response set approved=? where " + //$NON-NLS-1$
				"user_id =(select id from openchain_user where username=?) and spec_version in (select id from spec where version=?)", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		setApprovedIdsQuery = con.prepareStatement("update survey_response set approved=? where id=?", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		setRejectedQuery = con.prepareStatement("update survey_response set rejected=? where " + //$NON-NLS-1$
				"user_id =(select id from openchain_user where username=?) and spec_version in (select id from spec where version=?)", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		setRejectedIdsQuery = con.prepareStatement("update survey_response set rejected=? where id=?", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		getQuestionNameQuery = con.prepareStatement("select number from question where id=?", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		getUsersWithResponsesQuery = con.prepareStatement("select username, password_token, name, address, email," + //$NON-NLS-1$
				"verified, passwordReset, admin, verificationExpirationDate," + //$NON-NLS-1$
				" uuid, organization, openchain_user.id as id, submitted, approved, rejected, version, " + //$NON-NLS-1$
				"survey_response.id as responseid, name_permission, email_permission from survey_response join openchain_user " + //$NON-NLS-1$
				"on survey_response.user_id=openchain_user.id join spec on survey_response.spec_version=spec.id" + //$NON-NLS-1$
				" order by username asc", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		getResponsesForUserQuery = con.prepareStatement("select username, password_token, name, address, email," + //$NON-NLS-1$
				"verified, passwordReset, admin, verificationExpirationDate," + //$NON-NLS-1$
				" uuid, organization, openchain_user.id as id, submitted, approved, rejected, version, " + //$NON-NLS-1$
				"survey_response.id as responseid, name_permission, email_permission from survey_response join openchain_user " + //$NON-NLS-1$
				"on survey_response.user_id=openchain_user.id join spec on survey_response.spec_version=spec.id" + //$NON-NLS-1$
				" where username = ?" +	" order by username asc", //$NON-NLS-1$ //$NON-NLS-2$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		deleteAllAnswersForResponseQuery = con.prepareStatement("delete from answer where response_id in (select id from " + //$NON-NLS-1$
				"survey_response where user_id=(select id from openchain_user where username=?) and spec_version in (select id from spec where version=?))", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		deleteAllResponsesForSpecVersionQuery = con.prepareStatement("delete from survey_response where " + //$NON-NLS-1$
				"user_id=(select id from openchain_user where username=?) and spec_version in (select id from spec where version=?)", //$NON-NLS-1$
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}

	/**
	 * Get a survey response for a specific user
	 * @param username username for the survey response
	 * @param specVersion version of the spec for the survey response.  If null, will use the latest spec version available
	 * @param language tag in IETF RFC 5646 format
	 * @return
	 * @throws QuestionTypeException 
	 */
	public synchronized SurveyResponse getSurveyResponse(String username, String specVersion, String language) throws SQLException, QuestionException, SurveyResponseException {
		ResultSet result = null;
		try {
			getUserQuery.setString(1, username);
			result = getUserQuery.executeQuery();
			if (!result.next()) {
				return null;	// No user, no user response
			}
			User user = new User();
			user.setAddress(result.getString("address")); //$NON-NLS-1$
			user.setAdmin(result.getBoolean("admin")); //$NON-NLS-1$
			user.setEmail(result.getString("email")); //$NON-NLS-1$
			user.setName(result.getString("name")); //$NON-NLS-1$
			user.setPasswordReset(result.getBoolean("passwordReset")); //$NON-NLS-1$
			user.setPasswordToken(result.getString("password_token")); //$NON-NLS-1$
			user.setUsername(username);
			user.setUuid(result.getString("uuid")); //$NON-NLS-1$
			user.setVerificationExpirationDate(result.getDate("verificationExpirationDate")); //$NON-NLS-1$
			user.setVerified(result.getBoolean("verified")); //$NON-NLS-1$
			user.setOrganization(result.getString("organization")); //$NON-NLS-1$
			user.setNamePermission(result.getBoolean("name_permission")); //$NON-NLS-1$
			user.setEmailPermission(result.getBoolean("email_permission")); //$NON-NLS-1$
			long userId = result.getLong("id"); //$NON-NLS-1$
			if (specVersion == null) {
				// get the latest spec version that is stored in the database
				result.close();
				getLatestSpecVersionForUserQuery.setLong(1, userId);
				result = getLatestSpecVersionForUserQuery.executeQuery();
				if (!result.next()) {
					logger.info("No survey results found for username "+username); //$NON-NLS-1$
					return null;	// no survey responses for this user
				}
				specVersion = result.getString(1);
				if (specVersion == null || specVersion.trim().isEmpty()) {
					logger.info("No survey results found for username "+username); //$NON-NLS-1$
					return null;	// no survey responses for this user
				}
			}
			Survey survey = SurveyDbDao.getSurvey(con, specVersion, language);
			Map<String, Answer>answers = getAnswers(userId, specVersion, language);			
			SurveyResponse retval = new SurveyResponse(specVersion, language);
			retval.setResponder(user);
			retval.setResponses(answers);
			retval.setSurvey(survey);
			getStatusQuery.setLong(1, userId);
			getStatusQuery.setString(2, specVersion);
			result.close();
			result = getStatusQuery.executeQuery();
			if (!result.next()) {
				logger.warn("Failed to obtain status"); //$NON-NLS-1$
				return null;
			}
			retval.setSubmitted(result.getBoolean("submitted")); //$NON-NLS-1$
			retval.setApproved(result.getBoolean("approved")); //$NON-NLS-1$
			retval.setRejected(result.getBoolean("rejected")); //$NON-NLS-1$
			retval.setId(String.valueOf(result.getLong("id"))); //$NON-NLS-1$
			return retval;
		} catch (QuestionTypeException e) {
			logger.error("Invalid question type getting survey response for user "+username,e); //$NON-NLS-1$
			throw(e);
		} catch (SurveyResponseException e) {
			logger.error("Invalid survey response getting survey response for user "+username,e); //$NON-NLS-1$
			throw(e);
		} catch (SQLException e) {
			logger.error("SQL Exception getting survey response for user "+username,e); //$NON-NLS-1$
			throw(e);
		} finally {
			if (result != null) {
				result.close();
				con.commit();
			}
		}
	}
	
	/**
	 * @param language tag in IETF RFC 5646 format
	 * @return All survey responses
	 * @throws SQLException 
	 * @throws QuestionException 
	 * @throws SurveyResponseException 
	 */
	public synchronized List<SurveyResponse> getSurveyResponses(String language) throws SQLException, SurveyResponseException, QuestionException {
		ResultSet result = null;
		List<SurveyResponse> retval = new ArrayList<SurveyResponse>();
		Map<String, Survey> surveys = new HashMap<String, Survey>();	// Cache of spec version to survey
		try {
			result = getUsersWithResponsesQuery.executeQuery();
			while (result.next()) {
				String specVersion = result.getString("version"); //$NON-NLS-1$
				SurveyResponse response = new SurveyResponse(specVersion, language);
				User user = new User();
				user.setAddress(result.getString("address")); //$NON-NLS-1$
				user.setAdmin(result.getBoolean("admin")); //$NON-NLS-1$
				user.setEmail(result.getString("email")); //$NON-NLS-1$
				user.setName(result.getString("name")); //$NON-NLS-1$
				user.setPasswordReset(result.getBoolean("passwordReset")); //$NON-NLS-1$
				user.setPasswordToken(result.getString("password_token")); //$NON-NLS-1$
				user.setUsername(result.getString("username")); //$NON-NLS-1$
				user.setUuid(result.getString("uuid")); //$NON-NLS-1$
				user.setVerificationExpirationDate(result.getDate("verificationExpirationDate")); //$NON-NLS-1$
				user.setVerified(result.getBoolean("verified")); //$NON-NLS-1$
				user.setOrganization(result.getString("organization")); //$NON-NLS-1$
				user.setNamePermission(result.getBoolean("name_permission")); //$NON-NLS-1$
				user.setEmailPermission(result.getBoolean("email_permission")); //$NON-NLS-1$
				long userId = result.getLong("id"); //$NON-NLS-1$
				response.setResponder(user);
				response.setSubmitted(result.getBoolean("submitted")); //$NON-NLS-1$
				response.setApproved(result.getBoolean("approved")); //$NON-NLS-1$
				response.setRejected(result.getBoolean("rejected")); //$NON-NLS-1$
				response.setId(String.valueOf(result.getLong("responseid"))); //$NON-NLS-1$
				Survey survey = surveys.get(specVersion);
				if (survey == null) {
					survey = SurveyDbDao.getSurvey(con, specVersion, language);
					surveys.put(specVersion, survey);
				}
				response.setSurvey(survey);
				response.setResponses(getAnswers(userId, specVersion, language));
				retval.add(response);
			}
			return retval;
		} catch (SQLException e) {
			logger.error("SQL error getting users with responses",e); //$NON-NLS-1$
			throw(e);
		} finally {
			if (result != null) {
				result.close();
				this.con.commit();
			}
		}
	}
	
	/**
	 * Get all answers for a given user ID and specVersion
	 * @param userId ID for the user
	 * @param specVersion Specification version
	 * @param language tag in IETF RFC 5646 format
	 * @return
	 * @throws SQLException
	 * @throws QuestionTypeException
	 * @throws SurveyResponseException
	 */
	private Map<String, Answer> getAnswers(long userId, String specVersion, String language) throws SQLException, QuestionTypeException, SurveyResponseException {
		ResultSet result = null;
		Map<String, Answer> responses = new HashMap<String, Answer>();
		try {
			getAnswersQuery.setLong(1,userId);
			getAnswersQuery.setString(2,specVersion);
			result = getAnswersQuery.executeQuery();	
			Map<Long,SubQuestionAnswers> questionNumSubQuestionAnswer = new HashMap<Long,SubQuestionAnswers>();
			while (result.next()) {
				String questionNumber = result.getString("number"); //$NON-NLS-1$
				String type = result.getString("type"); //$NON-NLS-1$
				long questionId = result.getLong("question_id"); //$NON-NLS-1$
				long subQuestionOfId = result.getLong("subquestion_of"); //$NON-NLS-1$
				Answer answer;
				if (type == null) {
					throw(new QuestionTypeException("No question type stored in the database")); //$NON-NLS-1$
				}
				if (type.equals(YesNoQuestion.TYPE_NAME)) {
					answer = new YesNoAnswer(language, YesNo.valueOf(result.getString("answer"))); //$NON-NLS-1$
				} else if (type.equals(YesNoQuestionWithEvidence.TYPE_NAME)) {
					answer = new YesNoAnswerWithEvidence(language, YesNo.valueOf(result.getString("answer")), //$NON-NLS-1$
							result.getString("evidence")); //$NON-NLS-1$
				} else if (type.equals(YesNoNotApplicableQuestion.TYPE_NAME)) {
					answer = new YesNoAnswer(language, YesNo.valueOf(result.getString("answer"))); //$NON-NLS-1$
				} else if (type.equals(SubQuestion.TYPE_NAME)) {
					answer = questionNumSubQuestionAnswer.get(questionId);
					if (answer == null) {
						answer = new SubQuestionAnswers(language);
						questionNumSubQuestionAnswer.put(questionId, (SubQuestionAnswers)answer);
					}
				} else {
					throw(new QuestionTypeException("Unknown question type in database: "+type)); //$NON-NLS-1$
				}
				if (subQuestionOfId > 0) {
					SubQuestionAnswers parentAnswer = questionNumSubQuestionAnswer.get(subQuestionOfId);
					if (parentAnswer == null) {
						parentAnswer = new SubQuestionAnswers(language);
						questionNumSubQuestionAnswer.put(subQuestionOfId, parentAnswer);
						String parentQuestionNumber = getQuestionNumber(subQuestionOfId);
						responses.put(parentQuestionNumber, parentAnswer);
					}
					parentAnswer.addSubAnswer(questionNumber, answer);
				}
				responses.put(questionNumber, answer);
			}
			return responses;
		} finally {
			if (result != null) {
				result.close();
			}
		}
	}
	
	private String getQuestionNumber(long questionId) throws SQLException {
		getQuestionNameQuery.setLong(1, questionId);
		ResultSet result = null;
		try {
			result = getQuestionNameQuery.executeQuery();
			if (result.next()) {
				return result.getString(1);
			} else {
				return null;
			}
		} finally {
			if (result != null) {
				result.close();
			}
		}
		
		
	}

	/**
	 * @return the latest (most recent) version of the spec
	 * @throws SQLException
	 * @throws SurveyResponseException
	 */
	public String getLatestSpecVersion() throws SQLException, SurveyResponseException {
		Statement stmt = null;
		ResultSet result = null;
		
		try {
			stmt = con.createStatement();
			result = stmt.executeQuery("select max(version) from spec"); //$NON-NLS-1$
			if (!result.next()) {
				throw new SurveyResponseException("No specs found in database"); //$NON-NLS-1$
			}
			return result.getString(1);
		} finally {
			if (result != null) {
				result.close();
			}
			if (stmt != null) {
				stmt.close();
				con.commit();
			}
		}
	}
	
	/**
	 * Add a new survey response.  A response must not already exist for this user
	 * and for this version of the survey.
	 * @param response
	 * @throws SurveyResponseException 
	 * @throws QuestionTypeException 
	 */
	public synchronized void addSurveyResponse(SurveyResponse response, String language) throws SQLException, SurveyResponseException, QuestionTypeException {
		Savepoint save = con.setSavepoint();
		ResultSet result = null;
		try {
			long userId = getUserId(response.getResponder().getUsername());
			long versionId = SurveyDbDao.getSpecId(con, response.getSpecVersion(), User.DEFAULT_LANGUAGE, true);	// We always use the default language to prevent duplicates
			getStatusQuery.setLong(1, userId);
			getStatusQuery.setString(2, response.getSpecVersion());
			result = getStatusQuery.executeQuery();
			try {
				if (result.next()) {
					logger.error("Survey response already exists for user "+response.getResponder().getUsername()+" and spec version "+response.getSpecVersion());
					throw new SurveyResponseException("Survey response already exists for user "+response.getResponder().getUsername()+" and spec version "+response.getSpecVersion());
				}
			} finally {
				result.close();
				result = null;
			}
			addSurveyResponseQuery.setLong(1, userId);
			addSurveyResponseQuery.setLong(2, versionId);
			addSurveyResponseQuery.setBoolean(3, response.isSubmitted());
			addSurveyResponseQuery.setBoolean(4, response.isApproved());
			addSurveyResponseQuery.setBoolean(5, response.isRejected());
			int count = addSurveyResponseQuery.executeUpdate();
			if (count != 1) {
				logger.error("Unexpected count for adding survey response.  Expected one, returned "+String.valueOf(count)); //$NON-NLS-1$
			}
			result = addSurveyResponseQuery.getGeneratedKeys();
			if (!result.next()) {
				throw new SQLException("No key generated for add survey response"); //$NON-NLS-1$
			}
			response.setId(String.valueOf(result.getLong(1)));
			_updateSurveyResponseAnswers(response, userId, versionId, language);
			
			// Fill in the ID for the survey response
			
		} catch(SQLException ex) {
			logger.error("SQL exception updating survey response answers for "+response.getResponder().getUsername(),ex); //$NON-NLS-1$
			try {
				con.rollback(save);
			} catch (SQLException ex2) {
				logger.error("Error rolling back transaction",ex2); //$NON-NLS-1$
			}
			throw(ex);
		} finally {
			if (result != null) {
				result.close();
			}
			if (save != null) {
				con.commit();
			}
		}
	}
	
	public synchronized void setSubmitted(String userName, String specVersion, boolean submitted) throws SQLException {
		Savepoint save = con.setSavepoint();
		try {
			setSubmittedQuery.setBoolean(1, submitted);
			setSubmittedQuery.setString(2, userName);
			setSubmittedQuery.setString(3, specVersion);
			int count = setSubmittedQuery.executeUpdate();
			if (count != 1) {
				logger.warn("Unexpected count on setting submitted.  Expected 1, found "+String.valueOf(count)); //$NON-NLS-1$
			}
		} finally {
			if (save != null) {
				con.commit();
			}
		}
	}
	
	public synchronized void setApproved(String userName, String specVersion, boolean approved) throws SQLException {
		Savepoint save = con.setSavepoint();
		try {
			setApprovedQuery.setBoolean(1, approved);
			setApprovedQuery.setString(2, userName);
			setApprovedQuery.setString(3, specVersion);
			int count = setApprovedQuery.executeUpdate();
			if (count != 1) {
				logger.warn("Unexpected count on setting approved.  Expected 1, found "+String.valueOf(count)); //$NON-NLS-1$
			}
		} finally {
			if (save != null) {
				con.commit();
			}
		}
	}
	
	public synchronized void setRejected(String userName, String specVersion, boolean rejected) throws SQLException {
		Savepoint save = con.setSavepoint();
		try {
			setRejectedQuery.setBoolean(1, rejected);
			setRejectedQuery.setString(2, userName);
			setRejectedQuery.setString(3, specVersion);
			int count = setRejectedQuery.executeUpdate();
			if (count != 1) {
				logger.warn("Unexpected count on setting submitted.  Expected 1, found "+String.valueOf(count)); //$NON-NLS-1$
			}
		} finally {
			if (save != null) {
				con.commit();
			}
		}
	}

	private long getUserId(String username) throws SQLException, SurveyResponseException {
		ResultSet result = null;
		try {
			getUserIdQuery.setString(1, username);
			result = getUserIdQuery.executeQuery();
			if (!result.next()) {
				logger.error("Missing user for username "+username); //$NON-NLS-1$
				throw(new SurveyResponseException("Missing user "+username)); //$NON-NLS-1$
			}
			return result.getLong(1);
		} finally {
			if (result != null) {
				result.close();
			}
		}	
	}
	
	/**
	 * Update the answers for a survey for an existing survey response
	 * @param response
	 * @param language tag in IETF RFC 5646 format
	 * @throws SQLException 
	 * @throws SurveyResponseException 
	 * @throws QuestionTypeException 
	 */
	public synchronized void updateSurveyResponseAnswers(SurveyResponse response, String language) throws SQLException, SurveyResponseException, QuestionTypeException {
		Savepoint save = con.setSavepoint();
		try {
			long userId = getUserId(response.getResponder().getUsername());
			long versionId = SurveyDbDao.getSpecId(con, response.getSpecVersion(), User.DEFAULT_LANGUAGE, true);  // Always use the default language to prevent duplicate surveys
			_updateSurveyResponseAnswers(response, userId, versionId, language);
		} catch(SQLException ex) {
			logger.error("SQL exception updating survey response answers for "+response.getResponder().getUsername(),ex); //$NON-NLS-1$
			try {
				con.rollback(save);
			} catch (SQLException ex2) {
				logger.error("Error rolling back transaction",ex2); //$NON-NLS-1$
			}
			throw(ex);
		} finally {
			if (save != null) {
				con.commit();
			}
		}
	}
	
	private void _updateSurveyResponseAnswers(SurveyResponse response, long userId, long versionId, String language) throws SQLException, QuestionTypeException, SurveyResponseException {
		if (response == null) {
			logger.warn("Null survey response passed to update server response answers"); //$NON-NLS-1$
			return;
		}
		if (response.getSurvey() == null) {
			logger.warn("No survey in survey response"); //$NON-NLS-1$
			return;
		}
		// First, verify the question numbers
		Set<String> numbers = response.getSurvey().getQuestionNumbers();
		try {
			Map<String, Answer> storedAnswers = getAnswers(userId, response.getSpecVersion(), language);
			this.addAnswerQuery.clearBatch();
			this.updateAnswerQuery.clearBatch();
			this.deleteAnswerQuery.clearBatch();
			int numAdds = 0;
			int numUpdates = 0;
			int numDeletes = 0;
			Iterator<Entry<String, Answer>> iter = response.getResponses().entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Answer> entry = iter.next();
				if (!numbers.contains(entry.getKey())) {
					logger.error("Attempting to update an answer for a question that does not exist.  Username="+ //$NON-NLS-1$
									response.getResponder().getUsername()+", specVersion="+response.getSpecVersion() + //$NON-NLS-1$
									"language: " + language + ", " +  //$NON-NLS-1$ //$NON-NLS-2$
									"question number: "+entry.getKey()); //$NON-NLS-1$
					
					throw(new SurveyResponseException("Can not update answers.  Question "+entry.getKey()+" does not exist.")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (storedAnswers.containsKey(entry.getKey())) {
					// already there
					if (entry.getKey() == null) {
						// Delete the entry
						this.deleteAnswerQuery.clearParameters();
						this.deleteAnswerQuery.setLong(1, userId);
						this.deleteAnswerQuery.setLong(2, versionId);
						this.deleteAnswerQuery.setString(3, entry.getKey());
						this.deleteAnswerQuery.setLong(4, versionId);
						this.deleteAnswerQuery.addBatch();
						numDeletes++;
					} else {
						// We don't want to update with a null value
						Answer storedAnswer = storedAnswers.get(entry.getKey());
						if (storedAnswer == null || !storedAnswer.equals(entry.getValue())) {
							// must update
							this.updateAnswerQuery.clearParameters();
							this.updateAnswerQuery.setLong(3, userId);
							this.updateAnswerQuery.setLong(4, versionId);
							this.updateAnswerQuery.setString(5, entry.getKey());
							
							this.updateAnswerQuery.setLong(6, versionId);
							if (entry.getValue() instanceof YesNoAnswer) {
								YesNoAnswer yn = (YesNoAnswer)entry.getValue();
								this.updateAnswerQuery.setString(1, yn.getAnswer().toString());
							} else {
								this.updateAnswerQuery.setNull(1, java.sql.Types.VARCHAR);
							}
							if (entry.getValue() instanceof YesNoAnswerWithEvidence) {
								this.updateAnswerQuery.setString(2, ((YesNoAnswerWithEvidence)entry.getValue()).getEvidence());
							} else {
								this.updateAnswerQuery.setString(2, null);
							}
							this.updateAnswerQuery.addBatch();
							numUpdates++;
						}
					}
				} else {
					// Insert a new row
					this.addAnswerQuery.clearParameters();
					this.addAnswerQuery.setLong(1, userId);
					this.addAnswerQuery.setLong(2, versionId);
					this.addAnswerQuery.setString(3, entry.getKey());
					this.addAnswerQuery.setLong(4, versionId);
					if (entry.getValue() instanceof YesNoAnswer) {
						YesNoAnswer yn = (YesNoAnswer)entry.getValue();
						this.addAnswerQuery.setString(5, yn.getAnswer().toString());
					} else {
						this.addAnswerQuery.setString(5, null);
					}
					if (entry.getValue() instanceof YesNoAnswerWithEvidence) {
						this.addAnswerQuery.setString(6, ((YesNoAnswerWithEvidence)entry.getValue()).getEvidence());
					} else {
						this.addAnswerQuery.setString(6, null);
					}
					this.addAnswerQuery.addBatch();
					numAdds++;
				}
				storedAnswers.remove(entry.getKey());
			}
			// Delete the entries not found
			iter = storedAnswers.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Answer> entry = iter.next();
				this.deleteAnswerQuery.clearParameters();
				this.deleteAnswerQuery.setLong(1, userId);
				this.deleteAnswerQuery.setLong(2, versionId);
				this.deleteAnswerQuery.setString(3, entry.getKey());
				this.deleteAnswerQuery.setLong(4, versionId);
				this.deleteAnswerQuery.addBatch();
				numDeletes++;
			}
			if (numAdds > 0) {
				int[] counts = this.addAnswerQuery.executeBatch();
				if (counts.length != numAdds) {
					logger.warn("Number of adds did not match. Expected "+String.valueOf(numAdds)+".  Found "+String.valueOf(counts.length)); //$NON-NLS-1$ //$NON-NLS-2$
				}
				for (int count:counts) {
					if (count != 1) {
						logger.warn("Expected 1 update for add index.  found "+String.valueOf(count)); //$NON-NLS-1$
					}
				}
			}
			if (numDeletes > 0) {
				int[] counts = this.deleteAnswerQuery.executeBatch();
				if (counts.length != numDeletes) {
					logger.warn("Number of deletes did not match. Expected "+String.valueOf(numDeletes)+".  Found "+String.valueOf(counts.length)); //$NON-NLS-1$ //$NON-NLS-2$
				}
				for (int count:counts) {
					if (count != 1) {
						logger.warn("Expected 1 update for delete index.  found "+String.valueOf(count)); //$NON-NLS-1$
					}
				}
			}
			if (numUpdates > 0) {
				int[] counts = this.updateAnswerQuery.executeBatch();
				if (counts.length != numUpdates) {
					logger.warn("Number of updates did not match. Expected "+String.valueOf(numDeletes)+".  Found "+String.valueOf(counts.length)); //$NON-NLS-1$ //$NON-NLS-2$
				}
				for (int count:counts) {
					if (count != 1) {
						logger.warn("Expected 1 update for update index.  found "+String.valueOf(count)); //$NON-NLS-1$
					}
				}
			}
		} finally {
			// Didn't open a resultset, but leaving this as a hook if we add something that needs to be cleaned up
		}
	}

	/**
	 * @param ids any responses for any of these ID's will be set to approved
	 * @param value value to set approved to
	 * @throws SQLException 
	 * @throws SurveyResponseException 
	 */
	public synchronized void setApproved(String[] ids, boolean value) throws SQLException, SurveyResponseException {
		this.setApprovedIdsQuery.clearBatch();
		Savepoint save = con.setSavepoint();
		try {
			for (String id:ids) {
				setApprovedIdsQuery.setBoolean(1, value);
				try {
					setApprovedIdsQuery.setLong(2, Long.parseLong(id));
				} catch (Exception ex2) {
					throw new SurveyResponseException("Invalid ID - must be a number:"+id); //$NON-NLS-1$
				}
				setApprovedIdsQuery.addBatch();
			}
			int[] counts = setApprovedIdsQuery.executeBatch();
			if (counts.length != ids.length) {
				logger.warn("Number of counts from batch does not match.  Expected "+ //$NON-NLS-1$
						String.valueOf(ids.length) + ", found "+String.valueOf(counts.length)); //$NON-NLS-1$
			}
			for (int count:counts) {
				if (count != 1) {
					logger.warn("Unexpected count.  Expected 1, found "+String.valueOf(count)); //$NON-NLS-1$
				}
			}
		} catch(SQLException ex) {
			logger.error("SQL Exception: ",ex); //$NON-NLS-1$
			con.rollback(save);
			throw(ex);
		} catch(SurveyResponseException ex) {
			con.rollback(save);
			throw(ex);
		} finally {
			if (save != null) {
				con.commit();
			}
		}
	}

	/**
	 * @param ids any responses for any of these ID's will be set to rejected
	 * @param value value to set rejected to
	 * @throws SQLException 
	 * @throws SurveyResponseException 
	 */
	public void setRejected(String[] ids, boolean value) throws SQLException, SurveyResponseException {
		this.setRejectedIdsQuery.clearBatch();
		Savepoint save = con.setSavepoint();
		try {
			for (String id:ids) {
				setRejectedIdsQuery.setBoolean(1, value);
				try {
					setRejectedIdsQuery.setLong(2, Long.parseLong(id));
				} catch (Exception ex2) {
					throw new SurveyResponseException("Invalid ID - must be a number:"+id); //$NON-NLS-1$
				}
				setRejectedIdsQuery.addBatch();
			}
			int[] counts = setRejectedIdsQuery.executeBatch();
			if (counts.length != ids.length) {
				logger.warn("Number of counts from batch does not match.  Expected "+ //$NON-NLS-1$
						String.valueOf(ids.length) + ", found "+String.valueOf(counts.length)); //$NON-NLS-1$
			}
			for (int count:counts) {
				if (count != 1) {
					logger.warn("Unexpected count.  Expected 1, found "+String.valueOf(count)); //$NON-NLS-1$
				}
			}
		} catch(SQLException ex) {
			logger.error("SQL Exception: ",ex); //$NON-NLS-1$
			con.rollback(save);
			throw(ex);
		} catch(SurveyResponseException ex) {
			con.rollback(save);
			throw(ex);
		} finally {
			if (save != null) {
				con.commit();
			}
		}
	}
	
	/**
	 * Delete the survey response from the database for a specific spec version and user
	 * @param userName the User Name of the user
	 * @param specVerion the specification version
	 * @throws SQLException 
	 */
	public void deleteSurveyResponseAnswers(String userName, String specVersion) throws SurveyResponseException, SQLException {
		Statement stmt = null;
		Savepoint save = null;
		try {
			save = con.setSavepoint();
			stmt = con.createStatement();
			deleteAllAnswersForResponseQuery.setString(1, userName);
			deleteAllAnswersForResponseQuery.setString(2, specVersion);
			int count = deleteAllAnswersForResponseQuery.executeUpdate();
			deleteAllResponsesForSpecVersionQuery.setString(1, userName);
			deleteAllResponsesForSpecVersionQuery.setString(2, specVersion);
			count = deleteAllResponsesForSpecVersionQuery.executeUpdate();
			if (count != 1) {
				logger.warn("Unexpected update count for delete survey response.  Execpted 1, found "+String.valueOf(count)); //$NON-NLS-1$
			}
		} catch (SQLException e) {
			logger.error("SQL Exception deleting answers",e); //$NON-NLS-1$
			if (save != null) {
				try {
					con.rollback(save);
				} catch (SQLException e1) {
					logger.warn("SQL Exception rolling back transaction.",e1); //$NON-NLS-1$
				}
				throw(e);
			}
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					logger.warn("Error closing statement",e); //$NON-NLS-1$
				}
			}
			if (save != null) {
				try {
					con.commit();
				} catch (SQLException e) {
					logger.error("SQL Exception committing transaction",e); //$NON-NLS-1$
					throw(e);
				}
			}
		}
	}

	/**
	 * Delete the survey response from the database
	 * @param surveyResponseId ID of the SurveyResponse
	 * @throws SQLException 
	 */
	public void deleteSurveyResponseAnswers(String surveyResponseId) throws SurveyResponseException, SQLException {
		Statement stmt = null;
		Savepoint save = null;
		try {
			save = con.setSavepoint();
			stmt = con.createStatement();
			int count = stmt.executeUpdate("delete from answer where response_id="+surveyResponseId); //$NON-NLS-1$
			count = stmt.executeUpdate("delete from survey_response where id="+surveyResponseId); //$NON-NLS-1$
			if (count != 1) {
				logger.warn("Unexpected update count for delete survey response.  Execpted 1, found "+String.valueOf(count)); //$NON-NLS-1$
			}
		} catch (SQLException e) {
			logger.error("SQL Exception deleting answers",e); //$NON-NLS-1$
			if (save != null) {
				try {
					con.rollback(save);
				} catch (SQLException e1) {
					logger.warn("SQL Exception rolling back transaction.",e1); //$NON-NLS-1$
				}
				throw(e);
			}
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					logger.warn("Error closing statement",e); //$NON-NLS-1$
				}
			}
			if (save != null) {
				try {
					con.commit();
				} catch (SQLException e) {
					logger.error("SQL Exception committing transaction",e); //$NON-NLS-1$
					throw(e);
				}
			}
		}
	}

	/**
	 * @param username
	 * @param language tag in IETF RFC 5646 format
	 * @return
	 * @throws SurveyResponseException
	 * @throws QuestionException
	 * @throws SQLException
	 */
	public List<SurveyResponse> getSurveyResponses(String username, String language) throws SurveyResponseException, QuestionException, SQLException {
		ResultSet result = null;
		List<SurveyResponse> retval = new ArrayList<SurveyResponse>();
		Map<String, Survey> surveys = new HashMap<String, Survey>();	// Cache of spec version to survey
		try {
			getResponsesForUserQuery.setString(1, username);
			result = getResponsesForUserQuery.executeQuery();
			while (result.next()) {
				String specVersion = result.getString("version"); //$NON-NLS-1$
				SurveyResponse response = new SurveyResponse(specVersion, language);
				User user = new User();
				user.setAddress(result.getString("address")); //$NON-NLS-1$
				user.setAdmin(result.getBoolean("admin")); //$NON-NLS-1$
				user.setEmail(result.getString("email")); //$NON-NLS-1$
				user.setName(result.getString("name")); //$NON-NLS-1$
				user.setPasswordReset(result.getBoolean("passwordReset")); //$NON-NLS-1$
				user.setPasswordToken(result.getString("password_token")); //$NON-NLS-1$
				user.setUsername(result.getString("username")); //$NON-NLS-1$
				user.setUuid(result.getString("uuid")); //$NON-NLS-1$
				user.setVerificationExpirationDate(result.getDate("verificationExpirationDate")); //$NON-NLS-1$
				user.setVerified(result.getBoolean("verified")); //$NON-NLS-1$
				user.setOrganization(result.getString("organization")); //$NON-NLS-1$
				user.setNamePermission(result.getBoolean("name_permission")); //$NON-NLS-1$
				user.setEmailPermission(result.getBoolean("email_permission")); //$NON-NLS-1$
				long userId = result.getLong("id"); //$NON-NLS-1$
				response.setResponder(user);
				response.setSubmitted(result.getBoolean("submitted")); //$NON-NLS-1$
				response.setApproved(result.getBoolean("approved")); //$NON-NLS-1$
				response.setRejected(result.getBoolean("rejected")); //$NON-NLS-1$
				response.setId(String.valueOf(result.getLong("responseid"))); //$NON-NLS-1$
				Survey survey = surveys.get(specVersion);
				if (survey == null) {
					survey = SurveyDbDao.getSurvey(con, specVersion, language);
					surveys.put(specVersion, survey);
				}
				response.setSurvey(survey);
				response.setResponses(getAnswers(userId, specVersion, language));
				retval.add(response);
			}
			return retval;
		} catch (SQLException e) {
			logger.error("SQL error getting users with responses",e); //$NON-NLS-1$
			throw(e);
		} finally {
			if (result != null) {
				result.close();
				this.con.commit();
			}
		}
	}
}
