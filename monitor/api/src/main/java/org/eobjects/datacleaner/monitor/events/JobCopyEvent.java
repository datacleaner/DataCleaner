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
package org.eobjects.datacleaner.monitor.events;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.springframework.context.ApplicationEvent;

/**
 * Event raised when an {@link AnalysisJob} has been copied in the repository.
 */
public class JobCopyEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final JobContext _sourceJob;
    private final JobContext _targetJob;
    private final String _tenant;

    public JobCopyEvent(Object source, String tenant, JobContext sourceJob, JobContext targetJob) {
        super(source);
        _tenant = tenant;
        _sourceJob = sourceJob;
        _targetJob = targetJob;
    }

    public String getTenant() {
        return _tenant;
    }

    public JobContext getTargetJob() {
        return _targetJob;
    }

    public JobContext getSourceJob() {
        return _sourceJob;
    }

}
