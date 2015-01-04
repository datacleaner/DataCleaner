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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.runner.AnalysisJobFailedException;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.JobStatus;
import org.eobjects.analyzer.result.AnalyzerResult;

/**
 * An analysis result which is errornous because of a single issue, typically
 * while initializing the job.
 */
public class FailedAnalysisResultFuture implements AnalysisResultFuture {

    private final Exception _error;

    public FailedAnalysisResultFuture(Exception error) {
        _error = error;
    }

    @Override
    public boolean isErrornous() {
        return true;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public Date getCreationDate() {
        return null;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public void await() {
    }

    @Override
    public void cancel() {
    }

    @Override
    public void await(long timeout, TimeUnit timeUnit) {
    }

    @Override
    public boolean isSuccessful() {
        return false;
    }

    @Override
    public JobStatus getStatus() {
        return JobStatus.ERRORNOUS;
    }

    @Override
    public List<AnalyzerResult> getResults() throws AnalysisJobFailedException {
        return Collections.emptyList();
    }

    @Override
    public AnalyzerResult getResult(ComponentJob componentJob) throws AnalysisJobFailedException {
        return null;
    }
    
    @Override
    public <R extends AnalyzerResult> List<? extends R> getResults(Class<R> resultClass) {
        return Collections.emptyList();
    }

    @Override
    public Map<ComponentJob, AnalyzerResult> getResultMap() throws AnalysisJobFailedException {
        return Collections.emptyMap();
    }

    @Override
    public List<Throwable> getErrors() {
        final List<Throwable> list = new ArrayList<Throwable>(1);
        list.add(_error);
        return list;
    }

}
