package org.openchain.certification.git;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.jgit.lib.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QuestionnaireGitRepoTest {
	
	static final String TEST_TAG = "v1.1.0";
	static final String TEST_COMMIT = "24a01ebf28094299da338760871bf76e365a65d1";
	static final String TEST_SHORT_COMMIT = "24a01eb";
	static final ObjectId TEST_TAG_HEAD = ObjectId.fromString("f660c4334fa205027131c9671a53da70561632fd");
	static final ObjectId TEST_COMMIT_HEAD = ObjectId.fromString(TEST_COMMIT);

	@Before
	public void setUp() throws Exception {
		QuestionnaireGitRepo.cleanupDelete();
	}

	@After
	public void tearDown() throws Exception {
		QuestionnaireGitRepo.cleanupDelete();
	}

	@Test
	public void testGetQuestionnaireGitRepoClone() throws GitRepoException {
		File rootDir = QuestionnaireGitRepo.getQuestionnaireGitRepo().getDirectory();
		assertTrue(rootDir != null);
		assertTrue(rootDir.isDirectory());
		assertTrue(rootDir.listFiles().length > 0);
	}
	
	@Test
	public void testGetQuestionnaireGitRepoAlreadyExists() throws GitRepoException {
		QuestionnaireGitRepo.getQuestionnaireGitRepo().getDirectory();
		QuestionnaireGitRepo.resetInstance();
		File rootDir = QuestionnaireGitRepo.getQuestionnaireGitRepo().getDirectory();
		assertTrue(rootDir != null);
		assertTrue(rootDir.isDirectory());
		assertTrue(rootDir.listFiles().length > 0);
	}

	@Test
	public void testCheckOutTop() throws GitRepoException, IOException {
		QuestionnaireGitRepo repo = QuestionnaireGitRepo.getQuestionnaireGitRepo();
		ObjectId originalHead = repo.repo.getRefDatabase().getRef("HEAD").getObjectId();
		repo.checkOut(TEST_TAG, null);
		ObjectId tagHead = repo.repo.getRefDatabase().getRef("HEAD").getObjectId();
		assertEquals(TEST_TAG_HEAD, tagHead);
		repo.checkOut(null, TEST_COMMIT);
		ObjectId commitHead = repo.repo.getRefDatabase().getRef("HEAD").getObjectId();
		assertEquals(TEST_COMMIT_HEAD, commitHead);
		repo.checkOut(null, null);
		assertEquals(originalHead, repo.repo.getRefDatabase().getRef("HEAD").getObjectId());
		repo.checkOut(null, TEST_SHORT_COMMIT);
		assertEquals(TEST_COMMIT_HEAD, repo.repo.getRefDatabase().getRef("HEAD").getObjectId());
	}

	@Test
	public void testRefresh() throws GitRepoException, IOException {
		QuestionnaireGitRepo repo = QuestionnaireGitRepo.getQuestionnaireGitRepo();
		repo.refresh();
		File rootDir = repo.getDirectory();
		assertTrue(rootDir != null);
		assertTrue(rootDir.isDirectory());
		assertTrue(rootDir.listFiles().length > 0);
	}

	@Test
	public void testGetQuestionnaireJsonFiles() throws GitRepoException, IOException {
		QuestionnaireGitRepo repo = QuestionnaireGitRepo.getQuestionnaireGitRepo();
		repo.checkOut(null, TEST_COMMIT);
		Iterator<File> iter = repo.getQuestionnaireJsonFiles();
		assertTrue(iter.hasNext());
		File qfile = iter.next();
		assertTrue(qfile.isFile());
		assertEquals("questionnaire.json", qfile.getName());
	}

}
