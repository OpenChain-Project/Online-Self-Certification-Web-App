/**
 * Copyright (c) 2018 Source Auditor Inc.
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
package org.openchain.certification;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.openchain.certification.model.Question;

/**
 * Custom deserializer for Question objects that selects the correct subclass based 
 * on the QuestionType
 * 
 * @author Gary O'Neall
 *
 */
public class QuestionJsonDeserializer implements JsonDeserializer<Question> {
	
	static final String TYPE_PROPERTY = "type";
	static final Map<String, String> TYPE_TO_CLASS = new HashMap<String, String>();
	static {
		TYPE_TO_CLASS.put("SUBQUESTIONS", "org.openchain.certification.model.SubQuestion");
		TYPE_TO_CLASS.put("YES_NO", "org.openchain.certification.model.YesNoQuestion");
		TYPE_TO_CLASS.put("YES_NO_NA", "org.openchain.certification.model.YesNoNotApplicableQuestion");
		TYPE_TO_CLASS.put("YES_NO_EVIDENCE", "org.openchain.certification.model.YesNoQuestionWithEvidence");
	}

	/* (non-Javadoc)
	 * @see com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement, java.lang.reflect.Type, com.google.gson.JsonDeserializationContext)
	 */
	@Override
	public Question deserialize(JsonElement element, Type type,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject jo = element.getAsJsonObject();
		String questionType = jo.get(TYPE_PROPERTY).getAsString();
		String subClassName = TYPE_TO_CLASS.get(questionType);
		if (subClassName == null) {
			throw new JsonParseException("Unknown question type: "+questionType);
		}
		Class<?> clazz = null;
		try {
			clazz = Class.forName(subClassName);
		} catch (ClassNotFoundException e) {
			throw new JsonParseException(e.getMessage());
		}
		return context.deserialize(element, clazz);
	}

}
