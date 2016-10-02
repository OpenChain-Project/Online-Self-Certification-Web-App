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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openchain.certification.model.YesNoQuestion.YesNo;

/**
 * A question asked in the self certification
 * @author Gary O'Neall
 *
 */
public abstract class Question implements Comparable<Question> {
	protected String question;
	protected String sectionName;
	private String number;
	private String subQuestionNumber = null;
	protected String type;
	protected String specVersion;
	transient static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)(\\.\\d+)?(\\.\\d+)?");
	transient private Matcher numberMatch;
	private String specReference = "";

	public Question(String question, String sectionName, String number, String specVersion) throws QuestionException {
		if (specVersion == null) {
			throw(new QuestionException("Spec version for question was not specified"));
		}
		if (number == null) {
			throw(new QuestionException("Question number for question was not specified"));
		}
		this.numberMatch = NUMBER_PATTERN.matcher(number);
		if (!this.numberMatch.matches()) {
			throw(new QuestionException("Invalid format for question number "+number+".  Must be of the format N or N.N or N.N.N"));
		}
		this.question = question;
		this.sectionName = sectionName;
		this.number = number;
		this.specVersion = specVersion;
	}

	/**
	 * @return the question
	 */
	public String getQuestion() {
		return question;
	}

	/**
	 * @param question the question to set
	 */
	public void setQuestion(String question) {
		this.question = question;
	}
	
	/**
	 * @return true if the answer is the correct answer
	 */
	public abstract boolean validate(Object answer);

	/**
	 * @return the section
	 */
	public String getSectionName() {
		return sectionName;
	}

	/**
	 * @param section the section to set
	 */
	public void setSection(String sectionName) {
		this.sectionName = sectionName;
	}

	/**
	 * @return the number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * @param number the number to set
	 * @throws QuestionException 
	 */
	public void setNumber(String number) throws QuestionException {
		if (number == null) {
			throw(new QuestionException("Question number for question was not specified"));
		}
		this.numberMatch = NUMBER_PATTERN.matcher(number);
		if (!this.numberMatch.matches()) {
			this.numberMatch = NUMBER_PATTERN.matcher(this.number);
			throw(new QuestionException("Invalid format for question number "+number+".  Must be of the format N or N.N or N.N.N"));
		}
		this.number = number;
	}

	public void addSubQuestionOf(String subQuestionNumber) {
		this.subQuestionNumber = subQuestionNumber;
	}
	
	public String getSubQuestionNumber() {
		return this.subQuestionNumber;
	}
	
	
	
	/**
	 * @return the specVersion
	 */
	public String getSpecVersion() {
		return specVersion;
	}

	/**
	 * @param specVersion the specVersion to set
	 */
	public void setSpecVersion(String specVersion) {
		this.specVersion = specVersion;
	}
	
	public Matcher getNumberMatch() {
		return this.numberMatch;
	}

	@Override
	public int compareTo(Question compare) {
		int retval = this.specVersion.compareToIgnoreCase(compare.getSpecVersion());
		if (retval == 0) {
			Matcher compareMatch = compare.getNumberMatch();
			int digit1 = Integer.parseInt(this.numberMatch.group(1));
			int compareDigit1 = Integer.parseInt(compareMatch.group(1));
			retval = digit1 - compareDigit1;
			if (retval == 0) {
				if (this.numberMatch.groupCount() > 1 && this.numberMatch.group(2) != null) {
					if (compareMatch.groupCount() > 1 && compareMatch.group(2) != null) {
						int digit2 = Integer.parseInt(this.numberMatch.group(2).substring(1));
						int compareDigit2 = Integer.parseInt(compareMatch.group(2).substring(1));
						retval = digit2 - compareDigit2;
						if (retval == 0) {
							if (this.numberMatch.groupCount() > 2 && this.numberMatch.group(3) != null) {
								if (compareMatch.groupCount() > 2 && compareMatch.group(3) != null) {
									int digit3 = Integer.parseInt(this.numberMatch.group(3).substring(1));
									int compareDigit3 = Integer.parseInt(compareMatch.group(3).substring(1));
									return digit3 - compareDigit3;
								} else {
									return 1;
								}
							} else {
								if (compareMatch.groupCount() > 2 && compareMatch.group(3) != null) {
									return -1;
							}
								
							}
						}
					} else {
						return 1;
					}
				} else if (compareMatch.groupCount() > 1 && compareMatch.group(2) != null) {
					return -1;
				}
			}
		}
		return retval;
	}

	protected abstract Object getCorrectAnswer();
	
	
	
	/**
	 * @return the specReference
	 */
	public String getSpecReference() {
		return specReference;
	}

	/**
	 * @param specReference the specReference to set
	 */
	public void setSpecReference(String specReference) {
		this.specReference = specReference;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param sectionName the sectionName to set
	 */
	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	/**
	 * @param subQuestionNumber the subQuestionNumber to set
	 */
	public void setSubQuestionNumber(String subQuestionNumber) {
		this.subQuestionNumber = subQuestionNumber;
	}

	/**
	 * @return the qusetion information formatted in a CSV row for a Survey CSV file
	 */
	public String[] toCsvRow() {
		String[] retval = new String[Survey.CSV_COLUMNS.length];
		retval[0] = this.sectionName;
		retval[1] = this.number;
		retval[2] = this.specReference ;
		retval[3] = this.question;
		retval[4] = this.type;
		retval[5] = this.getCorrectAnswer().toString();
		if (this instanceof YesNoQuestionWithEvidence) {
			YesNoQuestionWithEvidence me = (YesNoQuestionWithEvidence)this;
			retval[6] = me.getEvidencePrompt();
			if (me.getEvidenceValidation() == null) {
				retval[7] = "";
			} else {
				retval[7] = me.getEvidenceValidation().toString();
			}		
		} else if (this instanceof YesNoNotApplicableQuestion) {
			retval[6] = ((YesNoNotApplicableQuestion)this).getNotApplicablePrompt();
		} else {
			retval[6] = "";
			retval[7] = "";
		}
		retval[8] = this.subQuestionNumber;
		return retval;
	}

	public static Question fromCsv(String[] row, String specVersion) throws QuestionException {
		if (row.length < 8) {
			throw(new QuestionTypeException("Not enough columns.  Expected 8, found "+String.valueOf(row.length)));
		}
		String sectionName = row[0];
		String number = row[1];
		String specReference = row[2];
		String question = row[3];
		String type = row[4];
		String correctAnswer = row[5];
		String evidencePrompt = row[6];
		String evidenceValidation = row[7];
		String subQuestionNumber = row[8];
		if (sectionName == null || sectionName.isEmpty()) {
			throw(new QuestionTypeException("No section name specified in CSV row"));
		}
		if (number == null || number.isEmpty()) {
			throw(new QuestionTypeException("No number specified in CSV row"));
		}
		if (type == null) {
			throw(new QuestionTypeException("No type specified in CSV row"));
		}
		Question retval = null;
		if (type.equals(SubQuestion.TYPE_NAME)) {
			int minAnswers = 0;
			try {
				minAnswers = Integer.parseInt(correctAnswer);
			} catch(Exception ex) {
				throw(new QuestionException("Invalid minimum number of answers for subquestion number "+
							number+":"+correctAnswer+".  Must be a number."));
			}
			retval = new SubQuestion(question, sectionName, number, specVersion, minAnswers);
		} else if (type.equals(YesNoQuestion.TYPE_NAME)) {
			retval = new YesNoQuestion(question, sectionName, number, specVersion, 
					YesNo.valueOf(correctAnswer));
		} else if (type.equals(YesNoQuestionWithEvidence.TYPE_NAME)) {
			Pattern valPattern = null;
			if (evidenceValidation != null && !evidenceValidation.isEmpty()) {
				valPattern = Pattern.compile(evidenceValidation);
			}
			retval = new YesNoQuestionWithEvidence(question, sectionName, number, specVersion, 
					YesNo.valueOf(correctAnswer), evidencePrompt, valPattern);
		} else if (type.equals(YesNoNotApplicableQuestion.TYPE_NAME)) {
			retval = new YesNoNotApplicableQuestion(question, sectionName, number, specVersion, 
					YesNo.valueOf(correctAnswer), evidencePrompt);
		} else {
			throw(new QuestionTypeException("Unknown question type: "+type));
		}
		if (specReference != null) {
			retval.setSpecReference(specReference);
		}
		if (subQuestionNumber != null && !subQuestionNumber.isEmpty()) {
			retval.addSubQuestionOf(subQuestionNumber);
		}
		return retval;
	}
}
