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
package org.datacleaner.spark.functions;

import java.util.Arrays;

import org.apache.spark.api.java.function.Function2;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.ResultDescriptor;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.spark.NamedAnalyzerResult;
import org.datacleaner.spark.SparkJobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AnalyzerResultReduceFunction
        implements Function2<NamedAnalyzerResult, NamedAnalyzerResult, NamedAnalyzerResult> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(AnalyzerResultReduceFunction.class);

    private final SparkJobContext _sparkJobContext;

    public AnalyzerResultReduceFunction(final SparkJobContext sparkJobContext) {
        _sparkJobContext = sparkJobContext;
    }

    @Override
    public NamedAnalyzerResult call(final NamedAnalyzerResult namedAnalyzerResult1,
            final NamedAnalyzerResult namedAnalyzerResult2) throws Exception {

        assert namedAnalyzerResult1.getName().equals(namedAnalyzerResult2.getName());

        final String key = namedAnalyzerResult1.getName();

        final ComponentJob componentJob = _sparkJobContext.getComponentByKey(key);

        final AnalyzerResult analyzerResult1 = namedAnalyzerResult1.getAnalyzerResult();
        final AnalyzerResult analyzerResult2 = namedAnalyzerResult2.getAnalyzerResult();

        logger.info("Reducing results with key '{}' of types: {} and {}", key, analyzerResult1.getClass(),
                analyzerResult2.getClass());

        final ResultDescriptor rd = getResultDescriptor(componentJob, analyzerResult1);
        final Class<? extends AnalyzerResultReducer<?>> resultReducerClass = rd.getResultReducerClass();

        if (resultReducerClass == null) {
            throw new IllegalStateException("The result type (" + analyzerResult1 + ") is not distributable!");
        }

        final AnalyzerResultReducer<AnalyzerResult> reducer = initializeReducer(resultReducerClass);

        final AnalyzerResult reducedAnalyzerResult = reducer.reduce(Arrays.asList(analyzerResult1, analyzerResult2));

        return new NamedAnalyzerResult(key, reducedAnalyzerResult);
    }

    private AnalyzerResultReducer<AnalyzerResult> initializeReducer(
            final Class<? extends AnalyzerResultReducer<?>> resultReducerClass) {

        final DataCleanerConfiguration configuration = _sparkJobContext.getConfiguration();
        final InjectionManager injectionManager = configuration.getEnvironment().getInjectionManagerFactory()
                .getInjectionManager(configuration, _sparkJobContext.getAnalysisJob());
        final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(injectionManager, false);

        final ComponentDescriptor<? extends AnalyzerResultReducer<?>> reducerDescriptor =
                Descriptors.ofComponent(resultReducerClass);

        @SuppressWarnings("unchecked") final AnalyzerResultReducer<AnalyzerResult> reducer =
                (AnalyzerResultReducer<AnalyzerResult>) reducerDescriptor.newInstance();

        lifeCycleHelper.assignProvidedProperties(reducerDescriptor, reducer);
        lifeCycleHelper.initialize(reducerDescriptor, reducer);

        return reducer;
    }

    protected ResultDescriptor getResultDescriptor(final ComponentJob componentJob,
            final AnalyzerResult analyzerResult) {
        final ComponentDescriptor<?> descriptor = componentJob.getDescriptor();
        if (descriptor instanceof ResultDescriptor) {
            return (ResultDescriptor) descriptor;
        }
        // slightly more expensive, but potentially also better / more specific!
        return Descriptors.ofResult(analyzerResult);
    }
}
