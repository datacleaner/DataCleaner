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

import org.apache.spark.api.java.function.Function;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.job.ComponentJob;

import scala.Tuple2;

public final class ExtractAnalyzerResultFromTupleFunction implements
        Function<Tuple2<String, Tuple2<AnalyzerResult, ComponentJob>>, Tuple2<String, AnalyzerResult>> {
    private static final long serialVersionUID = 1L;

    @Override
    public Tuple2<String, AnalyzerResult> call(
            Tuple2<String, Tuple2<AnalyzerResult, ComponentJob>> tuple) throws Exception {
        return new Tuple2<String, AnalyzerResult>(tuple._1, tuple._2._1);
    }
}