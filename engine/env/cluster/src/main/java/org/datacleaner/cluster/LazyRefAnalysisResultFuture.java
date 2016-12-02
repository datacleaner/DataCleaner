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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.metamodel.util.LazyRef;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.runner.AnalysisJobFailedException;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.JobStatus;
import org.datacleaner.result.AnalysisResult;

/**
 * An {@link AnalysisResultFuture} implementation which uses a {@link LazyRef}
 * for an {@link AnalysisResult} as the source of the result
 */
public class LazyRefAnalysisResultFuture implements AnalysisResultFuture {

    private final LazyRef<AnalysisResult> _resultRef;
    private final List<Throwable> _errors;

    public LazyRefAnalysisResultFuture(final LazyRef<AnalysisResult> resultRef, final List<Throwable> errors) {
        _resultRef = resultRef;
        _errors = errors;
    }

    @Override
    public boolean isErrornous() {
        return getStatus() == JobStatus.ERRORNOUS;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public Date getCreationDate() {
        final AnalysisResult analysisResult = _resultRef.get();
        if (analysisResult == null) {
            return null;
        }
        return analysisResult.getCreationDate();
    }

    @Override
    public boolean isDone() {
        return _resultRef.isFetched();
    }

    @Override
    public void await() {
        _resultRef.get();
    }

    @Override
    public void cancel() {
        throw new UnsupportedOperationException("Cancel not supported for slave jobs");
    }

    @Override
    public void await(final long timeout, final TimeUnit timeUnit) {
        final long offsetMillis = System.currentTimeMillis();

        final long millisToWait = timeUnit.convert(timeout, TimeUnit.MILLISECONDS);

        final long sleepInterval = (millisToWait % 1000 == 0 ? 1000 : 500);

        while (!isDone()) {
            try {
                Thread.sleep(sleepInterval);
            } catch (final InterruptedException e) {
                // do nothing
            }

            final long currentMillis = System.currentTimeMillis();
            final long duration = currentMillis - offsetMillis;
            if (duration >= millisToWait) {
                return;
            }
        }
    }

    @Override
    public boolean isSuccessful() {
        return getStatus() == JobStatus.SUCCESSFUL;
    }

    @Override
    public JobStatus getStatus() {
        if (!_resultRef.isFetched()) {
            return JobStatus.NOT_FINISHED;
        }

        if (_errors.isEmpty()) {
            return JobStatus.SUCCESSFUL;
        }

        return JobStatus.ERRORNOUS;
    }

    @Override
    public List<AnalyzerResult> getResults() throws AnalysisJobFailedException {
        final AnalysisResult analysisResult = _resultRef.get();
        if (analysisResult == null) {
            return null;
        }
        return analysisResult.getResults();
    }

    @Override
    public AnalyzerResult getResult(final ComponentJob componentJob) throws AnalysisJobFailedException {
        final AnalysisResult analysisResult = _resultRef.get();
        if (analysisResult == null) {
            return null;
        }
        return _resultRef.get().getResult(componentJob);
    }

    @Override
    public Map<ComponentJob, AnalyzerResult> getResultMap() throws AnalysisJobFailedException {
        final AnalysisResult analysisResult = _resultRef.get();
        if (analysisResult == null) {
            return null;
        }
        return _resultRef.get().getResultMap();
    }

    @Override
    public List<Throwable> getErrors() {
        return Collections.unmodifiableList(_errors);
    }

    @Override
    public <R extends AnalyzerResult> List<? extends R> getResults(final Class<R> resultClass) {
        return _resultRef.get().getResults(resultClass);
    }

}
