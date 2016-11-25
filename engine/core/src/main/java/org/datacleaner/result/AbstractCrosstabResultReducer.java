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
package org.datacleaner.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.metamodel.util.NumberComparator;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.AnalyzerResultReducer;

/**
 * Abstract reducer class for {@link CrosstabResult}s that are two dimensional
 * and the dimensions are the same on all slave results. This scenario is quite
 * common since a lot of analyzers produce crosstabs with measures on one
 * dimension and column names on another.
 */
public abstract class AbstractCrosstabResultReducer<R extends CrosstabResult> implements AnalyzerResultReducer<R> {

    /**
     * Helper method to get a sum of values (values will be checked whether they
     * are integers only, or else a double will be returned).
     *
     * @param slaveValues
     * @return
     */
    protected static Number sum(final List<?> slaveValues) {
        for (final Object slaveValue : slaveValues) {
            if (slaveValue != null) {
                final Class<? extends Object> cls = slaveValue.getClass();
                if (!(cls == Integer.class || cls == Short.class || cls == Byte.class)) {
                    return sumAsDouble(slaveValues);
                }
            }
        }
        return sumAsInteger(slaveValues);
    }

    /**
     * Helper method to get a sum of values (sum will be calculated as an
     * integer)
     *
     * @param slaveValues
     * @return
     */
    protected static Integer sumAsInteger(final List<?> slaveValues) {
        int sum = 0;
        for (final Object slaveValue : slaveValues) {
            final Number value = (Number) slaveValue;
            if (value != null) {
                sum += value.intValue();
            }
        }
        return sum;
    }

    /**
     * Helper method to get a sum of values (sum will be calculated as a double)
     *
     * @param slaveValues
     * @return
     */
    protected static Double sumAsDouble(final List<?> slaveValues) {
        double sum = 0;
        for (final Object slaveValue : slaveValues) {
            final Number value = (Number) slaveValue;
            if (value != null) {
                sum += value.doubleValue();
            }
        }
        return sum;
    }

    /**
     * Helper method to get the maximum of all values (must be numbers)
     *
     * @param slaveValues
     * @return
     */
    protected static Number maximum(final List<?> slaveValues) {
        Number max = null;
        for (final Object slaveValue : slaveValues) {
            if (max == null) {
                max = (Number) slaveValue;
            } else {
                final Comparable<Object> comparable = NumberComparator.getComparable(max);
                if (comparable.compareTo(slaveValue) < 0) {
                    max = (Number) slaveValue;
                }
            }
        }
        return max;
    }

    /**
     * Helper method to get the minimum of all values (must be numbers)
     *
     * @param slaveValues
     * @return
     */
    protected static Number minimum(final List<?> slaveValues) {
        Number min = null;
        for (final Object slaveValue : slaveValues) {
            if (min == null) {
                min = (Number) slaveValue;
            } else {
                final Comparable<Object> comparable = NumberComparator.getComparable(min);
                if (comparable.compareTo(slaveValue) > 0) {
                    min = (Number) slaveValue;
                }
            }
        }
        return min;
    }

    @Override
    public R reduce(final Collection<? extends R> results) {
        final Crosstab<Serializable> masterCrosstab = createMasterCrosstab(results);
        final Class<?> valueClass = masterCrosstab.getValueClass();
        final CrosstabDimension dimension1 = masterCrosstab.getDimension(0);
        final CrosstabDimension dimension2 = masterCrosstab.getDimension(1);

        final CrosstabNavigator<Serializable> masterNav = masterCrosstab.navigate();
        for (final String category1 : dimension1) {
            masterNav.where(dimension1.getName(), category1);
            for (final String category2 : dimension2) {
                masterNav.where(dimension2.getName(), category2);

                final String[] categories = new String[] { category1, category2 };

                final List<ResultProducer> slaveResultProducers = new ArrayList<>();
                final List<Object> slaveValues = new ArrayList<>(results.size());
                for (final R result : results) {
                    final Crosstab<?> slaveCrosstab = result.getCrosstab();
                    try {
                        final Object slaveValue = slaveCrosstab.getValue(categories);
                        slaveValues.add(slaveValue);

                        final ResultProducer resultProducer = slaveCrosstab.explore(categories);
                        if (resultProducer != null) {
                            slaveResultProducers.add(resultProducer);
                        }
                    } catch (final IllegalArgumentException e) {
                        // ignore this value - it was not present in one of the
                        // slaves
                    }
                }

                final Serializable masterValue = reduceValues(slaveValues, category1, category2, results, valueClass);
                masterNav.put(masterValue);

                if (!slaveResultProducers.isEmpty()) {
                    final ResultProducer masterResultProducer =
                            reduceResultProducers(slaveResultProducers, category1, category2, valueClass, masterValue);
                    if (masterResultProducer != null) {
                        masterNav.attach(masterResultProducer);
                    }
                }
            }
        }

        return buildResult(masterCrosstab, results);
    }

    /**
     * Builds the master crosstab, including all dimensions and categories that
     * will be included in the final result.
     *
     * By default this method will use the first result's crosstab dimensions
     * and categories, assuming that they are all the same.
     *
     * Subclasses can override this method to build the other dimensions.
     *
     * @param results
     * @return
     */
    protected Crosstab<Serializable> createMasterCrosstab(final Collection<? extends R> results) {
        final R firstResult = results.iterator().next();
        final Crosstab<?> firstCrosstab = firstResult.getCrosstab();

        final Class<?> valueClass = firstCrosstab.getValueClass();
        final CrosstabDimension dimension1 = firstCrosstab.getDimension(0);
        final CrosstabDimension dimension2 = firstCrosstab.getDimension(1);

        @SuppressWarnings({ "unchecked", "rawtypes" }) final Crosstab<Serializable> masterCrosstab =
                new Crosstab(valueClass, dimension1, dimension2);
        return masterCrosstab;
    }

    protected ResultProducer reduceResultProducers(final List<ResultProducer> slaveResultProducers,
            final String category1, final String category2, final Class<?> valueClass, final Serializable masterValue) {
        for (final ResultProducer resultProducer : slaveResultProducers) {
            final AnalyzerResult result = resultProducer.getResult();
            if (result instanceof AnnotatedRowsResult) {
                if (((AnnotatedRowsResult) result).getAnnotatedRowCount() > 0) {
                    // just return the first annotated rows result - these are
                    // anyways "just" samples
                    return resultProducer;
                }
            }
        }
        return null;
    }

    protected abstract Serializable reduceValues(List<Object> slaveValues, String category1, String category2,
            Collection<? extends R> results, Class<?> valueClass);

    protected abstract R buildResult(Crosstab<?> crosstab, Collection<? extends R> results);
}
