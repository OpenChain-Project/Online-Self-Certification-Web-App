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

import org.apache.log4j.Logger;
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

/**
 * Manage data access for Survey Responses
 * @author Gary O'Neall
 *
 */
public class SurveyResponseDao {
	static final Logger logger = Logger.getLogger(SurveyResponseDao.class);
	Connection con;
	private PreparedStatement getUserQuery;
	private PreparedStatement getLatestSpecVersionForUserQuery;
	private PreparedStatement getAnswersQuery;
	private PreparedStatement getUserIdQuery;
	private PreparedStatement getSpecVersionIdQuery;
	private PreparedStatement addSurveyResponseQuery;
	private PreparedStatement addAnswerQuery;
	private PreparedStatement updateAnswerQuery;
	private PreparedStatement deleteAnswerQuery;
	private PreparedStatement getSubmittedQuery;
	private PreparedStatement setSubmittedQuery;
	private PreparedStatement getQuestionNameQuery;
	private PreparedStatement getUsersWithResponsesQuery;
	
	public SurveyResponseDao(Connection con) throws SQLException {
		this.con = con;
		this.con.setAutoCommit(false);
		getUserQuery = con.prepareStatement("select password_token, name, address, email," +
				"verified, passwordReset, admin, verificationExpirationDate," +
				" uuid, organization, id from openchain_user where username=?",
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		getLatestSpecVersionForUserQuery = con.prepareStatement("select max(version) from survey_response join " +
				"spec on survey_response.spec_version=spec.id where user_id=?",
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		getAnswersQuery = con.prepareStatement("select number, answer, evidence, type, question_id, subquestion_of from " +
				"answer join survey_response on answer.response_id=survey_response.id " +
				"join question on answer.question_id=question.id " +
				"join spec on survey_response.spec_version=spec.id " +
				"where user_id=? and version=? order by number",
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		getUserIdQuery = con.prepareStatement("select id from openchain_user where username=?",
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		getSpecVersionIdQuery = con.prepareStatement("select id from spec where version=?",
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		addSurveyResponseQuery = con.prepareStatement("insert into survey_response (user_id, spec_version) values (?,?)",
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		deleteAnswerQuery = con.prepareStatement("delete from answer where response_id in (select id from " +
				"survey_response where user_id=? and spec_version=?) and question_id in (select id from question where number=?)",
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		updateAnswerQuery = con.prepareStatement("update answer set answer=?, evidence=? where response_id in (select id from " +
				"survey_response where user_id=? and spec_version=?) and question_id in (select id from question where number=?)",
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		addAnswerQuery = con.prepareStatement("insert into answer (response_id, question_id, answer, evidence) values((select id from " +
				"survey_response where user_id=? and spec_version=?), (select id from question where number=?), ?, ?)",
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		getSubmittedQuery = con.prepareStatement("select submitted from survey_response " +
				"join spec on survey_response.spec_version=spec.id " +
				" where user_id=? and version=?",
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		setSubmittedQuery = con.prepareStatement("update survey_response set submitted=? where " +
				"user_id =(select id from openchain_user where username=?) and spec_version = (select id from spec where version=?)",
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		getQuestionNameQuery = con.prepareStatement("select number from question where id=?",
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		getUsersWithResponsesQuery = con.prepareStatement("select username, password_token, name, address, email," +
				"verified, passwordReset, admin, verificationExpirationDate," +
				" uuid, organization, openchain_user.id, submitted, version from survey_response join openchain_user " +
				"on survey_response.user_id=openchain_user.id join spec on survey_response.spec_version=spec.id" +
				" order by username asc",
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	}

	/**
	 * Get a survery response for a specific user
	 * @param username username for the survey response
	 * @param specVersion version of the spec for the survey response.  If null, will use the latest spec version available
	 * @return
	 * @throws QuestionTypeException 
	 */
	public synchronized SurveyResponse getSurveyResponse(String username, String specVersion) throws SQLException, QuestionException, SurveyResponseException {
		ResultSet result = null;
		try {
			getUserQuery.setString(1, username);
			result = getUserQuery.executeQuery();
			if (!result.next()) {
				return null;	// No user, no user response
			}
			User user = new User();
			user.setAddress(result.getString("address"));
			user.setAdmin(result.getBoolean("admin"));
			user.setEmail(result.getString("email"));
			user.setName(result.getString("name"));
			user.setPasswordReset(result.getBoolean("passwordReset"));
			user.setPasswordToken(result.getString("password_token"));
			user.setUsername(username);
			user.setUuid(result.getString("uuid"));
			user.setVerificationExpirationDate(result.getDate("verificationExpirationDate"));
			user.setVerified(result.getBoolean("verified"));
			user.setOrganization(result.getString("organization"));
			long userId = result.getLong("id");
			if (specVersion == null) {
				// get the latest spec version that is stored in the database
				result.close();
				getLatestSpecVersionForUserQuery.setLong(1, userId);
				result = getLatestSpecVersionForUserQuery.executeQuery();
				if (!result.next()) {
					logger.info("No survey results found for username "+username);
					return null;	// no survey responses for this user
				}
				specVersion = result.getString(1);
				if (specVersion == null || specVersion.trim().isEmpty()) {
					logger.info("No survey results found for username "+username);
					return null;	// no survey responses for this user
				}
			}
			Survey survey = SurveyDbDao.getSurvey(con, specVersion);
			Map<String, Answer>answers = getAnswers(userId, specVersion);			
			SurveyResponse retval = new SurveyResponse();
			retval.setResponder(user);
			retval.setResponses(answers);
			retval.setSurvey(survey);
			retval.setSpecVersion(specVersion);
			retval.setSubmitted(getSubmitted(userId, specVersion));
			return retval;
		} catch (QuestionTypeException e) {
			logger.error("Invalid question type getting survey response for user "+username,e);
			throw(e);
		} catch (SurveyResponseException e) {
			logger.error("Invalid survey response getting survey response for user "+username,e);
			throw(e);
		} catch (SQLException e) {
			logger.error("SQL Exception getting survey response for user "+username,e);
			throw(e);
		} finally {
			if (result != null) {
				result.close();
				con.commit();
			}
		}
	}
	
	/**
	 * @return All survey responses
	 * @throws SQLException 
	 * @throws QuestionException 
	 * @throws SurveyResponseException 
	 */
	public synchronized List<SurveyResponse> getSurveyResponses() throws SQLException, SurveyResponseException, QuestionException {
		ResultSet result = null;
		List<SurveyResponse> retval = new ArrayList<SurveyResponse>();
		Map<String, Survey> surveys = new HashMap<String, Survey>();	// Cache of spec version to survey
		try {
			result = getUsersWithResponsesQuery.executeQuery();
			while (result.next()) {
				SurveyResponse response = new SurveyResponse();
				User user = new User();
				user.setAddress(result.getString("address"));
				user.setAdmin(result.getBoolean("admin"));
				user.setEmail(result.getString("email"));
				user.setName(result.getString("name"));
				user.setPasswordReset(result.getBoolean("passwordReset"));
				user.setPasswordToken(result.getString("password_token"));
				user.setUsername(result.getString("username"));
				user.setUuid(result.getString("uuid"));
				user.setVerificationExpirationDate(result.getDate("verificationExpirationDate"));
				user.setVerified(result.getBoolean("verified"));
				user.setOrganization(result.getString("organization"));
				long userId = result.getLong("id");
				response.setResponder(user);
				response.setSubmitted(result.getBoolean("submitted"));
				String specVersion = result.getString("version");
				response.setSpecVersion(specVersion);
				Survey survey = surveys.get(specVersion);
				if (survey == null) {
					survey = SurveyDbDao.getSurvey(con, specVersion);
					surveys.put(specVersion, survey);
				}
				response.setSurvey(survey);
				response.setResponses(getAnswers(userId, specVersion));
				retval.add(response);
			}
			return retval;
		} catch (SQLException e) {
			logger.error("SQL error getting users with responses",e);
			throw(e);
		} finally {
			if (result != null) {
				result.close();
			}
		}
	}
	
	/**
	 * Get all answers for a given user ID and specVersion
	 * @param userId
	 * @param specVersion
	 * @return
	 * @throws SQLException
	 * @throws QuestionTypeException
	 * @throws SurveyResponseException
	 */
	private Map<String, Answer> getAnswers(long userId, String specVersion) throws SQLException, QuestionTypeException, SurveyResponseException {
		ResultSet result = null;
		Map<String, Answer> responses = new HashMap<String, Answer>();
		try {
			getAnswersQuery.setLong(1,userId);
			getAnswersQuery.setString(2,specVersion);
			result = getAnswersQuery.executeQuery();	
			Map<Long,SubQuestionAnswers> questionNumSubQuestionAnswer = new HashMap<Long,SubQuestionAnswers>();
			while (result.next()) {
				String questionNumber = result.getString("number");
				String type = result.getString("type");
				long questionId = result.getLong("question_id");
				long subQuestionOfId = result.getLong("subquestion_of");
				Answer answer;
				if (type == null) {
					throw(new QuestionTypeException("No question type stored in the database"));
				}
				if (type.equals(YesNoQuestion.TYPE_NAME)) {
					answer = new YesNoAnswer(YesNo.valueOf(result.getString("answer")));
				} else if (type.equals(YesNoQuestionWithEvidence.TYPE_NAME)) {
					answer = new YesNoAnswerWithEvidence(YesNo.valueOf(result.getString("answer")),
							result.getString("evidence"));
				} else if (type.equals(YesNoNotApplicableQuestion.TYPE_NAME)) {
					answer = new YesNoAnswer(YesNo.valueOf(result.getString("answer")));
				} else if (type.equals(SubQuestion.TYPE_NAME)) {
					answer = questionNumSubQuestionAnswer.get(questionId);
					if (answer == null) {
						answer = new SubQuestionAnswers();
						questionNumSubQuestionAnswer.put(questionId, (SubQuestionAnswers)answer);
					}
				} else {
					throw(new QuestionTypeException("Unknown question type in database: "+type));
				}
				if (subQuestionOfId > 0) {
					SubQuestionAnswers parentAnswer = questionNumSubQuestionAnswer.get(subQuestionOfId);
					if (parentAnswer == null) {
						parentAnswer = new SubQuestionAnswers();
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
			result = stmt.executeQuery("select max(version) from spec");
			if (!result.next()) {
				throw new SurveyResponseException("No specs found in database");
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
	public synchronized void addSurveyResponse(SurveyResponse response) throws SQLException, SurveyResponseException, QuestionTypeException {
		Savepoint save = con.setSavepoint();
		try {
			long userId = getUserId(response.getResponder().getUsername());
			long versionId = getVersionId(response.getSpecVersion());
			addSurveyResponseQuery.setLong(1, userId);
			addSurveyResponseQuery.setLong(2, versionId);
			int count = addSurveyResponseQuery.executeUpdate();
			if (count != 1) {
				logger.error("Unexpected count for adding survey response.  Expected one, returned "+String.valueOf(count));
			}
			_updateSurveyResponseAnswers(response, userId, versionId);
		} catch(SQLException ex) {
			logger.error("SQL exception updating survey response answers for "+response.getResponder().getUsername(),ex);
			con.rollback(save);
			throw(ex);
		} finally {
			if (save != null) {
				con.commit();
			}
		}
	}
	
	private boolean getSubmitted(long userId, String specVersion) throws SQLException, SurveyResponseException {
		ResultSet result = null;
		try {
			getSubmittedQuery.setLong(1, userId);
			getSubmittedQuery.setString(2, specVersion);
			result = getSubmittedQuery.executeQuery();
			if (result.next()) {
				return result.getBoolean(1);
			} else {
				throw new SurveyResponseException("No survey response found for user ID "+String.valueOf(userId));
			}
		} finally {
			if (result != null) {
				result.close();
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
				logger.warn("Unexpected count on setting submitted.  Expected 1, found "+String.valueOf(count));
			}
		} finally {
			if (save != null) {
				con.commit();
			}
		}
	}
	
	private long getVersionId(String specVersion) throws SQLException, SurveyResponseException {
		ResultSet result = null;
		try {
			getSpecVersionIdQuery.setString(1, specVersion);
			result = getSpecVersionIdQuery.executeQuery();
			if (!result.next()) {
				logger.error("Invalid spec version "+specVersion);
				throw(new SurveyResponseException("Can not add response - invalid specification version"));
			}
			return result.getLong(1);
		} finally {
			if (result != null) {
				result.close();
			}
		}
	}

	private long getUserId(String username) throws SQLException, SurveyResponseException {
		ResultSet result = null;
		try {
			getUserIdQuery.setString(1, username);
			result = getUserIdQuery.executeQuery();
			if (!result.next()) {
				logger.error("Missing user for username "+username);
				throw(new SurveyResponseException("Missing user "+username));
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
	 * @throws SQLException 
	 * @throws SurveyResponseException 
	 * @throws QuestionTypeException 
	 */
	public synchronized void updateSurveyResponseAnswers(SurveyResponse response) throws SQLException, SurveyResponseException, QuestionTypeException {
		Savepoint save = con.setSavepoint();
		try {
			long userId = getUserId(response.getResponder().getUsername());
			long versionId = getVersionId(response.getSpecVersion());
			_updateSurveyResponseAnswers(response, userId, versionId);
		} catch(SQLException ex) {
			logger.error("SQL exception updating survey response answers for "+response.getResponder().getUsername(),ex);
			con.rollback(save);
			throw(ex);
		} finally {
			if (save != null) {
				con.commit();
			}
		}
	}
	
	private void _updateSurveyResponseAnswers(SurveyResponse response, long userId, long versionId) throws SQLException, QuestionTypeException, SurveyResponseException {
		// First, verify the question numbers
		Set<String> numbers = response.getSurvey().getQuestionNumbers();
		try {
			Map<String, Answer> storedAnswers = getAnswers(userId, response.getSpecVersion());
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
					logger.error("Attempting to update an answer for a question that does not exist.  Username="+
									response.getResponder().getUsername()+", specVersion="+response.getSpecVersion() +
									"question number: "+entry.getKey());
					
					throw(new SurveyResponseException("Can not update answers.  Question "+entry.getKey()+" does not exist."));
				}
				if (storedAnswers.containsKey(entry.getKey())) {
					// already there
					if (entry.getKey() == null) {
						// Delete the entry
						this.deleteAnswerQuery.clearParameters();
						this.deleteAnswerQuery.setLong(1, userId);
						this.deleteAnswerQuery.setLong(2, versionId);
						this.deleteAnswerQuery.setString(3, entry.getKey());
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
							if (entry.getValue() instanceof YesNoAnswer) {
								YesNoAnswer yn = (YesNoAnswer)entry.getValue();
								this.updateAnswerQuery.setString(1, yn.getAnswer().toString());
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
					if (entry.getValue() instanceof YesNoAnswer) {
						YesNoAnswer yn = (YesNoAnswer)entry.getValue();
						this.addAnswerQuery.setString(4, yn.getAnswer().toString());
					} else {
						this.addAnswerQuery.setString(4, null);
					}
					if (entry.getValue() instanceof YesNoAnswerWithEvidence) {
						this.addAnswerQuery.setString(5, ((YesNoAnswerWithEvidence)entry.getValue()).getEvidence());
					} else {
						this.addAnswerQuery.setString(5, null);
					}
					this.addAnswerQuery.addBatch();
					numAdds++;
				}
			}
			if (numAdds > 0) {
				int[] counts = this.addAnswerQuery.executeBatch();
				if (counts.length != numAdds) {
					logger.warn("Number of adds did not match. Expected "+String.valueOf(numAdds)+".  Found "+String.valueOf(counts.length));
				}
				for (int count:counts) {
					if (count != 1) {
						logger.warn("Expected 1 update for add index.  found "+String.valueOf(count));
					}
				}
			}
			if (numDeletes > 0) {
				int[] counts = this.deleteAnswerQuery.executeBatch();
				if (counts.length != numDeletes) {
					logger.warn("Number of deletes did not match. Expected "+String.valueOf(numDeletes)+".  Found "+String.valueOf(counts.length));
				}
				for (int count:counts) {
					if (count != 1) {
						logger.warn("Expected 1 update for delete index.  found "+String.valueOf(count));
					}
				}
			}
			if (numUpdates > 0) {
				int[] counts = this.updateAnswerQuery.executeBatch();
				if (counts.length != numUpdates) {
					logger.warn("Number of updates did not match. Expected "+String.valueOf(numDeletes)+".  Found "+String.valueOf(counts.length));
				}
				for (int count:counts) {
					if (count != 1) {
						logger.warn("Expected 1 update for update index.  found "+String.valueOf(count));
					}
				}
			}
		} finally {
			// Didn't open a resultset, but leaving this as a hook if we add something that needs to be cleaned up
		}
		
	}
}
