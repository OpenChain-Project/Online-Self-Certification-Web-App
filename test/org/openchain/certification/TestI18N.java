package org.openchain.certification;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class TestI18N {
	
	static final String TEST_LANGUAGE = "de";
	static final String TEST_KEY1 = "test.key1";
	static final String TEST_KEY1_DEFAULT_VALUE = "default key1";
	static final String TEST_KEY1_GERMAN_VALUE = "german key1";

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetMessage2char() {
		assertEquals(TEST_KEY1_GERMAN_VALUE, I18N.getMessage(TEST_KEY1, "de"));
	}
	
	@Test
	public void testGetMessage3char() {
		assertEquals(TEST_KEY1_GERMAN_VALUE, I18N.getMessage(TEST_KEY1, "deu"));
	}
	
	@Test
	public void testGetMessageDefault() {
		assertEquals(TEST_KEY1_DEFAULT_VALUE, I18N.getMessage(TEST_KEY1, null));
		assertEquals(TEST_KEY1_DEFAULT_VALUE, I18N.getMessage(TEST_KEY1, "rnd"));
	}

}
