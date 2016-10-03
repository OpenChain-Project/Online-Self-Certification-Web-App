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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;
import org.openchain.certification.model.User;

/**
 * This is a singleton DAO - since the requests will be relatively short and
 * this will be frequently used, we will maintain a connection just for the user
 * database.
 * 
 * @author Gary O'Neall
 *
 */
public class UserDb {
	
	static final Logger logger = Logger.getLogger(UserDb.class);
	
	private static UserDb _userDb;
	private Connection connection;
	private PreparedStatement getUserQuery;
	private PreparedStatement getAllUserQuery;
	private PreparedStatement addUserQuery;
	private PreparedStatement updateVerifiedQuery;
	private ServletConfig servletConfig;
	
	public static synchronized UserDb getUserDb(ServletConfig servletConfig) throws SQLException {
		if (_userDb == null) {
			_userDb = new UserDb(servletConfig);
		}
		_userDb.checkConnection();
		return _userDb;
	}
	
	protected void checkConnection() throws SQLException {
		if (this.connection == null || this.connection.isClosed()) {
			this.connection = SurveyDatabase.createConnection(servletConfig);
		}
	}
	private UserDb(ServletConfig servletConfig) throws SQLException {
		this.servletConfig = servletConfig;
		this.connection = SurveyDatabase.createConnection(servletConfig);
		this.connection.setAutoCommit(false);
		prepareStatements();
	}
	
	private void prepareStatements() throws SQLException {
		getUserQuery = connection.prepareStatement("select password_token, name, address, email," +
				"verified, passwordReset, admin, verificationExpirationDate," +
				" uuid, organization from openchain_user where username=?");
		getAllUserQuery = connection.prepareStatement("select username, password_token, name, address, email," +
				"verified, passwordReset, admin, verificationExpirationDate," +
				" uuid, organization from openchain_user order by username asc");
		addUserQuery = connection.prepareStatement("insert into openchain_user (username, password_token, name, address, email," +
				"verified, passwordReset, admin, verificationExpirationDate," +
				" uuid, organization) values (?,?,?,?,?,?,?,?,?,?,?)");
		updateVerifiedQuery = connection.prepareStatement("update openchain_user set verified=? where username=?");
	}

	public synchronized User getUser(String username) throws SQLException {
		ResultSet result = null;
		try {
			getUserQuery.setString(1, username);
			result = getUserQuery.executeQuery();
			if (!result.next()) {
				return null;
			}
			User retval = new User();
			retval.setAddress(result.getString("address"));
			retval.setAdmin(result.getBoolean("admin"));
			retval.setEmail(result.getString("email"));
			retval.setName(result.getString("name"));
			retval.setPasswordReset(result.getBoolean("passwordReset"));
			retval.setPasswordToken(result.getString("password_token"));
			retval.setUsername(username);
			retval.setUuid(result.getString("uuid"));
			retval.setVerificationExpirationDate(result.getDate("verificationExpirationDate"));
			retval.setVerified(result.getBoolean("verified"));
			retval.setOrganization(result.getString("organization"));
			return retval;
		} finally {
			if (result != null) {
				result.close();
				connection.commit();
			}
		}
	}
	
	public synchronized List<User> getUsers() throws SQLException {
		List<User> retval = new ArrayList<User>();
		ResultSet result = null;
		try {
			result = getAllUserQuery.executeQuery();
			while (result.next()) {
				User user = new User();
				user.setAddress(result.getString("address"));
				user.setAdmin(result.getBoolean("admin"));
				user.setEmail(result.getString("email"));
				user.setName(result.getString("name"));
				user.setPasswordReset(result.getBoolean("passwordReset"));
				user.setPasswordToken(result.getString("password_token"));
				user.setUsername(result.getString("username"));
				user.setUuid(result.getString("uuid"));
				user.setVerificationExpirationDate(result.getDate("verificationExpirationDate"));
				user.setVerified(result.getBoolean("verified"));
				user.setOrganization(result.getString("organization"));
				retval.add(user);
			}
			return retval;
		} finally {
			if (result != null) {
				result.close();
				connection.commit();
			}
		}
	}
	
	public synchronized int addUser(User user) throws SQLException {
		Savepoint save = connection.setSavepoint();
		try {
			this.addUserQuery.setString(1, user.getUsername());
			this.addUserQuery.setString(2, user.getPasswordToken());
			this.addUserQuery.setString(3, user.getName());
			this.addUserQuery.setString(4, user.getAddress());
			this.addUserQuery.setString(5, user.getEmail());
			this.addUserQuery.setBoolean(6, user.isVerified());
			this.addUserQuery.setBoolean(7, user.isPasswordReset());
			this.addUserQuery.setBoolean(8, user.isAdmin());
			java.sql.Date sqlDate = new java.sql.Date(user.getVerificationExpirationDate().getTime());
			this.addUserQuery.setDate(9, sqlDate);
			this.addUserQuery.setString(10, user.getUuid());
			this.addUserQuery.setString(11, user.getOrganization());
			return this.addUserQuery.executeUpdate();
		} catch(SQLException ex) {
			if (save != null) {
				try {
					connection.rollback(save);
				} catch (SQLException ex2) {
					logger.error("Error rolling back transaction",ex2);
				}
			}
			throw(ex);
		} finally {
			if (save != null) {
				this.connection.commit();
			}
		}
	}

	public synchronized int setVerified(String username, boolean verified) throws SQLException {
		Savepoint save = this.connection.setSavepoint();
		try {
			this.updateVerifiedQuery.setBoolean(1, verified);
			this.updateVerifiedQuery.setString(2, username);
			return this.updateVerifiedQuery.executeUpdate();
		} catch(SQLException ex) {
			if (save != null) {
				try {
					connection.rollback(save);
				} catch (SQLException ex2) {
					logger.error("Error rolling back transaction",ex2);
				}
			}
			throw(ex);
		} finally {
			if (save != null) {
				this.connection.commit();
			}
		}
	}
}
