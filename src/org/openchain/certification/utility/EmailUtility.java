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
import org.openchain.certification.I18N;
import org.openchain.certification.model.User;

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
	private static final String ACCESS_KEY_VAR = "AWS_ACCESS_KEY_ID"; //$NON-NLS-1$
	private static final String SECRET_KEY_VAR = "AWS_SECRET_ACCESS_KEY"; //$NON-NLS-1$
	
	private static AmazonSimpleEmailServiceClient getEmailClient(ServletConfig config, String language) throws EmailUtilException {
		String regionName = config.getServletContext().getInitParameter("email_ses_region"); //$NON-NLS-1$
		if (regionName == null || regionName.isEmpty()) {
			logger.error("Missing email_ses_region parameter in the web.xml file"); //$NON-NLS-1$
			throw(new EmailUtilException(I18N.getMessage("EmailUtility.4",language))); //$NON-NLS-1$
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
			String username, String responseServletUrl, ServletConfig config, String language) throws EmailUtilException {
		String fromEmail = config.getServletContext().getInitParameter("return_email"); //$NON-NLS-1$
		if (fromEmail == null || fromEmail.isEmpty()) {
			logger.error("Missing return_email parameter in the web.xml file"); //$NON-NLS-1$
			throw(new EmailUtilException(I18N.getMessage("EmailUtility.7",language))); //$NON-NLS-1$
		}
		String link = responseServletUrl + "?request=register&username=" + username + "&uuid=" + uuid.toString(); //$NON-NLS-1$ //$NON-NLS-2$
		StringBuilder msg = new StringBuilder("<div>"); //$NON-NLS-1$
		msg.append(I18N.getMessage("EmailUtility.11",language)); //$NON-NLS-1$
		msg.append(" "); //$NON-NLS-1$
		msg.append(name);
		msg.append(" "); //$NON-NLS-1$
		msg.append(I18N.getMessage("EmailUtility.14",language)); //$NON-NLS-1$
		msg.append("<br /> <br />"); //$NON-NLS-1$
		msg.append(I18N.getMessage("EmailUtility.16",language)); //$NON-NLS-1$
		msg.append("<a href=\""); //$NON-NLS-1$
		msg.append(link);
		msg.append("\">"); //$NON-NLS-1$
		msg.append(link);
		msg.append("</a><br/><br/>"); //$NON-NLS-1$
		msg.append(I18N.getMessage("EmailUtility.20",language)); //$NON-NLS-1$
		msg.append("<br/>"); //$NON-NLS-1$
		msg.append(I18N.getMessage("EmailUtility.22",language)); //$NON-NLS-1$
		msg.append("</div>"); //$NON-NLS-1$
		Destination destination = new Destination().withToAddresses(new String[]{email});
		Content subject = new Content().withData(I18N.getMessage("EmailUtility.24",language)); //$NON-NLS-1$
		Content bodyData = new Content().withData(msg.toString());
		Body body = new Body();
		body.setHtml(bodyData);
		Message message = new Message().withSubject(subject).withBody(body);
		SendEmailRequest request = new SendEmailRequest().withSource(fromEmail).withDestination(destination).withMessage(message);
		try {
			AmazonSimpleEmailServiceClient client = getEmailClient(config, language);
			client.sendEmail(request);
			logger.info("Invitation email sent to "+email); //$NON-NLS-1$
		} catch (Exception ex) {
			logger.error("Email send failed",ex); //$NON-NLS-1$
			throw(new EmailUtilException(I18N.getMessage("EmailUtility.27",language),ex)); //$NON-NLS-1$
		}
	}

	@SuppressWarnings("unused")
	private static void logMailInfo(AmazonSimpleEmailServiceClient client, ServletConfig config) {
		logger.info("Email Service Name: "+client.getServiceName()); //$NON-NLS-1$
		List<String> identities = client.listIdentities().getIdentities();
		for (String identity:identities) {
			logger.info("Email identity: "+identity); //$NON-NLS-1$
		}
		List<String> verifiedEmails = client.listVerifiedEmailAddresses().getVerifiedEmailAddresses();
		for (String email:verifiedEmails) {
			logger.info("Email verified email address: "+email); //$NON-NLS-1$
		}
		GetSendQuotaResult sendQuota = client.getSendQuota();
		logger.info("Max 24 hour send="+sendQuota.getMax24HourSend()+", Max Send Rate="+ //$NON-NLS-1$ //$NON-NLS-2$
		sendQuota.getMaxSendRate() + ", Sent last 24 hours="+sendQuota.getSentLast24Hours()); //$NON-NLS-1$
	}
	
	public static void emailCompleteSubmission(String username, String name, String email,
			String specVersion, ServletConfig config, String language) throws EmailUtilException {
		StringBuilder adminMsg = new StringBuilder("<div>"); //$NON-NLS-1$
		adminMsg.append("User"); //$NON-NLS-1$
		adminMsg.append(" "); //$NON-NLS-1$
		adminMsg.append(name);
		adminMsg.append(" "); //$NON-NLS-1$
		adminMsg.append("with username"); //$NON-NLS-1$
		adminMsg.append(" "); //$NON-NLS-1$
		adminMsg.append(username);
		adminMsg.append(" "); //$NON-NLS-1$
		adminMsg.append("and email"); //$NON-NLS-1$
		adminMsg.append(" "); //$NON-NLS-1$
		adminMsg.append(email);
		adminMsg.append(" "); //$NON-NLS-1$
		adminMsg.append("has just submitted a cerification request."); //$NON-NLS-1$
		adminMsg.append("</div>"); //$NON-NLS-1$
		emailAdmin("Notification - new OpenChain submission [do not reply]", adminMsg.toString(), config); //$NON-NLS-1$
		
		StringBuilder userMsg = new StringBuilder("<div>"); //$NON-NLS-1$
		userMsg.append(I18N.getMessage("EmailUtility.48",language)); //$NON-NLS-1$
		userMsg.append(" "); //$NON-NLS-1$
		userMsg.append(name);
		userMsg.append(I18N.getMessage("EmailUtility.50",language)); //$NON-NLS-1$
		userMsg.append("</div>"); //$NON-NLS-1$
		emailUser(email, I18N.getMessage("EmailUtility.52",language), userMsg.toString(), config, language); //$NON-NLS-1$

		logger.info("Submittal notification email sent for "+email); //$NON-NLS-1$
	}
	
	public static void emailUser(String toEmail, String subjectText, String msg, 
			ServletConfig config, String language) throws EmailUtilException {
		String fromEmail = config.getServletContext().getInitParameter("return_email"); //$NON-NLS-1$
		if (fromEmail == null || fromEmail.isEmpty()) {
			logger.error("Missing return_email parameter in the web.xml file"); //$NON-NLS-1$
			throw(new EmailUtilException(I18N.getMessage("EmailUtility.56",language))); //$NON-NLS-1$
		}
		if (toEmail == null || toEmail.isEmpty()) {
			logger.error("Missing notification_email parameter in the web.xml file"); //$NON-NLS-1$
			throw(new EmailUtilException(I18N.getMessage("EmailUtility.58",language))); //$NON-NLS-1$
		}
		
		Destination destination = new Destination().withToAddresses(new String[]{toEmail});
		Content subject = new Content().withData(subjectText);
		Content bodyData = new Content().withData(msg.toString());
		Body body = new Body();
		body.setHtml(bodyData);
		Message message = new Message().withSubject(subject).withBody(body);
		SendEmailRequest request = new SendEmailRequest().withSource(fromEmail).withDestination(destination).withMessage(message);
		try {
			AmazonSimpleEmailServiceClient client = getEmailClient(config, language);
			client.sendEmail(request);
			logger.info("User email sent to "+toEmail+": "+msg); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception ex) {
			logger.error("Email send failed",ex); //$NON-NLS-1$
			throw(new EmailUtilException(I18N.getMessage("EmailUtility.62",language),ex)); //$NON-NLS-1$
		}
	}

	public static void emailAdmin(String subjectText, String msg, ServletConfig config) throws EmailUtilException {
		String fromEmail = config.getServletContext().getInitParameter("return_email"); //$NON-NLS-1$
		if (fromEmail == null || fromEmail.isEmpty()) {
			logger.error("Missing return_email parameter in the web.xml file"); //$NON-NLS-1$
			throw(new EmailUtilException("The from email for the email facility has not been set.  Pleaese contact the OpenChain team with this error.")); //$NON-NLS-1$
		}
		String toEmail = config.getServletContext().getInitParameter("notification_email"); //$NON-NLS-1$
		if (toEmail == null || toEmail.isEmpty()) {
			logger.error("Missing notification_email parameter in the web.xml file"); //$NON-NLS-1$
			throw(new EmailUtilException("The to email for the email facility has not been set.  Pleaese contact the OpenChain team with this error.")); //$NON-NLS-1$
		}
		
		Destination destination = new Destination().withToAddresses(new String[]{toEmail});
		Content subject = new Content().withData(subjectText);
		Content bodyData = new Content().withData(msg.toString());
		Body body = new Body();
		body.setHtml(bodyData);
		Message message = new Message().withSubject(subject).withBody(body);
		SendEmailRequest request = new SendEmailRequest().withSource(fromEmail).withDestination(destination).withMessage(message);
		try {
			AmazonSimpleEmailServiceClient client = getEmailClient(config, User.DEFAULT_LANGUAGE);
			client.sendEmail(request);
			logger.info("Admin email sent to "+toEmail+": "+msg); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception ex) {
			logger.error("Email send failed",ex); //$NON-NLS-1$
			throw(new EmailUtilException("Exception occured during the emailing of the submission notification",ex)); //$NON-NLS-1$
		}
	}

	/**
	 * Email to notify a user that their profiles was updated
	 * @param username
	 * @param email
	 * @param config
	 * @param language
	 * @throws EmailUtilException
	 */
	public static void emailProfileUpdate(String username, String email,
			ServletConfig config, String language) throws EmailUtilException {
		String fromEmail = config.getServletContext().getInitParameter("return_email"); //$NON-NLS-1$
		if (fromEmail == null || fromEmail.isEmpty()) {
			logger.error("Missing return_email parameter in the web.xml file"); //$NON-NLS-1$
			throw(new EmailUtilException(I18N.getMessage("EmailUtility.75",language))); //$NON-NLS-1$
		}
		StringBuilder msg = new StringBuilder("<div>"); //$NON-NLS-1$
		msg.append(I18N.getMessage("EmailUtility.77",language)); //$NON-NLS-1$
		msg.append(" "); //$NON-NLS-1$
		msg.append(username);
		msg.append(" "); //$NON-NLS-1$
		msg.append(I18N.getMessage("EmailUtility.80",language)); //$NON-NLS-1$
		Destination destination = new Destination().withToAddresses(new String[]{email});
		Content subject = new Content().withData(I18N.getMessage("EmailUtility.81",language)); //$NON-NLS-1$
		Content bodyData = new Content().withData(msg.toString());
		Body body = new Body();
		body.setHtml(bodyData);
		Message message = new Message().withSubject(subject).withBody(body);
		SendEmailRequest request = new SendEmailRequest().withSource(fromEmail).withDestination(destination).withMessage(message);
		try {
			AmazonSimpleEmailServiceClient client = getEmailClient(config, language);
			client.sendEmail(request);
			logger.info("Notification email sent for "+email); //$NON-NLS-1$
		} catch (Exception ex) {
			logger.error("Email send failed",ex); //$NON-NLS-1$
			throw(new EmailUtilException(I18N.getMessage("EmailUtility.84",language),ex)); //$NON-NLS-1$
		}
	}

	public static void emailPasswordReset(String name, String email, UUID uuid,
			String username, String responseServletUrl, ServletConfig config, String language) throws EmailUtilException {
		String fromEmail = config.getServletContext().getInitParameter("return_email"); //$NON-NLS-1$
		if (fromEmail == null || fromEmail.isEmpty()) {
			logger.error("Missing return_email parameter in the web.xml file"); //$NON-NLS-1$
			throw(new EmailUtilException(I18N.getMessage("EmailUtility.87",language))); //$NON-NLS-1$
		}
		String link = responseServletUrl + "?request=pwreset&username=" + username + "&uuid=" + uuid.toString(); //$NON-NLS-1$ //$NON-NLS-2$
		StringBuilder msg = new StringBuilder("<div>"); //$NON-NLS-1$
		msg.append(I18N.getMessage("EmailUtility.91",language)); //$NON-NLS-1$
		msg.append("<a href=\""); //$NON-NLS-1$
		msg.append(link);
		msg.append("\">"); //$NON-NLS-1$
		msg.append(link);
		msg.append("</a><br/><br/><br/>"); //$NON-NLS-1$
		msg.append(I18N.getMessage("EmailUtility.95",language)); //$NON-NLS-1$
		msg.append("</div>"); //$NON-NLS-1$
		Destination destination = new Destination().withToAddresses(new String[]{email});
		Content subject = new Content().withData(I18N.getMessage("EmailUtility.96",language)); //$NON-NLS-1$
		Content bodyData = new Content().withData(msg.toString());
		Body body = new Body();
		body.setHtml(bodyData);
		Message message = new Message().withSubject(subject).withBody(body);
		SendEmailRequest request = new SendEmailRequest().withSource(fromEmail).withDestination(destination).withMessage(message);
		try {
			AmazonSimpleEmailServiceClient client = getEmailClient(config, language);
			client.sendEmail(request);
			logger.info("Reset password email sent to "+email); //$NON-NLS-1$
		} catch (Exception ex) {
			logger.error("Email send failed",ex); //$NON-NLS-1$
			throw(new EmailUtilException(I18N.getMessage("EmailUtility.99",language),ex)); //$NON-NLS-1$
		}
	}

	public static void emailUnsubmit(String username, String name,
			String email, String specVersion, ServletConfig config) throws EmailUtilException {
		StringBuilder msg = new StringBuilder("<div>User "); //$NON-NLS-1$
		msg.append(name);
		msg.append(" with username "); //$NON-NLS-1$
		msg.append(username);
		msg.append(" and email "); //$NON-NLS-1$
		msg.append(email);
		msg.append(" has just UN submitted a certification request."); //$NON-NLS-1$
		emailAdmin("Notification - pulled OpenChain submission [do not reply]", msg.toString(), config); //$NON-NLS-1$
		logger.info("Notification email sent for "+email); //$NON-NLS-1$
	}
	
}
