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
	
	/**
	 * Get the singleton UserDB instance
	 * @param servletConfig
	 * @return
	 * @throws SQLException
	 */
	public static synchronized UserDb getUserDb(ServletConfig servletConfig) throws SQLException {
		if (_userDb == null) {
			_userDb = new UserDb(servletConfig);
		}
		_userDb.checkConnection();
		return _userDb;
	}
	
	/**
	 * Check if the connection is still active, creating the connection if needed
	 * @throws SQLException
	 */
	protected synchronized void checkConnection() throws SQLException {
		if (this.connection == null || this.connection.isClosed()) {
			this.connection = SurveyDatabase.createConnection(servletConfig);
			this.connection.setAutoCommit(false);
			prepareStatements();
		}
	}
	
	/**
	 * This should only be called by the statice getUserDb method
	 * @param servletConfig
	 * @throws SQLException
	 */
	private UserDb(ServletConfig servletConfig) throws SQLException {
		this.servletConfig = servletConfig;
		this.connection = SurveyDatabase.createConnection(servletConfig);
		this.connection.setAutoCommit(false);
		prepareStatements();
	}
	
	/**
	 * Prepare all statements
	 * @throws SQLException
	 */
	private void prepareStatements() throws SQLException {
		getUserQuery = connection.prepareStatement("select password_token, name, address, email," + //$NON-NLS-1$
				"verified, passwordReset, admin, verificationExpirationDate," + //$NON-NLS-1$
				" uuid, organization, name_permission, email_permission, language from openchain_user where username=?"); //$NON-NLS-1$
		getAllUserQuery = connection.prepareStatement("select username, password_token, name, address, email," + //$NON-NLS-1$
				"verified, passwordReset, admin, verificationExpirationDate," + //$NON-NLS-1$
				" uuid, organization, name_permission, email_permission, language from openchain_user order by username asc"); //$NON-NLS-1$
		addUserQuery = connection.prepareStatement("insert into openchain_user (username, password_token, name, address, email," + //$NON-NLS-1$
				"verified, passwordReset, admin, verificationExpirationDate," + //$NON-NLS-1$
				" uuid, organization, name_permission, email_permission, language) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)"); //$NON-NLS-1$
		updateVerifiedQuery = connection.prepareStatement("update openchain_user set verified=? where username=?"); //$NON-NLS-1$
		updateUserQuery = connection.prepareStatement("update openchain_user set password_token=?, " + //$NON-NLS-1$
				"name=?, address=?, verified=?, passwordReset=?, admin=?, " + //$NON-NLS-1$
				"verificationExpirationDate=?, uuid=?, organization=?, email=?, name_permission=?, email_permission=?," + //$NON-NLS-1$
				"language=? where username=?"); //$NON-NLS-1$
		getUserIdQuery = connection.prepareStatement("select id from openchain_user where username=?"); //$NON-NLS-1$
	}

	/**
	 * Get the user from the database
	 * @param username Username of the user
	 * @return populated user from the DB
	 * @throws SQLException
	 */
	public synchronized User getUser(String username) throws SQLException {
		ResultSet result = null;
		try {
			getUserQuery.setString(1, username);
			result = getUserQuery.executeQuery();
			if (!result.next()) {
				return null;
			}
			User retval = new User();
			retval.setAddress(result.getString("address")); //$NON-NLS-1$
			retval.setAdmin(result.getBoolean("admin")); //$NON-NLS-1$
			retval.setEmail(result.getString("email")); //$NON-NLS-1$
			retval.setName(result.getString("name")); //$NON-NLS-1$
			retval.setPasswordReset(result.getBoolean("passwordReset")); //$NON-NLS-1$
			retval.setPasswordToken(result.getString("password_token")); //$NON-NLS-1$
			retval.setUsername(username);
			retval.setUuid(result.getString("uuid")); //$NON-NLS-1$
			retval.setVerificationExpirationDate(result.getDate("verificationExpirationDate")); //$NON-NLS-1$
			retval.setVerified(result.getBoolean("verified")); //$NON-NLS-1$
			retval.setOrganization(result.getString("organization")); //$NON-NLS-1$
			retval.setNamePermission(result.getBoolean("name_permission")); //$NON-NLS-1$
			retval.setEmailPermission(result.getBoolean("email_permission")); //$NON-NLS-1$
			retval.setLanguage(result.getString("language")); //$NON-NLS-1$
			return retval;
		} finally {
			if (result != null) {
				result.close();
			}
			connection.commit();
		}
	}
	
	/**
	 * @return all users from the database
	 * @throws SQLException
	 */
	public synchronized List<User> getUsers() throws SQLException {
		List<User> retval = new ArrayList<User>();
		ResultSet result = null;
		try {
			result = getAllUserQuery.executeQuery();
			while (result.next()) {
				User user = new User();
				user.setAddress(result.getString("address")); //$NON-NLS-1$
				user.setAdmin(result.getBoolean("admin")); //$NON-NLS-1$
				user.setEmail(result.getString("email")); //$NON-NLS-1$
				user.setName(result.getString("name")); //$NON-NLS-1$
				user.setPasswordReset(result.getBoolean("passwordReset")); //$NON-NLS-1$
				user.setPasswordToken(result.getString("password_token")); //$NON-NLS-1$
				user.setUsername(result.getString("username")); //$NON-NLS-1$
				user.setUuid(result.getString("uuid")); //$NON-NLS-1$
				user.setVerificationExpirationDate(result.getDate("verificationExpirationDate")); //$NON-NLS-1$
				user.setVerified(result.getBoolean("verified")); //$NON-NLS-1$
				user.setOrganization(result.getString("organization")); //$NON-NLS-1$
				user.setNamePermission(result.getBoolean("name_permission")); //$NON-NLS-1$
				user.setEmailPermission(result.getBoolean("email_permission")); //$NON-NLS-1$
				user.setLanguage(result.getString("language")); //$NON-NLS-1$
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
	
	/**
	 * Add a user to the database.  The username must not already exist
	 * @param user
	 * @return a positive integer if successful
	 * @throws SQLException
	 * @throws InvalidUserException
	 */
	public synchronized int addUser(User user) throws SQLException, InvalidUserException {
		Savepoint save = connection.setSavepoint();
		long userId = getUserId(user.getUsername());
		if (userId >0) {
			throw(new InvalidUserException("Can not add user "+user.getUsername()+": already exists.")); //$NON-NLS-1$ //$NON-NLS-2$
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
			this.addUserQuery.setBoolean(12, user.hasNamePermission());
			this.addUserQuery.setBoolean(13, user.hasEmailPermission());
			if (user.getLanguage() != null) {
				this.addUserQuery.setString(14, user.getLanguage());
			} else {
				this.addUserQuery.setNull(14, java.sql.Types.VARCHAR);
			}
			return this.addUserQuery.executeUpdate();
		} catch(SQLException ex) {
			if (save != null) {
				try {
					connection.rollback(save);
				} catch (SQLException ex2) {
					logger.error("Error rolling back transaction",ex2); //$NON-NLS-1$
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
	 * 
	 * @param username
	 * @return The unique user ID in the database
	 * @throws SQLException
	 */
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

	/**
	 * @param username
	 * @return true if the user exists
	 * @throws SQLException
	 */
	public synchronized boolean userExists(String username) throws SQLException {
		try {
			return getUserId(username) > 0;
		} finally {
			this.connection.commit();
		}
	}
	/**
	 * Set the verified flag based on the verified parameter
	 * @param username
	 * @param verified
	 * @return
	 * @throws SQLException
	 */
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
					logger.error("Error rolling back transaction",ex2); //$NON-NLS-1$
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
		if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
			throw(new InvalidUserException("Can not update user.  No username specified")); //$NON-NLS-1$
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
			updateUserQuery.setBoolean(11, user.hasNamePermission());
			updateUserQuery.setBoolean(12, user.hasEmailPermission());
			if (user.getLanguage() != null) {
				updateUserQuery.setString(13, user.getLanguage());
			} else {
				updateUserQuery.setNull(13, java.sql.Types.VARCHAR);
			}
			updateUserQuery.setString(14, user.getUsername());
			int count = updateUserQuery.executeUpdate();
			if (count != 1) {
				logger.warn("Unexpected count result from update user query.  Expected 1, found "+String.valueOf(count)); //$NON-NLS-1$
			}
		} finally {
			this.connection.commit();
		}
	}
}
