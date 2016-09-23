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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openchain.certification.PostResponse.Status;
import org.openchain.certification.dbdao.AnswersDbDao;
import org.openchain.certification.dbdao.SurveyDatabase;
import org.openchain.certification.dbdao.SurveyDbDao;
import org.openchain.certification.model.QuestionTypeException;
import org.openchain.certification.model.Survey;
import org.openchain.certification.utility.EmailUtility;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

/**
 * Servlet implementation class CertificationServlet
 */
public class CertificationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static final String version = "0.0.1";
	static final String SESSION_ATTRIBUTE_USER = "user";
	public static final String PARAMETER_REQUEST = "request";
	public static final String PARAMETER_USERNAME = "username";
	public static final String PARAMETER_UUID = "uuid";
	private static final String GET_VERSION_REQUEST = "version";
	private static final String GET_SURVEY = "getsurvey";
	private static final String GET_USER = "getuser";
	private static final String REGISTER_USER = "register";
	private static final String LOGIN_REQUEST = "login";
	private static final String SIGNUP_REQUEST = "signup";
	private static final String UPDATE_ANSWERS_REQUEST = "updateAnswers";
	private static final String LOGOUT_REQUEST = "logout";
	
       
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
	            }
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.setContentType("text"); 
				out.print("Unexpected SQL exception.  Please notify the OpenChain technical group that the following error has occured: "+e.getMessage());
			} catch (QuestionTypeException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.setContentType("text"); 
				out.print("Unexpected data exception.  Please notify the OpenChain technical group that the following error has occured: "+e.getMessage());
			} finally {
				out.close();
			}
		}
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
        		} else {
        			postResponse.setStatus(Status.ERROR);
        			postResponse.setError("Login failed: "+newUser.getLastError());
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
            		Connection con = SurveyDatabase.createConnection(getServletConfig());
                	try {
                		AnswersDbDao dao = new AnswersDbDao(con);
                		dao.addOrUpdateAnswers(rj.getAnswers(), user.getUsername());
                		postResponse.setSectionName(dao.getSectionName(rj.getAnswers().get(0)));
                	} finally {
                		con.close();
                	}
        		}
        	} else if (rj.getRequest().equals(LOGOUT_REQUEST)) {
        		user.logout();
        	}
        	gson.toJson(postResponse, out);
        } catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.setContentType("text"); 
			out.print("Unexpected SQL exception.  Please notify the OpenChain technical group that the following error has occured: "+e.getMessage());
		} finally {
        	out.close();
        }
	}
}
