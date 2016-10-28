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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.opencsv.CSVParser;
import com.sun.org.apache.xalan.internal.utils.Objects;

/**
 * Servlet implementation class CertificationServlet
 */
public class CertificationServlet extends HttpServlet {
	/**
	 * Version of this software - should be updated before every release
	 */
	static final String version = "0.0.12";
	
	static final Logger logger = Logger.getLogger(CertificationServlet.class);
	
	private static final long serialVersionUID = 1L;	
	static final String SESSION_ATTRIBUTE_USER = "user";
	public static final String PARAMETER_REQUEST = "request";
	public static final String PARAMETER_USERNAME = "username";
	public static final String PARAMETER_UUID = "uuid";
	public static final String PARAMETER_SPEC_VERSION = "specVersion";
	private static final String GET_VERSION_REQUEST = "version";
	private static final String GET_SURVEY = "getsurvey";
	private static final String GET_SURVEY_RESPONSE = "getsurveyResponse";
	private static final String GET_SUBMISSIONS = "getsubmissions";
	private static final String GET_USER = "getuser";
	private static final String DOWNLOAD_SURVEY = "downloadSurvey";
	private static final String REGISTER_USER = "register";
	private static final String LOGIN_REQUEST = "login";
	private static final String SIGNUP_REQUEST = "signup";
	private static final String UPDATE_ANSWERS_REQUEST = "updateAnswers";
	private static final String LOGOUT_REQUEST = "logout";
	private static final String FINAL_SUBMISSION_REQUEST = "finalSubmission";
	private static final String UPLOAD_SURVEY_REQUEST = "uploadsurvey";
	private static final String UPDATE_SURVEY_REQUEST = "updatesurvey";
	private static final String RESEND_VERIFICATION = "resendverify";
	private static final String DOWNLOAD_ANSWERS = "downloadanswers";
	private static final String SET_APPROVED = "setApproved";
	private static final String RESET_APPROVED = "resetApproved";
	private static final String SET_REJECTED = "setRejected";
	private static final String RESET_REJECTED = "resetRejected";  
	private static final String GET_CERTIFIED_REQUEST = "getcertified";

	private static final String RESET_ANSWERS_REQUEST = "resetanswers";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CertificationServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		String requestParam = request.getParameter(PARAMETER_REQUEST);
		if (requestParam != null) {
			PrintWriter out = response.getWriter();
			try {
				HttpSession session = request.getSession(true);
				UserSession user = (UserSession)session.getAttribute(SESSION_ATTRIBUTE_USER);
				Gson gson = new Gson();
	            response.setContentType("application/json"); 
	            if (requestParam.equals(GET_VERSION_REQUEST)) {
	            	gson.toJson(version, out);
	            } else if (requestParam.equals(GET_CERTIFIED_REQUEST)) {
	            	List<Submission> submissions = getSubmissions();
	            	List<Submission> certifiedSubmissions = new ArrayList<Submission>();
	            	for (Submission submission:submissions) {
	            		if (submission.isApproved()) {
	            			User certifiedUser = User.createNonPrivateInfo(submission.getUser());
	            			Submission certifiedSubmission = new Submission(certifiedUser, true, 
	            					submission.getPercentComplete(), submission.getScore(), 
	            					submission.isApproved(), submission.isRejected());
	            			certifiedSubmissions.add(certifiedSubmission);
	            		}
	            	}
	            	gson.toJson(certifiedSubmissions, out);
	            } else if (requestParam.equals(REGISTER_USER)) {
	            	String username = request.getParameter(PARAMETER_USERNAME);
	            	String uuid = request.getParameter(PARAMETER_UUID);
	            	try {
	            		EmailUtility.completeEmailVerification(username, uuid, getServletConfig());
	            		response.sendRedirect("regcomplete.html");
	            	} catch (Exception ex) {
	            		response.sendRedirect("regfailed.html");
	            	}	            	
	            } else if (requestParam.equals(GET_USER)) {
	            	if (user == null) {
	            		user = new UserSession(getServletConfig());	// creates a new user that is not logged in and a null username
	            	}
            		gson.toJson(user, out);
	            } else if (user == null) {
        			// Not logged in - set the status to unauthorized
            		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            	}  else if (requestParam.equals(GET_SURVEY)) {
	            	Connection con = SurveyDatabase.createConnection(getServletConfig());
	            	Survey survey;
	            	try {
	            		SurveyDbDao dao = new SurveyDbDao(con);
	            		survey = dao.getSurvey();
	            	} finally {
	            		con.close();
	            	}
	            	gson.toJson(survey, out);
	            } else if (requestParam.equals(GET_SURVEY_RESPONSE)) {
	            	gson.toJson(user.getSurveyResponse(), out);
	            } else if (requestParam.equals(GET_SUBMISSIONS)) {
	            	if (!user.isAdmin()) {
	            		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	            	} else {
		            	List<Submission> submissions = getSubmissions();
		            	gson.toJson(submissions, out);
	            	}
	            } else if (requestParam.equals(DOWNLOAD_ANSWERS)) {
	            	response.setContentType("text/csv");
	                response.setHeader("Content-Disposition", "attachment;filename=\"openchain-answers.csv\"");
	            	printAnswers(user, out);
	            } else if (requestParam.equals(DOWNLOAD_SURVEY)) {
	            	if (!user.isAdmin()) {
	            		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	            	} else {
		            	String specVersion = request.getParameter(PARAMETER_SPEC_VERSION);
		                response.setContentType("text/csv");
		                response.setHeader("Content-Disposition", "attachment;filename=\"openchain-survey-version-"+specVersion+".csv\"");
		            	printSurvey(specVersion, out);
	            	}
	            } else {
	            	logger.error("Unknown get request: "+requestParam);
	            	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	            	response.setContentType("text"); 
					out.print("Unknown server request: "+requestParam);
	            }
			} catch (SurveyResponseException e) {
				logger.error("Survey response error in servlet"+".  Request="+request,e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.setContentType("text"); 
				out.print("Unexpected survey response exception.  Please notify the OpenChain technical group that the following error has occured: "+e.getMessage());
			} catch (SQLException e) {
				logger.error("SQL error in servlet"+".  Request="+request,e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.setContentType("text"); 
				out.print("Unexpected SQL exception.  Please notify the OpenChain technical group that the following error has occured: "+e.getMessage());
			} catch (QuestionException e) {
				logger.error("Question error in servlet"+".  Request="+request,e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.setContentType("text"); 
				out.print("Unexpected data exception.  Please notify the OpenChain technical group that the following error has occured: "+e.getMessage());
			} catch (Exception e) {
				logger.error("Uncaught exception",e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.setContentType("text"); 
				out.print("An unexpected error occurred.  Please notify the OpenChain technical group that the following error has occured: "+e.getMessage());
			} finally {
				out.close();
			}
		}
	}


	private List<Submission> getSubmissions() throws SQLException, SurveyResponseException, QuestionException {
		Connection con = SurveyDatabase.createConnection(getServletConfig());
		try {
			SurveyResponseDao dao = new SurveyResponseDao(con);
			List<SurveyResponse> allResponses = dao.getSurveyResponses();
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
	 * Print a CSV version of a survey to an output stream
	 * @param specVersion
	 * @param out
	 * @throws SQLException 
	 * @throws QuestionException 
	 * @throws SurveyResponseException 
	 * @throws IOException 
	 */
	private void printSurvey(String specVersion, PrintWriter out) throws SQLException, SurveyResponseException, QuestionException, IOException {
		Connection con = SurveyDatabase.createConnection(getServletConfig());
		try {
			Survey survey = SurveyDbDao.getSurvey(con, specVersion);
			survey.printCsv(out);
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
		response.setContentType("application/json");
		HttpSession session = request.getSession(true);
		UserSession user = (UserSession)session.getAttribute(SESSION_ATTRIBUTE_USER);
		Gson gson = new Gson();
		RequestJson rj = gson.fromJson(new JsonReader(new InputStreamReader(request.getInputStream())), RequestJson.class);
        PrintWriter out = response.getWriter();
        PostResponse postResponse = new PostResponse(Status.OK);
        try {
        	if (rj.getRequest() == null) {
        		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    			response.setContentType("text"); 
    			out.println("Invalid request - missing request parameter");
        	} else if (rj.getRequest().equals(LOGIN_REQUEST)) {
        		if (user != null) {
        			user.logout();
        		}
        		UserSession newUser = new UserSession(rj.getUsername(),rj.getPassword(), getServletConfig());
        		if (newUser.login()) {
        			session.setAttribute(SESSION_ATTRIBUTE_USER, newUser);
        			postResponse.setAdmin(newUser.isAdmin());
        		} else if (newUser.isValidPasswordAnNotVerified()) {
        			postResponse.setStatus(Status.NOT_VERIFIED);
        			postResponse.setError("User has not been verified.");
        		} else {
        			postResponse.setStatus(Status.ERROR);
        			postResponse.setError("Login failed: "+newUser.getLastError());
        		}
        	} else if (rj.getRequest().equals(RESEND_VERIFICATION)) {
        		if (user != null && user.isLoggedIn()) {
        			postResponse.setStatus(Status.ERROR);
        			postResponse.setError("Can not send a re-registration email for a user that is already logged in.  Log-out first then select resend invitation");
        		} else {
            		UserSession newUser = new UserSession(rj.getUsername(),rj.getPassword(), getServletConfig());
            		String verificationUrl = request.getRequestURL().toString();
            		if (!newUser.resendVerification(rj.getUsername(), rj.getPassword(), verificationUrl)) {
            			postResponse.setStatus(Status.ERROR);
            			postResponse.setError("Error signing up: "+newUser.getLastError());
            		}
        		}
        	} else if (rj.getRequest().equals(SIGNUP_REQUEST)) {
        		if (user != null) {
        			user.logout();
        		}
        		UserSession newUser = new UserSession(rj.getUsername(), rj.getPassword(), getServletConfig());
        		String verificationUrl = request.getRequestURL().toString();
        		if (!newUser.signUp(rj.getName(), rj.getAddress(), rj.getOrganization(), rj.getEmail(), verificationUrl)) {
        			postResponse.setStatus(Status.ERROR);
        			postResponse.setError("Error signing up: "+newUser.getLastError());
        		}
        	} else if (user == null || !user.isLoggedIn()) {
        		// Not logged in - set the status to unauthorized
        		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        		postResponse.setStatus(Status.ERROR);
        		postResponse.setError("User is not logged in");
        	} else if (rj.getRequest().equals(UPDATE_ANSWERS_REQUEST)) {
        		if (rj.getAnswers() != null && rj.getAnswers().size() > 0) {
            		user.updateAnswers(rj.getAnswers());
        		}
        	} else if (rj.getRequest().equals(FINAL_SUBMISSION_REQUEST)) {
        		if (rj.getAnswers() != null && rj.getAnswers().size() > 0) {
            		user.updateAnswers(rj.getAnswers());
        		}
        		user.finalSubmission();
        	} else if (rj.getRequest().equals(RESET_ANSWERS_REQUEST)) {
        		user.resetAnswers();
        	} else if (rj.getRequest().equals(UPLOAD_SURVEY_REQUEST)) {
        		if (user.isAdmin()) {
        			try {
    					uploadSurvey(rj.getSpecVersion(), rj.getSectionTexts(),
    							rj.getCsvLines());
    				} catch (UpdateSurveyException e) {
    					logger.warn("Invalid survey question update",e);
    					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	        		postResponse.setStatus(Status.ERROR);
    	        		postResponse.setError("Invalid survey question update: "+e.getMessage());
    				} catch (SurveyResponseException e) {
    					logger.warn("Invalid survey question update",e);
    					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	        		postResponse.setStatus(Status.ERROR);
    	        		postResponse.setError("Invalid survey question update: "+e.getMessage());
    				} catch (QuestionException e) {
    					logger.warn("Invalid survey question update",e);
    					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	        		postResponse.setStatus(Status.ERROR);
    	        		postResponse.setError("Invalid survey question update: "+e.getMessage());
    				}
        		} else {
        			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        			postResponse.setStatus(Status.ERROR);
	        		postResponse.setError("Must have admin privileges to upload a survey");
        		}
        		
        	} else if (rj.getRequest().equals(UPDATE_SURVEY_REQUEST)) {
        		if (user.isAdmin()) {
        			try {
    					updateSurveyQuestions(rj.getSpecVersion(), rj.getCsvLines());
    				} catch (UpdateSurveyException e) {
    					logger.warn("Invalid survey question update",e);
    					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	        		postResponse.setStatus(Status.ERROR);
    	        		postResponse.setError("Invalid survey upload: "+e.getMessage());
    				}
        		} else {
        			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        			postResponse.setStatus(Status.ERROR);
	        		postResponse.setError("Must have admin privileges to update a survey");
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
	        		postResponse.setError("Must have admin privileges to set submission status");
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
	        		postResponse.setError("Must have admin privileges to set submission status");
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
	        		postResponse.setError("Must have admin privileges to set submission status");
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
	        		postResponse.setError("Must have admin privileges to set submission status");
        		}
        	} else if (rj.getRequest().equals(LOGOUT_REQUEST)) {
        		user.logout();
        	} else {
        		logger.error("Unknown post request: "+rj.getRequest());
    			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    			postResponse.setStatus(Status.ERROR);
    			postResponse.setError("Unknown post request: "+rj.getRequest()+".  Please notify the OpenChain technical group.");
        	}
        	gson.toJson(postResponse, out);
        } catch (SQLException e) {
        	logger.error("SQL Error in post"+".  Request="+rj.getRequest(),e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError("Unexpected SQL exception.  Please notify the OpenChain technical group that the following error has occured: "+e.getMessage());
			gson.toJson(postResponse, out);
		} catch (QuestionException e) {
        	logger.error("Question Error in post"+".  Request="+rj.getRequest(),e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError("Unexpected Question Type exception.  Please notify the OpenChain technical group that the following error has occured: "+e.getMessage());
			gson.toJson(postResponse, out);
		} catch (SurveyResponseException e) {
        	logger.error("Survey Response error in post"+".  Request="+rj.getRequest(),e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError("Unexpected survey response exception.  Please notify the OpenChain technical group that the following error has occured: "+e.getMessage());
			gson.toJson(postResponse, out);
		} catch (EmailUtilException e) {
        	logger.error("Error sending email in post"+".  Request="+rj.getRequest(),e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError("Your submission was saved, however, there was a problem sending the notification to the OpenChain team.  Please notify the OpenChain technical group that the following error has occured: "+e.getMessage());
			gson.toJson(postResponse, out);
		} catch (Exception e) {
        	logger.error("Uncaught exception",e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			postResponse.setStatus(Status.ERROR);
			postResponse.setError("An unexpected error occurred.  Please notify the OpenChain technical group that the following error has occured: "+e.getMessage());
			gson.toJson(postResponse, out);
		} finally {
        	out.close();
        }
	}

	/**
	 * Upload a new version of a survey
	 * @param specVersion
	 * @param sectionText
	 * @param csvLines
	 * @throws UpdateSurveyException
	 * @throws SQLException 
	 * @throws QuestionException 
	 * @throws SurveyResponseException 
	 * @throws IOException 
	 */
	private void uploadSurvey(String specVersion,
			SectionTextJson[] sectionTexts, String[] csvLines) throws UpdateSurveyException, SQLException, SurveyResponseException, QuestionException, IOException {
		// Check if the version is aready in the database
		cleanUpLines(csvLines);
		Survey survey = new Survey(specVersion);
		Map<String, List<Question>> sectionQuestions = new HashMap<String, List<Question>>();
		List<Section> sections = new ArrayList<Section>();
		for (SectionTextJson sectionText:sectionTexts) {
			Section section = new Section();
			if (sectionText.getName() == null || sectionText.getName().isEmpty()) {
				throw(new UpdateSurveyException("No name specified for a section"));
			}
			if (sectionText.getTitle() == null || sectionText.getTitle().isEmpty()) {
				throw(new UpdateSurveyException("No title specified for a section"));
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
		validateCsvHeader(csvLines, csvParser);
		for (int i = 1; i < csvLines.length; i++) {
			if (csvLines[i] == null || csvLines[i].isEmpty()) {
				logger.warn("Skipping blank CSV line");
				continue;
			}
			Question question = Question.fromCsv(csvParser.parseLine(csvLines[i]), specVersion);
			if (foundQuestionNumbers.contains(question.getNumber())) {
				throw(new UpdateSurveyException("Duplicate questions in survey upload: "+question.getNumber()));
			}
			foundQuestionNumbers.add(question.getNumber());
			if (question.getSubQuestionNumber() != null) {
				SubQuestion parentQuestion = questionsWithSubs.get(question.getSubQuestionNumber());
				if (parentQuestion == null) {
					try {
						parentQuestion = new SubQuestion("REPLACE", "REPLACE", question.getSubQuestionNumber(), specVersion, 0);
					} catch(QuestionException ex) {
						throw new UpdateSurveyException("Invalid parent question number for question number "+question.getNumber());
					}
					questionsWithSubs.put(question.getSubQuestionNumber(), parentQuestion);
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
				throw(new UpdateSurveyException("The section "+question.getSectionName()+" does not exist for question "+question.getNumber()));
			}
			questionList.add(question);
		}
		logger.info("Uploading new survey for spec version "+specVersion);
		Connection con = null;
		try {
			con = SurveyDatabase.createConnection(getServletConfig());
			SurveyDbDao dao = new SurveyDbDao(con);
			if (dao.surveyExists(specVersion)) {
				throw(new UpdateSurveyException("Survey version "+specVersion+" already exists.  Can not add.  Use update to update the questions."));
			}
			dao.addSurvey(survey);
		} finally {
			if (con != null) {
				con.close();
			}
		}
	}

	/**
	 * Clean up the lines, removing any trailing new lines or line feeds
	 * @param csvLines
	 */
	private void cleanUpLines(String[] csvLines) {
		for (int i = 0; i < csvLines.length; i++) {
			if (csvLines[i].endsWith("\n")) {
				csvLines[i] = csvLines[i].substring(0, csvLines[i].length()-1);
			}
			if (csvLines[i].endsWith("\r")) {
				csvLines[i] = csvLines[i].substring(0, csvLines[i].length()-1);
			}
		}
	}

	/**
	 * Update questions for an existing survey.  Note that questions can not be deleted, 
	 * only added and updated.  
	 * @param specVersion
	 * @param csvLines
	 * @throws SQLException
	 * @throws QuestionException
	 * @throws SurveyResponseException
	 * @throws UpdateSurveyException
	 * @throws IOException
	 */
	private void updateSurveyQuestions(String specVersion, String[] csvLines) throws SQLException, QuestionException, SurveyResponseException, UpdateSurveyException, IOException {
		cleanUpLines(csvLines);
		logger.info("Updating survey questions for spec version "+specVersion);
		Connection con = null;
		CSVParser csvParser = new CSVParser();
		
		try {
			con = SurveyDatabase.createConnection(getServletConfig());
			SurveyDbDao dao = new SurveyDbDao(con);
			Survey existing = dao.getSurvey(specVersion);
			if (existing == null) {
				throw(new UpdateSurveyException("Survey version "+specVersion+" does not exist.  Can only update questions for an existing survey."));
			}
			List<Question> updatedQuestions = new ArrayList<Question>();
			List<Question> addedQuestions = new ArrayList<Question>();
			Set<String> foundQuestionNumbers = new HashSet<String>();
			Set<String> existingQuestionNumbers = existing.getQuestionNumbers();
			Map<String, SubQuestion> questionsWithSubs = new HashMap<String, SubQuestion>();
			validateCsvHeader(csvLines, csvParser);
			for (int i = 1; i < csvLines.length; i++) {
				if (csvLines[i] == null || csvLines[i].isEmpty()) {
					logger.warn("Skipping blank CSV line");
					continue;
				}
				Question question = Question.fromCsv(csvParser.parseLine(csvLines[i]), specVersion);
				if (foundQuestionNumbers.contains(question.getNumber())) {
					throw(new UpdateSurveyException("Duplicate questions in question update: "+question.getNumber()));
				}
				foundQuestionNumbers.add(question.getNumber());
				if (question.getSubQuestionNumber() != null) {
					SubQuestion parentQuestion = questionsWithSubs.get(question.getSubQuestionNumber());
					if (parentQuestion == null) {
						parentQuestion = new SubQuestion("REPLACE", "REPLACE", question.getSubQuestionNumber(), specVersion, 0);
						questionsWithSubs.put(question.getSubQuestionNumber(), parentQuestion);
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
				throw(new UpdateSurveyException("Not all questions are present in the update.  Questions can not be removed during update.  A new specvesion should be created if questions are to be removed."));
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
	 * Verifies the header for a CSV questions file and throws an exception if not valid
	 * @param csvLines
	 * @param csvParser
	 * @throws UpdateSurveyException
	 * @throws IOException
	 */
	private void validateCsvHeader(String[] csvLines, CSVParser csvParser) throws UpdateSurveyException, IOException {
		if (csvLines.length > 0) {
			String[] headerCols = csvParser.parseLine(csvLines[0]);
			if (headerCols.length != Survey.CSV_COLUMNS.length) {
				throw(new UpdateSurveyException("Invalid CSV format.  Number of columns do not match.  Expected "+
						String.valueOf(Survey.CSV_COLUMNS.length)+" columns."));
			}
			for (int i = 0; i < headerCols.length; i++) {
				if (!Objects.equals(Survey.CSV_COLUMNS[i], headerCols[i])) {
					throw(new UpdateSurveyException("Invalid CSV column.  Expected "+
							Survey.CSV_COLUMNS[i] + ", found "+headerCols[i]));
				}
			}
		}
	}
}
