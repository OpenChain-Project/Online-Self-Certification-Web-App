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
package org.openchain.certification;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;
import org.openchain.certification.dbdao.SurveyDatabase;
import org.openchain.certification.dbdao.SurveyDbDao;
import org.openchain.certification.dbdao.SurveyResponseDao;
import org.openchain.certification.dbdao.UserDb;
import org.openchain.certification.model.Answer;
import org.openchain.certification.model.Question;
import org.openchain.certification.model.QuestionException;
import org.openchain.certification.model.QuestionTypeException;
import org.openchain.certification.model.SubQuestion;
import org.openchain.certification.model.SubQuestionAnswers;
import org.openchain.certification.model.SurveyResponse;
import org.openchain.certification.model.SurveyResponseException;
import org.openchain.certification.model.User;
import org.openchain.certification.model.YesNoAnswer;
import org.openchain.certification.model.YesNoAnswerWithEvidence;
import org.openchain.certification.model.YesNoQuestion;
import org.openchain.certification.model.YesNoQuestion.YesNo;
import org.openchain.certification.model.YesNoQuestionWithEvidence;
import org.openchain.certification.utility.EmailUtilException;
import org.openchain.certification.utility.EmailUtility;
import org.openchain.certification.utility.PasswordUtil;


/**
 * User information for the HTTP session
 * @author Gary O'Neall
 *
 */
public class UserSession {
	static final transient Logger logger = Logger.getLogger(UserSession.class);
	
	private boolean loggedIn = false;
	private String username;
	private transient String password;
	private String lastError;
	private transient ServletConfig config;
	private transient SurveyResponse surveyResponse = null;

	private boolean admin = false;
	
	public UserSession(String username, String password, ServletConfig config) {
		this.username = username;
		this.password = password;
		this.config = config;
		this.loggedIn = false;
	}
	public UserSession(ServletConfig config) {
		this.config = config;
		this.loggedIn = false;
		this.username = null;
		this.password = null;
	}
	public void logout() {
		this.loggedIn = false;
		this.username = null;
		this.password = null;
		this.admin = false;
	}
	
	/**
	 * @return true if the password is valid, but the user has not been registered
	 */
	public boolean isValidPasswordAnNotVerified() {
		if (this.loggedIn) {
			return false;
		}
		User user = null;
		try {
			user = UserDb.getUserDb(config).getUser(username);
		} catch (SQLException e) {
			this.lastError = "Unexpected SQL error.  Please report this error to the OpenChain team: "+e.getMessage();
			logger.error("SQL Exception logging in user",e);
			return false;
		}
		if (user == null) {
			this.lastError = "User "+username+" does not exist.  Please review the username or sign up as a new user.";
			return false;
		}
		if (user.isVerified()) {
			return false;
		}
		try {
			if (!PasswordUtil.validate(password, user.getPasswordToken())) {
				this.lastError = "Passwords do not match.  Please retry or reset your password";
				return false;
			}
		} catch (NoSuchAlgorithmException e) {
			logger.error("Unexpected No Such Algorithm error logging in user",e);
			this.lastError = "Unexpected No Such Algorithm error.  Please report this error to the OpenChain team";
			return false;
		} catch (InvalidKeySpecException e) {
			this.lastError = "Unexpected Invalid Key Spec error.  Please report this error to the OpenChain team";
			logger.error("Unexpected Invalid Key Spec error logging in user",e);
			return false;
		}
		return true;
	}
	public boolean login() {
		this.loggedIn = false;
		User user = null;
		try {
			user = UserDb.getUserDb(config).getUser(username);
		} catch (SQLException e) {
			this.lastError = "Unexpected SQL error.  Please report this error to the OpenChain team: "+e.getMessage();
			logger.error("SQL Exception logging in user",e);
			return false;
		}
		if (user == null) {
			this.lastError = "User "+username+" does not exist.  Please review the username or sign up as a new user.";
			return false;
		}
		if (!user.isVerified()) {
			this.lastError = "This use has not been verified.  Please check your email and click on the provided link to verify this user and email address";
			return false;
		}
		try {
			if (!PasswordUtil.validate(password, user.getPasswordToken())) {
				this.lastError = "Passwords do not match.  Please retry or reset your password";
				return false;
			}
		} catch (NoSuchAlgorithmException e) {
			logger.error("Unexpected No Such Algorithm error logging in user",e);
			this.lastError = "Unexpected No Such Algorithm error.  Please report this error to the OpenChain team";
			return false;
		} catch (InvalidKeySpecException e) {
			this.lastError = "Unexpected Invalid Key Spec error.  Please report this error to the OpenChain team";
			logger.error("Unexpected Invalid Key Spec error logging in user",e);
			return false;
		}
		this.loggedIn = true;
		this.admin  = user.isAdmin();
		return true;
	}
	public String getLastError() {
		return this.lastError;
	}
	/**
	 * Sign up a new user
	 * @param Name of the user (common name, not the username)
	 * @param address Address of the user
	 * @param organization User organization
	 * @param email Email - must already be validated
	 * @param responseServletUrl The URL of the servlet to handle the email validation link
	 * @return
	 */
	public boolean signUp(String name, String address, String organization,
			String email, String responseServletUrl) {
		User user = null;
		try {
			user = UserDb.getUserDb(config).getUser(username);
			if (user != null) {
				this.lastError = "User "+username+" already exist.  Please select a different unique username.";
				return false;
			}
			user = new User();
			user.setAddress(address);
			user.setAdmin(false);
			user.setEmail(email);
			user.setName(name);
			user.setOrganization(organization);
			user.setPasswordReset(false);
			user.setPasswordToken(PasswordUtil.getToken(this.password));
			user.setUsername(this.username);
			user.setVerified(false);			
			user.setVerificationExpirationDate(EmailUtility.generateVerificationExpirationDate());
			UUID uuid = UUID.randomUUID();
			String hashedUuid = PasswordUtil.getToken(uuid.toString());
			user.setUuid(hashedUuid);
			UserDb.getUserDb(config).addUser(user);
			EmailUtility.emailVerification(name, email, uuid, username, responseServletUrl, config);
	        return true;
		} catch (SQLException e) {
			this.lastError = "Unexpected SQL error.  Please report this error to the OpenChain team: "+e.getMessage();
			logger.error("SQL Exception signing up user",e);
			return false;
		} catch (NoSuchAlgorithmException e) {
			logger.error("Unexpected No Such Algorithm error signing up user",e);
			this.lastError = "Unexpected No Such Algorithm error.  Please report this error to the OpenChain team";
			return false;
		} catch (InvalidKeySpecException e) {
			logger.error("Unexpected Invalid Key Spec error signing up user",e);
			this.lastError = "Unexpected Invalid Key Spec error.  Please report this error to the OpenChain team";
			return false;
		} catch (EmailUtilException e) {
			logger.error("Error emailing invitation",e);
			this.lastError = "Unable to email the invitiation: "+e.getMessage();
			return false;
		} catch (InvalidUserException e) {
			logger.error("Invalid user specified in add user request",e);
			this.lastError = "Error adding user: "+e.getMessage();
			return false;
		}
	}
	public String getUsername() {
		return this.username;
	}
	
	public boolean isLoggedIn() {
		return this.loggedIn;
	}
	public SurveyResponse getSurveyResponse() throws SQLException, QuestionException, SurveyResponseException {
		checkLoggedIn();
		if (this.surveyResponse == null) {
			_getSurveyResponse();
		}
		return this.surveyResponse;
	}
	private void _getSurveyResponse() throws SQLException, QuestionException, SurveyResponseException {
		Connection con = SurveyDatabase.createConnection(config);
		try {
			SurveyResponseDao dao = new SurveyResponseDao(con);
			this.surveyResponse = dao.getSurveyResponse(this.username, null);
			if (this.surveyResponse == null) {
				// Create one
				this.surveyResponse = new SurveyResponse();
				User user = UserDb.getUserDb(config).getUser(username);
				surveyResponse.setResponder(user);
				surveyResponse.setResponses(new HashMap<String, Answer>());
				surveyResponse.setSpecVersion(dao.getLatestSpecVersion());
				surveyResponse.setSurvey(SurveyDbDao.getSurvey(con, surveyResponse.getSpecVersion()));
				con.commit();
				dao.addSurveyResponse(surveyResponse);
			}
		} finally {
			con.close();
		}
	}
	
	private void checkLoggedIn() throws SurveyResponseException {
		if (!this.isLoggedIn()) {
			throw new SurveyResponseException("User is not logged in");
		}
	}
	/**
	 * Update the answers from a list of response answers.  This does NOT remove
	 * any already answered questions
	 * @param responses
	 * @throws SQLException
	 * @throws QuestionException
	 * @throws SurveyResponseException
	 */
	public void updateAnswers(List<ResponseAnswer> responses) throws SQLException, QuestionException, SurveyResponseException {
		checkLoggedIn();
		if (this.surveyResponse == null) {
			_getSurveyResponse();
		}
		Map<String, Answer> currentResponses = this.surveyResponse.getResponses();
		// Keep track of the subquestions found so that we can update the answers
		for (ResponseAnswer response:responses) {
			if (!response.isChecked()) {
				continue;
			}
			Question question = this.surveyResponse.getSurvey().getQuestion(response.getQuestionNumber());
			if (question != null && response.getValue() != null && !response.getValue().trim().isEmpty()) {
				YesNo ynAnswer = null;
				if (question instanceof YesNoQuestion) {
					if (response.getValue() == null) {
						logger.error("No answer provided for a yes/no question");
						throw(new QuestionTypeException("No value specified for a yes/no question"));
					}
					if (response.getValue().toUpperCase().trim().equals("YES")) {
						ynAnswer = YesNo.Yes;
					} else if (response.getValue().toUpperCase().trim().equals("NO")) {
						ynAnswer = YesNo.No;
					} else if (response.getValue().toUpperCase().trim().equals("NA")) {
						ynAnswer = YesNo.NotApplicable;
					} else {
						logger.error("Invalid yes no value: "+response.getValue());
						throw(new QuestionTypeException("Invalid yes/no value: "+response.getValue()));
					}
				}

				Answer answer;
				if (question instanceof YesNoQuestionWithEvidence) {
					answer = new YesNoAnswerWithEvidence(ynAnswer, response.getEvidence());
				} else if (question instanceof YesNoQuestion) {
					answer = new YesNoAnswer(ynAnswer);
				} else if (question instanceof SubQuestion) {
					answer = new SubQuestionAnswers();
				} else {
					logger.error("Invalid answer type for question "+response.getQuestionNumber());
					throw(new QuestionTypeException("Invalid answer type for question "+response.getQuestionNumber()));
				}
				currentResponses.put(response.getQuestionNumber(), answer);
				if (question.getSubQuestionNumber() != null) {
					SubQuestionAnswers subQuestionAnswer = (SubQuestionAnswers)currentResponses.get(question.getSubQuestionNumber());
					if (subQuestionAnswer == null) {
						subQuestionAnswer = new SubQuestionAnswers();
						currentResponses.put(question.getSubQuestionNumber(), subQuestionAnswer);
					}
					subQuestionAnswer.addSubAnswer(question.getNumber(), answer);
				}
			} else {
				logger.warn("Skipping a response answer "+response.getQuestionNumber());
			}
		}
		Connection con = SurveyDatabase.createConnection(config);
		try {
			SurveyResponseDao dao = new SurveyResponseDao(con);
			dao.updateSurveyResponseAnswers(this.surveyResponse);
		} finally {
			con.close();
		}
	}
	/**
	 * Final submission of questions
	 * @throws SQLException 
	 * @throws QuestionTypeException 
	 * @throws SurveyResponseException 
	 * @throws EmailUtilException 
	 */
	public void finalSubmission() throws SQLException, SurveyResponseException, QuestionException, EmailUtilException {
		checkLoggedIn();
		Connection con = SurveyDatabase.createConnection(config);
		_getSurveyResponse();
		this.surveyResponse.setSubmitted(true);
		try {
			SurveyResponseDao dao = new SurveyResponseDao(con);
			dao.setSubmitted(username, this.surveyResponse.getSpecVersion(), true);
		} finally {
			con.close();
		}
		EmailUtility.emailCompleteSubmission(this.username,
				this.surveyResponse.getResponder().getName(),
				this.surveyResponse.getResponder().getEmail(),
				this.surveyResponse.getSpecVersion(), config);
	}
	public boolean isAdmin() {
		return this.admin;
	}
	
	/**
	 * Resend a verification email for the user specified
	 * @param username
	 * @param password
	 * @param responseServletUrl
	 * @return
	 */
	public boolean resendVerification(String username, String password, String responseServletUrl) {
		this.username = username;
		if (this.loggedIn) {
			return false;
		}
		User user = null;
		try {
			user = UserDb.getUserDb(config).getUser(username);
		} catch (SQLException e) {
			this.lastError = "Unexpected SQL error.  Please report this error to the OpenChain team: "+e.getMessage();
			logger.error("SQL Exception logging in user",e);
			return false;
		}
		if (user == null) {
			this.lastError = "User "+username+" does not exist.  Please review the username or sign up as a new user.";
			return false;
		}
		if (user.isVerified()) {
			return false;
		}
		try {
			if (!PasswordUtil.validate(password, user.getPasswordToken())) {
				this.lastError = "Passwords do not match.  Please retry or reset your password";
				return false;
			}
		} catch (NoSuchAlgorithmException e) {
			logger.error("Unexpected No Such Algorithm error logging in user",e);
			this.lastError = "Unexpected No Such Algorithm error.  Please report this error to the OpenChain team";
			return false;
		} catch (InvalidKeySpecException e) {
			this.lastError = "Unexpected Invalid Key Spec error.  Please report this error to the OpenChain team";
			logger.error("Unexpected Invalid Key Spec error logging in user",e);
			return false;
		}
		UUID uuid = UUID.randomUUID();
		String hashedUuid;
		try {
			hashedUuid = PasswordUtil.getToken(uuid.toString());
		} catch (NoSuchAlgorithmException e) {
			logger.error("Unexpected No Such Algorithm error logging in user",e);
			this.lastError = "Unexpected No Such Algorithm error.  Please report this error to the OpenChain team";
			return false;
		} catch (InvalidKeySpecException e) {
			this.lastError = "Unexpected Invalid Key Spec error.  Please report this error to the OpenChain team";
			logger.error("Unexpected Invalid Key Spec error logging in user",e);
			return false;
		}
		user.setUuid(hashedUuid);
		try {
			UserDb.getUserDb(config).updateUser(user);
		} catch (SQLException e) {
			this.lastError = "Unexpected SQL error.  Please report this error to the OpenChain team: "+e.getMessage();
			logger.error("SQL Exception updating user during re-verification",e);
			return false;
		} catch (InvalidUserException e) {
			this.lastError = "Unexpected invalid user error.  Please report this error to the OpenChain team: "+e.getMessage();
			logger.error("Invalid user error in resending verification",e);
		}
		try {
			EmailUtility.emailVerification(user.getName(), user.getEmail(), 
					uuid, username, responseServletUrl, config);
		} catch (EmailUtilException e) {
			logger.error("Error emailing invitation",e);
			this.lastError = "Unable to re-email the invitiation: "+e.getMessage();
			return false;
		}
		return true;
	}
}
