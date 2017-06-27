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

import java.util.List;
import java.util.UUID;

import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.GetSendQuotaResult;
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
	private static final String ACCESS_KEY_VAR = "AWS_ACCESS_KEY_ID";
	private static final String SECRET_KEY_VAR = "AWS_SECRET_ACCESS_KEY";
	
	private static AmazonSimpleEmailServiceClient getEmailClient(ServletConfig config) throws EmailUtilException {
		String regionName = config.getServletContext().getInitParameter("email_ses_region");
		if (regionName == null || regionName.isEmpty()) {
			logger.error("Missing email_ses_region parameter in the web.xml file");
			throw(new EmailUtilException("The region name for the email facility has not been set.  Pleaese contact the OpenChain team with this error."));
		}
		String secretKey = null;
		String accessKey = System.getenv(ACCESS_KEY_VAR);
		if (accessKey == null) {
			accessKey = System.getProperty(ACCESS_KEY_VAR);
			if (accessKey != null) {
				secretKey = System.getProperty(SECRET_KEY_VAR);
			}
		} else {
			secretKey = System.getenv(SECRET_KEY_VAR);
		}
		AmazonSimpleEmailServiceClient retval = null;
		if (accessKey != null) {
			AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
			retval = new AmazonSimpleEmailServiceClient(credentials);
		} else {
			retval = new AmazonSimpleEmailServiceClient();
		}
		Region region = Region.getRegion(Regions.fromName(regionName));
		retval.setRegion(region);
		return retval;
	}
	
	public static void emailVerification(String name, String email, UUID uuid, 
			String username, String responseServletUrl, ServletConfig config) throws EmailUtilException {
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
			AmazonSimpleEmailServiceClient client = getEmailClient(config);
			client.sendEmail(request);
			logger.info("Invitation email sent to "+email);
		} catch (Exception ex) {
			logger.error("Email send failed",ex);
			throw(new EmailUtilException("Exception occured during the emailing of the invitation",ex));
		}
	}

	@SuppressWarnings("unused")
	private static void logMailInfo(AmazonSimpleEmailServiceClient client, ServletConfig config) {
		logger.info("Email Service Name: "+client.getServiceName());
		List<String> identities = client.listIdentities().getIdentities();
		for (String identity:identities) {
			logger.info("Email identity: "+identity);
		}
		List<String> verifiedEmails = client.listVerifiedEmailAddresses().getVerifiedEmailAddresses();
		for (String email:verifiedEmails) {
			logger.info("Email verified email address: "+email);
		}
		GetSendQuotaResult sendQuota = client.getSendQuota();
		logger.info("Max 24 hour send="+sendQuota.getMax24HourSend()+", Max Send Rate="+
		sendQuota.getMaxSendRate() + ", Sent last 24 hours="+sendQuota.getSentLast24Hours());
	}
	
	public static void emailCompleteSubmission(String username, String name, String email,
			String specVersion, ServletConfig config) throws EmailUtilException {
		StringBuilder adminMsg = new StringBuilder("<div>User ");
		adminMsg.append(name);
		adminMsg.append(" with username ");
		adminMsg.append(username);
		adminMsg.append(" and email ");
		adminMsg.append(email);
		adminMsg.append(" has just submitted a cerification request.</div>");
		emailAdmin("Notification - new OpenChain submission [do not reply]", adminMsg.toString(), config);
		
		StringBuilder userMsg = new StringBuilder("<div>Congratulations ");
		userMsg.append(name);
		userMsg.append(".  Your certification request has been accepted.  If you did not submit a request for OpenChain certification, please notify the OpenChain group at openchain-conformance@lists.linuxfoundation.org.</div>");
		emailUser(email, "OpenChain certification request has been accepted [do not reply]", userMsg.toString(), config);

		logger.info("Submittal notification email sent for "+email);
	}
	
	public static void emailUser(String toEmail, String subjectText, String msg, ServletConfig config) throws EmailUtilException {
		String fromEmail = config.getServletContext().getInitParameter("return_email");
		if (fromEmail == null || fromEmail.isEmpty()) {
			logger.error("Missing return_email parameter in the web.xml file");
			throw(new EmailUtilException("The from email for the email facility has not been set.  Pleaese contact the OpenChain team with this error."));
		}
		if (toEmail == null || toEmail.isEmpty()) {
			logger.error("Missing notification_email parameter in the web.xml file");
			throw(new EmailUtilException("The to email for the email facility has not been set.  Pleaese contact the OpenChain team with this error."));
		}
		
		Destination destination = new Destination().withToAddresses(new String[]{toEmail});
		Content subject = new Content().withData(subjectText);
		Content bodyData = new Content().withData(msg.toString());
		Body body = new Body();
		body.setHtml(bodyData);
		Message message = new Message().withSubject(subject).withBody(body);
		SendEmailRequest request = new SendEmailRequest().withSource(fromEmail).withDestination(destination).withMessage(message);
		try {
			AmazonSimpleEmailServiceClient client = getEmailClient(config);
			client.sendEmail(request);
			logger.info("User email sent to "+toEmail+": "+msg);
		} catch (Exception ex) {
			logger.error("Email send failed",ex);
			throw(new EmailUtilException("Exception occured during the emailing of a user email",ex));
		}
	}

	public static void emailAdmin(String subjectText, String msg, ServletConfig config) throws EmailUtilException {
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
		
		Destination destination = new Destination().withToAddresses(new String[]{toEmail});
		Content subject = new Content().withData(subjectText);
		Content bodyData = new Content().withData(msg.toString());
		Body body = new Body();
		body.setHtml(bodyData);
		Message message = new Message().withSubject(subject).withBody(body);
		SendEmailRequest request = new SendEmailRequest().withSource(fromEmail).withDestination(destination).withMessage(message);
		try {
			AmazonSimpleEmailServiceClient client = getEmailClient(config);
			client.sendEmail(request);
			logger.info("Admin email sent to "+toEmail+": "+msg);
		} catch (Exception ex) {
			logger.error("Email send failed",ex);
			throw(new EmailUtilException("Exception occured during the emailing of the submission notification",ex));
		}
	}

	/**
	 * Email to notify a user that their profiles was updated
	 * @param username
	 * @param email
	 * @param config
	 * @throws EmailUtilException
	 */
	public static void emailProfileUpdate(String username, String email,
			ServletConfig config) throws EmailUtilException {
		String fromEmail = config.getServletContext().getInitParameter("return_email");
		if (fromEmail == null || fromEmail.isEmpty()) {
			logger.error("Missing return_email parameter in the web.xml file");
			throw(new EmailUtilException("The from email for the email facility has not been set.  Pleaese contact the OpenChain team with this error."));
		}
		StringBuilder msg = new StringBuilder("<div>The profile for username ");

		msg.append(username);
		msg.append(" has been updated.  If you this update has been made in error, please contact the OpenChain certification team.");
		Destination destination = new Destination().withToAddresses(new String[]{email});
		Content subject = new Content().withData("OpenChain Certification profile updated [do not reply]");
		Content bodyData = new Content().withData(msg.toString());
		Body body = new Body();
		body.setHtml(bodyData);
		Message message = new Message().withSubject(subject).withBody(body);
		SendEmailRequest request = new SendEmailRequest().withSource(fromEmail).withDestination(destination).withMessage(message);
		try {
			AmazonSimpleEmailServiceClient client = getEmailClient(config);
			client.sendEmail(request);
			logger.info("Notification email sent for "+email);
		} catch (Exception ex) {
			logger.error("Email send failed",ex);
			throw(new EmailUtilException("Exception occured during the emailing of the submission notification",ex));
		}
	}

	public static void emailPasswordReset(String name, String email, UUID uuid,
			String username, String responseServletUrl, ServletConfig config) throws EmailUtilException {
		String fromEmail = config.getServletContext().getInitParameter("return_email");
		if (fromEmail == null || fromEmail.isEmpty()) {
			logger.error("Missing return_email parameter in the web.xml file");
			throw(new EmailUtilException("The from email for the email facility has not been set.  Pleaese contact the OpenChain team with this error."));
		}
		String link = responseServletUrl + "?request=pwreset&username=" + username + "&uuid=" + uuid.toString();
		StringBuilder msg = new StringBuilder("<div>To reset the your password, click on the following or copy/paste into your web browser <a href=\"");
		msg.append(link);
		msg.append("\">");
		msg.append(link);
		msg.append("</a><br/><br/><br/>The OpenChain team</div>");
		Destination destination = new Destination().withToAddresses(new String[]{email});
		Content subject = new Content().withData("OpenChain Password Reset [do not reply]");
		Content bodyData = new Content().withData(msg.toString());
		Body body = new Body();
		body.setHtml(bodyData);
		Message message = new Message().withSubject(subject).withBody(body);
		SendEmailRequest request = new SendEmailRequest().withSource(fromEmail).withDestination(destination).withMessage(message);
		try {
			AmazonSimpleEmailServiceClient client = getEmailClient(config);
			client.sendEmail(request);
			logger.info("Reset password email sent to "+email);
		} catch (Exception ex) {
			logger.error("Email send failed",ex);
			throw(new EmailUtilException("Exception occured during the emailing of the password reset",ex));
		}
	}

	public static void emailUnsubmit(String username, String name,
			String email, String specVersion, ServletConfig config) throws EmailUtilException {
		StringBuilder msg = new StringBuilder("<div>User ");
		msg.append(name);
		msg.append(" with username ");
		msg.append(username);
		msg.append(" and email ");
		msg.append(email);
		msg.append(" has just UN submitted a certification request.");
		emailAdmin("Notification - pulled OpenChain submission [do not reply]", msg.toString(), config);
		logger.info("Notification email sent for "+email);
	}
	
}
