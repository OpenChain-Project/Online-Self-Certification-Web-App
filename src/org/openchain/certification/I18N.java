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

import org.apache.log4j.Logger;
import org.openchain.certification.model.User;

import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Supports the localization of Java strings for the OpenChainCertification web application
 * 
 * The primary interface is <code>getString(String key, String language)</code>
 * @author Gary O'Neall
 *
 */
public final class I18N {
	
	static final Logger logger = Logger.getLogger(I18N.class);

	private static final String BASE_RESOURCE_NAME = "messages";
	
	static Map<String, Locale> languageLocal = new HashMap<String, Locale>();
	static {
		String[] languages2Chars = Locale.getISOLanguages();
		for (String languages2Char : languages2Chars) {
		    Locale locale = new Locale(languages2Char);
		    languageLocal.put(locale.getISO3Language(), locale);
		    languageLocal.put(languages2Char, locale);
		}
	}

	/**
	 * @param key Key for the resource file
	 * @param language ISO 639 alpha-2 or alpha-3 language code
	 * @return
	 */
	public static String getMessage(String key, String language) {
		if (language == null) {
			language = User.DEFAULT_LANGUAGE;
		}
		Locale locale = languageLocal.get(language);
		if (locale == null) {
			logger.warn("Language "+language+" not supported by Java.  Using default language");
			locale = languageLocal.get(User.DEFAULT_LANGUAGE);
			if (locale == null) {
				logger.error("No locale for the default language");
				throw(new RuntimeException("No local for the default language"));
			}
		}
		ResourceBundle bundle = ResourceBundle.getBundle(BASE_RESOURCE_NAME, locale);
		return bundle.getString(key);
	}

}