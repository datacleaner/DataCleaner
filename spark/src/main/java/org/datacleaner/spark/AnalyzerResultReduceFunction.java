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
package org.datacleaner.spark;

import java.util.Arrays;

import org.apache.spark.api.java.function.Function2;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.ResultDescriptor;
import org.datacleaner.job.ComponentJob;

import scala.Tuple2;

public final class AnalyzerResultReduceFunction
        implements
        Function2<Tuple2<AnalyzerResult, ComponentJob>, Tuple2<AnalyzerResult, ComponentJob>, Tuple2<AnalyzerResult, ComponentJob>> {
    private static final long serialVersionUID = 1L;

    @Override
    public Tuple2<AnalyzerResult, ComponentJob> call(Tuple2<AnalyzerResult, ComponentJob> tuple1,
            Tuple2<AnalyzerResult, ComponentJob> tuple2) throws Exception {
        ComponentJob componentJob1 = tuple1._2;
        ComponentJob componentJob2 = tuple2._2;

        assert componentJob1.equals(componentJob2);

        final AnalyzerResult analyzerResult1 = tuple1._1;
        final AnalyzerResult analyzerResult2 = tuple2._1;

        ResultDescriptor rd = getResultDescriptor(componentJob1, analyzerResult1);
        final Class<? extends AnalyzerResultReducer<?>> resultReducerClass = rd.getResultReducerClass();
        
        if (resultReducerClass == null) {
            throw new IllegalStateException("The analyzer (" + analyzerResult1 + ") is not reducable!");
        }

        @SuppressWarnings("unchecked")
        AnalyzerResultReducer<AnalyzerResult> reducer = (AnalyzerResultReducer<AnalyzerResult>) resultReducerClass
                .newInstance();
        AnalyzerResult reducedAnalyzerResult = reducer.reduce(Arrays.asList(analyzerResult1,
                analyzerResult2));

        Tuple2<AnalyzerResult, ComponentJob> reducedTuple = new Tuple2<AnalyzerResult, ComponentJob>(
                reducedAnalyzerResult, componentJob1);
        return reducedTuple;
    }

    protected ResultDescriptor getResultDescriptor(ComponentJob componentJob,
            AnalyzerResult analyzerResult) {
        final ComponentDescriptor<?> descriptor = componentJob.getDescriptor();
        if (descriptor instanceof ResultDescriptor) {
            return (ResultDescriptor) descriptor;
        }
        // slightly more expensive, but potentially also better
        // / more specific!
        return Descriptors.ofResult(analyzerResult);
    }
}