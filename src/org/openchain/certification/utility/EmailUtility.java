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
package org.openchain.certification.utility;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;
import org.openchain.certification.dbdao.UserDb;
import org.openchain.certification.model.User;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

/**
 * Utility for managing emails including the verification emails and the 
 * password reset email.
 * 
 * This class uses the Amazon SES email services.
 * @author Gary O'Neall
 *
 */
public class EmailUtility {
	static final Logger logger = Logger.getLogger(EmailUtility.class);

	static final int HOURS_FOR_VERIFICATION_EMAIL_EXPIRATION = 24;
	/**
	 * @return a Date when the verification email is set to expire
	 */
	public static Date generateVerificationExpirationDate() {
		Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(cal.getTime().getTime()));
        cal.add(Calendar.HOUR, HOURS_FOR_VERIFICATION_EMAIL_EXPIRATION);
		return new Date(cal.getTime().getTime());
	}
	
	public static void emailVerification(String name, String email, UUID uuid, 
			String username, String responseServletUrl, ServletConfig config) throws EmailUtilException {
		String regionName = config.getServletContext().getInitParameter("email_ses_region");
		if (regionName == null || regionName.isEmpty()) {
			logger.error("Missing email_ses_region parameter in the web.xml file");
			throw(new EmailUtilException("The region name for the email facility has not been set.  Pleaese contact the OpenChain team with this error."));
		}
		String fromEmail = config.getServletContext().getInitParameter("return_email");
		if (fromEmail == null || fromEmail.isEmpty()) {
			logger.error("Missing return_email parameter in the web.xml file");
			throw(new EmailUtilException("The from email for the email facility has not been set.  Pleaese contact the OpenChain team with this error."));
		}
		String link = responseServletUrl + "?request=register&username=" + username + "&uuid=" + uuid.toString();
		StringBuilder msg = new StringBuilder("<div>Welcome ");
		msg.append(name);
		msg.append(" to the OpenChain Certification website.<br /> <br />To complete your registration, click on the following or copy/paste into your web browser <a href=\"");
		msg.append(link);
		msg.append("\">");
		msg.append(link);
		msg.append("</a><br/><br/>Thanks,<br/>The OpenChain team</div>");
		Destination destination = new Destination().withToAddresses(new String[]{email});
		Content subject = new Content().withData("OpenChain Registration [do not reply]");
		Content bodyData = new Content().withData(msg.toString());
		Body body = new Body();
		body.setHtml(bodyData);
		Message message = new Message().withSubject(subject).withBody(body);
		SendEmailRequest request = new SendEmailRequest().withSource(fromEmail).withDestination(destination).withMessage(message);
		try {
			AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient();
			Region region = Region.getRegion(Regions.fromName(regionName));
			client.setRegion(region);
			client.sendEmail(request);
			logger.info("Invitation email sent to "+email);
		} catch (Exception ex) {
			logger.error("Email send failed",ex);
			throw(new EmailUtilException("Exception occured during the emailing of the invitation",ex));
		}
	}

	public static void completeEmailVerification(String username, String uuid, ServletConfig config) throws EmailUtilException  {
		try {
			User user = UserDb.getUserDb(config).getUser(username);
			if (user == null) {
				logger.error("NO user found for completing email verification - username "+username);
				throw new EmailUtilException("User "+username+" not found.  Could not complete registration.");
			}
			if (user.isVerified()) {
				logger.warn("Attempting to verify an already verified user");
				return;
			}
			//TODO: Verify the date - once we have the resend invitation functionality implemented
			if (!PasswordUtil.validate(uuid.toString(), user.getUuid())) {
				logger.error("Verification tokens do not match for user "+username+".  Supplied = "+uuid+", expected = "+user.getUuid());
				throw(new EmailUtilException("Verification failed.  Invalid registration ID.  Please retry."));
			}
			UserDb.getUserDb(config).setVerified(username, true);
		} catch (SQLException e) {
			logger.error("Unexpected SQL exception completing the email verification",e);
			throw(new EmailUtilException("Unexpected SQL exception completing verification.  Please report this error to the OpenChain group"));
		} catch (NoSuchAlgorithmException e) {
			logger.error("Unexpected No Such Algorithm Exception completing the email verification",e);
			throw(new EmailUtilException("Unexpected No Such Algorithm exception completing verification.  Please report this error to the OpenChain group"));
		} catch (InvalidKeySpecException e) {
			logger.error("Unexpected Invalid Key Exception completing the email verification",e);
			throw(new EmailUtilException("Unexpected Invalid Key exception completing verification.  Please report this error to the OpenChain group"));
		}
	}

	public static void emailCompleteSubmission(String username, String name, String email,
			String specVersion, ServletConfig config) throws EmailUtilException {
		String regionName = config.getServletContext().getInitParameter("email_ses_region");
		if (regionName == null || regionName.isEmpty()) {
			logger.error("Missing email_ses_region parameter in the web.xml file");
			throw(new EmailUtilException("The region name for the email facility has not been set.  Pleaese contact the OpenChain team with this error."));
		}
		String fromEmail = config.getServletContext().getInitParameter("return_email");
		if (fromEmail == null || fromEmail.isEmpty()) {
			logger.error("Missing return_email parameter in the web.xml file");
			throw(new EmailUtilException("The from email for the email facility has not been set.  Pleaese contact the OpenChain team with this error."));
		}
		String toEmail = config.getServletContext().getInitParameter("notification_email");
		if (toEmail == null || toEmail.isEmpty()) {
			logger.error("Missing notification_email parameter in the web.xml file");
			throw(new EmailUtilException("The to email for the email facility has not been set.  Pleaese contact the OpenChain team with this error."));
		}
		StringBuilder msg = new StringBuilder("<div>User ");
		msg.append(name);
		msg.append(" with username ");
		msg.append(username);
		msg.append(" and email ");
		msg.append(email);
		msg.append(" has just submitted a cerification request.");
		Destination destination = new Destination().withToAddresses(new String[]{toEmail});
		Content subject = new Content().withData("Notification - new OpenChain submission [do not reply]");
		Content bodyData = new Content().withData(msg.toString());
		Body body = new Body();
		body.setHtml(bodyData);
		Message message = new Message().withSubject(subject).withBody(body);
		SendEmailRequest request = new SendEmailRequest().withSource(fromEmail).withDestination(destination).withMessage(message);
		try {
			AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient();
			Region region = Region.getRegion(Regions.fromName(regionName));
			client.setRegion(region);
			client.sendEmail(request);
			logger.info("Notification email sent for "+email);
		} catch (Exception ex) {
			logger.error("Email send failed",ex);
			throw(new EmailUtilException("Exception occured during the emailing of the submission notification",ex));
		}
	}
	
}
