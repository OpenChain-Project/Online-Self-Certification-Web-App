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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Fake servlet context for the unit tests
 * @author Gary O'Neall
 *
 */
public class MockServletContext implements ServletContext {

	static final Map<String, String> INIT_PARAMS = new HashMap<String, String>();
	static {
		INIT_PARAMS.put("openchaindb_dbname", TestHelper.TEST_DB_NAME);
		INIT_PARAMS.put("openchaindb_user", TestHelper.TEST_DB_USER_NAME);
		INIT_PARAMS.put("openchaindb_password", TestHelper.TEST_DB_PASSWORD);
		INIT_PARAMS.put("openchaindb_server", TestHelper.TEST_DB_HOST);
		INIT_PARAMS.put("openchaindb_port", "5432");
		INIT_PARAMS.put("return_email", "no-reply@sourceauditor.com");
		INIT_PARAMS.put("email_ses_region", "us-east-1");
		INIT_PARAMS.put("notification_email", "gary@sourceauditor.com");
	}
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String arg0) {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getAttributeNames()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getAttributeNames() {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getContext(java.lang.String)
	 */
	@Override
	public ServletContext getContext(String arg0) {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
	 */
	@Override
	public String getInitParameter(String arg0) {
		return INIT_PARAMS.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getInitParameterNames()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getInitParameterNames() {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getMajorVersion()
	 */
	@Override
	public int getMajorVersion() {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
	 */
	@Override
	public String getMimeType(String arg0) {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getMinorVersion()
	 */
	@Override
	public int getMinorVersion() {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
	 */
	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
	 */
	@Override
	public String getRealPath(String arg0) {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
	 */
	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getResource(java.lang.String)
	 */
	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
	 */
	@Override
	public InputStream getResourceAsStream(String arg0) {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Set getResourcePaths(String arg0) {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getServerInfo()
	 */
	@Override
	public String getServerInfo() {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getServlet(java.lang.String)
	 */
	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getServletContextName()
	 */
	@Override
	public String getServletContextName() {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getServletNames()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getServletNames() {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getServlets()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getServlets() {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#log(java.lang.String)
	 */
	@Override
	public void log(String arg0) {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#log(java.lang.Exception, java.lang.String)
	 */
	@Override
	public void log(Exception arg0, String arg1) {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#log(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void log(String arg0, Throwable arg1) {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
	 */
	@Override
	public void removeAttribute(String arg0) {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#setAttribute(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setAttribute(String arg0, Object arg1) {
		throw(new RuntimeException("Not implemented"));
	}

}
