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

import scala.Tuple2;

public class SkipHeaderLineFunction implements Function<Tuple2<Object[], Long>, Boolean> {

    private static final long serialVersionUID = 1L;
    private final int _linesToSkip;

    public SkipHeaderLineFunction(int columnNameLineNumber) {
        _linesToSkip = columnNameLineNumber;
    }

    @Override
    public Boolean call(Tuple2<Object[], Long> v1) throws Exception {
        return v1._2 >= _linesToSkip;
    }

}
