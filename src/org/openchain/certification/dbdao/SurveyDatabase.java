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
import java.sql.SQLException;

import javax.servlet.ServletConfig;

import org.postgresql.ds.PGSimpleDataSource;

/**
 * Singleton class to manage the database datasource and connection
 * @author Gary O'Neall
 *
 */
public class SurveyDatabase {
	//TODO: Add connection pooling if needed for performance
	
	private static SurveyDatabase surveyDatabase;
	private PGSimpleDataSource dataSource;
	
	private SurveyDatabase(ServletConfig servletConfig) throws SQLException {
		dataSource = new PGSimpleDataSource();
        String dbName = System.getProperty("RDS_DB_NAME"); 
        if (dbName == null) {
        	dbName = servletConfig.getServletContext().getInitParameter("openchaindb_dbname");
        }
        if (dbName == null) {
        	throw new SQLException("NO database configuration found");
        }
        dataSource.setDatabaseName(dbName);
        String userName = System.getProperty("RDS_USER"); 
        if (userName == null) {
        	userName = servletConfig.getServletContext().getInitParameter("openchaindb_user");
        }
        dataSource.setUser(userName);
        String password = System.getProperty("RDS_PASSWORD");
        if (password == null) {
        	password = servletConfig.getServletContext().getInitParameter("openchaindb_password");
        }
        dataSource.setPassword(password);
        String hostname = System.getProperty("RDS_HOSTNAME");
        if (hostname == null) {
        	hostname = servletConfig.getServletContext().getInitParameter("openchaindb_server");
        }
        dataSource.setServerName(hostname);
        String port = System.getProperty("RDS_PORT");
        if (port == null) {
        	port = servletConfig.getServletContext().getInitParameter("openchaindb_port");
        }
        dataSource.setPortNumber(Integer.parseInt(port));
	}
	
	public static synchronized Connection createConnection(ServletConfig servletConfig) throws SQLException {
		if (surveyDatabase == null) {
			surveyDatabase = new SurveyDatabase(servletConfig);
		}
		return surveyDatabase._createConnection();
	}

	private Connection _createConnection() throws SQLException {
		return this.dataSource.getConnection();
	}
}
