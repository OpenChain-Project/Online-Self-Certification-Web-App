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

import org.openchain.certification.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
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
	
	static final Logger logger = LoggerFactory.getLogger(I18N.class);

	private static final String BASE_RESOURCE_NAME = "messages"; //$NON-NLS-1$

	/**
	 * @param key Key for the resource file
	 * @param language tag in IETF RFC 5646 format
	 * @return
	 */
	public static String getMessage(String key, String language, Object... args) {
		if (language == null) {
			language = User.DEFAULT_LANGUAGE;
		}
		Locale locale = Locale.forLanguageTag(language);
		if (locale == null) {
			logger.warn("Language "+language+" not supported by Java.  Using default language"); //$NON-NLS-1$ //$NON-NLS-2$
			locale = Locale.forLanguageTag(User.DEFAULT_LANGUAGE);
			if (locale == null) {
				logger.error("No locale for the default language"); //$NON-NLS-1$
				throw(new RuntimeException("No local for the default language")); //$NON-NLS-1$
			}
		}
		ResourceBundle.Control utf8Control = new Utf8ResourceBundleControl();
		ResourceBundle bundle = ResourceBundle.getBundle(BASE_RESOURCE_NAME, locale, utf8Control);
		String template = bundle.getString(key);
		try {
			MessageFormat mf = new MessageFormat(template, locale);
			return mf.format(args, new StringBuffer(), null).toString();
		} catch(IllegalArgumentException ex) {
			logger.error("Invalid argument for message template with key `"+key+"`: "+template, ex);
			return template;
		}
	}

}
