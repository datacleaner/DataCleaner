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
package org.datacleaner.configuration;

import org.datacleaner.beans.api.ComponentContext;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.ComponentContextImpl;

/**
 * {@link InjectionManager} that will wrap an existing {@link InjectionManager}
 * but add context-specific injection capabilities that are only available to
 * specific components.
 */
public class ContextAwareInjectionManager implements InjectionManager {

    private final InjectionManager _delegate;
    private final ComponentJob _componentJob;
    private final AnalysisListener _listener;
    private final AnalysisJob _job;

    public ContextAwareInjectionManager(InjectionManager delegate, AnalysisJob job, ComponentJob componentJob,
            AnalysisListener listener) {
        _delegate = delegate;
        _job = job;
        _componentJob = componentJob;
        _listener = listener;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E getInstance(InjectionPoint<E> injectionPoint) {
        final Class<?> baseType = injectionPoint.getBaseType();
        if (baseType == ComponentContext.class) {
            final ComponentContext componentContext = new ComponentContextImpl(_job, _componentJob, _listener);
            return (E) componentContext;
        }

        return _delegate.getInstance(injectionPoint);
    }

}
