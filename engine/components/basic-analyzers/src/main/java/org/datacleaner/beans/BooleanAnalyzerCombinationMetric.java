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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.datacleaner.api.ParameterizableMetric;
import org.datacleaner.beans.convert.ConvertToBooleanTransformer;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.CrosstabNavigator;

/**
 * Metric implementation for the "Combination count" metric in the
 * {@link BooleanAnalyzerResult}.
 */
final class BooleanAnalyzerCombinationMetric implements ParameterizableMetric {

    private final Crosstab<Number> _valueCombinationCrosstab;

    public BooleanAnalyzerCombinationMetric(Crosstab<Number> valueCombinationCrosstab) {
        _valueCombinationCrosstab = valueCombinationCrosstab;
    }

    @Override
    public Number getValue(final String parameter) {
        if (_valueCombinationCrosstab == null) {
            return 0;
        }
        final CrosstabDimension measureDimension = _valueCombinationCrosstab
                .getDimension(BooleanAnalyzer.DIMENSION_MEASURE);
        if (measureDimension.containsCategory(parameter)) {
            return _valueCombinationCrosstab.where(measureDimension, parameter)
                    .where(BooleanAnalyzer.DIMENSION_COLUMN, BooleanAnalyzer.VALUE_COMBINATION_COLUMN_FREQUENCY).get();
        }

        // attempt to parse the parameter as a comma-separated list of
        // false/true tokens
        final String[] tokens = parameter.split(",");
        final boolean[] bools = new boolean[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();
            if (!("true".equalsIgnoreCase(token) || "false".equalsIgnoreCase(token))) {
                // not parseable as a boolean
                return 0;
            }
            bools[i] = ConvertToBooleanTransformer.transformValue(token);
        }

        final List<String> columnCategories = _valueCombinationCrosstab.getDimension(BooleanAnalyzer.DIMENSION_COLUMN)
                .getCategories();
        if (bools.length != columnCategories.size() - 1) {
            // the number of columns should match the number of booleans in the
            // parameter
            return 0;
        }

        final List<String> measureCategories = _valueCombinationCrosstab
                .getDimension(BooleanAnalyzer.DIMENSION_MEASURE).getCategories();
        for (String category : measureCategories) {
            final CrosstabNavigator<Number> nav = _valueCombinationCrosstab.where(BooleanAnalyzer.DIMENSION_MEASURE,
                    category);
            boolean[] combination = new boolean[bools.length];
            int i = 0;
            for (String column : columnCategories) {
                if (!BooleanAnalyzer.VALUE_COMBINATION_COLUMN_FREQUENCY.equals(column)) {
                    Number number = nav.where(BooleanAnalyzer.DIMENSION_COLUMN, column).get();
                    if (Byte.valueOf((byte) 1).equals(number)) {
                        combination[i] = true;
                    } else {
                        combination[i] = false;
                    }
                    i++;
                }
            }

            if (Arrays.equals(combination, bools)) {
                Number number = nav.where(BooleanAnalyzer.DIMENSION_COLUMN,
                        BooleanAnalyzer.VALUE_COMBINATION_COLUMN_FREQUENCY).get();
                return number;
            }
        }

        return 0;
    }

    @Override
    public Collection<String> getParameterSuggestions() {
        if (_valueCombinationCrosstab == null) {
            return Collections.emptyList();
        }

        final List<String> suggestions = new ArrayList<String>();
        suggestions.add(BooleanAnalyzer.MEASURE_MOST_FREQUENT);
        suggestions.add(BooleanAnalyzer.MEASURE_LEAST_FREQUENT);

        // create suggestions for each combination of true and false
        final List<String> columnCategories = _valueCombinationCrosstab.getDimension(BooleanAnalyzer.DIMENSION_COLUMN)
                .getCategories();
        final List<String> measureCategories = _valueCombinationCrosstab
                .getDimension(BooleanAnalyzer.DIMENSION_MEASURE).getCategories();
        for (String category : measureCategories) {
            StringBuilder sb = new StringBuilder();
            CrosstabNavigator<Number> nav = _valueCombinationCrosstab
                    .where(BooleanAnalyzer.DIMENSION_MEASURE, category);
            for (String column : columnCategories) {
                if (!BooleanAnalyzer.VALUE_COMBINATION_COLUMN_FREQUENCY.equals(column)) {
                    if (sb.length() != 0) {
                        sb.append(',');
                    }
                    Number number = nav.where(BooleanAnalyzer.DIMENSION_COLUMN, column).get();
                    if (Byte.valueOf((byte) 1).equals(number)) {
                        sb.append("true");
                    } else {
                        sb.append("false");
                    }
                }
            }
            suggestions.add(sb.toString());
        }

        return suggestions;
    }
}
