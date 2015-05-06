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

import java.util.concurrent.atomic.AtomicBoolean;

import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.job.concurrent.TaskListener;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloseTaskListener implements TaskListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final LifeCycleHelper _lifeCycleHelper;
    private final ComponentDescriptor<?> _descriptor;
    private final Object _component;
    private final AtomicBoolean _success;
    private final TaskListener _nextTaskListener;

    public CloseTaskListener(LifeCycleHelper lifeCycleHelper, ComponentDescriptor<?> descriptor, Object component,
            AtomicBoolean success) {
        this(lifeCycleHelper, descriptor, component, success, null);
    }

    public CloseTaskListener(LifeCycleHelper lifeCycleHelper, ComponentDescriptor<?> descriptor, Object component,
            AtomicBoolean success, TaskListener nextTaskListener) {
        _lifeCycleHelper = lifeCycleHelper;
        _descriptor = descriptor;
        _component = component;
        _success = success;
        _nextTaskListener = nextTaskListener;
    }

    private void cleanup() {
        logger.debug("execute()");

        // close can occur AFTER completion
        _lifeCycleHelper.close(_descriptor, _component, _success.get());
        _lifeCycleHelper.closeReferenceData();
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
        _success.set(false);
        cleanup();
        if (_nextTaskListener != null) {
            _nextTaskListener.onError(task, throwable);
        }
    }
}
