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
package org.datacleaner.job.tasks;

import java.util.Collection;

import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.job.ComponentConfiguration;
import org.datacleaner.job.runner.ActiveOutputDataStream;
import org.datacleaner.job.runner.RowProcessingConsumer;
import org.datacleaner.job.runner.RowProcessingPublisher;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task that runs for every component to initialize it before execution of a job
 */
public final class InitializeTask implements Task {

    private static final Logger logger = LoggerFactory.getLogger(InitializeTask.class);

    private final LifeCycleHelper _lifeCycleHelper;
    private final RowProcessingConsumer _consumer;

    public InitializeTask(LifeCycleHelper lifeCycleHelper, RowProcessingConsumer consumer) {
        _lifeCycleHelper = lifeCycleHelper;
        _consumer = consumer;
    }

    @Override
    public void execute() throws Exception {
        logger.debug("execute()");

        executeInternal(_consumer, _lifeCycleHelper);
    }

    private static void executeInternal(final RowProcessingConsumer consumer, LifeCycleHelper lifeCycleHelper) {
        final int publisherCount = consumer.incrementActivePublishers();
        if (publisherCount == 1) {
            final ComponentConfiguration configuration = consumer.getComponentJob().getConfiguration();
            final ComponentDescriptor<?> descriptor = consumer.getComponentJob().getDescriptor();
            final Object component = consumer.getComponent();
            
            lifeCycleHelper.assignConfiguredProperties(descriptor, component, configuration);
            lifeCycleHelper.assignProvidedProperties(descriptor, component);
            lifeCycleHelper.validate(descriptor, component);
            final Collection<ActiveOutputDataStream> activeOutputDataStreams = consumer.getActiveOutputDataStreams();
            for (ActiveOutputDataStream activeOutputDataStream : activeOutputDataStreams) {
                activeOutputDataStream.initialize();
                final RowProcessingPublisher publisher = activeOutputDataStream.getPublisher();
                for (RowProcessingConsumer outputDataStreamConsumer : publisher.getConsumers()) {
                    final LifeCycleHelper outputDataStreamLifeCycleHelper = publisher.getPublishers()
                            .getConsumerSpecificLifeCycleHelper(consumer);
                    executeInternal(outputDataStreamConsumer, outputDataStreamLifeCycleHelper);
                }
            }
            lifeCycleHelper.initialize(descriptor, component);
        }
    }

    @Override
    public String toString() {
        return "InitializeTask[" + _consumer + "]";
    }
}
