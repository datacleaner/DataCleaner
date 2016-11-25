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

import org.datacleaner.repository.Repository;
import org.datacleaner.result.AnalysisResult;
import org.springframework.context.ApplicationEvent;

/**
 * Event raised when an {@link AnalysisResult} stored in the {@link Repository}
 * has been modified.
 */
public class ResultModificationEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;
    private final String _tenant;
    private final String _newFilename;
    private final String _newJobName;
    private final long _newTimestamp;
    private final String _oldFilename;

    public ResultModificationEvent(final Object source, final String tenant, final String oldFilename,
            final String newFilename, final String newJobName, final long newTimestamp) {
        super(source);
        _tenant = tenant;
        _oldFilename = oldFilename;
        _newFilename = newFilename;
        _newJobName = newJobName;
        _newTimestamp = newTimestamp;
    }

    public String getNewFilename() {
        return _newFilename;
    }

    public String getNewJobName() {
        return _newJobName;
    }

    public long getNewTimestamp() {
        return _newTimestamp;
    }

    public String getOldFilename() {
        return _oldFilename;
    }

    public String getTenant() {
        return _tenant;
    }
}
