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

public abstract class AbstractWaitForCompleteFileStrategy implements WaitForCompleteFileStrategy {
    private static final int WAIT_INTERVAL_MS = 1000;
    private static final int WAIT_TIMEOUT_MS = 30 * 60 * WAIT_INTERVAL_MS;

    @Override
    public void waitForComplete(final File file) throws IncompleteFileException {
        long now = System.currentTimeMillis();
        final long timeoutReached = now + WAIT_TIMEOUT_MS;

        while (now < timeoutReached && !isReady(file)) {
            try {
                Thread.sleep(WAIT_INTERVAL_MS);
                now = System.currentTimeMillis();
            } catch (InterruptedException e) {
                throw new IncompleteFileException("Waiting for a complete file was interrupted. " + e.toString());
            }
        }

        if (now >= timeoutReached) {
            throw new IncompleteFileException("Timeout of waiting for a complete file expired. ");
        }
    }

    public abstract boolean isReady(File file);
}
