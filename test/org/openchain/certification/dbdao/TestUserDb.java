package org.openchain.certification.dbdao;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openchain.certification.InvalidUserException;
import org.openchain.certification.TestHelper;
import org.openchain.certification.model.User;

public class TestUserDb {

	Connection con;
	@Before
	public void setUp() throws Exception {
		con = TestHelper.getConnection();
		TestHelper.truncateDatabase(con);
	}

	@After
	public void tearDown() throws Exception {
		con.close();
	}

	@Test
	public void testAddUser() throws SQLException, InvalidUserException {
		User user = new User();
		String address = "Address";
		user.setAddress(address);
		boolean admin = true;
		user.setAdmin(admin);
		String email = "test@openchain.com";
		user.setEmail(email);
		String name = "Test User";
		user.setName(name);
		String organization = "Test Og.";
		user.setOrganization(organization);
		boolean passwordreset = true;
		user.setPasswordReset(passwordreset);
		String token = "TOKEN";
		user.setPasswordToken(token);
		String username = "testuser";
		user.setUsername(username);
		String uuid = UUID.randomUUID().toString();
		user.setUuid(uuid);
		Date expdate = new GregorianCalendar(2015, 4, 16).getTime();
		user.setVerificationExpirationDate(expdate);
		boolean verified = true;
		user.setVerified(verified);
		boolean namePermission = true;
		boolean emailPermission = false;
		user.setNamePermission(namePermission);
		user.setEmailPermission(emailPermission);
		String language = "deu";
		user.setLanguagePreference(language);
		UserDb.getUserDb(TestHelper.getTestServletConfig()).addUser(user);	
		User result = UserDb.getUserDb(TestHelper.getTestServletConfig()).getUser(username);
		assertEquals(address, result.getAddress());
		assertEquals(email, result.getEmail());
		assertEquals(name, result.getName());
		assertEquals(organization, result.getOrganization());
		assertEquals(token, result.getPasswordToken());
		assertEquals(username, result.getUsername());
		assertEquals(uuid, result.getUuid());
		assertEquals(expdate, result.getVerificationExpirationDate());
		assertEquals(admin, result.isAdmin());
		assertEquals(passwordreset, result.isPasswordReset());
		assertEquals(verified, result.isVerified());
		assertEquals(namePermission, result.hasNamePermission());
		assertEquals(emailPermission, result.hasEmailPermission());
		assertEquals(language, result.getLanguagePreference());
	}

	@Test
	public void testSetVerified() throws SQLException, InvalidUserException {
		User user = new User();
		String address = "Address";
		user.setAddress(address);
		boolean admin = true;
		user.setAdmin(admin);
		String email = "test@openchain.com";
		user.setEmail(email);
		String name = "Test User";
		user.setName(name);
		String organization = "Test Og.";
		user.setOrganization(organization);
		boolean passwordreset = true;
		user.setPasswordReset(passwordreset);
		String token = "TOKEN";
		user.setPasswordToken(token);
		String username = "testuser";
		user.setUsername(username);
		String uuid = UUID.randomUUID().toString();
		user.setUuid(uuid);
		Date expdate = new GregorianCalendar(2015, 4, 16).getTime();
		user.setVerificationExpirationDate(expdate);
		boolean verified = true;
		user.setVerified(verified);
		boolean namePermission = true;
		boolean emailPermission = false;
		user.setNamePermission(namePermission);
		user.setEmailPermission(emailPermission);
		UserDb.getUserDb(TestHelper.getTestServletConfig()).addUser(user);	
		User result = UserDb.getUserDb(TestHelper.getTestServletConfig()).getUser(username);
		assertTrue(result.isVerified());
		UserDb.getUserDb(TestHelper.getTestServletConfig()).setVerified(username, false);
		result = UserDb.getUserDb(TestHelper.getTestServletConfig()).getUser(username);
		assertFalse(result.isVerified());
	}

	@Test
	public void testUpdateUser() throws SQLException, InvalidUserException {
		User user = new User();
		String address = "Address";
		user.setAddress(address);
		boolean admin = true;
		user.setAdmin(admin);
		String email = "test@openchain.com";
		user.setEmail(email);
		String name = "Test User";
		user.setName(name);
		String organization = "Test Og.";
		user.setOrganization(organization);
		boolean passwordreset = true;
		user.setPasswordReset(passwordreset);
		String token = "TOKEN";
		user.setPasswordToken(token);
		String username = "testuser";
		user.setUsername(username);
		String uuid = UUID.randomUUID().toString();
		user.setUuid(uuid);
		Date expdate = new GregorianCalendar(2015, 4, 16).getTime();
		user.setVerificationExpirationDate(expdate);
		boolean verified = true;
		user.setVerified(verified);
		boolean namePermission = true;
		boolean emailPermission = false;
		user.setNamePermission(namePermission);
		user.setEmailPermission(emailPermission);
		String language = "ras";
		user.setLanguagePreference(language);
		UserDb.getUserDb(TestHelper.getTestServletConfig()).addUser(user);	
		User result = UserDb.getUserDb(TestHelper.getTestServletConfig()).getUser(username);
		assertEquals(address, result.getAddress());
		assertEquals(email, result.getEmail());
		assertEquals(name, result.getName());
		assertEquals(organization, result.getOrganization());
		assertEquals(token, result.getPasswordToken());
		assertEquals(username, result.getUsername());
		assertEquals(uuid, result.getUuid());
		assertEquals(expdate, result.getVerificationExpirationDate());
		assertEquals(admin, result.isAdmin());
		assertEquals(passwordreset, result.isPasswordReset());
		assertEquals(verified, result.isVerified());
		assertEquals(namePermission, result.hasNamePermission());
		assertEquals(emailPermission, result.hasEmailPermission());
		assertEquals(language, result.getLanguagePreference());
		
		String address2 = "Address2";
		user.setAddress(address2);
		boolean admin2 = false;
		user.setAdmin(admin2);
		String email2 = "2test2@openchain.com";
		user.setEmail(email2);
		String name2 = "Name2 User";
		user.setName(name2);
		String organization2 = "2Test Og.";
		user.setOrganization(organization2);
		boolean passwordreset2 = false;
		user.setPasswordReset(passwordreset2);
		String token2 = "2TOKEN";
		user.setPasswordToken(token2);
		String uuid2 = UUID.randomUUID().toString();
		user.setUuid(uuid2);
		Date expdate2 = new GregorianCalendar(2018, 9, 1).getTime();
		user.setVerificationExpirationDate(expdate2);
		boolean verified2 = false;
		user.setVerified(verified2);
		boolean namePermission2 = false;
		boolean emailPermission2 = true;
		user.setNamePermission(namePermission2);
		user.setEmailPermission(emailPermission2);
		String language2 = "aaa";
		user.setLanguagePreference(language2);
		UserDb.getUserDb(TestHelper.getTestServletConfig()).updateUser(user);
		
		result = UserDb.getUserDb(TestHelper.getTestServletConfig()).getUser(username);
		assertEquals(address2, result.getAddress());
		assertEquals(email2, result.getEmail());
		assertEquals(name2, result.getName());
		assertEquals(organization2, result.getOrganization());
		assertEquals(token2, result.getPasswordToken());
		assertEquals(username, result.getUsername());
		assertEquals(uuid2, result.getUuid());
		assertEquals(expdate2, result.getVerificationExpirationDate());
		assertEquals(admin2, result.isAdmin());
		assertEquals(passwordreset2, result.isPasswordReset());
		assertEquals(verified2, result.isVerified());
		assertEquals(namePermission2, result.hasNamePermission());
		assertEquals(emailPermission2, result.hasEmailPermission());
		assertEquals(language2, result.getLanguagePreference());
		
	}
	@Test
	public void testUserExists() throws SQLException, InvalidUserException {
		User user = new User();
		String address = "Address";
		user.setAddress(address);
		boolean admin = true;
		user.setAdmin(admin);
		String email = "test@openchain.com";
		user.setEmail(email);
		String name = "Test User";
		user.setName(name);
		String organization = "Test Og.";
		user.setOrganization(organization);
		boolean passwordreset = true;
		user.setPasswordReset(passwordreset);
		String token = "TOKEN";
		user.setPasswordToken(token);
		String username = "testuser";
		user.setUsername(username);
		String uuid = UUID.randomUUID().toString();
		user.setUuid(uuid);
		Date expdate = new GregorianCalendar(2015, 4, 16).getTime();
		user.setVerificationExpirationDate(expdate);
		boolean verified = true;
		user.setVerified(verified);
		boolean namePermission = true;
		boolean emailPermission = false;
		user.setNamePermission(namePermission);
		user.setEmailPermission(emailPermission);
		String language = "deu";
		user.setLanguagePreference(language);
		UserDb.getUserDb(TestHelper.getTestServletConfig()).addUser(user);	
		assertTrue(UserDb.getUserDb(TestHelper.getTestServletConfig()).userExists(user.getUsername()));
		assertFalse(UserDb.getUserDb(TestHelper.getTestServletConfig()).userExists("this ain't no user"));
	}
	
	@Test
	public void testNullLanguage() throws SQLException, InvalidUserException {
		User user = new User();
		String address = "Address";
		user.setAddress(address);
		boolean admin = true;
		user.setAdmin(admin);
		String email = "test@openchain.com";
		user.setEmail(email);
		String name = "Test User";
		user.setName(name);
		String organization = "Test Og.";
		user.setOrganization(organization);
		boolean passwordreset = true;
		user.setPasswordReset(passwordreset);
		String token = "TOKEN";
		user.setPasswordToken(token);
		String username = "testuser";
		user.setUsername(username);
		String uuid = UUID.randomUUID().toString();
		user.setUuid(uuid);
		Date expdate = new GregorianCalendar(2015, 4, 16).getTime();
		user.setVerificationExpirationDate(expdate);
		boolean verified = true;
		user.setVerified(verified);
		boolean namePermission = true;
		boolean emailPermission = false;
		user.setNamePermission(namePermission);
		user.setEmailPermission(emailPermission);
		user.setLanguagePreference(null);
		UserDb.getUserDb(TestHelper.getTestServletConfig()).addUser(user);
		
		User result = UserDb.getUserDb(TestHelper.getTestServletConfig()).getUser(username);
		assertTrue(result.getLanguagePreference() == null);
		
		String language = "abc";
		user.setLanguagePreference(language);
		UserDb.getUserDb(TestHelper.getTestServletConfig()).updateUser(user);
		result = UserDb.getUserDb(TestHelper.getTestServletConfig()).getUser(username);
		assertEquals(language, result.getLanguagePreference());
		
		user.setLanguagePreference(null);
		UserDb.getUserDb(TestHelper.getTestServletConfig()).updateUser(user);
		result = UserDb.getUserDb(TestHelper.getTestServletConfig()).getUser(username);
		assertTrue(result.getLanguagePreference() == null);
	}
	
}
