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
package org.datacleaner.monitor.server.hotfolder;

import java.io.File;

/**
 * An interface for a general approach to wait for a potentially incomplete file (that is e. g. being copied).
 */
public interface WaitForCompleteFileStrategy {
    /**
     * Waits for a given file to be completed.
     * @param file
     */
    void waitForComplete(File file) throws IncompleteFileException;

    /**
     * Returns true if the file is ready to be used, false otherwise.
     * @param file
     * @return
     */
    boolean isReady(File file);
}
