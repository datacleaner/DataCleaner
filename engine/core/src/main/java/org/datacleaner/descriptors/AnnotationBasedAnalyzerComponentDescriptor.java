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
package org.datacleaner.descriptors;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.NoAnalyzerResultReducer;
import org.datacleaner.util.ReflectionUtils;

final class AnnotationBasedAnalyzerComponentDescriptor<A extends Analyzer<?>> extends
        AbstractHasAnalyzerResultComponentDescriptor<A> implements AnalyzerDescriptor<A> {

    private static final long serialVersionUID = 1L;

    protected AnnotationBasedAnalyzerComponentDescriptor(Class<A> analyzerClass) throws DescriptorException {
        super(analyzerClass, true);

        if (!ReflectionUtils.is(analyzerClass, Analyzer.class)) {
            throw new DescriptorException(analyzerClass + " does not implement " + Analyzer.class.getName());
        }

        visitClass();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected String getDisplayNameIfNotNamed(Class<?> cls) {
        org.eobjects.analyzer.beans.api.AnalyzerBean annotation = ReflectionUtils.getAnnotation(cls,
                org.eobjects.analyzer.beans.api.AnalyzerBean.class);
        if (annotation == null) {
            return null;
        }
        return annotation.value();
    }

    @Override
    public Class<? extends AnalyzerResultReducer<?>> getResultReducerClass() {
        final Distributed distributedAnalyzer = getAnnotation(Distributed.class);
        if (distributedAnalyzer != null) {
            // the analyzer-level annotation always comes first (can override
            // the result-level annotation).
            if (!distributedAnalyzer.value()) {
                return super.getResultReducerClass();
            }
            final Class<? extends AnalyzerResultReducer<?>> reducer = distributedAnalyzer.reducer();
            if (reducer != null && reducer != NoAnalyzerResultReducer.class) {
                return reducer;
            }
        }

        return super.getResultReducerClass();
    }

    @Override
    public boolean isDistributable() {
        return getResultReducerClass() != null;
    }
}
