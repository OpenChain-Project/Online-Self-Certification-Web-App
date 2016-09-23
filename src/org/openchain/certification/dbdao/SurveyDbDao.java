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
import java.util.List;
import java.util.regex.Pattern;

import org.openchain.certification.model.Question;
import org.openchain.certification.model.QuestionTypeException;
import org.openchain.certification.model.Section;
import org.openchain.certification.model.Survey;
import org.openchain.certification.model.YesNoQuestion;
import org.openchain.certification.model.YesNoQuestion.YesNo;
import org.openchain.certification.model.YesNoQuestionWithEvidence;

/**
 * DB Data Access Object for the survey.
 * @author Gary O'Neall
 *
 */
public class SurveyDbDao {
	
	Connection connection = null;
	
	public SurveyDbDao(Connection connection) {
		this.connection = connection;
	}

	public Survey getSurvey() throws SQLException, QuestionTypeException {
		Survey retval = new Survey();
		Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet result = null;
		try {
			result = stmt.executeQuery("select name, title, id from section order by name asc");
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
				result = stmt.executeQuery("select name, question, type, correct_answer, evidence_prompt, evidence_validation from question where section_id="+
								String.valueOf(sectionIds.get(i)) + " order by name asc");
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
								result.getString("name"), 
								YesNo.valueOf(result.getString("correct_answer")));
					} else if (type.equals(YesNoQuestionWithEvidence.TYPE_NAME)) {
						question = new YesNoQuestionWithEvidence(result.getString("question"), section.getName(), 
								result.getString("name"), 
								YesNo.valueOf(result.getString("correct_answer")), 
								result.getString("evidence_prompt"), 
								Pattern.compile(result.getString("evidence_validation")));
					} else {
						throw(new QuestionTypeException("Unknown question type in database: "+type));
					}
					questions.add(question);
				}
			}
			return retval;
		} catch (SQLException ex) {
			throw ex;
			//TODO: add logging
		} finally {
			if (result != null) {
				result.close();
			}
			stmt.close();
		}
		
	}
}
