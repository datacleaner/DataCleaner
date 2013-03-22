/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.configuration;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.descriptors.PlaceholderComponentJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.DataSetResult;
import org.eobjects.analyzer.result.ListResult;
import org.eobjects.analyzer.result.NumberResult;
import org.eobjects.analyzer.result.SimpleAnalysisResult;
import org.eobjects.analyzer.util.ChangeAwareObjectInputStream;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.util.FileHelper;
import org.eobjects.metamodel.util.Func;

/**
 * Default implementation of the {@link ResultContext} interface.
 */
public class DefaultResultContext implements ResultContext {

    private final TenantContext _tenantContext;
    private final RepositoryFile _repositoryFile;

    public DefaultResultContext(final TenantContext tenantContext, final RepositoryFile resultFile) {
        _tenantContext = tenantContext;
        _repositoryFile = resultFile;
    }

    @Override
    public AnalysisResult getAnalysisResult() throws IllegalStateException {
        final Object deserializedObject = _repositoryFile.readFile(new Func<InputStream, Object>() {
            @Override
            public Object eval(InputStream in) {
                ChangeAwareObjectInputStream inputStream = null;
                try {
                    inputStream = new ChangeAwareObjectInputStream(in);
                    return inputStream.readObject();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                } finally {
                    FileHelper.safeClose(inputStream);
                }
            }
        });
        final AnalysisResult analysisResult = toAnalysisResult(deserializedObject);
        return analysisResult;
    }

    private AnalysisResult toAnalysisResult(Object deserializedObject) {
        if (deserializedObject instanceof AnalysisResult) {
            // this is the most common case
            return (AnalysisResult) deserializedObject;
        }

        // we allow custom jobs to serialize an AnalyzerResult directly, in
        // which case we'll wrap it in a AnalysisResult
        if (deserializedObject instanceof AnalyzerResult) {
            final AnalyzerResult analyzerResult = (AnalyzerResult) deserializedObject;
            final Date creationDate = new Date(_repositoryFile.getLastModified());
            final Map<ComponentJob, AnalyzerResult> results = new HashMap<ComponentJob, AnalyzerResult>(1);
            final JobContext job = getJob();
            
            @SuppressWarnings({ "rawtypes", "unchecked" })
            final ComponentJob componentJob = new PlaceholderComponentJob(job.getName(), analyzerResult.getClass(), analyzerResult);
            results.put(componentJob, analyzerResult);
            return new SimpleAnalysisResult(results, creationDate);
        }
        
        if (deserializedObject instanceof DataSet) {
            DataSetResult dataSetResult = new DataSetResult((DataSet)deserializedObject);
            return toAnalysisResult(dataSetResult);
        }

        if (deserializedObject instanceof List) {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            ListResult<?> listResult = new ListResult((List<?>) deserializedObject);
            return toAnalysisResult(listResult);
        }
        
        if (deserializedObject instanceof Number) {
            NumberResult numberResult = new NumberResult((Number) deserializedObject);
            return toAnalysisResult(numberResult);
        }

        throw new UnsupportedOperationException("No handling logic for result: " + deserializedObject);
    }

    @Override
    public RepositoryFile getResultFile() {
        return _repositoryFile;
    }

    @Override
    public JobContext getJob() {
        final String resultFilename = _repositoryFile.getName();
        // we assume a filename pattern like this:
        // {job}-{timestamp}.analysis.result.dat
        final int lastIndexOfDash = resultFilename.lastIndexOf('-');
        assert lastIndexOfDash != -1;
        final String jobName = resultFilename.substring(0, lastIndexOfDash);
        return _tenantContext.getJob(jobName);
    }

}
