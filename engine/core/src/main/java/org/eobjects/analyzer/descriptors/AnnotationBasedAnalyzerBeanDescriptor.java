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
package org.eobjects.analyzer.descriptors;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Distributed;
import org.eobjects.analyzer.beans.api.NoAnalyzerResultReducer;
import org.eobjects.analyzer.result.AnalyzerResultReducer;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.analyzer.util.StringUtils;

final class AnnotationBasedAnalyzerBeanDescriptor<A extends Analyzer<?>> extends
        AbstractHasAnalyzerResultBeanDescriptor<A> implements AnalyzerBeanDescriptor<A> {

    private static final long serialVersionUID = 1L;

    private final String _displayName;

    protected AnnotationBasedAnalyzerBeanDescriptor(Class<A> analyzerClass) throws DescriptorException {
        super(analyzerClass, true);

        AnalyzerBean analyzerAnnotation = ReflectionUtils.getAnnotation(analyzerClass, AnalyzerBean.class);
        if (analyzerAnnotation == null) {
            throw new DescriptorException(analyzerClass + " doesn't implement the AnalyzerBean annotation");
        }

        String displayName = analyzerAnnotation.value();
        if (StringUtils.isNullOrEmpty(displayName)) {
            displayName = ReflectionUtils.explodeCamelCase(analyzerClass.getSimpleName(), false);
        }
        _displayName = displayName.trim();

        visitClass();
    }

    @Override
    public String getDisplayName() {
        return _displayName;
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
