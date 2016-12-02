/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.beans.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaScriptLogger {
    private static final Logger logger = LoggerFactory.getLogger(JavaScriptLogger.class);

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public void trace(final String format, final Object... argv) {
        logger.trace(format, argv);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void debug(final String format, final Object... argv) {
        logger.debug(format, argv);
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public void info(final String format, final Object... argv) {
        logger.info(format, argv);
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public void warn(final String format, final Object... argv) {
        logger.warn(format, argv);
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public void error(final String format, final Object... argv) {
        logger.error(format, argv);
    }
}
