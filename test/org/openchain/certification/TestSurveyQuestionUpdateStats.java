package org.openchain.certification;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openchain.certification.model.Question;
import org.openchain.certification.model.YesNoQuestion;
import org.openchain.certification.model.YesNoQuestion.YesNo;

public class TestSurveyQuestionUpdateStats {
	
	static final String ENGLISH = "en";
	static final String GERMEN = "de";
	static final String SPECVERSION = "1.3";
	static final String QUESTION1 = "question1";
	static final String SECTION1 = "section1";
	static final String NUM1 = "1.a";
	static final String QUESTION2 = "question2";
	static final String SECTION2 = "section2";
	static final String NUM2 = "2.a";
	static final String QUESTION3 = "question3";
	static final String NUM3 = "2.b";
	static final String QUESTION4 = "question4";
	static final String NUM4 = "2.c";
	static final String QUESTION5 = "question5";
	static final String NUM5 = "2.d";
	static final String QUESTION6 = "question6";
	static final String NUM6 = "2.e";
	
	
	private Question question1;
	private Question question2;
	private Question question3;
	private Question question4;
	private Question question5;
	private Question question6;

	@Before
	public void setUp() throws Exception {
		question1 = new YesNoQuestion(QUESTION1, SECTION1, NUM1, SPECVERSION, new String[0], ENGLISH, YesNo.Any);
		question2 = new YesNoQuestion(QUESTION2, SECTION2, NUM2, SPECVERSION, new String[0], ENGLISH, YesNo.Any);
		question3 = new YesNoQuestion(QUESTION3, SECTION2, NUM3, SPECVERSION, new String[0], ENGLISH, YesNo.Any);
		question4 = new YesNoQuestion(QUESTION4, SECTION2, NUM4, SPECVERSION, new String[0], ENGLISH, YesNo.Any);
		question5 = new YesNoQuestion(QUESTION5, SECTION2, NUM5, SPECVERSION, new String[0], ENGLISH, YesNo.Any);
		question6 = new YesNoQuestion(QUESTION6, SECTION2, NUM6, SPECVERSION, new String[0], ENGLISH, YesNo.Any);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddUpdateQuestions() {
		SurveyQuestionUpdateStats stats = new SurveyQuestionUpdateStats();
		assertEquals(0, stats.getUpdatedQuestions().size());
		List<Question> updatedQuestions1 = new ArrayList<Question>();
		updatedQuestions1.add(question1);
		updatedQuestions1.add(question2);
		stats.addUpdateQuestions(updatedQuestions1);
		assertEquals(2, stats.getUpdatedQuestions().size());
		assertEquals(question1.getNumber(), stats.getUpdatedQuestions().get(0).getNumber());
		assertEquals(question2.getNumber(), stats.getUpdatedQuestions().get(1).getNumber());
		List<Question> updatedQuestions2 = new ArrayList<Question>();
		updatedQuestions2.add(question3);
		updatedQuestions2.add(question4);
		stats.addUpdateQuestions(updatedQuestions2);
		assertEquals(4, stats.getUpdatedQuestions().size());
		assertEquals(question1.getNumber(), stats.getUpdatedQuestions().get(0).getNumber());
		assertEquals(question2.getNumber(), stats.getUpdatedQuestions().get(1).getNumber());
		assertEquals(question3.getNumber(), stats.getUpdatedQuestions().get(2).getNumber());
		assertEquals(question4.getNumber(), stats.getUpdatedQuestions().get(3).getNumber());
	}

	@Test
	public void testAddAddedQuestions() {
		SurveyQuestionUpdateStats stats = new SurveyQuestionUpdateStats();
		assertEquals(0, stats.getAddedQuestions().size());
		List<Question> addedQuestions1 = new ArrayList<Question>();
		addedQuestions1.add(question1);
		addedQuestions1.add(question2);
		stats.addAddedQuestions(addedQuestions1);
		assertEquals(2, stats.getAddedQuestions().size());
		assertEquals(question1.getNumber(), stats.getAddedQuestions().get(0).getNumber());
		assertEquals(question2.getNumber(), stats.getAddedQuestions().get(1).getNumber());
		List<Question> addedQuestions2 = new ArrayList<Question>();
		addedQuestions2.add(question3);
		addedQuestions2.add(question4);
		stats.addAddedQuestions(addedQuestions2);
		assertEquals(4, stats.getAddedQuestions().size());
		assertEquals(question1.getNumber(), stats.getAddedQuestions().get(0).getNumber());
		assertEquals(question2.getNumber(), stats.getAddedQuestions().get(1).getNumber());
		assertEquals(question3.getNumber(), stats.getAddedQuestions().get(2).getNumber());
		assertEquals(question4.getNumber(), stats.getAddedQuestions().get(3).getNumber());
	}
	
	@Test
	public void testAddChangedSectionTitles() {
		SurveyQuestionUpdateStats stats = new SurveyQuestionUpdateStats();
		String section1Name = "section1";
		String section1Title = "section1Title";
		String section2Name = "section2";
		String section2Title = "section2Title";
		Map<String, String> updatedSectionTitles = new HashMap<>();
		updatedSectionTitles.put(section1Name, section1Title);
		updatedSectionTitles.put(section2Name, section2Title);
		assertEquals(0, stats.getNumChanges());
		assertEquals(0, stats.getUpdatedSectionTitles().size());
		stats.addUpdatedSectionTitles(updatedSectionTitles);
		assertEquals(2, stats.getNumChanges());
		Map<String, String> result = stats.getUpdatedSectionTitles();
		assertEquals(2, result.size());
		assertEquals(section1Title, result.get(section1Name));
		assertEquals(section2Title, result.get(section2Name));
	}

	@Test
	public void testToStringString() {
		// empty
		SurveyQuestionUpdateStats stats = new SurveyQuestionUpdateStats();
		String result = stats.toString(ENGLISH);
		assertEquals("No Changes.", result);
		// One update
		List<Question> updatedQuestions1 = new ArrayList<Question>();
		updatedQuestions1.add(question1);
		stats.addUpdateQuestions(updatedQuestions1);
		result = stats.toString(ENGLISH);
		assertEquals("Updated 1 questions: 1.a.", result);
		// Two updates
		List<Question> updatedQuestions2 = new ArrayList<Question>();
		updatedQuestions2.add(question2);
		stats.addUpdateQuestions(updatedQuestions2);
		result = stats.toString(ENGLISH);
		assertEquals("Updated 2 questions: 1.a and 2.a.", result);
		// Three updates
		List<Question> updatedQuestions3 = new ArrayList<Question>();
		updatedQuestions3.add(question3);
		stats.addUpdateQuestions(updatedQuestions3);
		result = stats.toString(ENGLISH);
		assertEquals("Updated 3 questions: 1.a, 2.a, and 2.b.", result);
		// four updates
		List<Question> updatedQuestions4 = new ArrayList<Question>();
		updatedQuestions4.add(question4);
		stats.addUpdateQuestions(updatedQuestions4);
		result = stats.toString(ENGLISH);
		assertEquals("Updated 4 questions: 1.a, 2.a, 2.b, and 2.c.", result);
		// Added question
		List<Question> addedQuestions1 = new ArrayList<Question>();
		addedQuestions1.add(question5);
		addedQuestions1.add(question6);
		stats.addAddedQuestions(addedQuestions1);
		result = stats.toString(ENGLISH);
		assertEquals("Added 2 questions: 2.d and 2.e.  Updated 4 questions: 1.a, 2.a, 2.b, and 2.c.", result);
		// One updated title
		Map<String, String> updatedTitles = new HashMap<>();
		updatedTitles.put("section1", "New section1 title");
		stats.addUpdatedSectionTitles(updatedTitles);
		result = stats.toString(ENGLISH);
		assertEquals("Added 2 questions: 2.d and 2.e.  Updated 4 questions: 1.a, 2.a, 2.b, and 2.c.  Updated 1 section titles: section1.", result);
		// Two updated titles
		updatedTitles.clear();
		updatedTitles.put("section2", "New section2 title");
		stats.addUpdatedSectionTitles(updatedTitles);
		result = stats.toString(ENGLISH);
		assertEquals("Added 2 questions: 2.d and 2.e.  Updated 4 questions: 1.a, 2.a, 2.b, and 2.c.  Updated 2 section titles: section1 and section2.", result);
	}

	@Test
	public void testGetNumChanges() {
		SurveyQuestionUpdateStats stats = new SurveyQuestionUpdateStats();
		assertEquals(0, stats.getNumChanges());
		List<Question> addedQuestions1 = new ArrayList<Question>();
		addedQuestions1.add(question5);
		addedQuestions1.add(question6);
		stats.addAddedQuestions(addedQuestions1);
		assertEquals(2, stats.getNumChanges());
		List<Question> updatedQuestions1 = new ArrayList<Question>();
		updatedQuestions1.add(question1);
		stats.addUpdateQuestions(updatedQuestions1);
		assertEquals(3, stats.getNumChanges());
	}

}
