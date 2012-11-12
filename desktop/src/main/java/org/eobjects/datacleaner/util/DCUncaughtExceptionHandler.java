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
package org.eobjects.datacleaner.util;

import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DCUncaughtExceptionHandler implements UncaughtExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(DCUncaughtExceptionHandler.class);

	@Override
	public void uncaughtException(Thread t, final Throwable e) {
		logger.error("Thread " + t.getName() + " threw uncaught exception", e);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				WidgetUtils.showErrorMessage("Unexpected error!", e);
			}
		});
	}

}
