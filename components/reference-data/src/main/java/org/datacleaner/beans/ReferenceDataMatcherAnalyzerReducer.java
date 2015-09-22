package org.datacleaner.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.CrosstabNavigator;

public class ReferenceDataMatcherAnalyzerReducer implements AnalyzerResultReducer<BooleanAnalyzerResult> {

    @Override
    public BooleanAnalyzerResult reduce(Collection<? extends BooleanAnalyzerResult> partialResults) {
        if (partialResults.isEmpty()) {
            return null;
        }

        final List<CrosstabDimension> columnStatisticCrosstabDimensions = new ArrayList<CrosstabDimension>();
        final List<CrosstabDimension> columnValueCombinationCrosstabDimensions = new ArrayList<CrosstabDimension>();

        for (BooleanAnalyzerResult partialResult : partialResults) {
            final Crosstab<Number> partialColumnStatisticsCrosstab = partialResult.getColumnStatisticsCrosstab();
            final Crosstab<Number> partialValueCombinationCrosstab = partialResult.getValueCombinationCrosstab();
            createDimensions(columnStatisticCrosstabDimensions, partialColumnStatisticsCrosstab);
            createDimensions(columnValueCombinationCrosstabDimensions, partialValueCombinationCrosstab);
        }

        final Crosstab<Number> newResultColumnStatistics = new Crosstab<Number>(Number.class,
                columnStatisticCrosstabDimensions);
        final Crosstab<Number> newResultColumnValueCombination = new Crosstab<Number>(Number.class,
                columnValueCombinationCrosstabDimensions);

        final CrosstabNavigator<Number> columnStatisticsNavigator = new CrosstabNavigator<Number>(
                newResultColumnStatistics);
        final CrosstabNavigator<Number> columnValueCombinationNavigator = new CrosstabNavigator<Number>(
                newResultColumnValueCombination);
        for (BooleanAnalyzerResult partialResult : partialResults) {
            final Crosstab<Number> partialColumnStatisticsCrosstab = partialResult.getColumnStatisticsCrosstab();
            final Crosstab<Number> partialValueCombinationCrosstab = partialResult.getValueCombinationCrosstab();
            addData(columnStatisticsNavigator, partialColumnStatisticsCrosstab);
            addData(columnValueCombinationNavigator, partialValueCombinationCrosstab);
        }

        return new BooleanAnalyzerResult(newResultColumnStatistics, newResultColumnValueCombination);
    }

    private void createDimensions(List<CrosstabDimension> crosstabDimensions,
            final Crosstab<Number> partialColumnCrosstab) {
        if (partialColumnCrosstab != null) {
            final List<CrosstabDimension> dimensions = partialColumnCrosstab.getDimensions();
            for (CrosstabDimension dimension : dimensions) {
                if (!dimensionExits(crosstabDimensions, dimension)) {
                    crosstabDimensions.add(dimension);
                }
            }
        }
    }

    private void addData(final CrosstabNavigator<Number> mainNavigator, final Crosstab<Number> partialCrosstab) {
        if (partialCrosstab != null) {
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
                            final Number newValue = oldValue.doubleValue() + categoryValue.doubleValue();
                            whereToPut.put(newValue);
                        } else {
                            whereToPut.put(categoryValue);
                        }
                    }
                }
            }
        }
    }

    private boolean dimensionExits(Collection<CrosstabDimension> list, CrosstabDimension dimension) {
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
}
