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
import java.util.concurrent.atomic.AtomicBoolean;

import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.concurrent.TaskListener;
import org.datacleaner.job.runner.ActiveOutputDataStream;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.RowProcessingConsumer;
import org.datacleaner.job.runner.RowProcessingPublisher;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task listener that runs for every component to close it after execution of a
 * job.
 * 
 * This class is NOT a {@link Task} because it needs to run regardless of
 * errors, which tasks don't.
 */
public class CloseTaskListener implements TaskListener {

    private static final Logger logger = LoggerFactory.getLogger(CloseTaskListener.class);

    private final AtomicBoolean _success;
    private final LifeCycleHelper _lifeCycleHelper;
    private final RowProcessingPublisher _publisher;
    private final RowProcessingConsumer _consumer;
    private final TaskListener _nextTaskListener;
    private final AnalysisListener _analysisListener;
    private final AnalysisJob _analysisJob;

    public CloseTaskListener(LifeCycleHelper lifeCycleHelper, RowProcessingPublisher publisher, RowProcessingConsumer consumer, AtomicBoolean success,
            TaskListener nextTaskListener, AnalysisListener analysisListener, AnalysisJob analysisJob) {
        _lifeCycleHelper = lifeCycleHelper;
        _publisher = publisher;
        _consumer = consumer;
        _success = success;
        _nextTaskListener = nextTaskListener;
        _analysisListener = analysisListener;
        _analysisJob = analysisJob;
    }

    public void cleanup() {
        logger.debug("cleanup()");
        
        final int publishersLeft = _consumer.onPublisherClosed(_publisher);
        if (publishersLeft == 0) {
            final Object component = _consumer.getComponent();
            final ComponentDescriptor<?> descriptor = _consumer.getComponentJob().getDescriptor();
            
            // close can occur AFTER completion
            _lifeCycleHelper.close(descriptor, component, _success.get());
            
            final Collection<ActiveOutputDataStream> activeOutputDataStreams = _consumer.getActiveOutputDataStreams();
            for (ActiveOutputDataStream activeOutputDataStream : activeOutputDataStreams) {
                activeOutputDataStream.close();
            }
        }
    }

    @Override
    public void onBegin(Task task) {
        if (_nextTaskListener != null) {
            _nextTaskListener.onBegin(task);
        }
    }

    @Override
    public void onComplete(Task task) {
        cleanup();
        if (_nextTaskListener != null) {
            _nextTaskListener.onComplete(task);
        }
    }

    @Override
    public void onError(Task task, Throwable throwable) {
        final boolean previouslySuccessful = _success.getAndSet(false);
        if (previouslySuccessful) {
            // only report the first such error
            _analysisListener.errorUnknown(_analysisJob, throwable);
        }
        cleanup();
        if (_nextTaskListener != null) {
            _nextTaskListener.onError(task, throwable);
        }
    }
}
