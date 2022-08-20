/**
 * Copyright (c) 2021 Source Auditor Inc.
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Public static helper class to manage ReCaptcha verifications
 * 
 * See https://developers.google.com/recaptcha/docs/verify?hl=en
 * @author Gary O'Neall
 *
 */
public class ReCaptcha {

	static final Logger logger = LoggerFactory.getLogger(ReCaptcha.class);
	public static final String RECAPTCHA_URL = "https://www.recaptcha.net/recaptcha/api/siteverify"; //$NON-NLS-1$
	private static final String USER_AGENT = "Mozilla/5.0"; //$NON-NLS-1$
	
	private static Gson gson = new Gson();
	
	/**
	 * Verifies the reCaptcha using the Google reCaptcha service
	 * @param servletConfig configuration which holds the secret key
	 * @param reCaptchaResponse response from the reCaptcha widget or JavaScript front end
	 * @return
	 * @throws ReCaptchaException 
	 */
	public static boolean verifyReCaptcha(String reCaptchaResponse, String locale) throws ReCaptchaException {
		if (Objects.isNull(reCaptchaResponse) || reCaptchaResponse.isEmpty()) {
			return false;
		}
		String secret = System.getProperty("RECAPTCHA_SECRET"); //$NON-NLS-1$
		URL reCaptchaUrl = null;
		try {
			reCaptchaUrl = new URL(RECAPTCHA_URL);
		} catch(MalformedURLException ex) {
			logger.error("Unexpected URL error accessing the ReCaptcha service.", ex); //$NON-NLS-1$
			throw new ReCaptchaException(I18N.getMessage("ReCaptcha.3", locale)); //$NON-NLS-1$
		}
		try {
			HttpsURLConnection con = (HttpsURLConnection) reCaptchaUrl.openConnection();
			con.setRequestMethod("POST"); //$NON-NLS-1$
			con.setRequestProperty("User-Agent", USER_AGENT); //$NON-NLS-1$
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5"); //$NON-NLS-1$ //$NON-NLS-2$

			String postParams = "secret=" + secret + "&response=" //$NON-NLS-1$ //$NON-NLS-2$
					+ reCaptchaResponse;

			// Send post request
			con.setDoOutput(true);
			try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
				wr.writeBytes(postParams);
				wr.flush();
			}

			int responseCode = con.getResponseCode();
			if (responseCode > 399) {
				// Some type of error
				logger.error("Error response code returned from ReCaptcha post: "+responseCode); //$NON-NLS-1$
				throw new ReCaptchaException(I18N.getMessage("ReCaptcha.11", locale)); //$NON-NLS-1$
			}
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(
					con.getInputStream()))) {
				String inputLine;
				StringBuffer sb = new StringBuffer();
				while ((inputLine = reader.readLine()) != null) {
					sb.append(inputLine);
				}
				ReCaptchaResponse response = decodeResponse(sb.toString());
				return response.isSuccess();
			}
		} catch (IOException ex) {
			logger.error("I/O error accessing the ReCaptcha service",ex); //$NON-NLS-1$
			throw new ReCaptchaException(I18N.getMessage("ReCaptcha.13", locale)); //$NON-NLS-1$
		} catch (Exception ex) {
			logger.error("Unexpected error accessing the ReCaptcha service",ex); //$NON-NLS-1$
			throw new ReCaptchaException(I18N.getMessage("ReCaptcha.15", locale)); //$NON-NLS-1$
		}
	}

	private synchronized static ReCaptchaResponse decodeResponse(String stringResponse) {
		return gson.fromJson(stringResponse, ReCaptchaResponse.class);
	}

}
