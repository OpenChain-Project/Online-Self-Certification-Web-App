package org.openchain.certification.git;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
	static final String[] EXPECTED_TAGS = new String[] {"1.2","v1.0.1","v1.0.3","v1.1.0"};
	static final String LANGUAGE = "en";

	@Before
	public void setUp() throws Exception {
		QuestionnaireGitRepo.cleanupDelete(LANGUAGE);
	}

	@After
	public void tearDown() throws Exception {
		QuestionnaireGitRepo.cleanupDelete(LANGUAGE);
	}

	@Test
	public void testGetQuestionnaireGitRepoClone() throws GitRepoException {
		File rootDir = QuestionnaireGitRepo.getQuestionnaireGitRepo(LANGUAGE).getDirectory();
		assertTrue(rootDir != null);
		assertTrue(rootDir.isDirectory());
		assertTrue(rootDir.listFiles().length > 0);
	}
	
	@Test
	public void testGetQuestionnaireGitRepoAlreadyExists() throws GitRepoException {
		QuestionnaireGitRepo.getQuestionnaireGitRepo(LANGUAGE).getDirectory();
		QuestionnaireGitRepo.resetInstance();
		File rootDir = QuestionnaireGitRepo.getQuestionnaireGitRepo(LANGUAGE).getDirectory();
		assertTrue(rootDir != null);
		assertTrue(rootDir.isDirectory());
		assertTrue(rootDir.listFiles().length > 0);
	}

	@Test
	public void testCheckOutTop() throws GitRepoException, IOException {
		QuestionnaireGitRepo repo = QuestionnaireGitRepo.getQuestionnaireGitRepo(LANGUAGE);
		ObjectId originalHead = repo.repo.getRefDatabase().getRef("HEAD").getObjectId();
		repo.checkOut(TEST_TAG, null, LANGUAGE);
		ObjectId tagHead = repo.repo.getRefDatabase().getRef("HEAD").getObjectId();
		assertEquals(TEST_TAG_HEAD, tagHead);
		repo.checkOut(null, TEST_COMMIT, LANGUAGE);
		ObjectId commitHead = repo.repo.getRefDatabase().getRef("HEAD").getObjectId();
		assertEquals(TEST_COMMIT_HEAD, commitHead);
		repo.checkOut(null, null, LANGUAGE);
		assertEquals(originalHead, repo.repo.getRefDatabase().getRef("HEAD").getObjectId());
		repo.checkOut(null, TEST_SHORT_COMMIT, LANGUAGE);
		assertEquals(TEST_COMMIT_HEAD, repo.repo.getRefDatabase().getRef("HEAD").getObjectId());
	}

	@Test
	public void testRefresh() throws GitRepoException, IOException {
		QuestionnaireGitRepo repo = QuestionnaireGitRepo.getQuestionnaireGitRepo(LANGUAGE);
		repo.refresh(LANGUAGE);
		File rootDir = repo.getDirectory();
		assertTrue(rootDir != null);
		assertTrue(rootDir.isDirectory());
		assertTrue(rootDir.listFiles().length > 0);
	}

	@Test
	public void testGetQuestionnaireJsonFiles() throws GitRepoException, IOException {
		QuestionnaireGitRepo repo = QuestionnaireGitRepo.getQuestionnaireGitRepo(LANGUAGE);
		repo.checkOut(null, TEST_COMMIT, LANGUAGE);
		Iterator<File> iter = repo.getQuestionnaireJsonFiles(LANGUAGE);
		assertTrue(iter.hasNext());
		File qfile = iter.next();
		assertTrue(qfile.isFile());
		assertEquals("questionnaire.json", qfile.getName());
	}
	
	@Test
	public void testGetTags() throws GitRepoException, IOException {
		QuestionnaireGitRepo repo = QuestionnaireGitRepo.getQuestionnaireGitRepo(LANGUAGE);
		String[] tags = repo.getTags(LANGUAGE);
		assertTrue(tags.length >= 4);
		Set<String> tagSet = new HashSet<String>();
		for (String tag:tags) {
			tagSet.add(tag);
		}
		for (String expected:EXPECTED_TAGS) {
			assertTrue(tagSet.contains(expected));
		}
	}
	
	@Test
	public void testGetHeadCommit() throws GitRepoException, IOException {
		QuestionnaireGitRepo repo = QuestionnaireGitRepo.getQuestionnaireGitRepo(LANGUAGE);
		String currentHead = repo.getHeadCommit(LANGUAGE);
		repo.checkOut(null, TEST_COMMIT, LANGUAGE);
		String result = repo.getHeadCommit(LANGUAGE);
		assertEquals(TEST_COMMIT, result);
		repo.checkOut(null, null, LANGUAGE);
		assertEquals(currentHead, repo.getHeadCommit(LANGUAGE));
	}

}
