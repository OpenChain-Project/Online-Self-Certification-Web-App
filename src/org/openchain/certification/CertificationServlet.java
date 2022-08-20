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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openchain.certification.PostResponse.Status;
import org.openchain.certification.dbdao.SurveyDatabase;
import org.openchain.certification.dbdao.SurveyDbDao;
import org.openchain.certification.dbdao.SurveyResponseDao;
import org.openchain.certification.dbdao.UserDb;
import org.openchain.certification.git.GitRepoException;
import org.openchain.certification.git.QuestionnaireGitRepo;
import org.openchain.certification.model.Question;
import org.openchain.certification.model.QuestionException;
import org.openchain.certification.model.Section;
import org.openchain.certification.model.SubQuestion;
import org.openchain.certification.model.Submission;
import org.openchain.certification.model.Survey;
import org.openchain.certification.model.SurveyResponse;
import org.openchain.certification.model.SurveyResponseException;
import org.openchain.certification.model.User;
import org.openchain.certification.utility.EmailUtilException;
import org.openchain.certification.utility.EmailUtility;
import org.openchain.certification.utility.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

/**
 * Servlet implementation class CertificationServlet
 */
public class CertificationServlet extends HttpServlet {
	/**
	 * Version of this software - should be updated before every release
	 */
	static final String version = "1.2.16"; //$NON-NLS-1$
	
	static final Logger logger = LoggerFactory.getLogger(CertificationServlet.class);
	
	private static final long serialVersionUID = 1L;  //$NON-NLS-1$
	static final String SESSION_ATTRIBUTE_USER = "user";  //$NON-NLS-1$
	static final String LANGUAGE_ATTRIBUTE = "lang";  //$NON-NLS-1$
	public static final String PARAMETER_REQUEST = "request";  //$NON-NLS-1$
	public static final String PARAMETER_LOCALE = "locale";  //$NON-NLS-1$
	public static final String PARAMETER_USERNAME = "username";  //$NON-NLS-1$
	public static final String PARAMETER_UUID = "uuid";  //$NON-NLS-1$
	public static final String PARAMETER_SPEC_VERSION = "specVersion";  //$NON-NLS-1$
	public static final String PARAMETER_GIT_TAG = "tag"; //$NON-NLS-1$
	public static final String PARAMETER_GIT_COMMIT = "commit"; //$NON-NLS-1$
	private static final String GET_SOFTWARE_VERSION_REQUEST = "version";  //$NON-NLS-1$
	private static final String GET_SURVEY = "getsurvey";  //$NON-NLS-1$
	private static final String GET_SUPPORTED_SPEC_VERSIONS = "getSupportedSpecVersions";  //$NON-NLS-1$
	private static final String GET_SURVEY_VERSIONS = "getSurveyVersions";  //$NON-NLS-1$
	private static final String GET_SURVEY_RESPONSE = "getsurveyResponse";  //$NON-NLS-1$
	private static final String GET_SUBMISSIONS = "getsubmissions";  //$NON-NLS-1$
	private static final String GET_USER = "getuser";  //$NON-NLS-1$
	private static final String DOWNLOAD_SURVEY = "downloadSurvey";  //$NON-NLS-1$
	private static final String REGISTER_USER = "register";  //$NON-NLS-1$
	private static final String LOGIN_REQUEST = "login";  //$NON-NLS-1$
	private static final String SIGNUP_REQUEST = "signup";  //$NON-NLS-1$
	private static final String UPDATE_ANSWERS_REQUEST = "updateAnswers";  //$NON-NLS-1$
	private static final String LOGOUT_REQUEST = "logout";  //$NON-NLS-1$
	private static final String FINAL_SUBMISSION_REQUEST = "finalSubmission";  //$NON-NLS-1$
	private static final String UPDATE_SURVEY_REQUEST = "updatesurvey";  //$NON-NLS-1$
	private static final String RESEND_VERIFICATION = "resendverify";  //$NON-NLS-1$
	private static final String DOWNLOAD_ANSWERS = "downloadanswers";  //$NON-NLS-1$
	private static final String SET_APPROVED = "setApproved";  //$NON-NLS-1$
	private static final String RESET_APPROVED = "resetApproved";  //$NON-NLS-1$
	private static final String SET_REJECTED = "setRejected";  //$NON-NLS-1$
	private static final String RESET_REJECTED = "resetRejected";   //$NON-NLS-1$ 
	private static final String GET_CERTIFIED_REQUEST = "getcertified";  //$NON-NLS-1$
	private static final String RESET_ANSWERS_REQUEST = "resetanswers";  //$NON-NLS-1$
	private static final String SET_CURRENT_SURVEY_RESPONSE = "setCurrentSurveyResponse";  //$NON-NLS-1$
	private static final String UPDATE_PROFILE_REQUEST = "updateUser";  //$NON-NLS-1$
	private static final String COMPLETE_PASSWORD_RESET = "pwreset";  //$NON-NLS-1$
	private static final String PASSWORD_CHANGE_REQUEST = "changePassword";  //$NON-NLS-1$
	private static final String REQUEST_RESET_PASSWORD = "requestResetPassword";	  //$NON-NLS-1$
	private static final String REQUEST_UNSUBMIT = "unsubmit";  //$NON-NLS-1$
	private static final String SET_LANGUAGE_REQUEST = "setlanguage";  //$NON-NLS-1$
	private static final String GET_GIT_TAGS_REQUEST = "getGitTags"; //$NON-NLS-1$
	private static final String GET_UPDATE_SURVEY_RESULTS = "getUpdateSurveyResults"; //$NON-NLS-1$
	private static final String GET_SUPPORTED_SPEC_LANGUAGES = "getSupportedSpecLanguages"; //$NON-NLS-1$
	private static final String SET_SURVEY_RESPONSE_LANGUAGE = "setSurveyResponseLanguage"; //$NON-NLS-1$
	
	private Gson gson;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CertificationServlet() {
        super();
        GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Question.class, new QuestionJsonDeserializer());
		gson = builder.create();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setHeader("Cache-Control", "no-cache, must-revalidate");  //$NON-NLS-1$  //$NON-NLS-2$
		String requestParam = request.getParameter(PARAMETER_REQUEST);
		String locale = User.DEFAULT_LANGUAGE;
		response.setContentType("application/json"); //$NON-NLS-1$
		response.setCharacterEncoding("UTF-8");  //$NON-NLS-1$
		if (requestParam != null) {
			PrintWriter out = response.getWriter();
			try {
				HttpSession session = request.getSession(true);
				UserSession user = (UserSession)session.getAttribute(SESSION_ATTRIBUTE_USER);
				locale = request.getParameter(PARAMETER_LOCALE);
				if (locale == null) {
					locale = (String)session.getAttribute(LANGUAGE_ATTRIBUTE);
				}
				if (locale == null) {
					locale = User.DEFAULT_LANGUAGE;
				}
	            response.setContentType("application/json");   //$NON-NLS-1$
	            if (requestParam.equals(GET_SOFTWARE_VERSION_REQUEST)) {
	            	gson.toJson(version, out);
	            } else if (requestParam.equals(GET_CERTIFIED_REQUEST)) {
	            	List<Submission> submissions = getSubmissions(locale);
	            	List<Submission> certifiedSubmissions = new ArrayList<Submission>();
	            	for (Submission submission:submissions) {
	            		if (submission.isApproved()) {
	            			User certifiedUser = User.createNonPrivateInfo(submission.getUser());
	            			Submission certifiedSubmission = new Submission(certifiedUser, true, 
	            					submission.getPercentComplete(), submission.getScore(), 
	            					submission.isApproved(), submission.isRejected(), submission.getSurveyVersion());
	            			certifiedSubmissions.add(certifiedSubmission);
	            		}
	            	}
	            	gson.toJson(certifiedSubmissions, out);
	            } else if (requestParam.equals(REGISTER_USER)) {
	            	String username = request.getParameter(PARAMETER_USERNAME);
	            	String uuid = request.getParameter(PARAMETER_UUID);
	            	try {
	            		UserSession.completeEmailVerification(username, uuid, getServletConfig(),locale);
	            		response.sendRedirect("regcomplete.html?locale="+locale); //$NON-NLS-1$
	            	} catch (Exception ex) {
	            		response.sendRedirect("regfailed.html?locale="+locale); //$NON-NLS-1$
	            	}
	            } else if (requestParam.equals(COMPLETE_PASSWORD_RESET)) {	// result from clicking email link on reset password
	            	String username = request.getParameter(PARAMETER_USERNAME);
	            	String uuid = request.getParameter(PARAMETER_UUID);
	            	if (user != null && user.isLoggedIn()) {
	            		user.logout();
	            	}
	            	user = new UserSession(username, "TEMP", getServletConfig()); //$NON-NLS-1$
	            	session.setAttribute(SESSION_ATTRIBUTE_USER, user);
	            	if (user.verifyPasswordReset(uuid, locale)) {
	            		response.sendRedirect("pwreset.html?locale="+locale); //$NON-NLS-1$
	            	} else {
	            		response.sendRedirect("pwresetinvalid.html?locale="+locale); //$NON-NLS-1$
	            	}
	            } else if (requestParam.equals(GET_USER)) {
	            	if (user == null) {
	            		user = new UserSession(getServletConfig());	// creates a new user that is not logged in and a null username
	            		user.setLanguagePreference(locale);	// Set the language to the session language
	            	}
            		gson.toJson(user, out);
	            } else if (requestParam.equals(GET_SUPPORTED_SPEC_VERSIONS)) {
	            	List<String> supportedSpecVersions = getSupportedSpecVersions(getServletConfig());
	            	gson.toJson(supportedSpecVersions, out);
	            } else if (requestParam.equals(GET_GIT_TAGS_REQUEST)) {
	            	String[] tags = QuestionnaireGitRepo.getQuestionnaireGitRepo(locale).getTags(locale);
	            	gson.toJson(tags, out);
	            } else if (user == null) {
        			// Not logged in - set the status to unauthorized
            		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            	}  else if (requestParam.equals(GET_SURVEY)) {
	            	Connection con = SurveyDatabase.createConnection(getServletConfig());
	            	Survey survey;
	            	try {
	            		SurveyDbDao dao = new SurveyDbDao(con);
	            		survey = dao.getSurvey(null, locale);
	            	} finally {
	            		con.close();
	            	}
	            	gson.toJson(survey, out);
	            } else if (requestParam.equals(GET_SURVEY_RESPONSE)) {
	            	gson.toJson(user.getSurveyResponse(locale), out);
	            } else if (requestParam.equals(GET_SURVEY_VERSIONS)) {
	            	gson.toJson(user.getSurveyResponseSpecVersions(locale), out);
	            } else if (requestParam.equals(GET_SUPPORTED_SPEC_LANGUAGES)) {
	            	gson.toJson(user.getSurveyResponseSpecLanguages(locale), out);
	            } else if (requestParam.equals(GET_SUBMISSIONS)) {
	            	if (!user.isAdmin()) {
	            		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	            	} else {
		            	List<Submission> submissions = getSubmissions(locale);
		            	gson.toJson(submissions, out);
	            	}
	            } else if (requestParam.equals(DOWNLOAD_ANSWERS)) {
	            	response.setContentType("text/csv");  //$NON-NLS-1$
	                response.setHeader("Content-Disposition", "attachment;filename=\"openchain-answers.csv\"");  //$NON-NLS-1$  //$NON-NLS-2$
	            	printAnswers(user, out, locale);
	            } else if (requestParam.equals(DOWNLOAD_SURVEY)) {
	            	if (!user.isAdmin()) {
	            		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	            	} else {
		            	String specVersion = request.getParameter(PARAMETER_SPEC_VERSION);
		                response.setContentType("text/json");  //$NON-NLS-1$
		                response.setHeader("Content-Disposition", "attachment;filename=\"openchain-survey-version-"+specVersion+".json\"");  //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		            	printSurvey(specVersion, locale, out);
	            	}
	            } else if (requestParam.equals(GET_UPDATE_SURVEY_RESULTS)) {
	            	if (!user.isAdmin()) {
	            		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	            	} else {
	            		String tag = request.getParameter(PARAMETER_GIT_TAG);
	            		String commit = request.getParameter(PARAMETER_GIT_COMMIT);
	            		gson.toJson(updateSurvey(tag, commit, locale, false), out);
	            	}
	            } else {
	            	logger.error("Unknown get request: "+requestParam);  //$NON-NLS-1$
	            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	            	response.setContentType("text");  //$NON-NLS-1$
					out.print(I18N.getMessage("CertificationServlet.7",locale,requestParam)); //$NON-NLS-1$
	            }
			} catch (SurveyResponseException e) {
				logger.error("Survey response error in servlet"+".  Request="+request,e);  //$NON-NLS-1$ //$NON-NLS-2$
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.setContentType("text");  //$NON-NLS-1$ 
				out.print(I18N.getMessage("CertificationServlet.9",locale,e.getMessage())); //$NON-NLS-1$
			} catch (SQLException e) {
				logger.error("SQL error in servlet"+".  Request="+request,e);  //$NON-NLS-1$ //$NON-NLS-2$
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.setContentType("text");   //$NON-NLS-1$
				out.print(I18N.getMessage("CertificationServlet.11",locale,e.getMessage())); //$NON-NLS-1$
			} catch (QuestionException e) {
				logger.error("Question error in servlet"+".  Request="+request,e);  //$NON-NLS-1$ //$NON-NLS-2$
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.setContentType("text");  //$NON-NLS-1$ 
				out.print(I18N.getMessage("CertificationServlet.13",locale,e.getMessage())); //$NON-NLS-1$
			} catch (Exception e) {
				logger.error("Uncaught exception",e);  //$NON-NLS-1$
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.setContentType("text");   //$NON-NLS-1$
				out.print(I18N.getMessage("CertificationServlet.14",locale,e.getMessage())); //$NON-NLS-1$
			} finally {
				out.close();
			}
		}
	}


	/**
	 * @return all supported spec versions
	 * @throws SQLException 
	 */
	protected static List<String> getSupportedSpecVersions(ServletConfig config) throws SQLException {
		Connection con = SurveyDatabase.createConnection(config);
		try {
			List<String> allSpecVersions = SurveyDbDao.getSurveyVersions(con);
			Collections.sort(allSpecVersions);
			List<String> retval = new ArrayList<String>();
			for (String fullVersion:allSpecVersions) {
				String ver = UserSession.extractSpecVersion(fullVersion);
				if (!retval.contains(ver)) {
					retval.add(ver);
				}
			} 
			return retval;
		} finally {
			con.close();
		}
	}

	private List<Submission> getSubmissions(String language) throws SQLException, SurveyResponseException, QuestionException {
		Connection con = SurveyDatabase.createConnection(getServletConfig());
		try {
			SurveyResponseDao dao = new SurveyResponseDao(con);
			List<SurveyResponse> allResponses = dao.getSurveyResponses(language);
			List<Submission> retval = new ArrayList<Submission>();
			for (SurveyResponse response:allResponses) {
				retval.add(new Submission(response));
			}
			return retval;
		} finally {
			con.close();
		}
	}

	/**
	 * Print a JSON version of a survey to an output stream
	 * @param specVersion
	 * @param out
	 * @throws SQLException 
	 * @throws QuestionException 
	 * @throws SurveyResponseException 
	 * @throws IOException 
	 */
	private void printSurvey(String specVersion, String language, PrintWriter out) throws SQLException, SurveyResponseException, QuestionException, IOException {
		Connection con = SurveyDatabase.createConnection(getServletConfig());
		try {
			Survey survey = SurveyDbDao.getSurvey(con, specVersion, language);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			// Remove redundant unnecessary fields
			survey.prettify();
			out.print(gson.toJson(survey));
		} finally {
			if (con != null) {
				con.close();
			}
		}
	}

	/**
	 * Print answers to the user survey in a CSV format
	 * @param out
	 * @param locale The local language of the current user
	 * @throws SurveyResponseException 
	 * @throws QuestionException 
	 * @throws SQLException 
	 * @throws IOException 
	 */
	private void printAnswers(UserSession session, PrintWriter out, String locale) throws SQLException, QuestionException, SurveyResponseException, IOException {
		session.getSurveyResponse(locale).printCsv(out);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");  //$NON-NLS-1$
		response.setContentType("application/json"); //$NON-NLS-1$
		response.setCharacterEncoding("UTF-8");  //$NON-NLS-1$
		HttpSession session = request.getSession(true);
		UserSession user = (UserSession)session.getAttribute(SESSION_ATTRIBUTE_USER);
		RequestJson rj = gson.fromJson(new JsonReader(new InputStreamReader(request.getInputStream(), "UTF-8")), RequestJson.class); //$NON-NLS-1$
        PrintWriter out = response.getWriter();
        PostResponse postResponse = new PostResponse(Status.OK);
        String locale = rj.getLocale();
        if (locale == null) {
        	locale = (String)session.getAttribute(LANGUAGE_ATTRIBUTE);
        }
		if (locale == null) {
			locale = User.DEFAULT_LANGUAGE;
		}
        try {
        	if (rj.getRequest() == null) {
        		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    			response.setContentType("text");  //$NON-NLS-1$
    			out.println(I18N.getMessage("CertificationServlet.17",locale)); //$NON-NLS-1$
        	} else if (rj.getRequest().equals(LOGIN_REQUEST)) {
        		if (!ReCaptcha.verifyReCaptcha(rj.getReCaptchaResponse(), locale)) {
	        		postResponse.setStatus(Status.ERROR);
	    			postResponse.setError(I18N.getMessage("CertificationServlet.71", locale)); //$NON-NLS-1$
        		} else {
        			if (user != null) {
            			user.logout();
            		}
            		UserSession newUser = new UserSession(rj.getUsername(),rj.getPassword(), getServletConfig());
            		if (newUser.login(locale)) {
            			session.setAttribute(SESSION_ATTRIBUTE_USER, newUser);
            			postResponse.setAdmin(newUser.isAdmin());
            			postResponse.setLanguagePreference(newUser.getLanguagePreference());
            			logger.info("User "+rj.getUsername()+" logged in");
            		} else if (newUser.isValidPasswordAndNotVerified(locale)) {
            			postResponse.setStatus(Status.NOT_VERIFIED);
            			postResponse.setError(I18N.getMessage("CertificationServlet.18",locale)); //$NON-NLS-1$
            		} else {
            			postResponse.setStatus(Status.ERROR);
            			postResponse.setError(I18N.getMessage("CertificationServlet.19",locale,newUser.getLastError())); //$NON-NLS-1$
            		}
        		}
        	} else if (rj.getRequest().equals(PASSWORD_CHANGE_REQUEST)) {	// request from the pwreset.html form
        		if (!ReCaptcha.verifyReCaptcha(rj.getReCaptchaResponse(), locale)) {
	        		postResponse.setStatus(Status.ERROR);
	    			postResponse.setError(I18N.getMessage("CertificationServlet.71", locale)); //$NON-NLS-1$
        		} else if (user == null || !user.isPasswordReset()) {
        			postResponse.setStatus(Status.ERROR);
        			logger.info("Invalid state for password reset for user "+ rj.getUsername());  //$NON-NLS-1$
        			postResponse.setError(I18N.getMessage("CertificationServlet.20",locale)); //$NON-NLS-1$
        		} else {
        			if (!user.setPassword(rj.getUsername(), rj.getPassword(), locale)) {
        				postResponse.setStatus(Status.ERROR);
        				logger.error("Unable to set password for user "+rj.getUsername());  //$NON-NLS-1$
        				postResponse.setError(user.getLastError());
        			}
        		}
        	} else if (rj.getRequest().equals(RESEND_VERIFICATION)) {
        		//Note: We do not need to check any reCaptchas since the passwords must match for the resend verification to work
        		//      and the captcha was already verified during the previous login
        		if (user != null && user.isLoggedIn()) {
        			postResponse.setStatus(Status.ERROR);
        			postResponse.setError(I18N.getMessage("CertificationServlet.21",locale)); //$NON-NLS-1$
        		} else {
        			UserSession newUser = new UserSession(rj.getUsername(),rj.getPassword(), getServletConfig());
            		String verificationUrl = request.getRequestURL().toString();
            		if (!newUser.resendVerification(rj.getUsername(), rj.getPassword(), verificationUrl, locale)) {
            			postResponse.setStatus(Status.ERROR);
            			postResponse.setError(I18N.getMessage("CertificationServlet.22",locale,newUser.getLastError())); //$NON-NLS-1$
            		}
        		}
        	} else if (rj.getRequest().equals(SIGNUP_REQUEST)) {
        		if (!ReCaptcha.verifyReCaptcha(rj.getReCaptchaResponse(), locale)) {
	        		postResponse.setStatus(Status.ERROR);
	    			postResponse.setError(I18N.getMessage("CertificationServlet.71", locale)); //$NON-NLS-1$
        		} else {
        			if (user != null) {
            			user.logout();
            		}
            		UserSession newUser = new UserSession(rj.getUsername(), rj.getPassword(), getServletConfig());
            		String verificationUrl = request.getRequestURL().toString();
            		if (!newUser.signUp(rj.getName(), rj.getAddress(), rj.getOrganization(), 
            				rj.getEmail(), verificationUrl, rj.getNamePermission(), rj.getEmailPermission(), 
            				rj.getLanguage(), locale)) {
            			postResponse.setStatus(Status.ERROR);
            			postResponse.setError(I18N.getMessage("CertificationServlet.22",locale,newUser.getLastError())); //$NON-NLS-1$
            		}
        		}
        	} else if (rj.getRequest().equals(REQUEST_RESET_PASSWORD)) {
        		if (!ReCaptcha.verifyReCaptcha(rj.getReCaptchaResponse(), locale)) {
	        		postResponse.setStatus(Status.ERROR);
	    			postResponse.setError(I18N.getMessage("CertificationServlet.71", locale)); //$NON-NLS-1$
        		} else if (!resetPassword(rj.getUsername(), rj.getEmail(), getServletConfig(), request.getRequestURL().toString())) {
	        		postResponse.setStatus(Status.ERROR);
	        		postResponse.setError(I18N.getMessage("CertificationServlet.24",locale)); //$NON-NLS-1$
        		}
        	}else if (rj.getRequest().equals(SET_LANGUAGE_REQUEST)) {
        		session.setAttribute(LANGUAGE_ATTRIBUTE, rj.getLanguage());
        		if (user != null) {
        			user.setLanguagePreference(rj.getLanguage());
        		}
        	} else if (user == null || !user.isLoggedIn()) {
        		if (!rj.getRequest().equals(LOGOUT_REQUEST)) {	// Ignore the logout request if not logged in
            		// Not logged in - set the status to unauthorized
            		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            		postResponse.setStatus(Status.ERROR);
            		postResponse.setError(I18N.getMessage("CertificationServlet.25",locale)); //$NON-NLS-1$
        		}
        	} else if (rj.getRequest().equals(UPDATE_PROFILE_REQUEST)) {
        		user.updateUser(rj.getName(), rj.getEmail(), rj.getOrganization(), 
        				rj.getAddress(), rj.getPassword(), rj.getNamePermission(), 
        				rj.getEmailPermission(), rj.getLanguage(), locale);
        	} else if (rj.getRequest().equals(UPDATE_ANSWERS_REQUEST)) {
        		if (rj.getAnswers() != null && rj.getAnswers().size() > 0) {
            		user.updateAnswers(rj.getAnswers(), locale);
        		}
        	} else if (rj.getRequest().equals(FINAL_SUBMISSION_REQUEST)) {
        		if (rj.getAnswers() != null && rj.getAnswers().size() > 0) {
            		user.updateAnswers(rj.getAnswers(), locale);
        		}
        		if (!user.finalSubmission(locale)) {
        			postResponse.setStatus(Status.ERROR);
        			postResponse.setError(user.getLastError());
        		}
        	} else if (rj.getRequest().equals(RESET_ANSWERS_REQUEST)) {
        		user.resetAnswers(rj.getSpecVersion(), locale);
        	} else if (rj.getRequest().equals(SET_CURRENT_SURVEY_RESPONSE)) {
        		user.setCurrentSurveyResponse(rj.getSpecVersion(), rj.isCreate(), locale);
        	} else if (rj.getRequest().equals(SET_SURVEY_RESPONSE_LANGUAGE)) {
        		user.setSurveyResponseLanguage(rj.getLanguage(), locale);
        	} else if (rj.getRequest().equals(REQUEST_UNSUBMIT)) {
        		user.unsubmit(locale);
        	} else if (rj.getRequest().equals(UPDATE_SURVEY_REQUEST)) {
        		if (user.isAdmin()) {
					SurveyUpdateResult result = updateSurvey(rj.getTag(), rj.getCommit(), locale, true);
					postResponse.setSurveyUpdateResult(result);
        		} else {
        			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        			postResponse.setStatus(Status.ERROR);
	        		postResponse.setError(I18N.getMessage("CertificationServlet.29",locale)); //$NON-NLS-1$
        		}
        		
        	} else if (rj.getRequest().equals(SET_APPROVED)) {
        		if (user.isAdmin()) {
        			Connection con = SurveyDatabase.createConnection(getServletConfig());
        			try {
            			SurveyResponseDao dao = new SurveyResponseDao(con);
            			dao.setApproved(rj.getIds(),true);
        			} finally {
        				con.close();
        			}
        		} else {
        			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        			postResponse.setStatus(Status.ERROR);
	        		postResponse.setError(I18N.getMessage("CertificationServlet.32",locale)); //$NON-NLS-1$
        		}
        	} else if (rj.getRequest().equals(RESET_APPROVED)) {
        		if (user.isAdmin()) {
        			Connection con = SurveyDatabase.createConnection(getServletConfig());
        			try {
            			SurveyResponseDao dao = new SurveyResponseDao(con);
            			dao.setApproved(rj.getIds(),false);
        			} finally {
        				con.close();
        			}
        		} else {
        			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        			postResponse.setStatus(Status.ERROR);
	        		postResponse.setError(I18N.getMessage("CertificationServlet.32",locale)); //$NON-NLS-1$
        		}
        	} else if (rj.getRequest().equals(SET_REJECTED)) {
        		if (user.isAdmin()) {
        			Connection con = SurveyDatabase.createConnection(getServletConfig());
        			try {
            			SurveyResponseDao dao = new SurveyResponseDao(con);
            			dao.setRejected(rj.getIds(),true);
        			} finally {
        				con.close();
        			}
        		} else {
        			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        			postResponse.setStatus(Status.ERROR);
	        		postResponse.setError(I18N.getMessage("CertificationServlet.32",locale)); //$NON-NLS-1$
        		}
        	} else if (rj.getRequest().equals(RESET_REJECTED)) {
        		if (user.isAdmin()) {
        			Connection con = SurveyDatabase.createConnection(getServletConfig());
        			try {
            			SurveyResponseDao dao = new SurveyResponseDao(con);
            			dao.setRejected(rj.getIds(),false);
        			} finally {
        				con.close();
        			}
        		} else {
        			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        			postResponse.setStatus(Status.ERROR);
	        		postResponse.setError(I18N.getMessage("CertificationServlet.32",locale)); //$NON-NLS-1$
        		}
        	} else if (rj.getRequest().equals(LOGOUT_REQUEST)) {
        		user.logout();
        	} else {
        		logger.error("Unknown post request: "+rj.getRequest());  //$NON-NLS-1$
    			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    			postResponse.setStatus(Status.ERROR);
    			postResponse.setError(I18N.getMessage("CertificationServlet.36",locale,rj.getRequest())); //$NON-NLS-1$
        	}
        	gson.toJson(postResponse, out);
        } catch (SQLException e) {
        	logger.error("SQL Error in post"+".  Request="+rj.getRequest(),e);  //$NON-NLS-1$ //$NON-NLS-2$
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError(I18N.getMessage("CertificationServlet.11",locale,e.getMessage())); //$NON-NLS-1$
			gson.toJson(postResponse, out);
		} catch (QuestionException e) {
        	logger.error("Question Error in post"+".  Request="+rj.getRequest(),e);  //$NON-NLS-1$ //$NON-NLS-2$
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError(I18N.getMessage("CertificationServlet.41",locale,e.getMessage())); //$NON-NLS-1$
			gson.toJson(postResponse, out);
		} catch (SurveyResponseException e) {
        	logger.error("Survey Response error in post"+".  Request="+rj.getRequest(),e);  //$NON-NLS-1$ //$NON-NLS-2$
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError(I18N.getMessage("CertificationServlet.9",locale,e.getMessage())); //$NON-NLS-1$
			gson.toJson(postResponse, out);
		} catch (EmailUtilException e) {
        	logger.error("Error sending email in post"+".  Request="+rj.getRequest(),e);  //$NON-NLS-1$ //$NON-NLS-2$
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError(I18N.getMessage("CertificationServlet.45",locale,e.getMessage())); //$NON-NLS-1$
			gson.toJson(postResponse, out);
		} catch(GitRepoException e) {
        	logger.error("GIT Repository exception.  Request="+rj.getRequest(),e);  //$NON-NLS-1$ //$NON-NLS-2$
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError(I18N.getMessage("CertificationServlet.1",locale)); //$NON-NLS-1$
			gson.toJson(postResponse, out);
		} catch(ReCaptchaException e) {
        	logger.error("ReCaptcha exception.  Request="+rj.getRequest(),e);  //$NON-NLS-1$ //$NON-NLS-2$
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError(e.getMessage()); //$NON-NLS-1$
			gson.toJson(postResponse, out);
		} catch (Exception e) {
        	logger.error("Uncaught exception",e);  //$NON-NLS-1$
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError(I18N.getMessage("CertificationServlet.14",locale,e.getMessage())); //$NON-NLS-1$
			gson.toJson(postResponse, out);
		} finally {
        	out.close();
        }
	}

	/**
	 * Send the reset password email only if the username and email match
	 * @param username
	 * @param email
	 * @param config
	 * @return
	 */
	private boolean resetPassword(String username, String email, ServletConfig config, String responseServletUrl) {
		User user = null;
		try {
			user = UserDb.getUserDb(config).getUser(username);
			if (user == null) {
				return false;
			}
			if (!user.getEmail().equals(email.trim())) {
				return false;
			}	
			user.setVerificationExpirationDate(UserSession.generateVerificationExpirationDate());
			UUID uuid = UUID.randomUUID();
			String hashedUuid = PasswordUtil.getToken(uuid.toString());
			user.setUuid(hashedUuid);
			user.setPasswordReset(true);
			UserDb.getUserDb(config).updateUser(user);
			EmailUtility.emailPasswordReset(user.getName(), email, uuid, username, responseServletUrl, config, user.getLanguagePreference());
	        return true;
		} catch (SQLException e) {
			logger.error("SQL Exception signing up user",e);  //$NON-NLS-1$
			return false;
		} catch (NoSuchAlgorithmException e) {
			logger.error("Unexpected No Such Algorithm error signing up user",e);  //$NON-NLS-1$
			return false;
		} catch (InvalidKeySpecException e) {
			logger.error("Unexpected Invalid Key Spec error signing up user",e);  //$NON-NLS-1$
			return false;
		} catch (EmailUtilException e) {
			logger.error("Error emailing invitation",e);  //$NON-NLS-1$
			return false;
		} catch (InvalidUserException e) {
			logger.error("Invalid user specified in add user request",e);  //$NON-NLS-1$
			return false;
		}
	}
	
	/**
	 * Update the survey results from the Questionnaire GIT repository.  New languages or versions
	 * will be added and existing versions will be updated.  Note - database will only be updated if
	 * updateDB is set to true
	 * @param tag Git tag to be used - most recent if null
	 * @param commit Commit hash to be used - if null, the most recent will be used
	 * @param language Language to be used for error handling
	 * @param updateDb if true, the database will be updated.  If false, only stats will be calculated
	 * @return statistics on what would be updated if the updateDb is set to true
	 * @throws GitRepoException
	 * @throws SQLException
	 */
	protected SurveyUpdateResult updateSurvey(String tag, String commit, String language, boolean updateDb) throws GitRepoException, SQLException {
		SurveyUpdateResult result = new SurveyUpdateResult();
		QuestionnaireGitRepo repo = QuestionnaireGitRepo.getQuestionnaireGitRepo(language);
		repo.refresh(language);
		repo.lock();
		Connection con = null;
		try {
			con = SurveyDatabase.createConnection(this.getServletConfig());
			SurveyDbDao dao = new SurveyDbDao(con);
			repo.checkOut(tag, commit, language);
			result.setCommit(repo.getHeadCommit(language));
			Iterator<File> jsonFiles = repo.getQuestionnaireJsonFiles(language);
			while (jsonFiles.hasNext()) {
				File jsonFile = jsonFiles.next();
				updateSurveyFromFile(jsonFile, language, updateDb, result, dao, gson);
			}
			result.verify(language);
			return result;
		} finally {
			repo.unlock();
			if (con != null) {
				con.close();
			}
		}
	}

	/**
	 * Update the survey results from a JSON file containing a the survey information.  New languages or versions
	 * will be added and existing versions will be updated.  Note - database will only be updated if
	 * updateDB is set to true
	 * @param jsonFile File containing the new survey in JSON format
	 * @param language Language to be used for error handling
	 * @param updateDb if true, the database will be updated.  If false, only stats will be calculated
	 * @param stats statistics on what would be updated if the updateDb is set to true
	 * @param dao Survey DBDAO
	 * @throws SQLException 
	 */
	protected static void updateSurveyFromFile(File jsonFile, String language, boolean updateDb,
			SurveyUpdateResult stats, SurveyDbDao dao, Gson gson) throws SQLException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile), "UTF8")); //$NON-NLS-1$
			Survey survey = gson.fromJson(reader, Survey.class);
			survey.addInfoToSectionQuestions();
			stats.addVersionQuestions(survey.getSpecVersion(), survey.getLanguage(), survey.getQuestionNumbers(), language);
			if (dao.surveyExists(survey.getSpecVersion(), survey.getLanguage(), false)) {
				try {
					SurveyQuestionUpdateStats updateStats = updateSurveyQuestions(survey, language, dao, updateDb);
					if (updateStats.getNumChanges() > 0) {
						stats.addVersionUpdated(survey.getSpecVersion(), survey.getLanguage(), updateStats, language);
					}
				} catch (QuestionException e) {
					logger.error("Question error updating spec version "+survey.getSpecVersion()+" language "+survey.getLanguage(),e);  //$NON-NLS-1$    //$NON-NLS-2$
					stats.addWarning(I18N.getMessage("CertificationServlet.8", language, survey.getSpecVersion(), survey.getLanguage())); //$NON-NLS-1$
				} catch (SurveyResponseException e) {
					logger.error("Survey response error updating spec version "+survey.getSpecVersion()+" language "+survey.getLanguage(),e);  //$NON-NLS-1$    //$NON-NLS-2$
					stats.addWarning(I18N.getMessage("CertificationServlet.8", language, survey.getSpecVersion(), survey.getLanguage())); //$NON-NLS-1$
				} catch (UpdateSurveyException e) {
					logger.error("Update survey error updating spec version "+survey.getSpecVersion()+" language "+survey.getLanguage(),e);  //$NON-NLS-1$    //$NON-NLS-2$
					stats.addWarning(I18N.getMessage("CertificationServlet.8", language, survey.getSpecVersion(), survey.getLanguage()) + "  " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (IOException e) {
					logger.error("I/O error updating spec version "+survey.getSpecVersion()+" language "+survey.getLanguage(),e);  //$NON-NLS-1$    //$NON-NLS-2$
					stats.addWarning(I18N.getMessage("CertificationServlet.8", language, survey.getSpecVersion(), survey.getLanguage())); //$NON-NLS-1$
				}
			} else {
				List<String> surveyWarnings = survey.verify(language);
				if (surveyWarnings.size() > 0) {
					StringBuilder sb = new StringBuilder(surveyWarnings.get(0));
					for (int i = 1; i < surveyWarnings.size(); i++) {
						sb.append("; "); //$NON-NLS-1$
						sb.append(surveyWarnings.get(i));
					}
					logger.error("Errors found in survey: "+sb.toString()); //$NON-NLS-1$
					stats.addWarning(I18N.getMessage("CertificationServlet.70",language,survey.getSpecVersion(),sb.toString())); //$NON-NLS-1$
				} else {
					if (updateDb) {
						try {
							logger.info("Adding survey questions for spec version "+survey.getSpecVersion()+", "+survey.getLanguage());  //$NON-NLS-1$    //$NON-NLS-2$
							dao.addSurvey(survey);
						} catch (SurveyResponseException e) {
							logger.error("Survey response error adding survey version "+survey.getSpecVersion()+" language "+survey.getLanguage(),e);  //$NON-NLS-1$    //$NON-NLS-2$
							stats.addWarning(I18N.getMessage("CertificationServlet.8", language, survey.getSpecVersion(), survey.getLanguage())); //$NON-NLS-1$
						} catch (QuestionException e) {
							logger.error("Question exception adding survey version "+survey.getSpecVersion()+" language "+survey.getLanguage(),e);  //$NON-NLS-1$    //$NON-NLS-2$
							stats.addWarning(I18N.getMessage("CertificationServlet.8", language, survey.getSpecVersion(), survey.getLanguage())); //$NON-NLS-1$
						}
					}
					stats.addVersionAdded(survey.getSpecVersion(), survey.getLanguage(), language);
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("File not found while updating survey questions from GIT: "+jsonFile.getName()); //$NON-NLS-1$
			stats.addWarning(I18N.getMessage("CertificationServlet.27",language)); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e1) {
			logger.error("Unsupported encoding exception found while updating survey questions from GIT: "+jsonFile.getName()); //$NON-NLS-1$
			stats.addWarning(I18N.getMessage("CertificationServlet.28",language)); //$NON-NLS-1$
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.warn("Unable to close reader for "+jsonFile.getName()); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * @param survey Survey with updated questions
	 * @param language Language to render any error in (this is not necessarily the same language as the survey language)
	 * @param con DB Connection to use
	 * @param updateDb if true, the database will be updated.  If false, only stats will be returned
	 * @throws SQLException
	 * @throws QuestionException
	 * @throws SurveyResponseException
	 * @throws UpdateSurveyException
	 * @throws IOException
	 */
	protected static SurveyQuestionUpdateStats updateSurveyQuestions(Survey survey, String language, SurveyDbDao dao, boolean updateDb) throws SQLException, QuestionException, SurveyResponseException, UpdateSurveyException, IOException {
		SurveyQuestionUpdateStats stats = new SurveyQuestionUpdateStats();
		survey.addInfoToSectionQuestions();
		List<String> surveyWarnings = survey.verify(language);
		if (surveyWarnings.size() > 0) {
			StringBuilder sb = new StringBuilder(surveyWarnings.get(0));
			for (int i = 1; i < surveyWarnings.size(); i++) {
				sb.append("; "); //$NON-NLS-1$
				sb.append(surveyWarnings.get(i));
			}
			logger.error("Errors found in survey for update: "+sb.toString()); //$NON-NLS-1$
			throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.70",language,survey.getSpecVersion(),sb.toString()))); //$NON-NLS-1$
		}
		long existingId = dao.getSpecId(survey.getSpecVersion(), survey.getLanguage(), false);
		if (existingId < 0) {
			throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.58",language,survey.getSpecVersion()))); //$NON-NLS-1$
		}
		Survey existing = dao.getSurvey(survey.getSpecVersion(), survey.getLanguage());
		if (existing == null) {
			throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.58",language,survey.getSpecVersion()))); //$NON-NLS-1$
		}
		List<Question> updatedQuestions = new ArrayList<>();
		List<Question> addedQuestions = new ArrayList<>();
		Map<String, String> updatedSectionTitles = new HashMap<>();
		Set<String> foundQuestionNumbers = new HashSet<>();
		Set<String> existingQuestionNumbers = existing.getQuestionNumbers();
		Map<String, SubQuestion> questionsWithSubs = new HashMap<String, SubQuestion>();
		for (Section section:survey.getSections()) {
			String existingSectionTitle = null;
			for (Section existingSection:existing.getSections()) {
				if (Objects.equals(existingSection.getName(), section.getName())) {
					existingSectionTitle = existingSection.getTitle();
					break;
				}
			}
			if (Objects.isNull(existingSectionTitle)) {
				throw new UpdateSurveyException(I18N.getMessage("CertificationServlet.12", language, section.getTitle(), survey.getSpecVersion(), survey.getLanguage())); //$NON-NLS-1$
			}
			if (!Objects.equals(section.getTitle(), existingSectionTitle)) {
				updatedSectionTitles.put(section.getName(), section.getTitle());
			}
			for (Question question:section.getQuestions()) {
				if (foundQuestionNumbers.contains(question.getNumber())) {
					throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.60",language,question.getNumber()))); //$NON-NLS-1$
				}
				foundQuestionNumbers.add(question.getNumber());
				if (question.getSubQuestionOfNumber() != null) {
					SubQuestion parentQuestion = questionsWithSubs.get(question.getSubQuestionOfNumber());
					if (parentQuestion == null) {
						parentQuestion = new SubQuestion("REPLACE", "REPLACE", question.getSubQuestionOfNumber(), survey.getSpecVersion(),  //$NON-NLS-1$ //$NON-NLS-2$
								new String[0], survey.getLanguage(), 0); //$NON-NLS-1$ //$NON-NLS-2$
						questionsWithSubs.put(question.getSubQuestionOfNumber(), parentQuestion);
					}
					parentQuestion.addSubQuestion(question);
				}
				if (question instanceof SubQuestion) {
					SubQuestion toBeReplaced = questionsWithSubs.get(question.getNumber());
					if (toBeReplaced != null) {
						for(Question qtoadd:toBeReplaced.getAllSubquestions()) {
							((SubQuestion) question).addSubQuestion(qtoadd);
						}
					}
					questionsWithSubs.put(question.getNumber(), (SubQuestion) question);
				}
				if (existingQuestionNumbers.contains(question.getNumber())) {
					if (!question.equivalent(existing.getQuestion(question.getNumber()))) {
						updatedQuestions.add(question);
					}
				} else {
					addedQuestions.add(question);
				}
			}
		}
		if (!foundQuestionNumbers.containsAll(existingQuestionNumbers)) {
			throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.63",language))); //$NON-NLS-1$
		}
		if (updateDb) {
			logger.info("Updating survey questions for spec version "+survey.getSpecVersion()+", "+survey.getLanguage());  //$NON-NLS-1$    //$NON-NLS-2$
			dao.updateQuestions(updatedQuestions);
			dao.addQuestions(addedQuestions);
			for (Entry<String, String> updatedSectionEntry:updatedSectionTitles.entrySet()) {
				dao.updateSectionTitle(survey.getSpecVersion(), survey.getLanguage(), updatedSectionEntry.getKey(), updatedSectionEntry.getValue());
			}
		}
		stats.addUpdateQuestions(updatedQuestions);
		stats.addAddedQuestions(addedQuestions);
		stats.addUpdatedSectionTitles(updatedSectionTitles);
		return stats;
	}
}
