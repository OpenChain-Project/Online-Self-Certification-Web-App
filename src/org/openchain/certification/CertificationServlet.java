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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.openchain.certification.PostResponse.Status;
import org.openchain.certification.dbdao.SurveyDatabase;
import org.openchain.certification.dbdao.SurveyDbDao;
import org.openchain.certification.dbdao.SurveyResponseDao;
import org.openchain.certification.dbdao.UserDb;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.opencsv.CSVParser;

/**
 * Servlet implementation class CertificationServlet
 */
public class CertificationServlet extends HttpServlet {
	/**
	 * Version of this software - should be updated before every release
	 */
	static final String version = "1.1.8"; //$NON-NLS-1$
	
	static final Logger logger = Logger.getLogger(CertificationServlet.class);
	
	private static final long serialVersionUID = 1L;  //$NON-NLS-1$
	static final String SESSION_ATTRIBUTE_USER = "user";  //$NON-NLS-1$
	static final String LANGUAGE_ATTRIBUTE = "lang";  //$NON-NLS-1$
	public static final String PARAMETER_REQUEST = "request";  //$NON-NLS-1$
	public static final String PARAMETER_USERNAME = "username";  //$NON-NLS-1$
	public static final String PARAMETER_UUID = "uuid";  //$NON-NLS-1$
	public static final String PARAMETER_SPEC_VERSION = "specVersion";  //$NON-NLS-1$
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
	private static final String UPLOAD_SURVEY_REQUEST = "uploadsurvey";  //$NON-NLS-1$
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
		String language = User.DEFAULT_LANGUAGE;
		if (requestParam != null) {
			PrintWriter out = response.getWriter();
			try {
				HttpSession session = request.getSession(true);
				UserSession user = (UserSession)session.getAttribute(SESSION_ATTRIBUTE_USER);
				language = (String)session.getAttribute(LANGUAGE_ATTRIBUTE);
				if (language == null) {
					language = User.DEFAULT_LANGUAGE;
				}
	            response.setContentType("application/json");   //$NON-NLS-1$
	            if (requestParam.equals(GET_SOFTWARE_VERSION_REQUEST)) {
	            	gson.toJson(version, out);
	            } else if (requestParam.equals(GET_CERTIFIED_REQUEST)) {
	            	List<Submission> submissions = getSubmissions(language);
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
	            		UserSession.completeEmailVerification(username, uuid, getServletConfig(),language);
	            		response.sendRedirect("regcomplete.html"); //$NON-NLS-1$
	            	} catch (Exception ex) {
	            		response.sendRedirect("regfailed.html"); //$NON-NLS-1$
	            	}	            	
	            } else if (requestParam.equals(COMPLETE_PASSWORD_RESET)) {	// result from clicking email link on reset password
	            	String username = request.getParameter(PARAMETER_USERNAME);
	            	String uuid = request.getParameter(PARAMETER_UUID);
	            	if (user != null && user.isLoggedIn()) {
	            		user.logout();
	            	}
	            	user = new UserSession(username, "TEMP", getServletConfig()); //$NON-NLS-1$
	            	session.setAttribute(SESSION_ATTRIBUTE_USER, user);
	            	if (user.verifyPasswordReset(uuid)) {
	            		response.sendRedirect("pwreset.html"); //$NON-NLS-1$
	            	} else {
	            		response.sendRedirect("pwresetinvalid.html"); //$NON-NLS-1$
	            	}
	            } else if (requestParam.equals(GET_USER)) {
	            	if (user == null) {
	            		user = new UserSession(getServletConfig());	// creates a new user that is not logged in and a null username
	            		user.setLanguage(language);	// Set the language to the session language
	            	}
            		gson.toJson(user, out);
	            } else if (requestParam.equals(GET_SUPPORTED_SPEC_VERSIONS)) {
	            	List<String> supportedSpecVersions = getSupportedSpecVersions(getServletConfig());
	            	gson.toJson(supportedSpecVersions, out);
	            } else if (user == null) {
        			// Not logged in - set the status to unauthorized
            		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            	}  else if (requestParam.equals(GET_SURVEY)) {
	            	Connection con = SurveyDatabase.createConnection(getServletConfig());
	            	Survey survey;
	            	try {
	            		SurveyDbDao dao = new SurveyDbDao(con);
	            		survey = dao.getSurvey(null, language);
	            	} finally {
	            		con.close();
	            	}
	            	gson.toJson(survey, out);
	            } else if (requestParam.equals(GET_SURVEY_RESPONSE)) {
	            	gson.toJson(user.getSurveyResponse(), out);
	            } else if (requestParam.equals(GET_SURVEY_VERSIONS)) {
	            	gson.toJson(user.getSurveyResponseSpecVersions(), out);
	            } else if (requestParam.equals(GET_SUBMISSIONS)) {
	            	if (!user.isAdmin()) {
	            		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	            	} else {
		            	List<Submission> submissions = getSubmissions(language);
		            	gson.toJson(submissions, out);
	            	}
	            } else if (requestParam.equals(DOWNLOAD_ANSWERS)) {
	            	response.setContentType("text/csv");  //$NON-NLS-1$
	                response.setHeader("Content-Disposition", "attachment;filename=\"openchain-answers.csv\"");  //$NON-NLS-1$  //$NON-NLS-2$
	            	printAnswers(user, out);
	            } else if (requestParam.equals(DOWNLOAD_SURVEY)) {
	            	if (!user.isAdmin()) {
	            		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	            	} else {
		            	String specVersion = request.getParameter(PARAMETER_SPEC_VERSION);
		                response.setContentType("text/json");  //$NON-NLS-1$
		                response.setHeader("Content-Disposition", "attachment;filename=\"openchain-survey-version-"+specVersion+".json\"");  //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		            	printSurvey(specVersion, language, out);
	            	}
	            } else {
	            	logger.error("Unknown get request: "+requestParam);  //$NON-NLS-1$
	            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	            	response.setContentType("text");  //$NON-NLS-1$
					out.print(I18N.getMessage("CertificationServlet.7",language,requestParam)); //$NON-NLS-1$
	            }
			} catch (SurveyResponseException e) {
				logger.error("Survey response error in servlet"+".  Request="+request,e);  //$NON-NLS-1$ //$NON-NLS-2$
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.setContentType("text");  //$NON-NLS-1$ 
				out.print(I18N.getMessage("CertificationServlet.9",language,e.getMessage())); //$NON-NLS-1$
			} catch (SQLException e) {
				logger.error("SQL error in servlet"+".  Request="+request,e);  //$NON-NLS-1$ //$NON-NLS-2$
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.setContentType("text");   //$NON-NLS-1$
				out.print(I18N.getMessage("CertificationServlet.11",language,e.getMessage())); //$NON-NLS-1$
			} catch (QuestionException e) {
				logger.error("Question error in servlet"+".  Request="+request,e);  //$NON-NLS-1$ //$NON-NLS-2$
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.setContentType("text");  //$NON-NLS-1$ 
				out.print(I18N.getMessage("CertificationServlet.13",language,e.getMessage())); //$NON-NLS-1$
			} catch (Exception e) {
				logger.error("Uncaught exception",e);  //$NON-NLS-1$
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.setContentType("text");   //$NON-NLS-1$
				out.print(I18N.getMessage("CertificationServlet.14",language,e.getMessage())); //$NON-NLS-1$
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
	 * @throws SurveyResponseException 
	 * @throws QuestionException 
	 * @throws SQLException 
	 * @throws IOException 
	 */
	private void printAnswers(UserSession session, PrintWriter out) throws SQLException, QuestionException, SurveyResponseException, IOException {
		session.getSurveyResponse().printCsv(out);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json"); //$NON-NLS-1$
		HttpSession session = request.getSession(true);
		UserSession user = (UserSession)session.getAttribute(SESSION_ATTRIBUTE_USER);
		RequestJson rj = gson.fromJson(new JsonReader(new InputStreamReader(request.getInputStream())), RequestJson.class);
        PrintWriter out = response.getWriter();
        PostResponse postResponse = new PostResponse(Status.OK);
        String language = User.DEFAULT_LANGUAGE;
        if (rj != null && rj.getLanguage() != null && !rj.getLanguage().isEmpty()) {
        	language = rj.getLanguage();
        }
        if (user != null && user.getLanguage() != null && !user.getLanguage().isEmpty() && User.DEFAULT_LANGUAGE.equals(language)) {
        	language = user.getLanguage();
        }
        try {
        	if (rj.getRequest() == null) {
        		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    			response.setContentType("text");  //$NON-NLS-1$
    			out.println(I18N.getMessage("CertificationServlet.17",language)); //$NON-NLS-1$
        	} else if (rj.getRequest().equals(LOGIN_REQUEST)) {
        		if (user != null) {
        			user.logout();
        		}
        		UserSession newUser = new UserSession(rj.getUsername(),rj.getPassword(), getServletConfig());
        		if (newUser.login()) {
        			session.setAttribute(SESSION_ATTRIBUTE_USER, newUser);
        			postResponse.setAdmin(newUser.isAdmin());
        		} else if (newUser.isValidPasswordAndNotVerified()) {
        			postResponse.setStatus(Status.NOT_VERIFIED);
        			postResponse.setError(I18N.getMessage("CertificationServlet.18",language)); //$NON-NLS-1$
        		} else {
        			postResponse.setStatus(Status.ERROR);
        			postResponse.setError(I18N.getMessage("CertificationServlet.19",language,newUser.getLastError())); //$NON-NLS-1$
        		}
        	} else if (rj.getRequest().equals(PASSWORD_CHANGE_REQUEST)) {	// request from the pwreset.html form
        		if (user == null || !user.isPasswordReset()) {
        			postResponse.setStatus(Status.ERROR);
        			logger.error("Invalid state for password reset for user "+ rj.getUsername());  //$NON-NLS-1$
        			postResponse.setError(I18N.getMessage("CertificationServlet.20",language)); //$NON-NLS-1$
        		} else {
        			if (!user.setPassword(rj.getUsername(), rj.getPassword())) {
        				postResponse.setStatus(Status.ERROR);
        				logger.error("Unable to set password for user "+rj.getUsername());  //$NON-NLS-1$
        				postResponse.setError(user.getLastError());
        			}
        		}
        	} else if (rj.getRequest().equals(RESEND_VERIFICATION)) {
        		if (user != null && user.isLoggedIn()) {
        			postResponse.setStatus(Status.ERROR);
        			postResponse.setError(I18N.getMessage("CertificationServlet.21",language)); //$NON-NLS-1$
        		} else {
            		UserSession newUser = new UserSession(rj.getUsername(),rj.getPassword(), getServletConfig());
            		String verificationUrl = request.getRequestURL().toString();
            		if (!newUser.resendVerification(rj.getUsername(), rj.getPassword(), verificationUrl)) {
            			postResponse.setStatus(Status.ERROR);
            			postResponse.setError(I18N.getMessage("CertificationServlet.22",language,newUser.getLastError())); //$NON-NLS-1$
            		}
        		}
        	} else if (rj.getRequest().equals(SIGNUP_REQUEST)) {
        		if (user != null) {
        			user.logout();
        		}
        		UserSession newUser = new UserSession(rj.getUsername(), rj.getPassword(), getServletConfig());
        		String verificationUrl = request.getRequestURL().toString();
        		if (!newUser.signUp(rj.getName(), rj.getAddress(), rj.getOrganization(), 
        				rj.getEmail(), verificationUrl, rj.getNamePermission(), rj.getEmailPermission(), rj.getLanguage())) {
        			postResponse.setStatus(Status.ERROR);
        			postResponse.setError(I18N.getMessage("CertificationServlet.22",language,newUser.getLastError())); //$NON-NLS-1$
        		}
        	} else if (rj.getRequest().equals(REQUEST_RESET_PASSWORD)) {
        		if (!resetPassword(rj.getUsername(), rj.getEmail(), getServletConfig(), request.getRequestURL().toString())) {
	        		postResponse.setStatus(Status.ERROR);
	        		postResponse.setError(I18N.getMessage("CertificationServlet.24",language)); //$NON-NLS-1$
        		}
        	}else if (rj.getRequest().equals(SET_LANGUAGE_REQUEST)) {
        		if (user != null) {
        			user.setLanguage(rj.getLanguage());
        		}
        		session.setAttribute(LANGUAGE_ATTRIBUTE, rj.getLanguage());
        	} else if (user == null || !user.isLoggedIn()) {
        		if (!rj.getRequest().equals(LOGOUT_REQUEST)) {	// Ignore the logout request if not logged in
            		// Not logged in - set the status to unauthorized
            		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            		postResponse.setStatus(Status.ERROR);
            		postResponse.setError(I18N.getMessage("CertificationServlet.25",language)); //$NON-NLS-1$
        		}
        	} else if (rj.getRequest().equals(UPDATE_PROFILE_REQUEST)) {
        		user.updateUser(rj.getName(), rj.getEmail(), rj.getOrganization(), 
        				rj.getAddress(), rj.getPassword(), rj.getNamePermission(), rj.getEmailPermission(), rj.getLanguage());
        	} else if (rj.getRequest().equals(UPDATE_ANSWERS_REQUEST)) {
        		if (rj.getAnswers() != null && rj.getAnswers().size() > 0) {
            		user.updateAnswers(rj.getAnswers());
        		}
        	} else if (rj.getRequest().equals(FINAL_SUBMISSION_REQUEST)) {
        		if (rj.getAnswers() != null && rj.getAnswers().size() > 0) {
            		user.updateAnswers(rj.getAnswers());
        		}
        		if (!user.finalSubmission()) {
        			postResponse.setStatus(Status.ERROR);
        			postResponse.setError(user.getLastError());
        		}
        	} else if (rj.getRequest().equals(RESET_ANSWERS_REQUEST)) {
        		user.resetAnswers(rj.getSpecVersion());
        	} else if (rj.getRequest().equals(SET_CURRENT_SURVEY_RESPONSE)) {
        		user.setCurrentSurveyResponse(rj.getSpecVersion(), rj.isCreate());
        	} else if (rj.getRequest().equals(REQUEST_UNSUBMIT)) {
        		user.unsubmit();
        	} else if (rj.getRequest().equals(UPLOAD_SURVEY_REQUEST)) {
        		if (user.isAdmin()) {
        			try {
        				uploadSurvey(rj.getSurvey(), language);
    				} catch (UpdateSurveyException e) {
    					logger.warn("Invalid survey question update",e);  //$NON-NLS-1$
    					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	        		postResponse.setStatus(Status.ERROR);
    	        		postResponse.setError(I18N.getMessage("CertificationServlet.26",language,e.getMessage())); //$NON-NLS-1$
    				} catch (SurveyResponseException e) {
    					logger.warn("Invalid survey question update",e);  //$NON-NLS-1$
    					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	        		postResponse.setStatus(Status.ERROR);
    	        		postResponse.setError(I18N.getMessage("CertificationServlet.26",language,e.getMessage())); //$NON-NLS-1$
    				} catch (QuestionException e) {
    					logger.warn("Invalid survey question update",e);  //$NON-NLS-1$
    					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	        		postResponse.setStatus(Status.ERROR);
    	        		postResponse.setError(I18N.getMessage("CertificationServlet.26",language,e.getMessage())); //$NON-NLS-1$
    				}
        		} else {
        			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        			postResponse.setStatus(Status.ERROR);
	        		postResponse.setError(I18N.getMessage("CertificationServlet.29",language)); //$NON-NLS-1$
        		}
        		
        	} else if (rj.getRequest().equals(UPDATE_SURVEY_REQUEST)) {
        		if (user.isAdmin()) {
        			try {
    					updateSurveyQuestions(rj.getSurvey(), language);
    				} catch (UpdateSurveyException e) {
    					logger.warn("Invalid survey question update",e);  //$NON-NLS-1$
    					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	        		postResponse.setStatus(Status.ERROR);
    	        		postResponse.setError(I18N.getMessage("CertificationServlet.30",language,e.getMessage())); //$NON-NLS-1$
    				}
        		} else {
        			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        			postResponse.setStatus(Status.ERROR);
	        		postResponse.setError(I18N.getMessage("CertificationServlet.29",language)); //$NON-NLS-1$
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
	        		postResponse.setError(I18N.getMessage("CertificationServlet.32",language)); //$NON-NLS-1$
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
	        		postResponse.setError(I18N.getMessage("CertificationServlet.32",language)); //$NON-NLS-1$
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
	        		postResponse.setError(I18N.getMessage("CertificationServlet.32",language)); //$NON-NLS-1$
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
	        		postResponse.setError(I18N.getMessage("CertificationServlet.32",language)); //$NON-NLS-1$
        		}
        	} else if (rj.getRequest().equals(LOGOUT_REQUEST)) {
        		user.logout();
        	} else {
        		logger.error("Unknown post request: "+rj.getRequest());  //$NON-NLS-1$
    			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    			postResponse.setStatus(Status.ERROR);
    			postResponse.setError(I18N.getMessage("CertificationServlet.36",language,rj.getRequest())); //$NON-NLS-1$
        	}
        	gson.toJson(postResponse, out);
        } catch (SQLException e) {
        	logger.error("SQL Error in post"+".  Request="+rj.getRequest(),e);  //$NON-NLS-1$ //$NON-NLS-2$
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError(I18N.getMessage("CertificationServlet.11",language,e.getMessage())); //$NON-NLS-1$
			gson.toJson(postResponse, out);
		} catch (QuestionException e) {
        	logger.error("Question Error in post"+".  Request="+rj.getRequest(),e);  //$NON-NLS-1$ //$NON-NLS-2$
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError(I18N.getMessage("CertificationServlet.41",language,e.getMessage())); //$NON-NLS-1$
			gson.toJson(postResponse, out);
		} catch (SurveyResponseException e) {
        	logger.error("Survey Response error in post"+".  Request="+rj.getRequest(),e);  //$NON-NLS-1$ //$NON-NLS-2$
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError(I18N.getMessage("CertificationServlet.9",language,e.getMessage())); //$NON-NLS-1$
			gson.toJson(postResponse, out);
		} catch (EmailUtilException e) {
        	logger.error("Error sending email in post"+".  Request="+rj.getRequest(),e);  //$NON-NLS-1$ //$NON-NLS-2$
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError(I18N.getMessage("CertificationServlet.45",language,e.getMessage())); //$NON-NLS-1$
			gson.toJson(postResponse, out);
		} catch (Exception e) {
        	logger.error("Uncaught exception",e);  //$NON-NLS-1$
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError(I18N.getMessage("CertificationServlet.14",language,e.getMessage())); //$NON-NLS-1$
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
			EmailUtility.emailPasswordReset(user.getName(), email, uuid, username, responseServletUrl, config, user.getLanguage());
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
	 * Add a new survey to the database
	 * @param survey survey to add
	 * @param language language for the user to be used for rendering any errors (not the language for the survey to be uploaded)
	 * @throws SQLException
	 * @throws UpdateSurveyException
	 * @throws SurveyResponseException
	 * @throws QuestionException
	 */
	private void uploadSurvey(Survey survey, String language) throws SQLException, UpdateSurveyException, SurveyResponseException, QuestionException {
		survey.addInfoToSectionQuestions();
		logger.info("Uploading new survey for spec version "+survey.getSpecVersion() + " language "+survey.getLanguage());  //$NON-NLS-1$  //$NON-NLS-2$
		Connection con = null;
		try {
			con = SurveyDatabase.createConnection(getServletConfig());
			SurveyDbDao dao = new SurveyDbDao(con);
			if (dao.surveyExists(survey.getSpecVersion(), survey.getLanguage(), true)) {
				throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.47",language,survey.getSpecVersion(),survey.getLanguage())));  //$NON-NLS-1$
			}
			dao.addSurvey(survey);
		} finally {
			if (con != null) {
				con.close();
			}
		}
	}

	/**
	 * Upload a new version of a survey
	 * @param specVersion
	 * @param language
	 * @param sectionText
	 * @param csvLines
	 * @throws UpdateSurveyException
	 * @throws SQLException 
	 * @throws QuestionException 
	 * @throws SurveyResponseException 
	 * @throws IOException 
	 */
	@SuppressWarnings("unused")	// Leaving this code in case we want to go back to a CSV version
	private void uploadSurvey(String specVersion, String language,
			SectionTextJson[] sectionTexts, String[] csvLines) throws UpdateSurveyException, SQLException, SurveyResponseException, QuestionException, IOException {
		// Check if the version is aready in the database
		cleanUpLines(csvLines);
		Survey survey = new Survey(specVersion, language);
		Map<String, List<Question>> sectionQuestions = new HashMap<String, List<Question>>();
		List<Section> sections = new ArrayList<Section>();
		for (SectionTextJson sectionText:sectionTexts) {
			Section section = new Section(language);
			if (sectionText.getName() == null || sectionText.getName().isEmpty()) {
				throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.50",language))); //$NON-NLS-1$
			}
			if (sectionText.getTitle() == null || sectionText.getTitle().isEmpty()) {
				throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.51",language))); //$NON-NLS-1$
			}
			section.setName(sectionText.getName());
			section.setTitle(sectionText.getTitle());
			List<Question> questions = new ArrayList<Question>();
			section.setQuestions(questions);
			sectionQuestions.put(sectionText.getName(), questions);
			sections.add(section);
		}
		survey.setSections(sections);
		CSVParser csvParser = new CSVParser();
		Set<String> foundQuestionNumbers = new HashSet<String>();
		Map<String, SubQuestion> questionsWithSubs = new HashMap<String, SubQuestion>();
		validateCsvHeader(csvLines, csvParser, language);
		for (int i = 1; i < csvLines.length; i++) {
			if (csvLines[i] == null || csvLines[i].isEmpty()) {
				logger.warn("Skipping blank CSV line");  //$NON-NLS-1$
				continue;
			}
			Question question = Question.fromCsv(csvParser.parseLine(csvLines[i]), specVersion, language);
			if (foundQuestionNumbers.contains(question.getNumber())) {
				throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.52",language,question.getNumber()))); //$NON-NLS-1$
			}
			foundQuestionNumbers.add(question.getNumber());
			if (question.getSubQuestionOfNumber() != null) {
				SubQuestion parentQuestion = questionsWithSubs.get(question.getSubQuestionOfNumber());
				if (parentQuestion == null) {
					try {
						parentQuestion = new SubQuestion("REPLACE", "REPLACE", question.getSubQuestionOfNumber(), specVersion, language, 0); //$NON-NLS-1$ //$NON-NLS-2$
					} catch(QuestionException ex) {
						throw new UpdateSurveyException(I18N.getMessage("CertificationServlet.55",language,question.getNumber())); //$NON-NLS-1$
					}
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
			List<Question> questionList = sectionQuestions.get(question.getSectionName());
			if (questionList == null) {
				throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.56",language,question.getSectionName(),question.getNumber()))); //$NON-NLS-1$
			}
			questionList.add(question);
		}
		uploadSurvey(survey, language);
	}

	/**
	 * Clean up the lines, removing any trailing new lines or line feeds
	 * @param csvLines
	 */
	private void cleanUpLines(String[] csvLines) {
		for (int i = 0; i < csvLines.length; i++) {
			if (csvLines[i].endsWith("\n")) {  //$NON-NLS-1$
				csvLines[i] = csvLines[i].substring(0, csvLines[i].length()-1);
			}
			if (csvLines[i].endsWith("\r")) {  //$NON-NLS-1$
				csvLines[i] = csvLines[i].substring(0, csvLines[i].length()-1);
			}
		}
	}

	/**
	 * Update questions for an existing survey.  Note that questions can not be deleted, 
	 * only added and updated.  
	 * @param specVersion
	 * @param language
	 * @param csvLines
	 * @throws SQLException
	 * @throws QuestionException
	 * @throws SurveyResponseException
	 * @throws UpdateSurveyException
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private void updateSurveyQuestions(String specVersion, String language, String[] csvLines) throws SQLException, QuestionException, SurveyResponseException, UpdateSurveyException, IOException {
		cleanUpLines(csvLines);
		logger.info("Updating survey questions for spec version "+specVersion);  //$NON-NLS-1$
		Connection con = null;
		CSVParser csvParser = new CSVParser();
		
		try {
			con = SurveyDatabase.createConnection(getServletConfig());
			SurveyDbDao dao = new SurveyDbDao(con);
			Survey existing = dao.getSurvey(specVersion, language);
			if (existing == null) {
				throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.58",language,specVersion))); //$NON-NLS-1$
			}
			List<Question> updatedQuestions = new ArrayList<Question>();
			List<Question> addedQuestions = new ArrayList<Question>();
			Set<String> foundQuestionNumbers = new HashSet<String>();
			Set<String> existingQuestionNumbers = existing.getQuestionNumbers();
			Map<String, SubQuestion> questionsWithSubs = new HashMap<String, SubQuestion>();
			validateCsvHeader(csvLines, csvParser, language);
			for (int i = 1; i < csvLines.length; i++) {
				if (csvLines[i] == null || csvLines[i].isEmpty()) {
					logger.warn("Skipping blank CSV line");  //$NON-NLS-1$
					continue;
				}
				Question question = Question.fromCsv(csvParser.parseLine(csvLines[i]), specVersion, language);
				if (foundQuestionNumbers.contains(question.getNumber())) {
					throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.60",language,question.getNumber()))); //$NON-NLS-1$
				}
				foundQuestionNumbers.add(question.getNumber());
				if (question.getSubQuestionOfNumber() != null) {
					SubQuestion parentQuestion = questionsWithSubs.get(question.getSubQuestionOfNumber());
					if (parentQuestion == null) {
						parentQuestion = new SubQuestion("REPLACE", "REPLACE", question.getSubQuestionOfNumber(), specVersion, language, 0); //$NON-NLS-1$ //$NON-NLS-2$
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
					updatedQuestions.add(question);
				} else {
					addedQuestions.add(question);
				}
			}
			if (!existingQuestionNumbers.containsAll(foundQuestionNumbers)) {
				throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.63",language))); //$NON-NLS-1$
			}
			// set the subquestions
			
			dao.updateQuestions(updatedQuestions);
			dao.addQuestions(addedQuestions);
		} finally {
			if (con != null) {
				con.close();
			}
		}
	}
	
	/**
	 * @param survey Survey with updated questions
	 * @param language Language to render any error in (this is not necessarily the same language as the survey language)
	 * @throws SQLException
	 * @throws QuestionException
	 * @throws SurveyResponseException
	 * @throws UpdateSurveyException
	 * @throws IOException
	 */
	private void updateSurveyQuestions(Survey survey, String language) throws SQLException, QuestionException, SurveyResponseException, UpdateSurveyException, IOException {
		survey.addInfoToSectionQuestions();
		logger.info("Updating survey questions for spec version "+survey.getSpecVersion()+", "+survey.getLanguage());  //$NON-NLS-1$    //$NON-NLS-2$
		Connection con = null;
		try {
			con = SurveyDatabase.createConnection(getServletConfig());
			long existingId = SurveyDbDao.getSpecId(con, survey.getSpecVersion(), survey.getLanguage(), false);
			if (existingId < 0) {
				throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.58",language,survey.getSpecVersion()))); //$NON-NLS-1$
			}
			SurveyDbDao dao = new SurveyDbDao(con);
			Survey existing = dao.getSurvey(survey.getSpecVersion(), survey.getLanguage());
			if (existing == null) {
				throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.58",language,survey.getSpecVersion()))); //$NON-NLS-1$
			}
			List<Question> updatedQuestions = new ArrayList<Question>();
			List<Question> addedQuestions = new ArrayList<Question>();
			Set<String> foundQuestionNumbers = new HashSet<String>();
			Set<String> existingQuestionNumbers = existing.getQuestionNumbers();
			Map<String, SubQuestion> questionsWithSubs = new HashMap<String, SubQuestion>();
			for (Section section:survey.getSections()) {
				for (Question question:section.getQuestions()) {
					if (foundQuestionNumbers.contains(question.getNumber())) {
						throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.60",language,question.getNumber()))); //$NON-NLS-1$
					}
					foundQuestionNumbers.add(question.getNumber());
					if (question.getSubQuestionOfNumber() != null) {
						SubQuestion parentQuestion = questionsWithSubs.get(question.getSubQuestionOfNumber());
						if (parentQuestion == null) {
							parentQuestion = new SubQuestion("REPLACE", "REPLACE", question.getSubQuestionOfNumber(), survey.getSpecVersion(), survey.getLanguage(), 0); //$NON-NLS-1$ //$NON-NLS-2$
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
						updatedQuestions.add(question);
					} else {
						addedQuestions.add(question);
					}
				}
			}
			if (!existingQuestionNumbers.containsAll(foundQuestionNumbers)) {
				throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.63",language))); //$NON-NLS-1$
			}
			dao.updateQuestions(updatedQuestions);
			dao.addQuestions(addedQuestions);
		} finally {
			if (con != null) {
				con.close();
			}
		}
	}

	/**
	 * Verifies the header for a CSV questions file and throws an exception if not valid
	 * @param csvLines
	 * @param csvParser
	 * @param language
	 * @throws UpdateSurveyException
	 * @throws IOException
	 */
	private void validateCsvHeader(String[] csvLines, CSVParser csvParser, String language) throws UpdateSurveyException, IOException {
		if (csvLines.length > 0) {
			String[] headerCols = csvParser.parseLine(csvLines[0]);
			if (headerCols.length != Survey.CSV_COLUMNS.length) {
				throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.64",language,String.valueOf(Survey.CSV_COLUMNS.length)))); //$NON-NLS-1$
			}
			for (int i = 0; i < headerCols.length; i++) {
				if (!Objects.equals(Survey.CSV_COLUMNS[i], headerCols[i])) {
					throw(new UpdateSurveyException(I18N.getMessage("CertificationServlet.66",language,Survey.CSV_COLUMNS[i],headerCols[i]))); //$NON-NLS-1$
				}
			}
		}
	}
}
