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
package org.openchain.certification.model;

import java.util.Map;

/**
 * Hold summary information about a questionairre submitted by a user
 * @author Gary O'Neall
 *
 */
public class Submission {
	
	/**
	 * User who submitted the request
	 */
	private User user;
	/**
	 * True if it was fully submitted
	 */
	private boolean submitted;
	/**
	 * Percent questions which have been answered
	 */
	private int percentComplete;
	/**
	 * Score calculated by the number of correct answers divided by the total answers
	 */
	private int score;
	
	public Submission(User user, boolean submitted, int percentComplete, int score) {
		this.user = user;
		this.submitted = submitted;
		this.percentComplete = percentComplete;
		this.score = score;
	}
	
	public Submission(SurveyResponse response) {
		this.user = response.getResponder();
		this.submitted = response.isSubmitted();
		this.percentComplete = calcPercentComplete(response.getSurvey(), response.getResponses());
		this.score = calcScore(response.getSurvey(), response.getResponses());
	}
	@SuppressWarnings("unused")
	private int calcScore(Survey survey, Map<String, Answer> answers) {
		int numQuestions = 0;
		int numCorrectAnswers = 0;
		int numIncorrectAnswers = 0;
		int numNotAnswered = 0;
		for (Section section:survey.getSections()) {
			for (Question question:section.getQuestions()) {
				if (question.getSubQuestionNumber() == null) {
					numQuestions++ ;	// we don't want to count subquestions
					Answer answer = answers.get(question.getNumber());
					if (answer != null) {
						if (question.validate(answer)) {
							numCorrectAnswers++;
						} else {
							numIncorrectAnswers++;
						}
					} else {
						numNotAnswered++;
					}
				}
			}
		}
		return (numCorrectAnswers * 100) / numQuestions;
	}

	private int calcPercentComplete(Survey survey, Map<String, Answer> answers) {
		int numQuestions = 0;
		int numAnswers = 0;
		for (Section section:survey.getSections()) {
			for (Question question:section.getQuestions()) {
				if (question.getSubQuestionNumber() == null) {
					numQuestions++ ;	// we don't want to count subquestions
					Answer answer = answers.get(question.getNumber());
					if (answer != null) {
						numAnswers++;
					}
				}
			}
		}
		return (numAnswers * 100) / numQuestions;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
	/**
	 * @return the submitted
	 */
	public boolean isSubmitted() {
		return submitted;
	}
	/**
	 * @param submitted the submitted to set
	 */
	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}
	/**
	 * @return the percentComplete
	 */
	public int getPercentComplete() {
		return percentComplete;
	}
	/**
	 * @param percentComplete the percentComplete to set
	 */
	public void setPercentComplete(int percentComplete) {
		this.percentComplete = percentComplete;
	}
	/**
	 * @return the score
	 */
	public int getScore() {
		return score;
	}
	/**
	 * @param score the score to set
	 */
	public void setScore(int score) {
		this.score = score;
	}
}
