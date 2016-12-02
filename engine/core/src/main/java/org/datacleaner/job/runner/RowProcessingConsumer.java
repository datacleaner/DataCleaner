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

import java.util.Collection;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Concurrent;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.FilterOutcomes;
import org.datacleaner.job.OutputDataStreamJob;

/**
 * Interface for objects that recieve rows from the RowProcessingPublisher.
 */
public interface RowProcessingConsumer {

    /**
     * @return a list of this consumer's active {@link OutputDataStream}s.
     */
    Collection<ActiveOutputDataStream> getActiveOutputDataStreams();

    void registerOutputDataStream(OutputDataStreamJob outputDataStreamJob,
            RowProcessingPublisher publisherForOutputDataStream);

    /**
     * @return the columns generated by this consumer
     */
    InputColumn<?>[] getOutputColumns();

    /**
     * @return true if this consumer is thread-safe and can thusly be invoked by
     *         several threads at the same time.
     *
     * @see Concurrent
     */
    boolean isConcurrent();

    /**
     * @return the required input columns for this consumer
     */
    InputColumn<?>[] getRequiredInput();

    /**
     * @param availableOutcomesInFlow
     *            a collection of all outcomes that <i>can</i> be available to
     *            the consumer given the proposed flow order.
     * @return whether or not the requirements (in terms of required outcomes)
     *         are sufficient for adding this consumer into the execution flow.
     *         If false the ordering mechanism will try to move the consumer to
     *         a later stage in the flow.
     */
    boolean satisfiedForFlowOrdering(FilterOutcomes availableOutcomesInFlow);

    /**
     * @param outcomes
     *            the current available outcomes in the processing of the
     *            particular row.
     * @return whether or not the requirements (in terms of required outcomes)
     *         are sufficient for including this component for a particular
     *         row's processing. If false, this component will be skipped.
     */
    boolean satisfiedForConsume(FilterOutcomes outcomes, InputRow inputRow);

    /**
     * Main method of the consumer. Recieves the input row, dispatches it to the
     * bean that needs to process it and returns the row for the next component
     * in the chain to process (often the same row).
     *
     * @param row
     * @param distinctCount
     * @param outcomes
     */
    void consume(InputRow row, int distinctCount, FilterOutcomes outcomes, RowProcessingChain chain);

    /**
     * @return the componbent job
     */
    ComponentJob getComponentJob();

    /**
     * @return the component instance
     */
    Object getComponent();

    /**
     * Determines if the {@link ComponentJob} represented in this consumer is
     * expected to produce an {@link AnalyzerResult}
     *
     * @return
     */
    boolean isResultProducer();

    /**
     * Gets the {@link AnalysisJob} that this consumer pertains to.
     *
     * @return
     */
    AnalysisJob getAnalysisJob();

    /**
     * Indicates to this consumer that a {@link RowProcessingPublisher} has
     * initialized the component that is represented.
     *
     * @param publisher
     * @return the count of initialized publishers
     */
    int onPublisherInitialized(RowProcessingPublisher publisher);

    /**
     * Indicates to this consumer that a {@link RowProcessingPublisher} has
     * closed the component that is represented.
     *
     * @param publisher
     * @return the count of not-closed publishers
     */
    int onPublisherClosed(RowProcessingPublisher publisher);

    /**
     * Registers a {@link RowProcessingPublisher} with this consumer to indicate
     * that it will be publishing records to it.
     *
     * @param publisher
     */
    void registerPublisher(RowProcessingPublisher publisher);

    /**
     * Determines whether all registered publishers have been initialized.
     *
     * @return
     */
    boolean isAllPublishersInitialized();

    /**
     * Determines whether all registered publishers have been closed.
     *
     * @return
     */
    boolean isAllPublishersClosed();
}
