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
import org.openchain.certification.InvalidUserException;
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
	private PreparedStatement updateUserQuery;
	private PreparedStatement getUserIdQuery;
	private ServletConfig servletConfig;
	
	public static synchronized UserDb getUserDb(ServletConfig servletConfig) throws SQLException {
		if (_userDb == null) {
			_userDb = new UserDb(servletConfig);
		}
		_userDb.checkConnection();
		return _userDb;
	}
	
	protected synchronized void checkConnection() throws SQLException {
		if (this.connection == null || this.connection.isClosed()) {
			this.connection = SurveyDatabase.createConnection(servletConfig);
			this.connection.setAutoCommit(false);
			prepareStatements();
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
		updateUserQuery = connection.prepareStatement("update openchain_user set password_token=?, " +
				"name=?, address=?, verified=?, passwordReset=?, admin=?, " +
				"verificationExpirationDate=?, uuid=?, organization=?, email=? where username=?");
		getUserIdQuery = connection.prepareStatement("select id from openchain_user where username=?");
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
	
	public synchronized int addUser(User user) throws SQLException, InvalidUserException {
		Savepoint save = connection.setSavepoint();
		long userId = getUserId(user.getUsername());
		if (userId >0) {
			throw(new InvalidUserException("Can not add user "+user.getUsername()+": already exists."));
		}
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

	private long getUserId(String username) throws SQLException {
		ResultSet result = null;
		try {
			getUserIdQuery.setString(1, username);
			result = getUserIdQuery.executeQuery();
			if (result.next()) {
				return result.getLong(1);
			} else {
				return -1;
			}
		} finally {
			if (result != null) {
				result.close();
			}
		}
	}

	public synchronized boolean userExists(String username) throws SQLException {
		try {
			return getUserId(username) > 0;
		} finally {
			this.connection.commit();
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

	/**
	 * Update all fields in the user with the username user.username
	 * @param user
	 * @throws SQLException 
	 * @throws InvalidUserException 
	 */
	public synchronized void updateUser(User user) throws SQLException, InvalidUserException {
//		"update openchain_user set password_token=?, " +
//				"name=?, address=?, verified=?, passwordReset=?, admin=?, " +
//				"verificationExpirationDate=?, uuid=?, organization=? where username=?"
		if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
			throw(new InvalidUserException("Can not update user.  No username specified"));
		}
		try {
			updateUserQuery.setString(1, user.getPasswordToken());
			updateUserQuery.setString(2, user.getName());
			updateUserQuery.setString(3, user.getAddress());
			updateUserQuery.setBoolean(4, user.isVerified());
			updateUserQuery.setBoolean(5, user.isPasswordReset());
			updateUserQuery.setBoolean(6, user.isAdmin());
			updateUserQuery.setDate(7, new java.sql.Date(user.getVerificationExpirationDate().getTime()));
			updateUserQuery.setString(8, user.getUuid());
			updateUserQuery.setString(9, user.getOrganization());
			updateUserQuery.setString(10, user.getEmail());
			updateUserQuery.setString(11, user.getUsername());
			int count = updateUserQuery.executeUpdate();
			if (count != 1) {
				logger.warn("Unexpected count result from update user query.  Expected 1, found "+String.valueOf(count));
			}
		} finally {
			this.connection.commit();
		}
	}
}
