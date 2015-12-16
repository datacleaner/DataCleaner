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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;

/**
 * A utility {@link PairFlatMapFunction} for conversion between a
 * {@link JavaRDD} of {@link Tuple2} into a {@link JavaPairRDD} of that same
 * tuple type.
 */
public class TuplesToTuplesFunction<K, V> implements PairFlatMapFunction<Iterator<Tuple2<K, V>>, K, V> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(TuplesToTuplesFunction.class);

    @Override
    public Iterable<Tuple2<K, V>> call(final Iterator<Tuple2<K, V>> iterator) throws Exception {
        logger.info("call(Iterator) invoked");
        return new Iterable<Tuple2<K, V>>() {

            private int i = 0;

            @Override
            public Iterator<Tuple2<K, V>> iterator() {
                i++;
                if (i == 1) {
                    logger.debug("Returning iterator for the first time");
                } else {
                    logger.warn("Returning iterator more than once! - {}", i);
                }
                return iterator;
            }
        };
    }

}
