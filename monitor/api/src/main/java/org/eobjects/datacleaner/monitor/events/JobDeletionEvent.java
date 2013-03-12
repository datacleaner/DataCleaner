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
package org.eobjects.datacleaner.monitor.events;

import org.eobjects.analyzer.job.AnalysisJob;
import org.springframework.context.ApplicationEvent;

/**
 * Event raised when an {@link AnalysisJob} has been deleted in the repository.
 */
public class JobDeletionEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String _tenant;
    private final String _jobName;

    public JobDeletionEvent(Object source, String tenant, String jobName) {
        super(source);
        _tenant = tenant;
        _jobName = jobName;
    }

    public String getTenant() {
        return _tenant;
    }

    public String getJobName() {
        return _jobName;
    }

}
