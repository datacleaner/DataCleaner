/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.concurrent.StatusAwareTaskListener;
import org.eobjects.analyzer.result.AbstractAnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AnalysisResultFutureImpl extends AbstractAnalysisResult implements AnalysisResultFuture {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisResultFutureImpl.class);

	private final Queue<JobAndResult> _resultQueue;
	private final ErrorAware _errorAware;
	private final StatusAwareTaskListener _jobTaskListener;
	private volatile boolean _done;

	public AnalysisResultFutureImpl(Queue<JobAndResult> resultQueue, StatusAwareTaskListener jobCompletionListener,
			ErrorAware errorAware) {
		_resultQueue = resultQueue;
		_jobTaskListener = jobCompletionListener;
		_errorAware = errorAware;
		_done = false;
	}

	@Override
	public boolean isDone() {
		if (!_done) {
			_done = _jobTaskListener.isDone();
		}
		return _done;
	}
	
	@Override
	public Date getCreationDate() {
		return _jobTaskListener.getCompletionTime();
	}

	@Override
	public void cancel() {
		if (!_done) {
			_jobTaskListener.onError(null, new AnalysisJobCancellation());
		}
	}

	@Override
	public void await(long timeout, TimeUnit timeUnit) {
		if (!isDone()) {
			try {
				logger.debug("_closeCompletionListener.await({},{})", timeout, timeUnit);
				_jobTaskListener.await(timeout, timeUnit);
			} catch (InterruptedException e) {
				logger.error("Unexpected error while retreiving results", e);
			}
		}
	}

	@Override
	public void await() {
		while (!isDone()) {
			try {
				logger.debug("_closeCompletionListener.await()");
				_jobTaskListener.await();
			} catch (Exception e) {
				logger.error("Unexpected error while retreiving results", e);
			}
		}
	}

	@Override
	public List<AnalyzerResult> getResults() throws IllegalStateException {
		await();
		if (isErrornous()) {
		    throw new AnalysisJobFailedException(getErrors());
		}
		ArrayList<JobAndResult> resultQueueCopy = new ArrayList<JobAndResult>(_resultQueue);
		ArrayList<AnalyzerResult> result = new ArrayList<AnalyzerResult>(resultQueueCopy.size());
		for (JobAndResult jobResult : resultQueueCopy) {
			result.add(jobResult.getResult());
		}
		return result;
	}

	@Override
	public AnalyzerResult getResult(ComponentJob componentJob) throws AnalysisJobFailedException {
		await();
		if (isErrornous()) {
			throw new AnalysisJobFailedException(getErrors());
		}
		ArrayList<JobAndResult> resultQueueCopy = new ArrayList<JobAndResult>(_resultQueue);
		for (JobAndResult jobResult : resultQueueCopy) {
			if (jobResult.getJob().equals(componentJob)) {
				return jobResult.getResult();
			}
		}
		return null;
	}

	@Override
	public Map<ComponentJob, AnalyzerResult> getResultMap() throws IllegalStateException {
		await();
		if (isErrornous()) {
		    throw new AnalysisJobFailedException(getErrors());
		}
		ArrayList<JobAndResult> resultQueueCopy = new ArrayList<JobAndResult>(_resultQueue);
		Map<ComponentJob, AnalyzerResult> result = new HashMap<ComponentJob, AnalyzerResult>();
		for (JobAndResult jobResult : resultQueueCopy) {
			ComponentJob job = jobResult.getJob();
			AnalyzerResult analyzerResult = jobResult.getResult();
			result.put(job, analyzerResult);
		}
		return result;
	}

	@Override
	public boolean isSuccessful() {
		await();
		return !_errorAware.isErrornous();
	}

	@Override
	public List<Throwable> getErrors() {
		return _errorAware.getErrors();
	}

	@Override
	public boolean isErrornous() {
		return !isSuccessful();
	}

	@Override
	public JobStatus getStatus() {
		if (isDone()) {
			if (isSuccessful()) {
				return JobStatus.SUCCESSFUL;
			}
			return JobStatus.ERRORNOUS;
		}
		if (!_errorAware.isErrornous()) {
			return JobStatus.NOT_FINISHED;
		}
		return JobStatus.ERRORNOUS;
	}

	@Override
	public boolean isCancelled() {
		return _errorAware.isCancelled();
	}
}
