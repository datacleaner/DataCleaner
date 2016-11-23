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
package org.datacleaner.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.CrosstabNavigator;

/**
 * Helper class for reductions of crosstabs
 *
 */
public class CrosstabReducerHelper {

    /**
     * Add the croosstab dimensions to the list of dimensions
     *
     * @param crosstabDimensions
     *            - list of dimensions
     * @param partialCrosstab
     *            - crosstab
     */
    public static void createDimensionsColumnCrosstab(final List<CrosstabDimension> crosstabDimensions,
            final Crosstab<Number> partialCrosstab) {
        if (partialCrosstab != null) {
            final List<CrosstabDimension> dimensions = partialCrosstab.getDimensions();
            for (final CrosstabDimension dimension : dimensions) {
                if (!dimensionExits(crosstabDimensions, dimension)) {
                    crosstabDimensions.add(dimension);
                }
            }
        }
    }

    public static boolean dimensionExits(final Collection<CrosstabDimension> list, final CrosstabDimension dimension) {
        if (list.size() > 0) {
            boolean allreadyExits = false;
            for (final CrosstabDimension dim : list) {
                if (dimension.equals(dim)) {
                    allreadyExits = true;
                    break;
                }
            }
            return allreadyExits;
        }
        return false;
    }

    /**
     * Add the values of partial crosstab to the main crosstab
     *
     * @param mainCrosstab
     *            - main crosstab
     * @param partialCrosstab
     *            - partial crosstab
     */
    public static void addData(final Crosstab<Number> mainCrosstab, final Crosstab<Number> partialCrosstab,
            final CrosstabDimension columnDimension, final CrosstabDimension measureDimension) {
        if (partialCrosstab != null) {

            final CrosstabNavigator<Number> mainNavigator = new CrosstabNavigator<>(mainCrosstab);
            final CrosstabNavigator<Number> nav = new CrosstabNavigator<>(partialCrosstab);

            for (final String columnCategory : columnDimension.getCategories()) {
                // just navigate through the dimensions because is the column
                // dimension
                nav.where(columnDimension, columnCategory);
                mainNavigator.where(columnDimension, columnCategory);
                // navigate and sum up data
                final List<String> categories = measureDimension.getCategories();
                for (final String measureCategory : categories) {
                    sumUpData(mainNavigator, nav, measureDimension, measureCategory);
                }
            }
        }
    }

    private static void sumUpData(final CrosstabNavigator<Number> mainNavigator, final CrosstabNavigator<Number> nav,
            final CrosstabDimension dimension, final String category) {
        final CrosstabNavigator<Number> where = nav.where(dimension, category);
        final CrosstabNavigator<Number> whereToPut = mainNavigator.where(dimension, category);
        final Number categoryValue = where.safeGet(null);
        if (categoryValue != null) {
            final Number oldValue = whereToPut.safeGet(null);
            if (oldValue != null) {
                final Number newValue = sum(oldValue, categoryValue);
                whereToPut.put(newValue);
            } else {
                whereToPut.put(categoryValue);
            }
        }
    }

    public static boolean findDimension(final Crosstab<Number> crosstab, final String dimensionName) {
        try {
            final CrosstabDimension dimension = crosstab.getDimension(dimensionName);
            if (dimension == null) {
                return false;
            }
        } catch (final Exception e) {
            return false;
        }
        return true;
    }

    public static Number sum(final Number n1, final Number n2) {
        if (isIntegerType(n1) && isIntegerType(n2)) {
            return BigInteger.valueOf(n1.longValue()).add(BigInteger.valueOf(n2.longValue()));
        }
        return new BigDecimal(n1.doubleValue()).add(new BigDecimal(n2.doubleValue()));
    }

    public static Number subtract(final Number n1, final Number n2) {
        if (isIntegerType(n1) && isIntegerType(n2)) {
            return BigInteger.valueOf(n1.longValue()).subtract(BigInteger.valueOf(n2.longValue()));
        }
        return new BigDecimal(n1.doubleValue()).subtract(new BigDecimal(n2.doubleValue()));
    }

    private static boolean isIntegerType(final Number num) {
        return (num instanceof Byte || num instanceof Short || num instanceof Integer || num instanceof Long
                || num instanceof BigInteger);
    }
}
