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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.AnalyzerJobHelper;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.RowProcessingPublisher;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.AnalyzerResultReducer;
import org.eobjects.analyzer.util.CollectionUtils2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to perform the reduction phase of {@link AnalyzerResult}s
 * collected in a {@link DistributedAnalysisResultFuture}.
 */
final class DistributedAnalysisResultReducer {

    private static final Logger logger = LoggerFactory.getLogger(DistributedAnalysisResultReducer.class);

    private final AnalysisJob _masterJob;
    private final LifeCycleHelper _lifeCycleHelper;
    private final RowProcessingPublisher _publisher;
    private final AnalysisListener _analysisListener;
    private final AtomicBoolean _hasRun;

    public DistributedAnalysisResultReducer(AnalysisJob masterJob, LifeCycleHelper lifeCycleHelper,
            RowProcessingPublisher publisher, AnalysisListener analysisListener) {
        _masterJob = masterJob;
        _lifeCycleHelper = lifeCycleHelper;
        _publisher = publisher;
        _analysisListener = analysisListener;
        _hasRun = new AtomicBoolean(false);
    }

    public void reduce(final List<AnalysisResultFuture> results, final Map<ComponentJob, AnalyzerResult> resultMap,
            final List<AnalysisResultReductionException> reductionErrors) {
        final int size = results.size();
        try {
            logger.debug("Starting reduce phase of {} results", size);
            reduceResults(results, resultMap, reductionErrors);
            logger.debug("Finished reduce phase of {} results", size);
        } finally {
            closeNonDistributableComponents();
        }
    }

    private void closeNonDistributableComponents() {
        _publisher.closeConsumers();
    }

    /**
     * Reduces all the analyzer results of an analysis
     * 
     * @param results
     * @param resultMap
     * @param reductionErrors
     */
    private void reduceResults(final List<AnalysisResultFuture> results,
            final Map<ComponentJob, AnalyzerResult> resultMap,
            final List<AnalysisResultReductionException> reductionErrors) {

        if (_hasRun.get()) {
            // already reduced
            return;
        }
        
        _hasRun.set(true);

        for (AnalysisResultFuture result : results) {
            if (result.isErrornous()) {
                logger.error("Encountered errorneous slave result. Result reduction will stop. Result={}", result);
                final List<Throwable> errors = result.getErrors();
                if (!errors.isEmpty()) {
                    final Throwable firstError = errors.get(0);
                    logger.error(
                            "Encountered error before reducing results (showing stack trace of invoking the reducer): "
                                    + firstError.getMessage(), new Throwable());
                    _analysisListener.errorUknown(_masterJob, firstError);
                }

                // error occurred!
                return;
            }
        }

        final Collection<AnalyzerJob> analyzerJobs = _masterJob.getAnalyzerJobs();
        for (AnalyzerJob masterAnalyzerJob : analyzerJobs) {
            final Collection<AnalyzerResult> slaveResults = new ArrayList<AnalyzerResult>();
            logger.info("Reducing {} slave results for component: {}", results.size(), masterAnalyzerJob);
            for (AnalysisResultFuture result : results) {

                final Map<ComponentJob, AnalyzerResult> slaveResultMap = result.getResultMap();
                final List<AnalyzerJob> slaveAnalyzerJobs = CollectionUtils2.filterOnClass(slaveResultMap.keySet(),
                        AnalyzerJob.class);
                final AnalyzerJobHelper analyzerJobHelper = new AnalyzerJobHelper(slaveAnalyzerJobs);
                final AnalyzerJob slaveAnalyzerJob = analyzerJobHelper.getAnalyzerJob(masterAnalyzerJob);
                if (slaveAnalyzerJob == null) {
                    throw new IllegalStateException("Could not resolve slave component matching [" + masterAnalyzerJob
                            + "] in slave result: " + result);
                }

                final AnalyzerResult analyzerResult = result.getResult(slaveAnalyzerJob);
                slaveResults.add(analyzerResult);
            }

            reduce(masterAnalyzerJob, slaveResults, resultMap, reductionErrors);
        }
    }

    /**
     * Reduces result for a single analyzer
     * 
     * @param analyzerJob
     * @param slaveResults
     * @param resultMap
     * @param reductionErrors
     */
    @SuppressWarnings("unchecked")
    private void reduce(AnalyzerJob analyzerJob, Collection<AnalyzerResult> slaveResults,
            Map<ComponentJob, AnalyzerResult> resultMap, List<AnalysisResultReductionException> reductionErrors) {

        if (slaveResults.size() == 1) {
            // special case where these was only 1 slave job
            final AnalyzerResult firstResult = slaveResults.iterator().next();
            resultMap.put(analyzerJob, firstResult);
            _analysisListener.analyzerSuccess(_masterJob, analyzerJob, firstResult);
            return;
        }

        final Class<? extends AnalyzerResultReducer<?>> reducerClass = analyzerJob.getDescriptor()
                .getResultReducerClass();

        final ComponentDescriptor<? extends AnalyzerResultReducer<?>> reducerDescriptor = Descriptors
                .ofComponent(reducerClass);

        AnalyzerResultReducer<AnalyzerResult> reducer = null;
        boolean success = false;
        try {
            reducer = (AnalyzerResultReducer<AnalyzerResult>) reducerDescriptor.newInstance();

            _lifeCycleHelper.assignProvidedProperties(reducerDescriptor, reducer);
            _lifeCycleHelper.initialize(reducerDescriptor, reducer);

            final AnalyzerResult reducedResult = reducer.reduce(slaveResults);
            resultMap.put(analyzerJob, reducedResult);
            
            success = true;
            _analysisListener.analyzerSuccess(_masterJob, analyzerJob, reducedResult);

        } catch (Exception e) {
            AnalysisResultReductionException reductionError = new AnalysisResultReductionException(analyzerJob,
                    slaveResults, e);
            reductionErrors.add(reductionError);

            _analysisListener.errorInComponent(_masterJob, analyzerJob, null, e);
        } finally {
            if (reducer != null) {
                _lifeCycleHelper.close(reducerDescriptor, reducer, success);
            }
        }
    }
}
