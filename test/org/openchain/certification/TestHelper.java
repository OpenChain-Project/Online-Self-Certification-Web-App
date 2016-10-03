package org.openchain.certification;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletConfig;

import org.postgresql.ds.PGSimpleDataSource;

public class TestHelper {

	public static final String TEST_DB_NAME = "openchain_test";
	public static final String TEST_DB_USER_NAME = "openchain_test";
	public static final String TEST_DB_PASSWORD = "openchain_test";
	public static final String TEST_DB_HOST = "localhost";
	public static final int TEST_DB_PORT = 5432;
	static final PGSimpleDataSource dataSource = new PGSimpleDataSource();
	static {
		dataSource.setDatabaseName(TEST_DB_NAME);
		dataSource.setUser(TEST_DB_USER_NAME);
		dataSource.setPassword(TEST_DB_PASSWORD);
		dataSource.setServerName(TEST_DB_HOST);
		dataSource.setPortNumber(TEST_DB_PORT);
	}
	
	static final ServletConfig TEST_SERVLET_CONFIG = new MockServletConfig();

	public static Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	public static ServletConfig getTestServletConfig() {
		return TEST_SERVLET_CONFIG;
	}

	/**
	 * Deletes ALL the data in the database
	 * @param con
	 * @throws SQLException 
	 */
	public static void truncateDatabase(Connection con) throws SQLException {
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			stmt.executeUpdate("truncate answer, survey_response, openchain_user, question, section, spec");
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

}
