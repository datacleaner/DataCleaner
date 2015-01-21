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
package org.datacleaner.cluster;

import org.datacleaner.job.AnalysisJob;

/**
 * A simple {@link JobDivisionManager} which builds divisions based on a
 * preferred chunk size. If for instance there's a chunk size of 1000 records,
 * and an incoming job has an expected row count of 30,000 records - then there
 * will be 30 divisions made.
 */
public class FixedChunkSizeJobDivisionManager implements JobDivisionManager {

    private final int _chunkSize;

    public FixedChunkSizeJobDivisionManager(int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be a positive integer");
        }
        _chunkSize = chunkSize;
    }

    @Override
    public int calculateDivisionCount(AnalysisJob masterJob, int expectedRows) {
        final int chunkCount = (int) Math.ceil((1.0d * expectedRows / _chunkSize));

        return chunkCount;
    }

}
