/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.beans.api.ComponentContext;
import org.eobjects.analyzer.beans.api.ComponentMessage;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.ComponentJob;

/**
 * Default implementation of {@link ComponentContext}.
 */
public class ComponentContextImpl implements ComponentContext {

    private final AnalysisJob _job;
    private final ComponentJob _component;
    private final AnalysisListener _listener;

    public ComponentContextImpl(AnalysisJob job, ComponentJob component, AnalysisListener listener) {
        _job = job;
        _component = component;
        _listener = listener;
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
