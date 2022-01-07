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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class to manage the database datasource and connection
 * @author Gary O'Neall
 *
 */
public class SurveyDatabase {
	
	static final Logger logger = LoggerFactory.getLogger(SurveyDatabase.class);
	//TODO: Add connection pooling if needed for performance
	
	private static SurveyDatabase surveyDatabase;
	private PGSimpleDataSource dataSource;
	
	private SurveyDatabase(ServletConfig servletConfig) throws SQLException {
		dataSource = new PGSimpleDataSource();
        String dbName = System.getProperty("RDS_DB_NAME");  //$NON-NLS-1$
        //TODO: Find out why the environment variable is not taking for the DB
        // name in AWS - showing EBDB instead of what was set!
//        if (dbName == null) {
//        	logger.debug("Using servlet dbname");
        	dbName = servletConfig.getServletContext().getInitParameter("openchaindb_dbname"); //$NON-NLS-1$
//        }
        if (dbName == null) {
        	throw new SQLException("NO database configuration found"); //$NON-NLS-1$
        }
        dataSource.setDatabaseName(dbName);
        String userName = System.getProperty("RDS_USERNAME");  //$NON-NLS-1$
        if (userName == null) {
        	logger.debug("Using servlet dbuser"); //$NON-NLS-1$
        	userName = servletConfig.getServletContext().getInitParameter("openchaindb_user"); //$NON-NLS-1$
        }
        dataSource.setUser(userName);
        String password = System.getProperty("RDS_PASSWORD"); //$NON-NLS-1$
        if (password == null) {
        	logger.debug("Using servlet db password"); //$NON-NLS-1$
        	password = servletConfig.getServletContext().getInitParameter("openchaindb_password"); //$NON-NLS-1$
        }
        dataSource.setPassword(password);
        String hostname = System.getProperty("RDS_HOSTNAME"); //$NON-NLS-1$
        if (hostname == null) {
        	logger.debug("Using servlet dbhost name"); //$NON-NLS-1$
        	hostname = servletConfig.getServletContext().getInitParameter("openchaindb_server"); //$NON-NLS-1$
        }
        dataSource.setServerName(hostname);
        String port = System.getProperty("RDS_PORT"); //$NON-NLS-1$
        if (port == null) {
        	logger.debug("Using servlet dbport"); //$NON-NLS-1$
        	port = servletConfig.getServletContext().getInitParameter("openchaindb_port"); //$NON-NLS-1$
        }
        dataSource.setPortNumber(Integer.parseInt(port));
        logger.info("Database connection with database name "+dbName+ //$NON-NLS-1$
        		" on host" + hostname + "with user" + userName); //$NON-NLS-1$ //$NON-NLS-2$
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
