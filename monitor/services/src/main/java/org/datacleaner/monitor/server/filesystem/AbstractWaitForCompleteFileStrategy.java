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
import java.io.FileInputStream;
import java.io.IOException;

public abstract class AbstractWaitForCompleteFileStrategy implements WaitForCompleteFileStrategy {
    protected static final int ATTEMPT_WAIT_INTERVAL_MS = 500;
    protected static final int WAIT_ATTEMPT_LIMIT = 20 * 60 * (2 * ATTEMPT_WAIT_INTERVAL_MS);

    @Override
    public void waitForComplete(final File file) throws IncompleteFileException {
        int attempts = 0;

        while (attempts < WAIT_ATTEMPT_LIMIT && !isReady(file)) {
            attempts++;

            try {
                Thread.sleep(ATTEMPT_WAIT_INTERVAL_MS);
            } catch (InterruptedException e) {
                throw new IncompleteFileException("Waiting for a complete file was interrupted. " + e.toString());
            }
        }

        if (attempts >= WAIT_ATTEMPT_LIMIT) {
            throw new IncompleteFileException("Timeout of waiting for a complete file expired. ");
        }
    }

    public abstract boolean isReady(File file);
}
