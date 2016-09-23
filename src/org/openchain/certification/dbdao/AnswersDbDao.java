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
import java.util.List;

import org.openchain.certification.ResponseAnswer;

/**
 * Answers database access object
 * @author Gary O'Neall
 *
 */
public class AnswersDbDao {

	private Connection connection;
	
	public AnswersDbDao(Connection connection) {
		
	}

	public void addOrUpdateAnswers(List<ResponseAnswer> answers, String username) {
		// TODO Auto-generated method stub
		
	}

	public String getSectionName(ResponseAnswer responseAnswer) {
		// TODO Auto-generated method stub
		return null;
	}
}
