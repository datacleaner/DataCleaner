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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.datacleaner.beans.BooleanAnalyzer;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.CrosstabNavigator;

/**
 * Helper class for reductions of crosstabs
 *
 */
public class CrosstabReducerHelper {

    private static final Comparator<Map.Entry<ValueCombination<Number>, Number>> frequentValueCombinationComparator = new Comparator<Map.Entry<ValueCombination<Number>, Number>>() {
        @Override
        public int compare(Entry<ValueCombination<Number>, Number> o1, Entry<ValueCombination<Number>, Number> o2) {

            Number result = substract(o2.getValue(), o1.getValue());
            if (result.intValue() == 0) {
                result = o2.getKey().compareTo(o1.getKey());
            }
            return result.intValue();
        }
    };

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

    /**
     * 
     * @param crosstabDimensions
     * @param partialCrosstab
     * @throws IllegalStateException
     */
    public static void createDimensionsValueCombinationCrosstab(List<CrosstabDimension> crosstabDimensions,
            final Crosstab<Number> partialCrosstab) throws IllegalStateException {

        if (partialCrosstab != null) {
            final CrosstabDimension columnDimension = partialCrosstab.getDimension(BooleanAnalyzer.DIMENSION_COLUMN);

            if (crosstabDimensions.size() == 0) {
                crosstabDimensions.add(columnDimension);
                // the Value Combination crosstab gets an empty measure
                // dimension because the measure categories need to be recreated
                // based on the value combinations found
                final CrosstabDimension measureDimension = new CrosstabDimension(BooleanAnalyzer.DIMENSION_MEASURE);
                crosstabDimensions.add(measureDimension);
            } else {
                // trying to be smart
                if (!dimensionExits(crosstabDimensions, columnDimension)) {
                    throw new IllegalStateException("The crosstabs do not have the same categories in dimension Column");
                }
            }
        }
    }

    /**
     * Gather the sum of all possible value combinations of the partial
     * crosstabs
     * 
     * @param valueCombinations
     * @param partialCrosstab
     */
    public static void addValueCombinationsCrosstabDimension(Map<ValueCombination<Number>, Number> valueCombinations,
            final Crosstab<Number> partialCrosstab) {

        final CrosstabNavigator<Number> nav = new CrosstabNavigator<Number>(partialCrosstab);
        final CrosstabDimension columnDimension = partialCrosstab.getDimension(BooleanAnalyzer.DIMENSION_COLUMN);
        final CrosstabDimension measureDimension = partialCrosstab.getDimension(BooleanAnalyzer.DIMENSION_MEASURE);

        final List<String> columnDimCategories = columnDimension.getCategories();
        final List<String> measureCategories = measureDimension.getCategories();

        for (String measureCategory : measureCategories) {
            final Number[] values = new Number[columnDimCategories.size() - 1];
            for (int i = 0; i < columnDimCategories.size() - 1; i++) {
                nav.where(columnDimension, columnDimCategories.get(i));
                final CrosstabNavigator<Number> where = nav.where(measureDimension, measureCategory);
                final Number value = where.safeGet(null);
                if (!columnDimCategories.get(0).equals(BooleanAnalyzer.VALUE_COMBINATION_COLUMN_FREQUENCY)) {
                    values[i] = value;
                }
            }

            final CrosstabNavigator<Number> where = nav.where(columnDimension,
                    BooleanAnalyzer.VALUE_COMBINATION_COLUMN_FREQUENCY);
            final Number frequency = where.safeGet(null);
            final Number frequencyVal = frequency != null ? frequency : 0;
            final ValueCombination<Number> valueCombination = new ValueCombination<Number>(values);
            final Number combination = valueCombinations.get(valueCombination);
            if (combination == null) {
                valueCombinations.put(valueCombination, frequencyVal);
            } else {
                valueCombinations.replace(valueCombination, sum(combination, frequencyVal));
            }
        }
    }

    /**
     * Creates the measure dimension based on the sorted value combinations
     * 
     * @param valueCombinations
     * @param valueCombinationCrosstab
     */
    public static void createMeasureDimensionValueCombinationCrosstab(
            Map<ValueCombination<Number>, Number> valueCombinations, final Crosstab<Number> valueCombinationCrosstab) {

        if (findDimension(valueCombinationCrosstab, BooleanAnalyzer.DIMENSION_MEASURE)) {

            SortedSet<Entry<ValueCombination<Number>, Number>> entries = new TreeSet<Map.Entry<ValueCombination<Number>, Number>>(
                    frequentValueCombinationComparator);
            entries.addAll(valueCombinations.entrySet());

            final CrosstabNavigator<Number> nav = new CrosstabNavigator<Number>(valueCombinationCrosstab);
            final CrosstabDimension measureDimension = valueCombinationCrosstab
                    .getDimension(BooleanAnalyzer.DIMENSION_MEASURE);
            final CrosstabDimension columnDimension = valueCombinationCrosstab
                    .getDimension(BooleanAnalyzer.DIMENSION_COLUMN);
            final List<String> columnDimCategories = columnDimension.getCategories();
            int row = 0;
            for (Entry<ValueCombination<Number>, Number> entry : entries) {

                // create the category
                String measureName;
                if (row == 0) {
                    measureName = BooleanAnalyzer.MEASURE_MOST_FREQUENT;
                } else if (row == entries.size() - 1) {
                    measureName = BooleanAnalyzer.MEASURE_LEAST_FREQUENT;
                } else {
                    measureName = BooleanAnalyzer.DIMENSION_COMBINATION + row;
                }
                measureDimension.addCategory(measureName);

                // extract data
                final Number[] values = new Number[columnDimCategories.size()];
                final ValueCombination<Number> key = entry.getKey();
                for (int i = 0; i < key.getValueCount(); i++) {
                    values[i] = key.getValueAt(i);
                }
                values[columnDimCategories.size() - 1] = entry.getValue();

                // put data into crosstab
                for (int i = 0; i < columnDimCategories.size(); i++) {
                    nav.where(columnDimension, columnDimCategories.get(i));
                    nav.where(measureDimension, measureName);
                    nav.put(values[i]);
                }
                row++;
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
            final CrosstabDimension columnDimension = partialCrosstab.getDimension(BooleanAnalyzer.DIMENSION_COLUMN);
            final CrosstabDimension measureDimension = partialCrosstab.getDimension(BooleanAnalyzer.DIMENSION_MEASURE);

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

    private static boolean findDimension(final Crosstab<Number> crosstab, String dimensionName) {
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

    private static Number sum(Number n1, Number n2) {
        if (isIntegerType(n1) && isIntegerType(n2)) {
            return BigInteger.valueOf(n1.longValue()).add(BigInteger.valueOf(n2.longValue()));
        }
        return new BigDecimal(n1.doubleValue()).add(new BigDecimal(n2.doubleValue()));
    }

    private static Number substract(Number n1, Number n2) {
        if (isIntegerType(n1) && isIntegerType(n2)) {
            return BigInteger.valueOf(n1.longValue()).subtract(BigInteger.valueOf(n2.longValue()));
        }
        return new BigDecimal(n1.doubleValue()).subtract(new BigDecimal(n2.doubleValue()));
    }

    private static boolean isIntegerType(Number n) {
        return (n instanceof Byte || n instanceof Short || n instanceof Integer || n instanceof Long || n instanceof BigInteger);
    }
}
