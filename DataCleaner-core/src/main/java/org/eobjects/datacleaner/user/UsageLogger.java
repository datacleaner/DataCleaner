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
package org.eobjects.datacleaner.user;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.eobjects.datacleaner.Main;
import org.eobjects.datacleaner.util.HttpXmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that handles remote logging of usage data
 * 
 * @author Kasper Sørensen
 */
public final class UsageLogger {

	private static final Logger logger = LoggerFactory.getLogger(UsageLogger.class);

	private static final UsageLogger instance = new UsageLogger();

	private final ExecutorService _executorService;
	private final UserPreferences _userPreferences;

	public static UsageLogger getInstance() {
		return instance;
	}

	// prevent instantiation
	private UsageLogger() {
		_executorService = Executors.newSingleThreadExecutor();
		_userPreferences = UserPreferences.getInstance();
	}

	public void logApplicationStartup() {
		final String action = "Startup: " + Main.VERSION;
		final String username = getUsername();

		logger.debug("Logging '{}'", action);
		final Runnable runnable = new UsageLoggerRunnable(username, action);
		_executorService.submit(runnable);
	}

	private String getUsername() {
		if (_userPreferences.isLoggedIn()) {
			return _userPreferences.getUsername();
		} else {
			return "[not-logged-in]";
		}
	}

	public void logApplicationShutdown() {
		final String action = "Shutdown: " + Main.VERSION;
		final String username = getUsername();
		logger.debug("Logging '{}'", action);
		final Runnable runnable = new UsageLoggerRunnable(username, action);
		try {
			_executorService.submit(runnable).get();
		} catch (Exception e) {
			logger.warn("Exception occurred sending shutdown message", e);
		}
	}

	public void log(final String action) {
		if (!_userPreferences.isLoggedIn()) {
			logger.debug("Not logging '{}', because user is not logged in", action);
			return;
		}

		final String username = getUsername();
		logger.debug("Logging '{}'", action);
		final Runnable runnable = new UsageLoggerRunnable(username, action);
		_executorService.submit(runnable);
	}

	/**
	 * Runnable implementation that does the actual remote notification. This is
	 * executed in a separate thread to avoid waiting for the user.
	 * 
	 * @author Kasper Sørensen
	 */
	private static final class UsageLoggerRunnable implements Runnable {

		private final String _username;
		private final String _action;

		public UsageLoggerRunnable(final String username, final String action) {
			_username = username;
			_action = action;
		}

		@Override
		public void run() {
			try {
				final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				final HttpPost req = new HttpPost("http://datacleaner.eobjects.org/ws/user_action");
				nameValuePairs.add(new BasicNameValuePair("username", _username));
				nameValuePairs.add(new BasicNameValuePair("action", _action));
				req.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpXmlUtils.getHttpClient().execute(req);
			} catch (Exception e) {
				logger.warn("Could not dispatch usage log for action: {} ({})", _action, e.getMessage());
				logger.debug("Error occurred while dispatching usage log", e);
			}
		}
	}
}
