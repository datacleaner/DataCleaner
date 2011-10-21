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

public class EmailConfiguration {

	private final boolean sendEmailOnSuccess;
	private final String from;
	private final String to;
	private final boolean tlsEnabled;
	private final String smtpHostname;
	private final int smtpPort;
	private final String smtpUsername;
	private final String smtpPassword;

	public EmailConfiguration(boolean sendEmailOnSuccess, String from, String to, boolean tlsEnabled, String smtpHostname, int smtpPort,
			String smtpUsername, String smtpPassword) {
		this.sendEmailOnSuccess = sendEmailOnSuccess;
		this.from = from;
		this.to = to;
		this.tlsEnabled = tlsEnabled;
		this.smtpHostname = smtpHostname;
		this.smtpPort = smtpPort;
		this.smtpUsername = smtpUsername;
		this.smtpPassword = smtpPassword;
	}
	
	public boolean isSendEmailOnSuccess() {
		return sendEmailOnSuccess;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}
	
	public boolean isTlsEnabled() {
		return tlsEnabled;
	}

	public String getSmtpHostname() {
		return smtpHostname;
	}

	public String getSmtpUsername() {
		return smtpUsername;
	}

	public String getSmtpPassword() {
		return smtpPassword;
	}

	public int getSmtpPort() {
		return smtpPort;
	}
}
