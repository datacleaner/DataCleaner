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

import org.datacleaner.api.ComponentContext;
import org.datacleaner.api.ComponentMessage;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;

/**
 * Default implementation of {@link ComponentContext}.
 */
public class ComponentContextImpl implements ComponentContext {

    private final AnalysisJob _job;
    private final ComponentJob _component;
    private final AnalysisListener _listener;

    /**
     * The preferred constructor which is aware of the component and allows
     * publishing messages to an {@link AnalysisListener}.
     * 
     * @param job
     * @param component
     * @param listener
     */
    public ComponentContextImpl(AnalysisJob job, ComponentJob component, AnalysisListener listener) {
        _job = job;
        _component = component;
        _listener = listener;
    }

    /**
     * Constructor used in situations where an {@link AnalysisListener} is not
     * available, typically at job building time rather than run time.
     * 
     * @param job
     */
    public ComponentContextImpl(AnalysisJob job) {
        _job = job;
        _component = null;
        _listener = null;
    }

    @Override
    public AnalysisJob getAnalysisJob() {
        return _job;
    }

    @Override
    public void publishMessage(ComponentMessage message) {
        if (_listener == null) {
            return;
        }
        _listener.onComponentMessage(_job, _component, message);
    }

}
