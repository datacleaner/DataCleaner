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
package org.datacleaner.job.builder;

import org.datacleaner.api.OutputDataStream;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.OutputDataStreamJob;

/**
 * Represents a lazily evaluated {@link OutputDataStreamJob} which is still
 * being built.
 */
public class LazyOutputDataStreamJob implements OutputDataStreamJob {

    private static final long serialVersionUID = 1L;

    private final OutputDataStream _outputDataStream;
    private final transient AnalysisJobBuilder _jobBuilder;

    public LazyOutputDataStreamJob(OutputDataStream outputDataStream, AnalysisJobBuilder jobBuilder) {
        _outputDataStream = outputDataStream;
        _jobBuilder = jobBuilder;

    }

    @Override
    public OutputDataStream getOutputDataStream() {
        return _outputDataStream;
    }

    @Override
    public AnalysisJob getJob() {
        return getJob(false);
    }

    public AnalysisJob getJob(boolean validate) {
        return _jobBuilder.toAnalysisJob(validate);
    }

}
