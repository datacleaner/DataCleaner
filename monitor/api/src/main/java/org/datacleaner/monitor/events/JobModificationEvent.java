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
package org.datacleaner.monitor.events;

import org.datacleaner.job.AnalysisJob;
import org.datacleaner.repository.Repository;
import org.springframework.context.ApplicationEvent;

/**
 * Event raised when an {@link AnalysisJob} stored in the {@link Repository} is
 * modified.
 */
public class JobModificationEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final String _tenant;
    private final String _newJobName;
    private final String _oldJobName;

    public JobModificationEvent(Object source, String tenant, String oldJobName, String newJobName) {
        super(source);
        _tenant = tenant;
        _oldJobName = oldJobName;
        _newJobName = newJobName;
    }

    public String getNewJobName() {
        return _newJobName;
    }

    public String getOldJobName() {
        return _oldJobName;
    }

    public String getTenant() {
        return _tenant;
    }
}
