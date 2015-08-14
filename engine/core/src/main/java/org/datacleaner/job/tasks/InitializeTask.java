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
    private final RowProcessingPublisher _publisher;

    public InitializeTask(LifeCycleHelper lifeCycleHelper, RowProcessingConsumer consumer, RowProcessingPublisher publisher) {
        _lifeCycleHelper = lifeCycleHelper;
        _consumer = consumer;
        _publisher = publisher;
    }

    @Override
    public void execute() throws Exception {
        logger.debug("execute()");

        final ComponentConfiguration configuration = _consumer.getComponentJob().getConfiguration();
        final ComponentDescriptor<?> descriptor = _consumer.getComponentJob().getDescriptor();
        final Object component = _consumer.getComponent();

        _lifeCycleHelper.assignConfiguredProperties(descriptor, component, configuration);
        _lifeCycleHelper.assignProvidedProperties(descriptor, component);
        _lifeCycleHelper.validate(descriptor, component);
        final Collection<ActiveOutputDataStream> activeOutputDataStreams = _consumer.getActiveOutputDataStreams();
        for (ActiveOutputDataStream activeOutputDataStream : activeOutputDataStreams) {
            activeOutputDataStream.initialize();
        }
        _lifeCycleHelper.initialize(descriptor, component);
        _lifeCycleHelper.initializeReferenceData();
    }

    @Override
    public String toString() {
        return "InitializeTask[" + _consumer + "]";
    }
}
