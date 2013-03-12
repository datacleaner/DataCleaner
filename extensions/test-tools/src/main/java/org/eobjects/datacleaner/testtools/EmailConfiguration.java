/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import java.io.Serializable;

import org.eobjects.metamodel.util.HasName;

public class EmailConfiguration implements Serializable, HasName {

	private static final long serialVersionUID = 1L;

	private final String name;
	private final boolean sendEmailOnSuccess;
	private final String from;
	private final String to;
	private final boolean tlsEnabled;
	private final String smtpHostname;
	private final int smtpPort;
	private final String smtpUsername;
	private final String smtpPassword;
	private final boolean sslEnabled;

	public EmailConfiguration(String name, boolean sendEmailOnSuccess,
			String from, String to, boolean tlsEnabled, boolean sslEnabled,
			String smtpHostname, int smtpPort, String smtpUsername,
			String smtpPassword) {
		this.name = name;
		this.sendEmailOnSuccess = sendEmailOnSuccess;
		this.from = from;
		this.to = to;
		this.tlsEnabled = tlsEnabled;
		this.sslEnabled = sslEnabled;
		this.smtpHostname = smtpHostname;
		this.smtpPort = smtpPort;
		this.smtpUsername = smtpUsername;
		this.smtpPassword = smtpPassword;
	}

	public boolean isSslEnabled() {
		return sslEnabled;
	}

	@Override
	public String getName() {
		return name;
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
