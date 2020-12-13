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
import org.openchain.certification.model.User;
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
	private PreparedStatement insertSectionQuery;
	private PreparedStatement updateSectionTitleQuery;
	private PreparedStatement getLanguagesQuery;
	
	public SurveyDbDao(Connection connection) throws SQLException {
		this.connection = connection;
		this.connection.setAutoCommit(false);
	}
	
	/**
	 * @param specVersion
	 * @param language tag in IETF RFC 5646 format
	 * @return The survey for the specVersion and language.  If the language is not available, the default language will be returned
	 * @throws SQLException
	 * @throws QuestionException
	 * @throws SurveyResponseException
	 */
	public synchronized Survey getSurvey(String specVersion, String language)  throws SQLException, QuestionException, SurveyResponseException {
		return getSurvey(this.connection, specVersion, language);
	}
	
	/**
	 * @param con SQL Connection
	 * @param specVersion Specification version for the survey
	 * @param language Language.  If null, the default language will be used.
	 * @param allowDefaultLanguage If true, use the default language if the specified languages is not found.  If false, return a -1 if the language is not found
	 * @return The ID for the specification based on the version and the language.  If the language is not available, the default language will be used.
	 * @throws SQLException
	 */
	public static long getSpecId(Connection con, String specVersion, String language, boolean allowDefaultLanguage) throws SQLException {
		ResultSet result = null;
		try {
			if (language == null) {
				PreparedStatement getSpecIdQuery = con.prepareStatement("select id from spec where version=? and language is null"); //$NON-NLS-1$
				getSpecIdQuery.setString(1, specVersion);
				result = getSpecIdQuery.executeQuery();
				if (result.next()) {
					return result.getLong(1);
				} else {
					return -1;
				}
			} else {
				PreparedStatement getSpecIdQuery = con.prepareStatement("select id from spec where version=? and language=?"); //$NON-NLS-1$
				getSpecIdQuery.setString(1, specVersion);
				getSpecIdQuery.setString(2, language);
				result = getSpecIdQuery.executeQuery();
				if (result.next()) {
					return result.getLong(1);
				} else {
					if (!allowDefaultLanguage) {
						return -1;
					}
					logger.warn("No spec exists for version "+specVersion+", language "+language+".  Using default language."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					getSpecIdQuery.setString(2, User.DEFAULT_LANGUAGE);
					result = getSpecIdQuery.executeQuery();
					if (result.next()) {
						return result.getLong(1);
					} else {
						logger.warn("No spec exists for version "+specVersion+" default language.  Using null language."); //$NON-NLS-1$ //$NON-NLS-2$
						PreparedStatement getSpecIdQueryNull = con.prepareStatement("select id from spec where version=? and language is null"); //$NON-NLS-1$
						getSpecIdQueryNull.setString(1, specVersion);
						result = getSpecIdQueryNull.executeQuery();
						if (result.next()) {
							return result.getLong(1);
						} else {
							logger.warn("No spec exists for version "+specVersion); //$NON-NLS-1$
							return -1;
						}
					}
				}
			}
		} finally {
			if (result != null) {
				result.close();
			}
		}
	}

	/**
	 * @param con SQL connect
	 * @param specVersion Version of the specification.  If null, will get the latest spec version available
	 * @param language tag in IETF RFC 5646 format
	 * @return Survey with questions (static information) for the specific version.  If the language isn't supported, the default language will be used
	 * @throws SQLException
	 * @throws SurveyResponseException 
	 * @throws QuestionException 
	 */
	public static Survey getSurvey(Connection con, String specVersion, String language) throws SQLException, SurveyResponseException, QuestionException {
		Survey retval = null;
		Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet result = null;
		try {
			if (specVersion == null) {
				// Get the latest from the spec
				result = stmt.executeQuery("select max(version) from spec"); //$NON-NLS-1$
				if (!result.next()) {
					throw new SurveyResponseException("No specs found in database"); //$NON-NLS-1$
				}
				specVersion = result.getString(1);
				result.close();
			}
			int specId = 0;
			if (language == null) {
				language = User.DEFAULT_LANGUAGE;
			} else {
				result = stmt.executeQuery("select id from spec where version='"+specVersion+"' and language='"+language+"'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (!result.next()) {
					result.close();
					logger.warn("Language "+language+" does not exist for spec version "+specVersion+".  Using default language."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					language = User.DEFAULT_LANGUAGE;
					result = stmt.executeQuery("select id from spec where version='"+specVersion+"' and language='"+language+"'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					if (!result.next()) {
						logger.warn("No default language for survey with spec version "+specVersion); //$NON-NLS-1$
						result = stmt.executeQuery("select id from spec where version='"+specVersion+"' and language is null"); //$NON-NLS-1$ //$NON-NLS-2$
						if (!result.next()) {
							logger.warn("Empty survey for version "+specVersion); //$NON-NLS-1$
						} else {
							specId = result.getInt(1);
						}
					} else {
						specId = result.getInt(1);
					}
				} else {
					specId = result.getInt(1);
				}
			}
			retval = new Survey(specVersion, language);
			result = stmt.executeQuery("select name, title, id from section where spec_version="+specId+" order by name asc"); //$NON-NLS-1$ //$NON-NLS-2$
			List<Section> sections = new ArrayList<Section>();
			List<Long> sectionIds = new ArrayList<Long>();
			while (result.next()) {
				sectionIds.add(result.getLong("id")); //$NON-NLS-1$
				Section section = new Section(language);
				section.setName(result.getString("name")); //$NON-NLS-1$
				section.setTitle(result.getString("title")); //$NON-NLS-1$
				section.setQuestions(new ArrayList<Question>());
				sections.add(section);
			}
			retval.setSections(sections);
			// fill in the questions
			Map<String, SubQuestion> foundSubQuestions = new HashMap<String, SubQuestion>();
			for (int i = 0; i < sections.size(); i++) {
				result.close();
				result = stmt.executeQuery("select number, question, type, correct_answer, evidence_prompt, evidence_validation, subquestion_of, spec_reference from question where section_id="+ //$NON-NLS-1$
								String.valueOf(sectionIds.get(i)) + " order by number asc"); //$NON-NLS-1$
				Section section = sections.get(i);
				List<Question> questions = section.getQuestions();
				while (result.next()) {
					String type = result.getString("type"); //$NON-NLS-1$
					if (type == null) {
						throw(new QuestionTypeException("No question type stored in the database")); //$NON-NLS-1$
					}
					Question question;
					if (type.equals(YesNoQuestion.TYPE_NAME)) {
						question = new YesNoQuestion(result.getString("question"), section.getName(),  //$NON-NLS-1$
								result.getString("number"), specVersion, Question.specReferenceStrToArray(result.getString("spec_reference")), //$NON-NLS-1$ //$NON-NLS-2$
								 language, YesNo.valueOf(result.getString("correct_answer"))); //$NON-NLS-1$
					} else if (type.equals(YesNoQuestionWithEvidence.TYPE_NAME)) {
						Pattern evidenceValidation = null;
						String evidenceValString = result.getString("evidence_validation"); //$NON-NLS-1$
						if (evidenceValString != null) {
							evidenceValidation = Pattern.compile(evidenceValString);
						}
						question = new YesNoQuestionWithEvidence(result.getString("question"), section.getName(),  //$NON-NLS-1$
								result.getString("number"), specVersion, Question.specReferenceStrToArray(result.getString("spec_reference")), language, //$NON-NLS-1$ //$NON-NLS-2$
								YesNo.valueOf(result.getString("correct_answer")),  //$NON-NLS-1$
								result.getString("evidence_prompt"),  //$NON-NLS-1$
								evidenceValidation);
					} else if (type.equals(YesNoNotApplicableQuestion.TYPE_NAME)) {
						question = new YesNoNotApplicableQuestion(result.getString("question"), section.getName(),  //$NON-NLS-1$
								result.getString("number"),  specVersion, Question.specReferenceStrToArray(result.getString("spec_reference")), language, //$NON-NLS-1$ //$NON-NLS-2$
								YesNo.valueOf(result.getString("correct_answer")), //$NON-NLS-1$
										"Not required by IL"); //$NON-NLS-1$
					} else if (type.equals(SubQuestion.TYPE_NAME)) {
						int minValidAnswers = 0;
						try {
							minValidAnswers = Integer.parseInt(result.getString("correct_answer")); //$NON-NLS-1$
						} catch (Exception ex) {
							logger.error("Can not parse min correct answers in DB: "+result.getString("correct_answer")); //$NON-NLS-1$ //$NON-NLS-2$
							throw new QuestionTypeException("Unexpected error getting sub question information from the database."); //$NON-NLS-1$
						}
						question = foundSubQuestions.get(result.getString("number")); //$NON-NLS-1$
						if (question == null) {
							question = new SubQuestion(result.getString("question"), section.getName(),  //$NON-NLS-1$
									result.getString("number"),  specVersion, //$NON-NLS-1$
									Question.specReferenceStrToArray(result.getString("spec_reference")), language, minValidAnswers); //$NON-NLS-1$
							foundSubQuestions.put(question.getNumber(), (SubQuestion)question);
						} else {
							question.setQuestion(result.getString("question")); //$NON-NLS-1$
							question.setSpecReference(Question.specReferenceStrToArray(result.getString("spec_reference"))); //$NON-NLS-1$
							((SubQuestion)question).setMinNumberValidatedAnswers(minValidAnswers);
						}
						
					} else {
						throw(new QuestionTypeException("Unknown question type in database: "+type)); //$NON-NLS-1$
					}
					long subQuestionId = result.getLong("subquestion_of"); //$NON-NLS-1$
					if (subQuestionId > 0) {
						String subQuestionNumber = getQuestionNumber(subQuestionId, con);
						question.setSubQuestionOfNumber(subQuestionNumber);
						SubQuestion parent = foundSubQuestions.get(subQuestionNumber);
						if (parent == null) {
							parent = new SubQuestion("", section.getName(), subQuestionNumber, specVersion, //$NON-NLS-1$
									new String[0], language, 0); //$NON-NLS-1$
							foundSubQuestions.put(subQuestionNumber, parent);
						}
						parent.addSubQuestion(question);
					}
					questions.add(question);
				}
				Collections.sort(questions);
			}
			return retval;
		} catch (SQLException ex) {
			logger.error("SQL Error getting survey", ex); //$NON-NLS-1$
			throw ex;
		} catch (QuestionException e) {
			logger.error("Invalid question found in database",e); //$NON-NLS-1$
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
			result = stmt.executeQuery("select number from question where id="+String.valueOf(subQuestionId)); //$NON-NLS-1$
			if (result.next()) {
				return result.getString(1);
			} else {
				return ""; //$NON-NLS-1$
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
						"update question set (question, type, correct_answer," + //$NON-NLS-1$
						"evidence_prompt, evidence_validation, " + //$NON-NLS-1$
						"subquestion_of, spec_reference) = " + //$NON-NLS-1$
						"(?, ?, ?, ?, ?, ?, ?)" + //$NON-NLS-1$
						"where number=? and section_id=(select id from section where name=? and " + //$NON-NLS-1$
						"spec_version=(select id from spec where version=? and language=?))"); //$NON-NLS-1$
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
					throw(new QuestionTypeException("Unknown question type")); //$NON-NLS-1$
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
				if (question.getSubQuestionOfNumber() != null && !question.getSubQuestionOfNumber().isEmpty()) {
					long subQuestionId = getQuestionId(question.getSubQuestionOfNumber(), question.getSectionName(), 
							question.getSpecVersion(), question.getLanguage());
					if (subQuestionId < 0) {
						throw(new QuestionException("Invalid subquestion number "+question.getSubQuestionOfNumber())); //$NON-NLS-1$
					}
					updateQuestionQuery.setLong(6,subQuestionId);
				} else {
					updateQuestionQuery.setNull(6, Types.BIGINT);
				}
				updateQuestionQuery.setString(7, Question.specReferenceArrayToStr(question.getSpecReference()));
				updateQuestionQuery.setString(8, question.getNumber());
				updateQuestionQuery.setString(9, question.getSectionName());
				updateQuestionQuery.setString(10, question.getSpecVersion());
				updateQuestionQuery.setString(11, question.getLanguage());
				updateQuestionQuery.addBatch();
			}
			int[] counts = updateQuestionQuery.executeBatch();
			if (counts.length != updatedQuestions.size()) {
				logger.warn("Number of batch updated question counts do not match.  Expected" + //$NON-NLS-1$
						String.valueOf(updatedQuestions.size())+".  Actual="+String.valueOf(counts.length)); //$NON-NLS-1$
			}
			for (int count:counts) {
				if (count != 1) {
					logger.warn("Unexpected update count.  Expected 1, found "+String.valueOf(count)); //$NON-NLS-1$
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
			String specVersion, String language) throws SQLException {
		if (this.getQuestionIdQuery == null) {
			this.getQuestionIdQuery = connection.prepareStatement("select question.id from "+ //$NON-NLS-1$
					"question join section on question.section_id=section.id " + //$NON-NLS-1$
					"join spec on section.spec_version=spec.id where " + //$NON-NLS-1$
					"number=? and section.name=? and version=? and language=?"); //$NON-NLS-1$
		}
		ResultSet result = null;
		try {
			this.getQuestionIdQuery.setString(1, questionNumber);
			this.getQuestionIdQuery.setString(2, sectionName);
			this.getQuestionIdQuery.setString(3, specVersion);
			this.getQuestionIdQuery.setString(4, language);
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
					logger.error("Error rolling back transaction",ex2); //$NON-NLS-1$
				}
			}
			throw(ex);
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
				connection.commit();
			}
		}

	}
	
	private void _addQuestions(List<Question> addedQuestions) throws SQLException, QuestionException {
		if (addedQuestions == null) {
			logger.error("Null questions passed to addQuestion"); //$NON-NLS-1$
			throw(new QuestionException("Null exception passed to addQuestions")); //$NON-NLS-1$
		}
		if (addQuestionQuery == null) {
			addQuestionQuery = this.connection.prepareStatement(
					"insert into question (number, question, type, correct_answer," + //$NON-NLS-1$
					"evidence_prompt, evidence_validation, " + //$NON-NLS-1$
					"subquestion_of, spec_reference, section_id) values " + //$NON-NLS-1$
					"(?, ?, ?, ?, ?, ?, ?, ?, (select id from section where name=? and " + //$NON-NLS-1$
					"spec_version=(select id from spec where version=? and language=?)))"); //$NON-NLS-1$
		}
		// Need to add the subquestions first
		addQuestionQuery.clearBatch();
		int numAdded = 0;
		for (Question question:addedQuestions) {
			if (question == null) {
				logger.error("Null question passed to addQuestion"); //$NON-NLS-1$
				throw(new QuestionException("Null question passed to addQuestions")); //$NON-NLS-1$
			}
			if (question instanceof SubQuestion) {
				numAdded++;
				addQuestionQuery.setString(1, question.getNumber());
				addQuestionQuery.setString(2, question.getQuestion());
				addQuestionQuery.setString(3, question.getType());
				String correctAnswer = String.valueOf(((SubQuestion)question).getMinNumberValidatedAnswers());
				addQuestionQuery.setString(4, correctAnswer);
				addQuestionQuery.setString(5, null);
				addQuestionQuery.setString(6, null);
				if (question.getSubQuestionOfNumber() != null && !question.getSubQuestionOfNumber().isEmpty()) {
					long subQuestionId = getQuestionId(question.getSubQuestionOfNumber(), question.getSectionName(), 
							question.getSpecVersion(), question.getLanguage());
					if (subQuestionId < 0) {
						throw(new QuestionException("Invalud subquestion number "+question.getSubQuestionOfNumber())); //$NON-NLS-1$
					}
					addQuestionQuery.setLong(7,subQuestionId);
				} else {
					addQuestionQuery.setNull(7, Types.BIGINT);
				}
				addQuestionQuery.setString(8, Question.specReferenceArrayToStr(question.getSpecReference()));
				addQuestionQuery.setString(9, question.getSectionName());
				addQuestionQuery.setString(10, question.getSpecVersion());
				addQuestionQuery.setString(11, question.getLanguage());
				addQuestionQuery.addBatch();
			}
		}
		int[] counts = addQuestionQuery.executeBatch();
		if (counts.length != numAdded) {
			logger.warn("Number of batch added subquestion counts do not match.  Expected" + //$NON-NLS-1$
					String.valueOf(numAdded)+".  Actual="+String.valueOf(counts.length)); //$NON-NLS-1$
		}
		for (int count:counts) {
			if (count != 1) {
				logger.warn("Unexpected update count.  Expected 1, found "+String.valueOf(count)); //$NON-NLS-1$
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
					throw(new QuestionTypeException("Unknown question type")); //$NON-NLS-1$
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
				if (question.getSubQuestionOfNumber() != null && !question.getSubQuestionOfNumber().isEmpty()) {
					long subQuestionId = getQuestionId(question.getSubQuestionOfNumber(), 
							question.getSectionName(), question.getSpecVersion(), question.getLanguage());
					if (subQuestionId < 0) {
						throw(new QuestionException("Invalid subquestion number "+question.getSubQuestionOfNumber())); //$NON-NLS-1$
					}
					addQuestionQuery.setLong(7,subQuestionId);
				} else {
					addQuestionQuery.setNull(7, Types.BIGINT);
				}
				addQuestionQuery.setString(8, Question.specReferenceArrayToStr(question.getSpecReference()));
				addQuestionQuery.setString(9, question.getSectionName());
				addQuestionQuery.setString(10, question.getSpecVersion());
				addQuestionQuery.setString(11, question.getLanguage());
				addQuestionQuery.addBatch();
			}
		}
		counts = addQuestionQuery.executeBatch();
		if (counts.length != numAdded) {
			logger.warn("Number of batch added question counts do not match.  Expected" + //$NON-NLS-1$
					String.valueOf(numAdded)+".  Actual="+String.valueOf(counts.length)); //$NON-NLS-1$
		}
		for (int count:counts) {
			if (count != 1) {
				logger.warn("Unexpected update count.  Expected 1, found "+String.valueOf(count)); //$NON-NLS-1$
			}
		}
	}

	public synchronized void addSurvey(Survey survey) throws SQLException, SurveyResponseException, QuestionException {
		if (survey.getSpecVersion() == null) {
			throw(new SurveyResponseException("Spec version missing for survey")); //$NON-NLS-1$
		}
		if (survey.getSections() == null) {
			throw(new SurveyResponseException("Sections missing for survey")); //$NON-NLS-1$
		}
		if (this.getSpecId(survey.getSpecVersion(), survey.getLanguage(), false)> 0) {
			throw(new SurveyResponseException("Can not add survey.  Survey version "+survey.getSpecVersion()+" already exists.")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		Savepoint save = this.connection.setSavepoint();
		Statement stmt = null;
		if (insertSpecQuery == null) {
			insertSpecQuery = connection.prepareStatement("insert into spec (version,language) values (?,?)"); //$NON-NLS-1$
		}
		if (insertSectionQuery == null) {
			insertSectionQuery = connection.prepareStatement("insert into section (name, title, spec_version) "+ //$NON-NLS-1$
					"values (?,?,?)"); //$NON-NLS-1$
		}
		try {
			stmt = this.connection.createStatement();
			long specId = getSpecId(survey.getSpecVersion(), survey.getLanguage(), false);
			if (specId < 0) {
				insertSpecQuery.setString(1, survey.getSpecVersion());
				if (survey.getLanguage() == null) {
					insertSpecQuery.setNull(2, java.sql.Types.VARCHAR);
				} else {
					insertSpecQuery.setString(2, survey.getLanguage());
				}
				int t = insertSpecQuery.executeUpdate();
				if (t != 1) {
					logger.error("Unable to create new spec version for version "+survey.getSpecVersion()); //$NON-NLS-1$
					throw(new SurveyResponseException("Unable to create new spec version for version "+survey.getSpecVersion())); //$NON-NLS-1$
				}
				specId = getSpecId(survey.getSpecVersion(), survey.getLanguage(), false);
				if (specId < 0) {
					logger.error("Unable to create new spec version for version "+survey.getSpecVersion()); //$NON-NLS-1$
					throw(new SurveyResponseException("Unable to create new spec version for version "+survey.getSpecVersion())); //$NON-NLS-1$
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
				logger.warn("Number of batch updated sections counts do not match.  Expected" + //$NON-NLS-1$
						String.valueOf(survey.getSections().size())+".  Actual="+String.valueOf(counts.length)); //$NON-NLS-1$
			}
			for (int count:counts) {
				if (count != 1) {
					logger.warn("Unexpected update count.  Expected 1, found "+String.valueOf(count)); //$NON-NLS-1$
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
					logger.error("Error rolling back transaction",ex2); //$NON-NLS-1$
				}
				save = null;
			}
			throw(ex);
		} catch(SurveyResponseException ex) {
			if (save != null) {
				try {
					this.connection.rollback(save);
				} catch (Exception ex2) {
					logger.error("Error rolling back transaction",ex2); //$NON-NLS-1$
				}
				save = null;
			}
			throw(ex);
		} catch(QuestionException ex) {	
			if (save != null) {
				try {
					this.connection.rollback(save);
				} catch (Exception ex2) {
					logger.error("Error rolling back transaction",ex2); //$NON-NLS-1$
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
	 * @param language tag in IETF RFC 5646 format
	 * @param allowDefaultLanguage If true, use the default language if the specificed languages is not found.  If false, return a -1 if the language is not found
	 * @return ID from the spec table associated with the version and language. If the language is not available, the ID for the default language will be returned.
	 * @throws SQLException
	 */
	public long getSpecId(String specVersion, String language, boolean allowDefaultLanguage) throws SQLException {
		return getSpecId(this.connection, specVersion, language, allowDefaultLanguage);
	}

	/**
	 * @param specVersion Version for the spec
	 * @param language tag in IETF RFC 5646 format
	 * @param allowDefaultLanguage If true, use the default language if the specificed languages is not found.  If false, return a -1 if the language is not found
	 * @return true if a survey exists for the version and language
	 * @throws SQLException
	 */
	public boolean surveyExists(String specVersion, String language, boolean allowDefaultLanguage) throws SQLException {
		try {
			return getSpecId(specVersion, language, allowDefaultLanguage) > 0;
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
			result = stmt.executeQuery("select distinct version from spec order by version"); //$NON-NLS-1$
			ArrayList<String> retval = new ArrayList<String>();
			while (result.next()) {
				retval.add(result.getString(1));
			}
			return retval;
		} catch (SQLException ex) {
			logger.error("SQL Error getting survey versions", ex); //$NON-NLS-1$
			throw ex;
		} finally {
			if (result != null) {
				result.close();
			}
			stmt.close();
		}
	}

	/**
	 * Updates the title for a section with sectionName in the survey specVersion,locale
	 * @param specVersion Version of the spec
	 * @param locale Locale or language of the spec
	 * @param sectionName Name of the section
	 * @param sectionTitle Title to replace the existing title with
	 * @throws SQLException 
	 */
	public void updateSectionTitle(String specVersion, String locale, String sectionName, String sectionTitle) throws SQLException {
		Savepoint save = this.connection.setSavepoint();
		try {
			if (updateSectionTitleQuery == null) {
				updateSectionTitleQuery = this.connection.prepareStatement(
						"update section set (title) = (?) " + //$NON-NLS-1$
						"where name=? and " + //$NON-NLS-1$
						"spec_version=(select id from spec where version=? and language=?)"); //$NON-NLS-1$
			}
			updateSectionTitleQuery.setString(1, sectionTitle);
			updateSectionTitleQuery.setString(2, sectionName);
			updateSectionTitleQuery.setString(3, specVersion);
			updateSectionTitleQuery.setString(4, locale);
			int numRows = updateSectionTitleQuery.executeUpdate();
			if (numRows < 1) {
				logger.warn("No title updated for spec version "+specVersion+" locale "+locale+" section "+sectionName); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else if (numRows > 1) {
				logger.warn(Integer.toString(numRows)+"were updated in title updated for spec version "+specVersion+" locale "+locale+" section "+sectionName); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
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

	/**
	 * @param specVersion Version of the specification
	 * @return all languages supported for the specific spec version
	 * @throws SQLException 
	 */
	public List<String> getSurveyLanguages(String specVersion) throws SQLException {
		if (this.getLanguagesQuery == null) {
			this.getLanguagesQuery = connection.prepareStatement("select language from spec where version=?"); //$NON-NLS-1$
		}
		ResultSet result = null;
		try {
			this.getLanguagesQuery.setString(1, specVersion);
			List<String> retval = new ArrayList<>();
			result = this.getLanguagesQuery.executeQuery();
			while (result.next()) {
				retval.add(result.getString(1));
			}
			return retval;
		} finally {
			if (result != null) {
				result.close();
			}
		}
	}
}