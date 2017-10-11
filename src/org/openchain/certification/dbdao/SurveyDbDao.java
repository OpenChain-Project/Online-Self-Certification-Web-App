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
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openchain.certification.model.Question;
import org.openchain.certification.model.QuestionException;
import org.openchain.certification.model.QuestionTypeException;
import org.openchain.certification.model.Section;
import org.openchain.certification.model.SubQuestion;
import org.openchain.certification.model.Survey;
import org.openchain.certification.model.SurveyResponseException;
import org.openchain.certification.model.YesNoNotApplicableQuestion;
import org.openchain.certification.model.YesNoQuestion;
import org.openchain.certification.model.YesNoQuestion.YesNo;
import org.openchain.certification.model.YesNoQuestionWithEvidence;

/**
 * DB Data Access Object for the survey.
 * @author Gary O'Neall
 *
 */
public class SurveyDbDao {
	
	static final Logger logger = Logger.getLogger(SurveyDbDao.class);
	
	Connection connection = null;
	private PreparedStatement updateQuestionQuery;
	private PreparedStatement addQuestionQuery;
	private PreparedStatement getQuestionIdQuery;

	private PreparedStatement insertSpecQuery;

	private PreparedStatement getSpecIdQuery;

	private PreparedStatement insertSectionQuery;
	
	public SurveyDbDao(Connection connection) throws SQLException {
		this.connection = connection;
		this.connection.setAutoCommit(false);
	}
	
	public Survey getSurvey() throws SQLException, QuestionException, SurveyResponseException {
		return getSurvey(this.connection, null);
	}
	
	public synchronized Survey getSurvey(String specVersion)  throws SQLException, QuestionException, SurveyResponseException {
		return getSurvey(this.connection, specVersion);
	}

	/**
	 * @param specVersion Version of the specification.  If null, will get the latest spec version available
	 * @return Survey with questions (static information) for the latest version
	 * @throws SQLException
	 * @throws SurveyResponseException 
	 * @throws QuestionException 
	 */
	public static Survey getSurvey(Connection con, String specVersion) throws SQLException, SurveyResponseException, QuestionException {
		Survey retval = null;
		Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet result = null;
		try {
			if (specVersion == null) {
				// Get the latest from the spec
				result = stmt.executeQuery("select max(version) from spec");
				if (!result.next()) {
					throw new SurveyResponseException("No specs found in database");
				}
				specVersion = result.getString(1);
				result.close();
			}
			retval = new Survey(specVersion);
			result = stmt.executeQuery("select name, title, id from section where spec_version=(select id from spec where version='"+specVersion+"') order by name asc");
			List<Section> sections = new ArrayList<Section>();
			List<Long> sectionIds = new ArrayList<Long>();
			while (result.next()) {
				sectionIds.add(result.getLong("id"));
				Section section = new Section();
				section.setName(result.getString("name"));
				section.setTitle(result.getString("title"));
				section.setQuestions(new ArrayList<Question>());
				sections.add(section);
			}
			retval.setSections(sections);
			// fill in the questions
			Map<String, SubQuestion> foundSubQuestions = new HashMap<String, SubQuestion>();
			for (int i = 0; i < sections.size(); i++) {
				result.close();
				result = stmt.executeQuery("select number, question, type, correct_answer, evidence_prompt, evidence_validation, subquestion_of, spec_reference from question where section_id="+
								String.valueOf(sectionIds.get(i)) + " order by number asc");
				Section section = sections.get(i);
				List<Question> questions = section.getQuestions();
				while (result.next()) {
					String type = result.getString("type");
					if (type == null) {
						throw(new QuestionTypeException("No question type stored in the database"));
					}
					Question question;
					if (type.equals(YesNoQuestion.TYPE_NAME)) {
						question = new YesNoQuestion(result.getString("question"), section.getName(), 
								result.getString("number"), specVersion, 
								YesNo.valueOf(result.getString("correct_answer")));
					} else if (type.equals(YesNoQuestionWithEvidence.TYPE_NAME)) {
						Pattern evidenceValidation = null;
						String evidenceValString = result.getString("evidence_validation");
						if (evidenceValString != null) {
							evidenceValidation = Pattern.compile(evidenceValString);
						}
						question = new YesNoQuestionWithEvidence(result.getString("question"), section.getName(), 
								result.getString("number"), specVersion,
								YesNo.valueOf(result.getString("correct_answer")), 
								result.getString("evidence_prompt"), 
								evidenceValidation);
					} else if (type.equals(YesNoNotApplicableQuestion.TYPE_NAME)) {
						question = new YesNoNotApplicableQuestion(result.getString("question"), section.getName(), 
								result.getString("number"),  specVersion,
								YesNo.valueOf(result.getString("correct_answer")),
										"Not required by IL");
					} else if (type.equals(SubQuestion.TYPE_NAME)) {
						int minValidAnswers = 0;
						try {
							minValidAnswers = Integer.parseInt(result.getString("correct_answer"));
						} catch (Exception ex) {
							logger.error("Can not parse min correct answers in DB: "+result.getString("correct_answer"));
							throw new QuestionTypeException("Unexpected error getting sub question information from the database.");
						}
						question = foundSubQuestions.get(result.getString("number"));
						if (question == null) {
							question = new SubQuestion(result.getString("question"), section.getName(), 
									result.getString("number"),  specVersion, minValidAnswers);
							foundSubQuestions.put(question.getNumber(), (SubQuestion)question);
						} else {
							question.setQuestion(result.getString("question"));
							((SubQuestion)question).setMinNumberValidatedAnswers(minValidAnswers);
						}
						
					} else {
						throw(new QuestionTypeException("Unknown question type in database: "+type));
					}
					long subQuestionId = result.getLong("subquestion_of");
					if (subQuestionId > 0) {
						String subQuestionNumber = getQuestionNumber(subQuestionId, con);
						question.addSubQuestionOf(subQuestionNumber);
						SubQuestion parent = foundSubQuestions.get(subQuestionNumber);
						if (parent == null) {
							parent = new SubQuestion("", section.getName(), subQuestionNumber, specVersion, 0);
							foundSubQuestions.put(subQuestionNumber, parent);
						}
						parent.addSubQuestion(question);
					}
					question.setSpecReference(result.getString("spec_reference"));
					questions.add(question);
				}
				Collections.sort(questions);
			}
			return retval;
		} catch (SQLException ex) {
			logger.error("SQL Error getting survey", ex);
			throw ex;
		} catch (QuestionException e) {
			logger.error("Invalid question found in database",e);
			throw(e);
		} finally {
			if (result != null) {
				result.close();
			}
			stmt.close();
		}
		
	}

	private static String getQuestionNumber(long subQuestionId, Connection con) throws SQLException {
		ResultSet result = null;
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			result = stmt.executeQuery("select number from question where id="+String.valueOf(subQuestionId));
			if (result.next()) {
				return result.getString(1);
			} else {
				return "";
			}
		} finally {
			if (result != null) {
				result.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * Update the questions identified by the question number and spec version
	 * @param updatedQuestions
	 * @throws SQLException
	 * @throws QuestionException
	 */
	public synchronized void updateQuestions(List<Question> updatedQuestions) throws SQLException, QuestionException {
		Savepoint save = this.connection.setSavepoint();
		try {
			if (updateQuestionQuery == null) {
				updateQuestionQuery = this.connection.prepareStatement(
						"update question set (question, type, correct_answer," +
						"evidence_prompt, evidence_validation, " +
						"subquestion_of, spec_reference) = " +
						"(?, ?, ?, ?, ?, ?, ?)" +
						"where number=? and section_id=(select id from section where name=? and " +
						"spec_version=(select id from spec where version=?))");
			}
			updateQuestionQuery.clearBatch();
			for (Question question:updatedQuestions) {
				updateQuestionQuery.setString(1, question.getQuestion());
				updateQuestionQuery.setString(2, question.getType());
				String correctAnswer = null;
				if (question instanceof YesNoQuestion) {
					correctAnswer = ((YesNoQuestion)question).getCorrectAnswer().toString();
				} else if (question instanceof SubQuestion) {
					correctAnswer = String.valueOf(((SubQuestion)question).getMinNumberValidatedAnswers());
				} else {
					throw(new QuestionTypeException("Unknown question type"));
				}
				updateQuestionQuery.setString(3, correctAnswer);
				if (question instanceof YesNoQuestionWithEvidence) {
					updateQuestionQuery.setString(4, ((YesNoQuestionWithEvidence)question).getEvidencePrompt());
					Pattern validate = ((YesNoQuestionWithEvidence)question).getEvidenceValidation();
					if (validate != null) {
						updateQuestionQuery.setString(5, validate.toString());
					} else {
						updateQuestionQuery.setString(5, null);
					}		
				} else if (question instanceof YesNoNotApplicableQuestion) {
					updateQuestionQuery.setString(4, ((YesNoNotApplicableQuestion)question).getNotApplicablePrompt());
					updateQuestionQuery.setString(5, null);
				} else {
					updateQuestionQuery.setString(4, null);
					updateQuestionQuery.setString(5, null);
				}
				if (question.getSubQuestionNumber() != null && !question.getSubQuestionNumber().isEmpty()) {
					long subQuestionId = getQuestionId(question.getSubQuestionNumber(), question.getSectionName(), question.getSpecVersion());
					if (subQuestionId < 0) {
						throw(new QuestionException("Invalid subquestion number "+question.getSubQuestionNumber()));
					}
					updateQuestionQuery.setLong(6,subQuestionId);
				} else {
					updateQuestionQuery.setNull(6, Types.BIGINT);
				}
				updateQuestionQuery.setString(7, question.getSpecReference());
				updateQuestionQuery.setString(8, question.getNumber());
				updateQuestionQuery.setString(9, question.getSectionName());
				updateQuestionQuery.setString(10, question.getSpecVersion());
				updateQuestionQuery.addBatch();
			}
			int[] counts = updateQuestionQuery.executeBatch();
			if (counts.length != updatedQuestions.size()) {
				logger.warn("Number of batch updated question counts do not match.  Expected" +
						String.valueOf(updatedQuestions.size())+".  Actual="+String.valueOf(counts.length));
			}
			for (int count:counts) {
				if (count != 1) {
					logger.warn("Unexpected update count.  Expected 1, found "+String.valueOf(count));
				}
			}
		} catch(QuestionException ex) {
			if (save != null) {
				this.connection.rollback(save);
			}
			throw(ex);
		} catch(SQLException ex) {
			if (save != null) {
				this.connection.rollback(save);
			}
			throw(ex);
		} finally {
			if (save != null) {
				this.connection.commit();
			}
		}
	}

	private synchronized long getQuestionId(String questionNumber, String sectionName,
			String specVersion) throws SQLException {
		if (this.getQuestionIdQuery == null) {
			this.getQuestionIdQuery = connection.prepareStatement("select question.id from "+
					"question join section on question.section_id=section.id " +
					"join spec on section.spec_version=spec.id where " +
					"number=? and section.name=? and version=?");
		}
		ResultSet result = null;
		try {
			this.getQuestionIdQuery.setString(1, questionNumber);
			this.getQuestionIdQuery.setString(2, sectionName);
			this.getQuestionIdQuery.setString(3, specVersion);
			result = this.getQuestionIdQuery.executeQuery();
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
	 * Add questions to a survey
	 * @param addedQuestions
	 * @throws SQLException
	 * @throws QuestionException
	 */
	public synchronized void addQuestions(List<Question> addedQuestions) throws SQLException, QuestionException {
		Savepoint save = this.connection.setSavepoint();
		try {
			_addQuestions(addedQuestions);
		} catch(QuestionException ex) {
			if (save != null) {
				try {
					connection.rollback(save);
				} catch (SQLException ex2) {
					logger.error("Error rolling back transaction",ex2);
				}
			}
			throw(ex);
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
				connection.commit();
			}
		}

	}
	
	private void _addQuestions(List<Question> addedQuestions) throws SQLException, QuestionException {
		if (addQuestionQuery == null) {
			addQuestionQuery = this.connection.prepareStatement(
					"insert into question (number, question, type, correct_answer," +
					"evidence_prompt, evidence_validation, " +
					"subquestion_of, spec_reference, section_id) values " +
					"(?, ?, ?, ?, ?, ?, ?, ?, (select id from section where name=? and " +
					"spec_version=(select id from spec where version=?)))");
		}
		// Need to add the subquestions first
		addQuestionQuery.clearBatch();
		int numAdded = 0;
		for (Question question:addedQuestions) {
			if (question instanceof SubQuestion) {
				numAdded++;
				addQuestionQuery.setString(1, question.getNumber());
				addQuestionQuery.setString(2, question.getQuestion());
				addQuestionQuery.setString(3, question.getType());
				String correctAnswer = String.valueOf(((SubQuestion)question).getMinNumberValidatedAnswers());
				addQuestionQuery.setString(4, correctAnswer);
				addQuestionQuery.setString(5, null);
				addQuestionQuery.setString(6, null);
				if (question.getSubQuestionNumber() != null && !question.getSubQuestionNumber().isEmpty()) {
					long subQuestionId = getQuestionId(question.getSubQuestionNumber(), question.getSectionName(), question.getSpecVersion());
					if (subQuestionId < 0) {
						throw(new QuestionException("Invalud subquestion number "+question.getSubQuestionNumber()));
					}
					addQuestionQuery.setLong(7,subQuestionId);
				} else {
					addQuestionQuery.setNull(7, Types.BIGINT);
				}
				addQuestionQuery.setString(8, question.getSpecReference());
				addQuestionQuery.setString(9, question.getSectionName());
				addQuestionQuery.setString(10, question.getSpecVersion());
				addQuestionQuery.addBatch();
			}
		}
		int[] counts = addQuestionQuery.executeBatch();
		if (counts.length != numAdded) {
			logger.warn("Number of batch added subquestion counts do not match.  Expected" +
					String.valueOf(numAdded)+".  Actual="+String.valueOf(counts.length));
		}
		for (int count:counts) {
			if (count != 1) {
				logger.warn("Unexpected update count.  Expected 1, found "+String.valueOf(count));
			}
		}
		// Now we add the rest
		addQuestionQuery.clearBatch();
		numAdded = 0;
		for (Question question:addedQuestions) {
			if (!(question instanceof SubQuestion)) {
				numAdded++;
				addQuestionQuery.setString(1, question.getNumber());
				addQuestionQuery.setString(2, question.getQuestion());
				addQuestionQuery.setString(3, question.getType());
				String correctAnswer = null;
				if (question instanceof YesNoQuestion) {
					correctAnswer = ((YesNoQuestion)question).getCorrectAnswer().toString();
				} else if (question instanceof SubQuestion) {
					correctAnswer = String.valueOf(((SubQuestion)question).getMinNumberValidatedAnswers());
				} else {
					throw(new QuestionTypeException("Unknown question type"));
				}
				addQuestionQuery.setString(4, correctAnswer);
				if (question instanceof YesNoQuestionWithEvidence) {
					addQuestionQuery.setString(5, ((YesNoQuestionWithEvidence)question).getEvidencePrompt());
					Pattern validate = ((YesNoQuestionWithEvidence)question).getEvidenceValidation();
					if (validate != null) {
						addQuestionQuery.setString(6, validate.toString());
					} else {
						addQuestionQuery.setString(6, null);
					}
				} else if (question instanceof YesNoNotApplicableQuestion) {
					addQuestionQuery.setString(5, ((YesNoNotApplicableQuestion)question).getNotApplicablePrompt());
					addQuestionQuery.setString(6, null);
				} else {
					addQuestionQuery.setString(5, null);
					addQuestionQuery.setString(6, null);
				}
				if (question.getSubQuestionNumber() != null && !question.getSubQuestionNumber().isEmpty()) {
					long subQuestionId = getQuestionId(question.getSubQuestionNumber(), question.getSectionName(), question.getSpecVersion());
					if (subQuestionId < 0) {
						throw(new QuestionException("Invalid subquestion number "+question.getSubQuestionNumber()));
					}
					addQuestionQuery.setLong(7,subQuestionId);
				} else {
					addQuestionQuery.setNull(7, Types.BIGINT);
				}
				addQuestionQuery.setString(8, question.getSpecReference());
				addQuestionQuery.setString(9, question.getSectionName());
				addQuestionQuery.setString(10, question.getSpecVersion());
				addQuestionQuery.addBatch();
			}
		}
		counts = addQuestionQuery.executeBatch();
		if (counts.length != numAdded) {
			logger.warn("Number of batch added question counts do not match.  Expected" +
					String.valueOf(numAdded)+".  Actual="+String.valueOf(counts.length));
		}
		for (int count:counts) {
			if (count != 1) {
				logger.warn("Unexpected update count.  Expected 1, found "+String.valueOf(count));
			}
		}
	}

	public synchronized void addSurvey(Survey survey) throws SQLException, SurveyResponseException, QuestionException {
		if (survey.getSpecVersion() == null) {
			throw(new SurveyResponseException("Spec version missing for survey"));
		}
		if (survey.getSections() == null) {
			throw(new SurveyResponseException("Sections missing for survey"));
		}
		if (this.getSpecId(survey.getSpecVersion())> 0) {
			throw(new SurveyResponseException("Can not add survey.  Survey version "+survey.getSpecVersion()+" already exists."));
		}
		Savepoint save = this.connection.setSavepoint();
		Statement stmt = null;
		if (insertSpecQuery == null) {
			insertSpecQuery = connection.prepareStatement("insert into spec (version) values (?)");
		}
		if (insertSectionQuery == null) {
			insertSectionQuery = connection.prepareStatement("insert into section (name, title, spec_version) "+
					"values (?,?,?)");
		}
		try {
			stmt = this.connection.createStatement();
			long specId = getSpecId(survey.getSpecVersion());
			if (specId < 0) {
				insertSpecQuery.setString(1, survey.getSpecVersion());
				insertSpecQuery.executeUpdate();
				specId = getSpecId(survey.getSpecVersion());
				if (specId < 0) {
					logger.error("Unable to create new spec version for version "+survey.getSpecVersion());
					throw(new SurveyResponseException("Unable to create new spec version for version "+survey.getSpecVersion()));
				}
			}
			insertSectionQuery.clearBatch();
			for (Section section:survey.getSections()) {
				insertSectionQuery.setString(1, section.getName());
				insertSectionQuery.setString(2, section.getTitle());
				insertSectionQuery.setLong(3, specId);
				insertSectionQuery.addBatch();
			}
			int[] counts = insertSectionQuery.executeBatch();
			if (counts.length != survey.getSections().size()) {
				logger.warn("Number of batch updated sections counts do not match.  Expected" +
						String.valueOf(survey.getSections().size())+".  Actual="+String.valueOf(counts.length));
			}
			for (int count:counts) {
				if (count != 1) {
					logger.warn("Unexpected update count.  Expected 1, found "+String.valueOf(count));
				}
			}
			// Add the questions
			for (Section section:survey.getSections()) {
				_addQuestions(section.getQuestions());
			}
		} catch(SQLException ex) {
			if (save != null) {
				try {
					this.connection.rollback(save);
				} catch (Exception ex2) {
					logger.error("Error rolling back transaction",ex2);
				}
				save = null;
			}
			throw(ex);
		} catch(SurveyResponseException ex) {
			if (save != null) {
				try {
					this.connection.rollback(save);
				} catch (Exception ex2) {
					logger.error("Error rolling back transaction",ex2);
				}
				save = null;
			}
			throw(ex);
		} catch(QuestionException ex) {	
			if (save != null) {
				try {
					this.connection.rollback(save);
				} catch (Exception ex2) {
					logger.error("Error rolling back transaction",ex2);
				}
				save = null;
			}
			throw(ex);
		} finally {
			if(save != null) {
				this.connection.commit();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * @param specVersion Version for the spec
	 * @return ID from the spec table associated with the version
	 * @throws SQLException
	 */
	private long getSpecId(String specVersion) throws SQLException {
		if (getSpecIdQuery == null) {
			getSpecIdQuery = connection.prepareStatement("select id from spec where version=?");
		}
		getSpecIdQuery.setString(1, specVersion);
		ResultSet result = null;
		try {
			result = getSpecIdQuery.executeQuery();
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

	public boolean surveyExists(String specVersion) throws SQLException {
		try {
			return getSpecId(specVersion) > 0;
		} finally {
			this.connection.commit();
		}
	}
	
	/**
	 * @return All survey versions in the database
	 * @throws SQLException
	 */
	public synchronized List<String> getSurveyVesions() throws SQLException {
		return getSurveyVersions(this.connection);
	}

	/**
	 * @param con
	 * @return All survey versions in the database
	 * @throws SQLException
	 */
	public static List<String> getSurveyVersions(Connection con) throws SQLException {
		Statement stmt = null;
		ResultSet result = null;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			result = stmt.executeQuery("select distinct version from spec order by version");
			ArrayList<String> retval = new ArrayList<String>();
			while (result.next()) {
				retval.add(result.getString(1));
			}
			return retval;
		} catch (SQLException ex) {
			logger.error("SQL Error getting survey versions", ex);
			throw ex;
		} finally {
			if (result != null) {
				result.close();
			}
			stmt.close();
		}
	}
}
