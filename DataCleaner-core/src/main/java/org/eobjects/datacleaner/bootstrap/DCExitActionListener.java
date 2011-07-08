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
package org.eobjects.datacleaner.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DCExitActionListener implements ExitActionListener {

	private static final Logger logger = LoggerFactory.getLogger(DCExitActionListener.class);

	@Override
	public void exit(final int statusCode) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					// sleep for 5 seconds, to give the non-daemon threads a
					// chance to shut down the applications properly
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
				logger.warn("Invoking system.exit({})", statusCode);
				System.exit(statusCode);
			}
		};
		thread.setDaemon(true);

		logger.info("Scheduling shutdown thread");
		thread.start();
	}
}
