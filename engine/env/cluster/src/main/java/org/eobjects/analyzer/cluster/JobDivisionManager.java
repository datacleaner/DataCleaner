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
 * Component responsible for determining how many divisions/chunks to build from
 * a single master job. Each division will be represented by a slave job.
 * 
 * Too many divisions will cause too much distribution, i.e. units of work being
 * too small and time is wasted in distribution instead of execution.
 * 
 * Too few divisions will cause potential bottleneck situations because the
 * slowest execution nodes will be determining the total job execution time.
 */
public interface JobDivisionManager {

    public int calculateDivisionCount(AnalysisJob masterJob, int expectedRows);

}
