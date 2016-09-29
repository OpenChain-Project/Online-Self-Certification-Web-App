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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
	
	public SurveyDbDao(Connection connection) {
		this.connection = connection;
	}
	
	public Survey getSurvey() throws SQLException, QuestionException, SurveyResponseException {
		return getSurvey(this.connection, null);
	}

	/**
	 * @param specVersion Version of the specification.  If null, will get the latest spec version available
	 * @return Survey with questions (static information) for the latest version
	 * @throws SQLException
	 * @throws SurveyResponseException 
	 * @throws QuestionException 
	 */
	public static Survey getSurvey(Connection con, String specVersion) throws SQLException, SurveyResponseException, QuestionException {
		Survey retval = new Survey();
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
			for (int i = 0; i < sections.size(); i++) {
				result.close();
				result = stmt.executeQuery("select number, question, type, correct_answer, evidence_prompt, evidence_validation, subquestion_of from question where section_id="+
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
						int minValidQuestions = 0;
						try {
							minValidQuestions = Integer.parseInt(result.getString("correct_answer"));
						} catch (Exception ex) {
							logger.error("Can not parse min correct answers in DB: "+result.getString("correct_answer"));
							throw new QuestionTypeException("Unexpected error getting sub question information from the database.");
						}
						question = new SubQuestion(result.getString("question"), section.getName(), 
								result.getString("number"),  specVersion, minValidQuestions);
					} else {
						throw(new QuestionTypeException("Unknown question type in database: "+type));
					}
					long subQuestionId = result.getLong("subquestion_of");
					if (subQuestionId > 0) {
						question.addSubQuestionOf(getQuestionNumber(subQuestionId, con));
					}
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
}
