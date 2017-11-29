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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
	/**
	 * List of all survey responses for this user
	 */
	private List<SurveyResponse> surveyResponses = null;
	/**
	 * Currently active survey response
	 */
	private SurveyResponse currentSurveyResponse = null;
	/**
	 * All specification versions available
	 */
	private transient List<String> allSpecVersions = null;

	private boolean admin = false;

	private String address = null;

	private String email = null;

	private String name = null;

	private String organization = null;

	private boolean passwordReset = false;
	
	private boolean namePermission = false;
	private boolean emailPermission = false;
	
	public UserSession(String username, String password, ServletConfig config) {
		this(config);
		this.username = username;
		this.password = password;
		this.loggedIn = false;
	}
	public UserSession(ServletConfig config) {
		this.config = config;
		this.loggedIn = false;
		this.username = null;
		this.password = null;
		this.admin = false;
		this.address = null;
		this.email = null;
		this.name = null;
		this.organization = null;
		this.namePermission = false;
		this.emailPermission = false;
	}
	
	static final int HOURS_FOR_VERIFICATION_EMAIL_EXPIRATION = 24;
	/**
	 * @return a Date when the verification email is set to expire
	 */
	public static Date generateVerificationExpirationDate() {
		Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(cal.getTime().getTime()));
        cal.add(Calendar.HOUR, HOURS_FOR_VERIFICATION_EMAIL_EXPIRATION);
		return new Date(cal.getTime().getTime());
	}
	
	/**
	 * @param expiration Date of the expiration
	 * @return true if the expirate date has not passed
	 */
	public static boolean isValidExpirationDate(Date expiration) {
		Date now = new Date();
		return now.compareTo(expiration) < 0;
	}
	
	/**
	 * Complete the email verification process validating the verification information
	 * @param username Username
	 * @param uuid UUID generated for the verification email
	 * @param config
	 * @throws InvalidUserException
	 */
	public static void completeEmailVerification(String username, String uuid, ServletConfig config) throws InvalidUserException  {
		try {
			User user = UserDb.getUserDb(config).getUser(username);
			if (user == null) {
				logger.error("NO user found for completing email verification - username "+username);
				throw new InvalidUserException("User "+username+" not found.  Could not complete registration.");
			}
			if (user.isVerified()) {
				logger.warn("Attempting to verify an already verified user");
				return;
			}
			if (!isValidExpirationDate(user.getVerificationExpirationDate())) {
				logger.error("Expiration date for verification has passed for user "+username);
				throw(new InvalidUserException("The verification has expired.  Please resend the verification email.  When logging in using your username and password, you will be prompted to resend the verification."));
			}
			if (!PasswordUtil.validate(uuid.toString(), user.getUuid())) {
				logger.error("Verification tokens do not match for user "+username+".  Supplied = "+uuid+", expected = "+user.getUuid());
				throw(new InvalidUserException("Verification failed.  Invalid registration ID.  Please retry."));
			}
			UserDb.getUserDb(config).setVerified(username, true);
		} catch (SQLException e) {
			logger.error("Unexpected SQL exception completing the email verification",e);
			throw(new InvalidUserException("Unexpected SQL exception completing verification.  Please report this error to the OpenChain group"));
		} catch (NoSuchAlgorithmException e) {
			logger.error("Unexpected No Such Algorithm Exception completing the email verification",e);
			throw(new InvalidUserException("Unexpected No Such Algorithm exception completing verification.  Please report this error to the OpenChain group"));
		} catch (InvalidKeySpecException e) {
			logger.error("Unexpected Invalid Key Exception completing the email verification",e);
			throw(new InvalidUserException("Unexpected Invalid Key exception completing verification.  Please report this error to the OpenChain group"));
		}
	}
	
	public void logout() {
		this.loggedIn = false;
		this.username = null;
		this.password = null;
		this.admin = false;
		this.address = null;
		this.email = null;
		this.name = null;
		this.organization = null;
		this.namePermission = false;
		this.emailPermission = false;
	}
	
	/**
	 * @return true if the password is valid, but the user has not been registered
	 */
	public boolean isValidPasswordAndNotVerified() {
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
	
	
	/**
	 * Log the user in and populate the user data
	 * @return true if the login was successful
	 */
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
		if (user.isPasswordReset()) {
			this.lastError = "A password reset is in process.  Login is not allowed until the password has been reset.";
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
		this.address = user.getAddress();
		this.email = user.getEmail();
		this.name = user.getName();
		this.organization = user.getOrganization();
		this.namePermission = user.hasNamePermission();
		this.emailPermission = user.hasEmailPermission();
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
	 * @param namePermission If true, user has given permission to publish their name on the website
	 * @param emailPermission If true, user has given permission to publish their email address on the website
	 * @return
	 */
	public boolean signUp(String name, String address, String organization,
			String email, String responseServletUrl, boolean namePermission,
			boolean emailPermission) {
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
			user.setNamePermission(namePermission);
			user.setEmailPermission(emailPermission);
			user.setVerificationExpirationDate(generateVerificationExpirationDate());
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
		if (this.surveyResponses == null) {
			_getSurveyResponses();
		}
		return this.currentSurveyResponse;
	}
	
	/**
	 * @return a list of survey response spec versions available to the user in sort order - does not include survey versions (e.g. will return 1.0 not 1.0.1)
	 * @throws SurveyResponseException 
	 * @throws QuestionException 
	 * @throws SQLException 
	 */
	public List<String> getSurveyResponseSpecVersions() throws SQLException, QuestionException, SurveyResponseException {
		checkLoggedIn();
		List<String> retval = new ArrayList<String>();
		if (surveyResponses == null) {
			_getSurveyResponses();
		}
		for (SurveyResponse sr:surveyResponses) {
			String ver = extractSpecVersion(sr.getSpecVersion());
			if (!retval.contains(ver)) {
				retval.add(ver);
			}			
		}
		Collections.sort(retval);
		return retval;
	}
	
	/**
	 * @param specVersion
	 * @return Only the spec version portion of a full spec version (e.g. 1.1.1 -> 1.1)
	 */
	static String extractSpecVersion(String fullSpecVersion) {
		String[] versionParts = fullSpecVersion.split("\\.");
		StringBuilder sb = new StringBuilder();
		sb.append(versionParts[0]);
		if (versionParts.length > 1) {
			sb.append('.');
			sb.append(versionParts[1]);
		}
		return sb.toString();
	}
	
	/**
	 * @param majorVersion
	 * @return the latest survey version based on the major spec vesion
	 * @throws SurveyResponseException 
	 * @throws SQLException 
	 */
	private String getLatestMinorVersion(String majorVersion) throws SurveyResponseException, SQLException {
		if (allSpecVersions == null) {
			_getAllSpecVersions();
		}
		String retval = "";
		for (String fullSpecVer:allSpecVersions) {
			String majorSpecVer = extractSpecVersion(fullSpecVer);
			if (majorSpecVer.equals(majorVersion)) {
				if (fullSpecVer.compareToIgnoreCase(retval) > 0) {
					retval = fullSpecVer;
				}
			}
		}
		if (retval.isEmpty()) {
			throw new SurveyResponseException("Could not find a full spec version for major version "+majorVersion);
		}
		return retval;
	}
	
	/**
	 * Populate the field allSpecVersions from the database
	 * @throws SQLException 
	 */
	private void _getAllSpecVersions() throws SQLException {
		Connection con = SurveyDatabase.createConnection(config);
		try {
			allSpecVersions = SurveyDbDao.getSurveyVersions(con);
			Collections.sort(allSpecVersions);
		} finally {
			con.close();
		}
	}
	/**
	 * Retrieve the survey responses from the database
	 * @throws SQLException
	 * @throws QuestionException
	 * @throws SurveyResponseException
	 */
	private void _getSurveyResponses() throws SQLException, QuestionException, SurveyResponseException {
		Connection con = SurveyDatabase.createConnection(config);
		try {
			SurveyResponseDao dao = new SurveyResponseDao(con);
			surveyResponses = dao.getSurveyResponses(this.username);
			if (this.surveyResponses.size() == 0) {
				// Create one
				currentSurveyResponse = new SurveyResponse();
				User user = UserDb.getUserDb(config).getUser(username);
				currentSurveyResponse.setResponder(user);
				currentSurveyResponse.setResponses(new HashMap<String, Answer>());
				currentSurveyResponse.setSpecVersion(dao.getLatestSpecVersion());
				currentSurveyResponse.setSurvey(SurveyDbDao.getSurvey(con, currentSurveyResponse.getSpecVersion()));
				con.commit();
				dao.addSurveyResponse(currentSurveyResponse);
				surveyResponses.add(currentSurveyResponse);
			} else {
				// set the current version to the latest
				currentSurveyResponse = surveyResponses.get(0);
				for (int i = 1; i < surveyResponses.size(); i++) {
					if (surveyResponses.get(i).getSpecVersion().compareToIgnoreCase(currentSurveyResponse.getSpecVersion()) > 0) {
						currentSurveyResponse = surveyResponses.get(1);
					}
				}
				
			}
		} finally {
			con.close();
		}
	}
	
	/**
	 * Set the current survey response to the matching version.
	 * @param specVersion Version of the spec
	 * @param create if no existing survey response was found by this version and create is true, a new survey response will be created
	 * @throws SQLException
	 * @throws QuestionException
	 * @throws SurveyResponseException
	 */
	public void setCurrentSurveyResponse(String specVersion, boolean create) throws SQLException, QuestionException, SurveyResponseException {
		checkLoggedIn();
		if (surveyResponses == null) {
			_getSurveyResponses();
		}
		boolean found = false;
		for (SurveyResponse sr:surveyResponses) {
			if (extractSpecVersion(sr.getSpecVersion()).equals(specVersion)) {
				if (found) {
					// check to see if this is a larger survey version and replace if needed
					if (sr.getSpecVersion().compareTo(currentSurveyResponse.getSpecVersion()) > 0) {
						currentSurveyResponse = sr;
					}
				} else {
					found = true;
					currentSurveyResponse = sr;
				}
			}
		}
		if (!found) {
			if (!create) {
				throw new SurveyResponseException("No survey response was found matching version "+specVersion);
			}
			Connection con;
			try {
				con = SurveyDatabase.createConnection(config);
			} catch (SQLException e) {
				logger.error("Unable to get connection for creating answers",e);
				throw new SurveyResponseException("Unable to get connection for creating answers.  Please report this error to the OpenChain team",e);
			}
			try {
				SurveyResponseDao dao = new SurveyResponseDao(con);
				User saveUser = currentSurveyResponse.getResponder(); 
				currentSurveyResponse = new SurveyResponse();
				currentSurveyResponse.setResponder(saveUser);
				currentSurveyResponse.setResponses(new HashMap<String, Answer>());
				currentSurveyResponse.setSpecVersion(getLatestMinorVersion(specVersion));
				currentSurveyResponse.setSurvey(SurveyDbDao.getSurvey(con, currentSurveyResponse.getSpecVersion()));
				con.commit();
				dao.addSurveyResponse(currentSurveyResponse);
				surveyResponses.add(currentSurveyResponse);
			} catch (SQLException e) {
				logger.error("SQL Exception adding answers",e);
				throw new SurveyResponseException("Unexpectes SQL error adding answers.  Please report this error to the OpenChain team",e);
			} finally {
				try {
					con.close();
				} catch (SQLException e) {
					logger.warn("Error closing connection",e);
				}
			}
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
		if (this.surveyResponses == null) {
			_getSurveyResponses();
		}
		Map<String, Answer> currentResponses = currentSurveyResponse.getResponses();
		// Keep track of the subquestions found so that we can update the answers
		for (ResponseAnswer response:responses) {
			if (!response.isChecked()) {
				continue;
			}
			Question question = currentSurveyResponse.getSurvey().getQuestion(response.getQuestionNumber());
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
			dao.updateSurveyResponseAnswers(currentSurveyResponse);
		} finally {
			con.close();
		}
	}
	/**
	 * Final submission of questions
	 * @return true if successful; on fail, lasterror will contain the error message
	 * @throws SQLException 
	 * @throws QuestionTypeException 
	 * @throws SurveyResponseException 
	 * @throws EmailUtilException 
	 */
	public boolean finalSubmission() throws SQLException, SurveyResponseException, QuestionException, EmailUtilException {
		checkLoggedIn();
		Connection con = SurveyDatabase.createConnection(config);
		if (this.surveyResponses == null) {
			_getSurveyResponses();
		}
		List<Question> invalidQuestions = currentSurveyResponse.invalidAnswers();
		if (invalidQuestions.size() > 0) {
			StringBuilder er = new StringBuilder("Can not submit - the following question(s) either has missing answers or invalid answers: ");
			er.append(invalidQuestions.get(0).getNumber());
			for (int i = 1; i < invalidQuestions.size(); i++) {
				er.append(", ");
				er.append(invalidQuestions.get(i).getNumber());
			}
			this.lastError = er.toString();
			return false;
		}
		currentSurveyResponse.setSubmitted(true);
		currentSurveyResponse.setApproved(true);
		currentSurveyResponse.setRejected(false);
		try {
			SurveyResponseDao dao = new SurveyResponseDao(con);
			dao.setSubmitted(username, currentSurveyResponse.getSpecVersion(), true);
			//NOTE: We automatically approve per openchain call on Monday Dec. 5
			dao.setApproved(username, currentSurveyResponse.getSpecVersion(), true);
			dao.setRejected(username, currentSurveyResponse.getSpecVersion(), false);
		} finally {
			con.close();
		}
		EmailUtility.emailCompleteSubmission(this.username,
				currentSurveyResponse.getResponder().getName(),
				currentSurveyResponse.getResponder().getEmail(),
				currentSurveyResponse.getSpecVersion(), config);
		return true;
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
		user.setVerificationExpirationDate(generateVerificationExpirationDate());
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
	/**
	 * Delete all answers and start a new survey
	 * @param specVersion Version for the new survey responses
	 * @throws SurveyResponseException 
	 * @throws QuestionException 
	 */
	public void resetAnswers(String specVersion) throws SurveyResponseException, QuestionException {
		checkLoggedIn();
		Connection con;
		try {
			con = SurveyDatabase.createConnection(config);
		} catch (SQLException e) {
			logger.error("Unable to get connection for resetting answers",e);
			throw new SurveyResponseException("Unable to get connection for resetting answers.  Please report this error to the OpenChain team",e);
		}
		try {
			SurveyResponseDao dao = new SurveyResponseDao(con);
			User saveUser = currentSurveyResponse.getResponder(); 
			dao.deleteSurveyResponseAnswers(currentSurveyResponse);
			surveyResponses.remove(currentSurveyResponse);
			currentSurveyResponse = new SurveyResponse();
			currentSurveyResponse.setResponder(saveUser);
			currentSurveyResponse.setResponses(new HashMap<String, Answer>());
			currentSurveyResponse.setSpecVersion(getLatestMinorVersion(specVersion));
			currentSurveyResponse.setSurvey(SurveyDbDao.getSurvey(con, currentSurveyResponse.getSpecVersion()));
			dao.addSurveyResponse(currentSurveyResponse);
			con.commit();
			surveyResponses.add(currentSurveyResponse);
		} catch (SQLException e) {
			logger.error("SQL Exception resetting answers",e);
			throw new SurveyResponseException("Unexpectes SQL error resetting answers.  Please report this error to the OpenChain team",e);
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				logger.warn("Error closing connection",e);
			}
		}
	}
	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the organization
	 */
	public String getOrganization() {
		return organization;
	}
	
	/**
	 * @return the namePermission
	 */
	public boolean hasNamePermission() {
		return namePermission;
	}
	/**
	 * @return the emailPermission
	 */
	public boolean hasEmailPermission() {
		return emailPermission;
	}
	/**
	 * @return the hoursForVerificationEmailExpiration
	 */
	public static int getHoursForVerificationEmailExpiration() {
		return HOURS_FOR_VERIFICATION_EMAIL_EXPIRATION;
	}
	/**
	 * Update the User information
	 * @param newName
	 * @param newEmail
	 * @param newOrganization
	 * @param newAddress
	 * @param newPassword
	 * @param newNamePermission
	 * @param newEmailPermission
	 * @throws InvalidUserException 
	 * @throws SQLException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 * @throws EmailUtilException 
	 */
	public void updateUser(String newName, String newEmail, String newOrganization, 
			String newAddress, String newPassword, boolean newNamePermission,
			boolean newEmailPermission) throws InvalidUserException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException, EmailUtilException {
		if (!loggedIn) {
			this.lastError = "Can not update a user which is not logged in.";
			throw new InvalidUserException(this.lastError);
		}
			User user = null;
			try {
				user = UserDb.getUserDb(config).getUser(username);
				if (user == null) {
					this.lastError = "User "+username+" no longer exist.  Please report this error to the open chain team.";
					throw new InvalidUserException(this.lastError);
				}
				boolean needUpdate = false;
				if (!Objects.equals(newName, this.name)) {
					user.setName(newName);
					this.name = newName;
					needUpdate = true;
				}
				if (!Objects.equals(newEmail, this.email)) {
					this.email = newEmail;
					user.setEmail(newEmail);
					needUpdate = true;
				}
				if (!Objects.equals(newOrganization, this.organization)) {
					this.organization = newOrganization;
					user.setOrganization(newOrganization);
					needUpdate = true;
				}
				if (newPassword != null && !Objects.equals(newPassword, this.password)) {
					this.password = newPassword;
					user.setPasswordToken(PasswordUtil.getToken(newPassword));
					needUpdate = true;
				}
				if (!Objects.equals(newAddress, this.address)) {
					this.address = newAddress;
					user.setAddress(newAddress);
					needUpdate = true;
				}
				if (user.hasEmailPermission() != newEmailPermission) {
					this.emailPermission = newEmailPermission;
					user.setEmailPermission(newEmailPermission);
					needUpdate = true;
				}
				if (user.hasNamePermission() != newNamePermission) {
					this.namePermission = newNamePermission;
					user.setNamePermission(newNamePermission);
					needUpdate = true;
				}
				if (needUpdate) {
					UserDb.getUserDb(config).updateUser(user);
					this.password = newPassword;
					this.address = newAddress;
					this.email = newEmail;
					this.name = newName;
					this.organization = newOrganization;
					try {
						EmailUtility.emailProfileUpdate(username, this.email, config);
					}catch (EmailUtilException e) {
						logger.warn("Error emailing profile update notice",e);
						this.lastError = "Unable to email the for the profile update: "+e.getMessage();
					}
				}
			} catch (SQLException e) {
				this.lastError = "Unexpected SQL error.  Please report this error to the OpenChain team: "+e.getMessage();
				logger.error("SQL Exception signing up user",e);
				throw e;
			} catch (NoSuchAlgorithmException e) {
				logger.error("Unexpected No Such Algorithm error signing up user",e);
				this.lastError = "Unexpected No Such Algorithm error.  Please report this error to the OpenChain team";
				throw e;
			} catch (InvalidKeySpecException e) {
				logger.error("Unexpected Invalid Key Spec error signing up user",e);
				this.lastError = "Unexpected Invalid Key Spec error.  Please report this error to the OpenChain team";
				throw e;
			} 
	}
	/**
	 * Set the password reset to in progress.  Verifies the UUID, reset in progress and expiration date.
	 * @param uuid
	 * @return
	 * @throws SQLException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 */
	public boolean verifyPasswordReset(String uuid) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
		User user = null;
		try {
			user = UserDb.getUserDb(config).getUser(username);
			if (!user.isPasswordReset()) {
				this.lastError = "Attempting to reset a password when a reset password email was not sent.";
				return false;
			}
			if (!PasswordUtil.validate(uuid.toString(), user.getUuid())) {
				logger.error("Password reset tokens do not match for user "+username+".  Supplied = "+uuid+", expected = "+user.getUuid());
				this.lastError = "Email password reset tokens does not match.  Please re-send the password reset.";
				return false;
			}
			if (!isValidExpirationDate(user.getVerificationExpirationDate())) {
				logger.error("Expiration date for verification has passed for user "+username);
				this.lastError = "The verification has expired.  Please resend the verification email.  When logging in using your username and password, you will be prompted to resend the verification.";
				return false;
			}
			this.passwordReset  = true;
			return true;
		} catch (SQLException e) {
			logger.error("SQL Exception setting password reset",e);
			this.lastError = "Unexpected SQL error.  Please report this error to the OpenChain team: "+e.getMessage();
			throw(e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Unexpected No Such Algorithm error signing up user",e);
			this.lastError = "Unexpected No Such Algorithm error.  Please report this error to the OpenChain team";
			throw e;
		} catch (InvalidKeySpecException e) {
			logger.error("Unexpected Invalid Key Spec error signing up user",e);
			this.lastError = "Unexpected Invalid Key Spec error.  Please report this error to the OpenChain team";
			throw e;
		} 
	}
	public boolean isPasswordReset() {
		return this.passwordReset;
	}
	public boolean setPassword(String username, String password) {
		if (!Objects.equals(username, this.username)) {
			this.lastError = "Username for password reset does not match the username in the email reset.";
			return false;
		}
		User user = null;
		try {
			user = UserDb.getUserDb(config).getUser(username);
			if (!user.isPasswordReset()) {
				this.lastError = "Attempting to reset a password when a reset password email was not sent.";
				return false;
			}
			user.setPasswordReset(false);
			user.setPasswordToken(PasswordUtil.getToken(password));
			UserDb.getUserDb(config).updateUser(user);
			this.passwordReset = false;
			this.password = password;
			return true;
		} catch (SQLException e) {
			logger.error("SQL Exception setting password reset",e);
			this.lastError = "Unexpected SQL error.  Please report this error to the OpenChain team: "+e.getMessage();
			return false;
		} catch (NoSuchAlgorithmException e) {
			logger.error("Unexpected No Such Algorithm error signing up user",e);
			this.lastError = "Unexpected No Such Algorithm error.  Please report this error to the OpenChain team";
			return false;
		} catch (InvalidKeySpecException e) {
			logger.error("Unexpected Invalid Key Spec error signing up user",e);
			this.lastError = "Unexpected Invalid Key Spec error.  Please report this error to the OpenChain team";
			return false;
		} catch (InvalidUserException e) {
			logger.error("Unexpected Invalid User error signing up user",e);
			this.lastError = "Unexpected Invalid User error.  Please report this error to the OpenChain team";
			return false;
		} 
	}
	/**
	 * unsubmits responses
	 * @throws SurveyResponseException 
	 * @throws QuestionException 
	 * @throws SQLException 
	 * @throws EmailUtilException 
	 */
	public void unsubmit() throws SQLException, QuestionException, SurveyResponseException, EmailUtilException {
		checkLoggedIn();
		Connection con = SurveyDatabase.createConnection(config);
		if (this.surveyResponses == null) {
			_getSurveyResponses();
		}
		if (!currentSurveyResponse.isSubmitted()) {
			logger.warn("Attempting to unsubmit an unsubmitted response for user "+this.username);
			return;
		}
		currentSurveyResponse.setApproved(false);
		currentSurveyResponse.setRejected(false);
		currentSurveyResponse.setSubmitted(false);
		try {
			SurveyResponseDao dao = new SurveyResponseDao(con);
			dao.setSubmitted(username, currentSurveyResponse.getSpecVersion(), false);
			dao.setApproved(username, currentSurveyResponse.getSpecVersion(), false);
			dao.setRejected(username, currentSurveyResponse.getSpecVersion(), false);
		} finally {
			con.close();
		}
		EmailUtility.emailUnsubmit(this.username,
				currentSurveyResponse.getResponder().getName(),
				currentSurveyResponse.getResponder().getEmail(),
				currentSurveyResponse.getSpecVersion(), config);
	}
}
