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
package org.datacleaner.monitor.server.filesystem;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link WaitForCompleteFileStrategy}.
 * It contains a decision tree. According to the environment it selects a proper strategy to use.
 */
public class DefaultWaitForCompleteFileStrategy implements WaitForCompleteFileStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DefaultWaitForCompleteFileStrategy.class);
    private final WaitForCompleteFileStrategy _waitStrategy;

    public DefaultWaitForCompleteFileStrategy() {
        final String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("windows")) {
            logger.info("Using WindowsWaitForCompleteFileStrategy. ");
            _waitStrategy = new WindowsWaitForCompleteFileStrategy();
        } else if (osName.contains("linux")) {
            logger.info("Using LinuxWaitForCompleteFileStrategy. ");
            _waitStrategy = new LinuxWaitForCompleteFileStrategy();
        } else {
            logger.info("Using GeneralWaitForCompleteFileStrategy. ");
            _waitStrategy = new GeneralWaitForCompleteFileStrategy();
        }

    }

    @Override
    public void waitForComplete(final File file) throws IncompleteFileException {
        _waitStrategy.waitForComplete(file);
    }

    @Override
    public boolean isReady(File file) {
        return _waitStrategy.isReady(file);
    }
}
