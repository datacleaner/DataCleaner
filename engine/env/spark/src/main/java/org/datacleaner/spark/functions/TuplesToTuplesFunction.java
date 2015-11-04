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

import java.util.Iterator;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.PairFlatMapFunction;

import scala.Tuple2;

/**
 * A utility {@link PairFlatMapFunction} for conversion between a
 * {@link JavaRDD} of {@link Tuple2} into a {@link JavaPairRDD} of that same
 * tuple type.
 */
public class TuplesToTuplesFunction<K, V> implements PairFlatMapFunction<Iterator<Tuple2<K, V>>, K, V> {

    private static final long serialVersionUID = 1L;

    @Override
    public Iterable<Tuple2<K, V>> call(final Iterator<Tuple2<K, V>> iterator) throws Exception {
        return new Iterable<Tuple2<K, V>>() {
            @Override
            public Iterator<Tuple2<K, V>> iterator() {
                return iterator;
            }
        };
    }

}
