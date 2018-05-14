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

import org.openchain.certification.I18N;
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
	private String subQuestionOfNumber = null;
	protected String type;
	protected String specVersion; 
	transient static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)(\\.\\d+)?(\\.\\d+)?"); //$NON-NLS-1$
	transient static final Pattern NUM_ALPH_AROMAN_PATTERN = Pattern.compile("(\\d+)(\\.[a-z]+)?(\\.[ivxlmcd]+)?"); //$NON-NLS-1$
	transient private Matcher numberMatch;
	private String[] specReference = new String[0];
	private String language;
	
	/**
	 * @param question Text for the question
	 * @param sectionName Name of the section
	 * @param number Number of the question
	 * @param specVersion Version for the spec
	 * @param language tag in IETF RFC 5646 format
	 * @throws QuestionException
	 */
	public Question(String question, String sectionName, String number, String specVersion, String language) throws QuestionException {
		if (specVersion == null) {
			throw(new QuestionException(I18N.getMessage("Question.2",language))); //$NON-NLS-1$
		}
		if (number == null) {
			throw(new QuestionException(I18N.getMessage("Question.3",language))); //$NON-NLS-1$
		}
		this.setNumberMatcher(number, specVersion);
		if (!this.numberMatch.matches()) {
			if (specVersion.compareTo("1.0.2") < 0) { //$NON-NLS-1$
				throw(new QuestionException(I18N.getMessage("Question.7",language)+number+I18N.getMessage("Question.6",language))); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				throw(new QuestionException(I18N.getMessage("Question.7",language)+number+I18N.getMessage("Question.8",language))); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		this.question = question;
		this.sectionName = sectionName;
		this.number = number;
		this.specVersion = specVersion;
		this.language = language;
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}



	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
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
			throw(new QuestionException(I18N.getMessage("Question.9",language))); //$NON-NLS-1$
		}
		setNumberMatcher(number, this.specVersion);
		if (!this.numberMatch.matches()) {
			setNumberMatcher(this.number, this.specVersion);
			throw(new QuestionException(I18N.getMessage("Question.7",language)+number+I18N.getMessage("Question.6",language))); //$NON-NLS-1$ //$NON-NLS-2$
		}
		this.number = number;
	}
	
	public String getSubQuestionOfNumber() {
		return this.subQuestionOfNumber;
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

	/**
	 * Create a number matcher.
	 * @param number
	 */
	private void setNumberMatcher(String number, String specVersion) {
		// Since we changed numbering systems after spec version 1.0.2, we need
		// to set the appropriate pattern matches
		if (specVersion.compareTo("1.0.2") < 0) { //$NON-NLS-1$
			this.numberMatch = NUMBER_PATTERN.matcher(number);
		} else {
			this.numberMatch = NUM_ALPH_AROMAN_PATTERN.matcher(number);
		}
	}
	
	@Override
	public int compareTo(Question compare) {
		int retval = this.specVersion.compareToIgnoreCase(compare.getSpecVersion());
		if (retval != 0) {
			return retval;
		} else if (this.specVersion.compareTo("1.0.2") < 0) { //$NON-NLS-1$
				return compareNumeric(compare);
		} else {
			return compareNumAlphaRoman(compare);
		}
	}

	private int compareNumAlphaRoman(Question compare) {
		Matcher compareMatch = compare.getNumberMatch();
		int digit1 = Integer.parseInt(this.numberMatch.group(1));
		int compareDigit1 = Integer.parseInt(compareMatch.group(1));
		int retval = digit1 - compareDigit1;
		if (retval == 0) {
			if (this.numberMatch.groupCount() > 1 && this.numberMatch.group(2) != null) {
				if (compareMatch.groupCount() > 1 && compareMatch.group(2) != null) {
					int lev2 = alphaToInt(this.numberMatch.group(2).substring(1));
					int compareLev2 = alphaToInt(compareMatch.group(2).substring(1));
					retval = lev2 - compareLev2;
					if (retval == 0) {
						if (this.numberMatch.groupCount() > 2 && this.numberMatch.group(3) != null) {
							if (compareMatch.groupCount() > 2 && compareMatch.group(3) != null) {
								int lev3 = romanToInt(this.numberMatch.group(3).substring(1));
								int compareLev3 = romanToInt(compareMatch.group(3).substring(1));
								return lev3 - compareLev3;
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
		return retval;
	}

	private int romanToInt(char letter) {
		// NOTE: the letter is assumed to be upper case
		switch(letter) {
		case 'M': return 1000;
		case 'D': return 500;
		case 'C': return 100;
		case 'L': return 50;
		case 'X': return 10;
		case 'V': return 5;
		case 'I': return 1;
		default: return 0;
	}
}
	private int alphaToInt(String alpha) {
		int retval = 0;
		int numCharsUsed = 27; // Assum standard alphabet lowercase only
		int valueOfa = Character.getNumericValue('a')-1;
		String lowerAlppha = alpha.toLowerCase();
		for (int i = lowerAlppha.length()-1; i >= 0; i--) {
			retval = retval * numCharsUsed + (Character.getNumericValue(lowerAlppha.charAt(i))-valueOfa);
		}
		return retval;
	}

	private int romanToInt(String roman) {
		int retval = 0;
		String upperRoman = roman.toUpperCase();
		for (int i = 0; i < upperRoman.length()-1; i++) {
			int d1 = romanToInt(upperRoman.charAt(i));
			int d2 = romanToInt(upperRoman.charAt(i+1));
			if (d1 < d2) {
				retval = retval - d1;
			} else {
				retval = retval + d1;
			}
		}
		retval = retval + romanToInt(upperRoman.charAt(upperRoman.length()-1));
		return retval;
	}

	private int compareNumeric(Question compare) {
		Matcher compareMatch = compare.getNumberMatch();
		int digit1 = Integer.parseInt(this.numberMatch.group(1));
		int compareDigit1 = Integer.parseInt(compareMatch.group(1));
		int retval = digit1 - compareDigit1;
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
		return retval;
	}

	protected abstract Object getCorrectAnswer();
	
	
	
	/**
	 * @return the specReference
	 */
	public String[] getSpecReference() {
		return specReference;
	}

	/**
	 * @param specReference the specReference to set
	 */
	public void setSpecReference(String[] specReference) {
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
	public void setSubQuestionOfNumber(String subQuestionNumber) {
		this.subQuestionOfNumber = subQuestionNumber;
	}

	/**
	 * @return the question information formatted in a CSV row for a Survey CSV file
	 */
	public String[] toCsvRow() {
		String[] retval = new String[Survey.CSV_COLUMNS.length];
		retval[0] = this.sectionName;
		retval[1] = this.number;
		retval[2] = specReferenceArrayToStr(this.specReference);
		retval[3] = this.question;
		retval[4] = this.type;
		retval[5] = this.getCorrectAnswer().toString();
		if (this instanceof YesNoQuestionWithEvidence) {
			YesNoQuestionWithEvidence me = (YesNoQuestionWithEvidence)this;
			retval[6] = me.getEvidencePrompt();
			if (me.getEvidenceValidation() == null) {
				retval[7] = ""; //$NON-NLS-1$
			} else {
				retval[7] = me.getEvidenceValidation().toString();
			}		
		} else if (this instanceof YesNoNotApplicableQuestion) {
			retval[6] = ((YesNoNotApplicableQuestion)this).getNotApplicablePrompt();
		} else {
			retval[6] = ""; //$NON-NLS-1$
			retval[7] = ""; //$NON-NLS-1$
		}
		retval[8] = this.subQuestionOfNumber;
		return retval;
	}

	/**
	 * @param row CSV row of columns containing the question
	 * @param specVersion Version of the specification
	 * @param language tag in IETF RFC 5646 format
	 * @return
	 * @throws QuestionException
	 */
	public static Question fromCsv(String[] row, String specVersion, String language) throws QuestionException {
		if (row.length < 8) {
			throw(new QuestionTypeException("Not enough columns.  Expected 8, found "+String.valueOf(row.length))); //$NON-NLS-1$
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
			throw(new QuestionTypeException("No section name specified in CSV row")); //$NON-NLS-1$
		}
		if (number == null || number.isEmpty()) {
			throw(new QuestionTypeException("No number specified in CSV row")); //$NON-NLS-1$
		}
		if (type == null) {
			throw(new QuestionTypeException("No type specified in CSV row")); //$NON-NLS-1$
		}
		Question retval = null;
		if (type.equals(SubQuestion.TYPE_NAME)) {
			int minAnswers = 0;
			try {
				minAnswers = Integer.parseInt(correctAnswer);
			} catch(Exception ex) {
				throw(new QuestionException("Invalid minimum number of answers for subquestion number "+ //$NON-NLS-1$
							number+":"+correctAnswer+".  Must be a number.")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			retval = new SubQuestion(question, sectionName, number, specVersion, language, minAnswers);
		} else if (type.equals(YesNoQuestion.TYPE_NAME)) {
			retval = new YesNoQuestion(question, sectionName, number, specVersion, language,
					YesNo.valueOf(correctAnswer));
		} else if (type.equals(YesNoQuestionWithEvidence.TYPE_NAME)) {
			Pattern valPattern = null;
			if (evidenceValidation != null && !evidenceValidation.isEmpty()) {
				valPattern = Pattern.compile(evidenceValidation);
			}
			retval = new YesNoQuestionWithEvidence(question, sectionName, number, specVersion, language, 
					YesNo.valueOf(correctAnswer), evidencePrompt, valPattern);
		} else if (type.equals(YesNoNotApplicableQuestion.TYPE_NAME)) {
			retval = new YesNoNotApplicableQuestion(question, sectionName, number, specVersion, language,
					YesNo.valueOf(correctAnswer), evidencePrompt);
		} else {
			throw(new QuestionTypeException("Unknown question type: "+type)); //$NON-NLS-1$
		}
		retval.setSpecReference(specReferenceStrToArray(specReference));
		if (subQuestionNumber != null && !subQuestionNumber.isEmpty()) {
			retval.setSubQuestionOfNumber(subQuestionNumber);
		}
		return retval;
	}
	
	public abstract Question clone();
	
	/**
	 * Convert a specification reference to a string using comma's to separate the spec references
	 * @param specReference
	 * @return
	 */
	public static String specReferenceArrayToStr(String[] specReference) {
		StringBuilder specRefStr = new StringBuilder();
		if (specReference != null && specReference.length > 0) {
			specRefStr.append(specReference[0]);
			for (int i = 1; i < specReference.length; i++) {
				specRefStr.append(","); //$NON-NLS-1$
				specRefStr.append(specReference[i]);
			}
		}
		return specRefStr.toString();
	}

	/**
	 * Convert a string to a specification reference array
	 * @param specReferenceStr
	 * @return
	 */
	public static String[] specReferenceStrToArray(String specReferenceStr) {
		if (specReferenceStr == null) {
			return new String[0];
		} else {
			String[] specReferenceAr = specReferenceStr.split(","); //$NON-NLS-1$
			for (int i = 0; i < specReferenceAr.length; i++) {
				specReferenceAr[i] = specReferenceAr[i].trim();
			}
			return specReferenceAr;
		}
	}
}
