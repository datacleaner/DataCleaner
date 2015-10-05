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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.api.HasAnalyzerResult;
import org.datacleaner.api.HasOutputDataStreams;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnyComponentRequirement;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.FilterOutcomes;
import org.datacleaner.job.HasComponentRequirement;
import org.datacleaner.job.InputColumnSinkJob;
import org.datacleaner.job.OutputDataStreamJob;
import org.datacleaner.util.SourceColumnFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of {@link RowProcessingConsumer}. Contains utility
 * methods to help make the 'is satisfied for execution' methods easier to
 * implement.
 */
abstract class AbstractRowProcessingConsumer implements RowProcessingConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRowProcessingConsumer.class);

    private final AnalysisJob _analysisJob;
    private final AnalysisListener _analysisListener;
    private final HasComponentRequirement _hasComponentRequirement;
    private final Set<HasComponentRequirement> _sourceJobsOfInputColumns;
    private final boolean _alwaysSatisfiedForConsume;
    private final List<ActiveOutputDataStream> _outputDataStreams;
    private final AtomicInteger _publishersRegisteredCount;
    private final AtomicInteger _publishersInitializedCount;
    private final AtomicInteger _publishersClosedCount;

    protected AbstractRowProcessingConsumer(RowProcessingPublisher publisher, HasComponentRequirement outcomeSinkJob,
            InputColumnSinkJob inputColumnSinkJob) {
        this(publisher.getAnalysisJob(), publisher.getAnalysisListener(), outcomeSinkJob, inputColumnSinkJob, publisher
                .getSourceColumnFinder());
    }

    protected AbstractRowProcessingConsumer(AnalysisJob analysisJob, AnalysisListener analysisListener,
            HasComponentRequirement outcomeSinkJob, InputColumnSinkJob inputColumnSinkJob,
            SourceColumnFinder sourceColumnFinder) {
        this(analysisJob, analysisListener, outcomeSinkJob, buildSourceJobsOfInputColumns(inputColumnSinkJob,
                sourceColumnFinder));
    }

    protected AbstractRowProcessingConsumer(AnalysisJob analysisJob, AnalysisListener analysisListener,
            HasComponentRequirement outcomeSinkJob, Set<HasComponentRequirement> sourceJobsOfInputColumns) {
        _analysisJob = analysisJob;
        _analysisListener = analysisListener;
        _hasComponentRequirement = outcomeSinkJob;
        _sourceJobsOfInputColumns = sourceJobsOfInputColumns;
        _outputDataStreams = new ArrayList<>(2);
        _alwaysSatisfiedForConsume = isAlwaysSatisfiedForConsume();
        _publishersRegisteredCount = new AtomicInteger(0);
        _publishersInitializedCount = new AtomicInteger(0);
        _publishersClosedCount = new AtomicInteger(0);
    }

    private boolean isAlwaysSatisfiedForConsume() {
        if (_sourceJobsOfInputColumns.isEmpty()) {
            return true;
        }

        if (isAlwaysSatisfiedRequirement()) {
            return true;
        }
        return false;
    }

    private boolean isAlwaysSatisfiedRequirement() {
        final ComponentRequirement componentRequirement = _hasComponentRequirement.getComponentRequirement();
        if (componentRequirement == null) {
            return false;
        }

        if (componentRequirement instanceof AnyComponentRequirement) {
            return true;
        }

        return false;
    }

    private static Set<HasComponentRequirement> buildSourceJobsOfInputColumns(InputColumnSinkJob inputColumnSinkJob,
            SourceColumnFinder sourceColumnFinder) {
        final Set<HasComponentRequirement> result = new HashSet<HasComponentRequirement>();

        final Set<Object> sourceJobsOfInputColumns = sourceColumnFinder.findAllSourceJobs(inputColumnSinkJob);
        for (Iterator<Object> it = sourceJobsOfInputColumns.iterator(); it.hasNext();) {
            final Object sourceJob = it.next();
            if (sourceJob instanceof HasComponentRequirement) {
                final HasComponentRequirement sourceOutcomeSinkJob = (HasComponentRequirement) sourceJob;
                final ComponentRequirement componentRequirement = sourceOutcomeSinkJob.getComponentRequirement();
                if (componentRequirement != null) {
                    result.add(sourceOutcomeSinkJob);
                }
            }
        }
        return result;
    }

    /**
     * Ensures that just a single outcome is satisfied
     */
    @Override
    public final boolean satisfiedForConsume(FilterOutcomes outcomes, InputRow row) {
        boolean satisfiedOutcomesForConsume = satisfiedOutcomesForConsume(_hasComponentRequirement, row, outcomes);
        if (!satisfiedOutcomesForConsume) {
            return false;
        }
        boolean satisfiedInputsForConsume = satisfiedInputsForConsume(row, outcomes);
        return satisfiedInputsForConsume;
    }

    @Override
    public InputColumn<?>[] getOutputColumns() {
        return new InputColumn[0];
    }

    @Override
    public boolean isResultProducer() {
        return getComponent() instanceof HasAnalyzerResult;
    }

    @Override
    public final void consume(InputRow row, int distinctCount, FilterOutcomes outcomes, RowProcessingChain chain) {
        try {
            consumeInternal(row, distinctCount, outcomes, chain);
        } catch (RuntimeException e) {
            final ComponentJob componentJob = getComponentJob();
            if (_analysisListener == null) {
                logger.error("Error occurred in component '" + componentJob + "' and no AnalysisListener is available",
                        e);
                throw e;
            } else {
                _analysisListener.errorInComponent(_analysisJob, componentJob, row, e);
            }
        }
    }

    /**
     * Overrideable method for subclasses
     * 
     * @param row
     * @param distinctCount
     * @param outcomes
     * @param chain
     */
    protected abstract void consumeInternal(InputRow row, int distinctCount, FilterOutcomes outcomes,
            RowProcessingChain chain);

    private boolean satisfiedInputsForConsume(InputRow row, FilterOutcomes outcomes) {
        if (_alwaysSatisfiedForConsume) {
            return _alwaysSatisfiedForConsume;
        }

        final ComponentRequirement componentRequirement = _hasComponentRequirement.getComponentRequirement();
        if (componentRequirement == null) {
            for (final Object sourceJobsOfInputColumn : _sourceJobsOfInputColumns) {
                // if any of the source jobs is satisfied, then continue
                if (sourceJobsOfInputColumn instanceof HasComponentRequirement) {
                    final HasComponentRequirement hasComponentRequirement = (HasComponentRequirement) sourceJobsOfInputColumn;
                    final boolean satisfiedOutcomesForConsume = satisfiedOutcomesForConsume(hasComponentRequirement,
                            row, outcomes);
                    if (satisfiedOutcomesForConsume) {
                        return true;
                    }
                }
            }
            return false;
        }

        return true;
    }

    private boolean satisfiedOutcomesForConsume(HasComponentRequirement component, InputRow row, FilterOutcomes outcomes) {
        boolean isSatisfiedOutcomes = false;

        final ComponentRequirement componentRequirement = component.getComponentRequirement();

        if (componentRequirement == null) {
            isSatisfiedOutcomes = true;
        } else {
            isSatisfiedOutcomes = componentRequirement.isSatisfied(row, outcomes);
        }
        return isSatisfiedOutcomes;
    }

    /**
     * Ensures that ALL outcomes are available
     */
    @Override
    public final boolean satisfiedForFlowOrdering(FilterOutcomes outcomes) {
        if (isAlwaysSatisfiedRequirement()) {
            return true;
        }

        final ComponentRequirement componentRequirement = _hasComponentRequirement.getComponentRequirement();
        if (componentRequirement == null) {
            return true;
        }

        final Collection<FilterOutcome> dependencies = componentRequirement.getProcessingDependencies();
        for (FilterOutcome filterOutcome : dependencies) {
            boolean contains = outcomes.contains(filterOutcome);
            if (!contains) {
                return false;
            }
        }

        return componentRequirement.isSatisfied(null, outcomes);
    }

    @Override
    public List<ActiveOutputDataStream> getActiveOutputDataStreams() {
        return Collections.unmodifiableList(_outputDataStreams);
    }

    @Override
    public void registerOutputDataStream(OutputDataStreamJob outputDataStreamJob,
            RowProcessingPublisher publisherForOutputDataStream) {
        final HasOutputDataStreams component = (HasOutputDataStreams) getComponent();
        _outputDataStreams
                .add(new ActiveOutputDataStream(outputDataStreamJob, publisherForOutputDataStream, component));
    }

    @Override
    public AnalysisJob getAnalysisJob() {
        return _analysisJob;
    }

    @Override
    public int onPublisherInitialized(RowProcessingPublisher publisher) {
        return _publishersInitializedCount.incrementAndGet();
    }

    @Override
    public int onPublisherClosed(RowProcessingPublisher publisher) {
        final int closedCount = _publishersClosedCount.incrementAndGet();
        return _publishersRegisteredCount.get() - closedCount;
    }

    @Override
    public boolean isAllPublishersInitialized() {
        return _publishersRegisteredCount.get() == _publishersInitializedCount.get();
    }
    
    @Override
    public boolean isAllPublishersClosed() {
        return _publishersRegisteredCount.get() == _publishersClosedCount.get();
    }

    @Override
    public void registerPublisher(RowProcessingPublisher publisher) {
        _publishersRegisteredCount.incrementAndGet();
    }
}
