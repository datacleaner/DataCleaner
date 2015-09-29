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
    public static void createDimensions(List<CrosstabDimension> crosstabDimensions,
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

    private static boolean dimensionExits(Collection<CrosstabDimension> list, CrosstabDimension dimension) {
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
    public static void addData(final Crosstab<Number> mainCrosstab, final Crosstab<Number> partialCrosstab) {
        if (partialCrosstab != null) {

            final CrosstabNavigator<Number> mainNavigator = new CrosstabNavigator<Number>(mainCrosstab);
            final CrosstabNavigator<Number> nav = new CrosstabNavigator<Number>(partialCrosstab);
            final List<CrosstabDimension> dimensions = partialCrosstab.getDimensions();
            for (CrosstabDimension dimension : dimensions) {
                final List<String> categories = dimension.getCategories();
                for (String category : categories) {
                    final Number categoryValue = nav.where(dimension, category).safeGet(null);
                    final CrosstabNavigator<Number> whereToPut = mainNavigator.where(dimension, category);
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
            }
        }
    }

    private static Number sum(Number n1, Number n2) {
        if (isIntegerType(n1) && isIntegerType(n2)) {
            return BigInteger.valueOf(n1.longValue()).add(BigInteger.valueOf(n2.longValue()));
        } 
        return new BigDecimal(n1.doubleValue()).add(new BigDecimal(n2.doubleValue()));
    }

    private static boolean isIntegerType(Number n) {
        return (n instanceof Byte || n instanceof Short || n instanceof Integer || n instanceof Long || n instanceof BigInteger);
    }
}
