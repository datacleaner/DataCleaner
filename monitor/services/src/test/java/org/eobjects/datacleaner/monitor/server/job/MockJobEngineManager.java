/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.server.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eobjects.datacleaner.monitor.job.JobEngine;

/**
 * Simple job engine manager that just holds a single engine (DataCleaner's
 * engine)
 */
public class MockJobEngineManager extends JobEngineManagerImpl {

    private final DataCleanerAnalysisJobEngine _jobEngine;

    public MockJobEngineManager() {
        super(null);
        _jobEngine = new DataCleanerAnalysisJobEngine(null);
    }

    @Override
    public Collection<JobEngine<?>> getJobEngines() {
        final List<JobEngine<?>> list = new ArrayList<JobEngine<?>>(1);
        list.add(_jobEngine);
        return list;
    }

}
