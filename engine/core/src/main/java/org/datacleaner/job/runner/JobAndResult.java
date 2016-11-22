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
package org.datacleaner.job.runner;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.job.ComponentJob;

/**
 * Represents a reference to a component job (Typically AnalyzerJob) and it's
 * result
 */
public final class JobAndResult {

    private final ComponentJob _job;
    private final AnalyzerResult _result;

    public JobAndResult(final ComponentJob job, final AnalyzerResult result) {
        _job = job;
        _result = result;
    }

    public ComponentJob getJob() {
        return _job;
    }

    public AnalyzerResult getResult() {
        return _result;
    }
}
