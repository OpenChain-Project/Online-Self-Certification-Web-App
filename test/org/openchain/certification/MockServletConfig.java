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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * Mock servlet config for unit tests
 * @author Gary O'Neall
 *
 */
public class MockServletConfig implements ServletConfig {
	
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
	
	ServletContext servletConext = null;

	public MockServletConfig() {
		this.servletConext = new MockServletContext();
	}
	/* (non-Javadoc)
	 * @see javax.servlet.ServletConfig#getInitParameter(java.lang.String)
	 */
	@Override
	public String getInitParameter(String paramName) {
		return INIT_PARAMS.get(paramName);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletConfig#getInitParameterNames()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getInitParameterNames() {
		throw(new RuntimeException("Not implemented"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletConfig#getServletContext()
	 */
	@Override
	public ServletContext getServletContext() {
		return this.servletConext;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletConfig#getServletName()
	 */
	@Override
	public String getServletName() {
		return "MockServlet";
	}

}
