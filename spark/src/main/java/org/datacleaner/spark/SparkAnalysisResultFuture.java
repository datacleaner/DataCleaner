package org.datacleaner.spark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.runner.AnalysisJobFailedException;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.JobStatus;
import org.datacleaner.result.AbstractAnalysisResult;

import scala.Tuple2;

public class SparkAnalysisResultFuture extends AbstractAnalysisResult implements AnalysisResultFuture {

    private final Date _creationDate;
    private final List<Tuple2<String, AnalyzerResult>> _results;

    public SparkAnalysisResultFuture(List<Tuple2<String, AnalyzerResult>> results) {
        _creationDate = new Date();
        _results = results;
    }

    @Override
    public boolean isErrornous() {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public Date getCreationDate() {
        return _creationDate;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public void await(long timeout, TimeUnit timeUnit) {
    }

    @Override
    public void await() {
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public JobStatus getStatus() {
        return JobStatus.SUCCESSFUL;
    }

    @Override
    public List<AnalyzerResult> getResults() throws AnalysisJobFailedException {
        final List<AnalyzerResult> list = new ArrayList<>();
        for (Tuple2<String, AnalyzerResult> tuple : _results) {
            list.add(tuple._2);
        }
        return list;
    }

    @Override
    public Map<ComponentJob, AnalyzerResult> getResultMap() throws AnalysisJobFailedException {
        // TODO: Needs to be implemented, but probably "results" will change anyway
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Throwable> getErrors() {
        return Collections.emptyList();
    }
}
