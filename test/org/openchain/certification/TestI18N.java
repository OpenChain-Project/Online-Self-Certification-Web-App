package org.openchain.certification;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class TestI18N {
	
	static final String TEST_LANGUAGE = "de";
	static final String TEST_KEY1 = "test.key1";
	static final String TEST_KEY2 = "test.key2";
	static final String TEST_KEY1_DEFAULT_VALUE = "default key1";
	static final String TEST_KEY1_GERMAN_VALUE = "german key1";
	static final String TEST_SWISS_GERMAN = "de-CH";

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
		assertEquals(TEST_KEY1_GERMAN_VALUE, I18N.getMessage(TEST_KEY1, TEST_LANGUAGE));
	}
	
	@Test
	public void testGetMessageLocaleDefault() {
		assertEquals(TEST_KEY1_GERMAN_VALUE, I18N.getMessage(TEST_KEY1, TEST_SWISS_GERMAN));
	}
	
	@Test
	public void testGetMessageDefault() {
		assertEquals(TEST_KEY1_DEFAULT_VALUE, I18N.getMessage(TEST_KEY1, null));
		assertEquals(TEST_KEY1_DEFAULT_VALUE, I18N.getMessage(TEST_KEY1, "rnd"));
	}

	@Test
	public void testTemplate() {
		String template1 = "This is a template ";
		String template2 = " with ";
		@SuppressWarnings("unused")
		String template = template1 + "{0}" + "\"{1}\"";
		String p1 = "test1";
		Integer p2 = new Integer(7);
		assertEquals(template1+p1+template2+"\""+p2+"\"", I18N.getMessage(TEST_KEY2, TEST_LANGUAGE, p1, p2));
	}
}
