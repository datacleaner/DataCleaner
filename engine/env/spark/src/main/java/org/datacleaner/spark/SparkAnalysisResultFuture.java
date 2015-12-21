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
package org.datacleaner.spark;

import java.util.ArrayList;
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

import scala.Tuple2;

public class SparkAnalysisResultFuture extends AbstractAnalysisResult implements AnalysisResultFuture {

    private final Date _creationDate;
    private final List<Tuple2<String, AnalyzerResult>> _results;
    private final SparkJobContext _sparkJobContext;

    public SparkAnalysisResultFuture(List<Tuple2<String, AnalyzerResult>> results, SparkJobContext sparkJobContext) {
        _creationDate = new Date();
        _results = results;
        _sparkJobContext = sparkJobContext;
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
        final Map<ComponentJob, AnalyzerResult> resultMap = new HashMap<>();
        for (Tuple2<String, AnalyzerResult> tuple : _results) {
            final ComponentJob component = _sparkJobContext.getComponentByKey(tuple._1);
            final AnalyzerResult analyzerResult = tuple._2;
            if (analyzerResult != null) {
                resultMap.put(component, analyzerResult);
            }
        }
        return resultMap;
    }

    @Override
    public List<Throwable> getErrors() {
        return Collections.emptyList();
    }
}
