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
package org.datacleaner.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.CrosstabNavigator;
import org.datacleaner.util.CrosstabReducerHelper;
import org.datacleaner.util.ValueCombination;

public class BooleanAnalyzerReducer implements AnalyzerResultReducer<BooleanAnalyzerResult> {

    private static final Comparator<Map.Entry<ValueCombination<Number>, Number>> frequentValueCombinationComparator =
            (o1, o2) -> {

                Number result = CrosstabReducerHelper.subtract(o2.getValue(), o1.getValue());
                if (result.intValue() == 0) {
                    result = o2.getKey().compareTo(o1.getKey());
                }
                return result.intValue();
            };

    @Override
    public BooleanAnalyzerResult reduce(final Collection<? extends BooleanAnalyzerResult> partialResults) {
        if (partialResults.isEmpty()) {
            return null;
        }

        // Create the dimensions
        final List<CrosstabDimension> columnStatisticCrosstabDimensions = new ArrayList<>();
        final List<CrosstabDimension> columnValueCombinationCrosstabDimensions = new ArrayList<>();
        for (final BooleanAnalyzerResult partialResult : partialResults) {
            final Crosstab<Number> partialColumnStatisticsCrosstab = partialResult.getColumnStatisticsCrosstab();
            final Crosstab<Number> partialValueCombinationCrosstab = partialResult.getValueCombinationCrosstab();
            CrosstabReducerHelper
                    .createDimensionsColumnCrosstab(columnStatisticCrosstabDimensions, partialColumnStatisticsCrosstab);
            createDimensionsValueCombinationCrosstab(columnValueCombinationCrosstabDimensions,
                    partialValueCombinationCrosstab);
        }
        final Crosstab<Number> newResultColumnStatistics =
                new Crosstab<>(Number.class, columnStatisticCrosstabDimensions);
        final Crosstab<Number> newResultColumnValueCombination =
                new Crosstab<>(Number.class, columnValueCombinationCrosstabDimensions);

        final Map<ValueCombination<Number>, Number> valueCombinations = new HashMap<>();

        // add the partial results
        for (final BooleanAnalyzerResult partialResult : partialResults) {
            final Crosstab<Number> partialColumnStatisticsCrosstab = partialResult.getColumnStatisticsCrosstab();
            final Crosstab<Number> partialValueCombinationCrosstab = partialResult.getValueCombinationCrosstab();
            if (partialColumnStatisticsCrosstab != null) {
                final CrosstabDimension columnDimension =
                        partialColumnStatisticsCrosstab.getDimension(BooleanAnalyzer.DIMENSION_COLUMN);
                final CrosstabDimension measureDimension =
                        partialColumnStatisticsCrosstab.getDimension(BooleanAnalyzer.DIMENSION_MEASURE);
                CrosstabReducerHelper
                        .addData(newResultColumnStatistics, partialColumnStatisticsCrosstab, columnDimension,
                                measureDimension);
            }
            // gather the sum of all possible value combinations found in the
            // partial crosstabs
            if (partialValueCombinationCrosstab != null) {
                addValueCombinationsCrosstabDimension(valueCombinations, partialValueCombinationCrosstab);
            }
        }
        // create a new measure dimension for Value Combination crosstab
        createMeasureDimensionValueCombinationCrosstab(valueCombinations, newResultColumnValueCombination);

        return new BooleanAnalyzerResult(newResultColumnStatistics, newResultColumnValueCombination);

    }

    /**
     * Creates the measure dimension based on the sorted value combinations
     *
     * @param valueCombinations
     * @param valueCombinationCrosstab
     */
    public void createMeasureDimensionValueCombinationCrosstab(
            final Map<ValueCombination<Number>, Number> valueCombinations,
            final Crosstab<Number> valueCombinationCrosstab) {

        if (CrosstabReducerHelper.findDimension(valueCombinationCrosstab, BooleanAnalyzer.DIMENSION_MEASURE)) {

            final SortedSet<Entry<ValueCombination<Number>, Number>> entries =
                    new TreeSet<>(frequentValueCombinationComparator);
            entries.addAll(valueCombinations.entrySet());

            final CrosstabNavigator<Number> nav = new CrosstabNavigator<>(valueCombinationCrosstab);
            final CrosstabDimension measureDimension =
                    valueCombinationCrosstab.getDimension(BooleanAnalyzer.DIMENSION_MEASURE);
            final CrosstabDimension columnDimension =
                    valueCombinationCrosstab.getDimension(BooleanAnalyzer.DIMENSION_COLUMN);
            final List<String> columnDimCategories = columnDimension.getCategories();
            int row = 0;
            for (final Entry<ValueCombination<Number>, Number> entry : entries) {

                // create the category
                final String measureName;
                if (row == 0) {
                    measureName = BooleanAnalyzer.MEASURE_MOST_FREQUENT;
                } else if (row == entries.size() - 1) {
                    measureName = BooleanAnalyzer.MEASURE_LEAST_FREQUENT;
                } else {
                    measureName = BooleanAnalyzer.DIMENSION_COMBINATION_PREFIX + row;
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

    /**
     * Gather the sum of all possible value combinations of the partial
     * crosstabs
     *
     * @param valueCombMapList
     * @param partialCrosstab
     */
    public void addValueCombinationsCrosstabDimension(final Map<ValueCombination<Number>, Number> valueCombMapList,
            final Crosstab<Number> partialCrosstab) {

        final CrosstabNavigator<Number> nav = new CrosstabNavigator<>(partialCrosstab);
        final CrosstabDimension columnDimension = partialCrosstab.getDimension(BooleanAnalyzer.DIMENSION_COLUMN);
        final CrosstabDimension measureDimension = partialCrosstab.getDimension(BooleanAnalyzer.DIMENSION_MEASURE);

        final List<String> columnDimCategories = columnDimension.getCategories();
        final List<String> measureCategories = measureDimension.getCategories();

        for (final String measureCategory : measureCategories) {
            final Number[] values = new Number[columnDimCategories.size() - 1];
            for (int i = 0; i < columnDimCategories.size() - 1; i++) {
                nav.where(columnDimension, columnDimCategories.get(i));
                final CrosstabNavigator<Number> where = nav.where(measureDimension, measureCategory);
                final Number value = where.safeGet(null);
                if (!columnDimCategories.get(0).equals(BooleanAnalyzer.VALUE_COMBINATION_COLUMN_FREQUENCY)) {
                    values[i] = value;
                }
            }

            final CrosstabNavigator<Number> where =
                    nav.where(columnDimension, BooleanAnalyzer.VALUE_COMBINATION_COLUMN_FREQUENCY);
            final Number frequency = where.safeGet(null);
            final Number frequencyVal = frequency != null ? frequency : 0;
            final ValueCombination<Number> valComb = new ValueCombination<>(values);
            final Number combination = valueCombMapList.get(valComb);
            if (combination == null) {
                valueCombMapList.put(valComb, frequencyVal);
            } else {
                final Number newValue = CrosstabReducerHelper.sum(combination, frequencyVal);
                valueCombMapList.put(valComb, newValue);
            }
        }
    }

    /**
     *
     * @param crosstabDimensions
     * @param partialCrosstab
     * @throws IllegalStateException
     */
    public void createDimensionsValueCombinationCrosstab(final List<CrosstabDimension> crosstabDimensions,
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
                if (!CrosstabReducerHelper.dimensionExits(crosstabDimensions, columnDimension)) {
                    throw new IllegalStateException(
                            "The crosstabs do not have the same categories in dimension Column");
                }
            }
        }
    }

}
