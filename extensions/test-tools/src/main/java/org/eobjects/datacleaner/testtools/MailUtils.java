/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.testtools;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailUtils {

	public static void sendMail(final EmailConfiguration configuration,
			final String subject, final String body) {
		try {
			final Properties props = new Properties();
			props.setProperty("mail.smtp.host", configuration.getSmtpHostname());
			if (configuration.isTlsEnabled()) {
				props.setProperty("mail.smtp.starttls.enable", "true");
			}
			props.setProperty("mail.smtp.host", configuration.getSmtpHostname());
			final String port = configuration.getSmtpPort() + "";
			props.setProperty("mail.smtp.port", port);

			if (configuration.isSslEnabled()) {
				props.put("mail.smtp.socketFactory.port", port);
				props.put("mail.smtp.socketFactory.class",
						"javax.net.ssl.SSLSocketFactory");
			}

			final Session session;
			if (configuration.getSmtpUsername() != null) {
				props.setProperty("mail.smtp.auth", "true");
				session = Session.getDefaultInstance(props,
						new Authenticator() {
							@Override
							protected PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication(configuration
										.getSmtpUsername(), configuration
										.getSmtpPassword());
							}
						});
			} else {
				session = Session.getDefaultInstance(props);
			}
			session.setDebug(true);

			final MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(configuration.getFrom()));
			message.setRecipient(RecipientType.TO, new InternetAddress(
					configuration.getTo()));

			message.setSubject(subject);
			message.setContent(body, "text/plain");

			Transport.send(message);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new IllegalStateException("Failed to send email", e);
		}
	}
}
