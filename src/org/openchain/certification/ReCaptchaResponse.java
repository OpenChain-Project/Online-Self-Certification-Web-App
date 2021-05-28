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

import com.google.gson.annotations.SerializedName;

/**
 * @author gary
 *
 */
public class ReCaptchaResponse {
	
	boolean success;
	String challenge_ts;
	String hostname;
	@SerializedName("error-codes")
	String[] error_codes;

	/**
	 * Construct an empty reCaptcha
	 */
	public ReCaptchaResponse() {
		// required empty constructor
	}

	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * @param success the success to set
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * @return the challenge_ts
	 */
	public String getChallenge_ts() {
		return challenge_ts;
	}

	/**
	 * @param challenge_ts the challenge_ts to set
	 */
	public void setChallenge_ts(String challenge_ts) {
		this.challenge_ts = challenge_ts;
	}

	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @param hostname the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * @return the error_codes
	 */
	public String[] getError_codes() {
		return error_codes;
	}

	/**
	 * @param error_codes the error_codes to set
	 */
	public void setError_codes(String[] error_codes) {
		this.error_codes = error_codes;
	}
	

}
