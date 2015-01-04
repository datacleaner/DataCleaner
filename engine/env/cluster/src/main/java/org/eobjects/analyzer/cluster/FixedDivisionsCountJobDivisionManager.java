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
package org.eobjects.analyzer.cluster;

import org.eobjects.analyzer.job.AnalysisJob;

/**
 * A simple {@link JobDivisionManager} which builds divisions based on a
 * preferred fixed number of divisions. Typically this fixed number will be the
 * number of slave nodes in the cluster.
 */
public class FixedDivisionsCountJobDivisionManager implements JobDivisionManager {

    private final int _divisionCount;

    public FixedDivisionsCountJobDivisionManager(int divisionCount) {
        if (divisionCount <= 0) {
            throw new IllegalArgumentException("Division count must be a positive integer");
        }
        _divisionCount = divisionCount;
    }

    @Override
    public int calculateDivisionCount(AnalysisJob masterJob, int expectedRows) {
        return _divisionCount;
    }

}
