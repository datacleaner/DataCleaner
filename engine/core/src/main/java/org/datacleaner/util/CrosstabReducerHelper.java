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
    public static void createDimensionsColumnCrosstab(List<CrosstabDimension> crosstabDimensions,
            final Crosstab<Number> partialCrosstab) {
        if (partialCrosstab != null) {
            final List<CrosstabDimension> dimensions = partialCrosstab.getDimensions();
            for (CrosstabDimension dimension : dimensions) {
                if (!dimensionExits(crosstabDimensions, dimension)) {
                    crosstabDimensions.add(dimension);
                }
            }
        }
    }

    public static boolean dimensionExits(Collection<CrosstabDimension> list, CrosstabDimension dimension) {
        if (list.size() > 0) {
            boolean allreadyExits = false;
            for (CrosstabDimension dim : list) {
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
            CrosstabDimension columnDimension, CrosstabDimension measureDimension) {
        if (partialCrosstab != null) {

            final CrosstabNavigator<Number> mainNavigator = new CrosstabNavigator<Number>(mainCrosstab);
            final CrosstabNavigator<Number> nav = new CrosstabNavigator<Number>(partialCrosstab);

            for (String columnCategory : columnDimension.getCategories()) {
                // just navigate through the dimensions because is the column
                // dimension
                nav.where(columnDimension, columnCategory);
                mainNavigator.where(columnDimension, columnCategory);
                // navigate and sum up data
                final List<String> categories = measureDimension.getCategories();
                for (String measureCategory : categories) {
                    sumUpData(mainNavigator, nav, measureDimension, measureCategory);
                }
            }
        }
    }

    private static void sumUpData(final CrosstabNavigator<Number> mainNavigator, final CrosstabNavigator<Number> nav,
            CrosstabDimension dimension, String category) {
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

    public static boolean findDimension(final Crosstab<Number> crosstab, String dimensionName) {
        try {
            final CrosstabDimension dimension = crosstab.getDimension(dimensionName);
            if (dimension == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static Number sum(Number n1, Number n2) {
        if (isIntegerType(n1) && isIntegerType(n2)) {
            return BigInteger.valueOf(n1.longValue()).add(BigInteger.valueOf(n2.longValue()));
        }
        return new BigDecimal(n1.doubleValue()).add(new BigDecimal(n2.doubleValue()));
    }

    public static Number substract(Number n1, Number n2) {
        if (isIntegerType(n1) && isIntegerType(n2)) {
            return BigInteger.valueOf(n1.longValue()).subtract(BigInteger.valueOf(n2.longValue()));
        }
        return new BigDecimal(n1.doubleValue()).subtract(new BigDecimal(n2.doubleValue()));
    }

    private static boolean isIntegerType(Number n) {
        return (n instanceof Byte || n instanceof Short || n instanceof Integer || n instanceof Long || n instanceof BigInteger);
    }
}
