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

public class GeneralWaitForCompleteFileStrategy extends AbstractWaitForCompleteFileStrategy {
    private final long[] _lastSizes = new long[] { 0L, 0L, 0L };
    private int _index = 0;

    /**
     * Continuously checks the size of the file. If there is no change within the three last updates,
     * the file is considered complete.
     * @param file
     * @return
     */
    @Override
    public boolean isReady(final File file) {
        _lastSizes[_index] = file.getTotalSpace();
        _index = (_index + 1) % _lastSizes.length;

        return !isDifferenceInSizes();
    }

    private boolean isDifferenceInSizes() {
        final long size = _lastSizes[0];

        for (int i = 1; i < _lastSizes.length; i++) {
            if (_lastSizes[i] != size) {
                return true;
            }
        }

        return false;
    }
}
