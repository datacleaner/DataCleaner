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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.runner.AnalysisJobFailedException;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.JobStatus;
import org.datacleaner.result.AbstractAnalysisResult;

/**
 * {@link AnalysisResultFuture} implementation for clustered/distributed
 * set-ups.
 */
public final class DistributedAnalysisResultFuture extends AbstractAnalysisResult implements AnalysisResultFuture {

    private final DistributedAnalysisResultReducer _reducer;
    private final List<AnalysisResultFuture> _results;
    private final Map<ComponentJob, AnalyzerResult> _resultMap;
    private final List<AnalysisResultReductionException> _reductionErrors;
    private volatile Date _creationDate;
    private volatile boolean _cancelled;

    public DistributedAnalysisResultFuture(List<AnalysisResultFuture> results, DistributedAnalysisResultReducer reducer) {
        _results = results;
        _reducer = reducer;
        _resultMap = new HashMap<ComponentJob, AnalyzerResult>();
        _reductionErrors = new ArrayList<AnalysisResultReductionException>();
        _cancelled = false;
    }

    @Override
    public void cancel() {
        if (isDone()) {
            // too late to cancel
            return;
        }

        if (!_cancelled) {
            for (AnalysisResultFuture result : _results) {
                result.cancel();
            }
            _cancelled = true;
        }
    }

    @Override
    public boolean isCancelled() {
        return _cancelled;
    }

    @Override
    public Date getCreationDate() {
        if (!isDone()) {
            return null;
        }
        return _creationDate;
    }

    @Override
    public boolean isDone() {
        for (AnalysisResultFuture result : _results) {
            if (!result.isDone()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void await() {
        for (AnalysisResultFuture result : _results) {
            result.await();
        }
        if (_resultMap.isEmpty()) {
            synchronized (this) {
                if (_resultMap.isEmpty() && _reductionErrors.isEmpty()) {
                    _reducer.reduce(_results, _resultMap, _reductionErrors);
                }
            }
        }
    }

    @Override
    public void await(long timeout, TimeUnit timeUnit) {
        final long offsetMillis = System.currentTimeMillis();
        final long millisToWait = timeUnit.convert(timeout, TimeUnit.MILLISECONDS);
        for (AnalysisResultFuture result : _results) {
            if (!isDone()) {
                result.await(timeout, TimeUnit.MILLISECONDS);
                final long currentMillis = System.currentTimeMillis();
                final long duration = currentMillis - offsetMillis;
                if (duration >= millisToWait) {
                    return;
                }
            }
        }
    }

    @Override
    public boolean isErrornous() {
        return !isSuccessful();
    }

    @Override
    public boolean isSuccessful() {
        await();
        for (AnalysisResultFuture result : _results) {
            if (result.isErrornous()) {
                return false;
            }
        }
        return _reductionErrors.isEmpty();
    }

    @Override
    public JobStatus getStatus() {
        if (isCancelled()) {
            return JobStatus.ERRORNOUS;
        }
        for (AnalysisResultFuture result : _results) {
            JobStatus slaveStatus = result.getStatus();
            if (slaveStatus == JobStatus.NOT_FINISHED) {
                return JobStatus.NOT_FINISHED;
            }
        }

        if (isSuccessful()) {
            return JobStatus.SUCCESSFUL;
        }

        return JobStatus.ERRORNOUS;
    }

    @Override
    public List<AnalyzerResult> getResults() throws AnalysisJobFailedException {
        await();
        if (isErrornous()) {
            throw new AnalysisJobFailedException(getErrors());
        }

        final Collection<AnalyzerResult> values = _resultMap.values();
        return new ArrayList<AnalyzerResult>(values);
    }

    @Override
    public Map<ComponentJob, AnalyzerResult> getResultMap() throws AnalysisJobFailedException {
        await();
        if (isErrornous()) {
            throw new AnalysisJobFailedException(getErrors());
        }

        return Collections.unmodifiableMap(_resultMap);
    }

    @Override
    public List<Throwable> getErrors() {
        final List<Throwable> errors = new ArrayList<Throwable>();
        for (AnalysisResultFuture result : _results) {
            final List<Throwable> slaveErrors = result.getErrors();
            if (slaveErrors != null) {
                errors.addAll(slaveErrors);
            }
        }
        errors.addAll(_reductionErrors);
        return errors;
    }

}
