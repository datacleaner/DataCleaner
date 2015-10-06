package org.datacleaner.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.util.CrosstabReducerHelper;
import org.datacleaner.util.ValueCombination;

public class BooleanAnalyzerReducer implements AnalyzerResultReducer<BooleanAnalyzerResult> {

    @Override
    public BooleanAnalyzerResult reduce(Collection<? extends BooleanAnalyzerResult> partialResults) {
        if (partialResults.isEmpty()) {
            return null;
        }

        // Create the dimensions
        final List<CrosstabDimension> columnStatisticCrosstabDimensions = new ArrayList<CrosstabDimension>();
        final List<CrosstabDimension> columnValueCombinationCrosstabDimensions = new ArrayList<CrosstabDimension>();
        for (BooleanAnalyzerResult partialResult : partialResults) {
            final Crosstab<Number> partialColumnStatisticsCrosstab = partialResult.getColumnStatisticsCrosstab();
            final Crosstab<Number> partialValueCombinationCrosstab = partialResult.getValueCombinationCrosstab();
            CrosstabReducerHelper.createDimensionsColumnCrosstab(columnStatisticCrosstabDimensions,
                    partialColumnStatisticsCrosstab);
            CrosstabReducerHelper.createDimensionsValueCombinationCrosstab(
                    columnValueCombinationCrosstabDimensions, partialValueCombinationCrosstab);
        }
        final Crosstab<Number> newResultColumnStatistics = new Crosstab<Number>(Number.class,
                columnStatisticCrosstabDimensions);
        final Crosstab<Number> newResultColumnValueCombination = new Crosstab<Number>(Number.class,
                columnValueCombinationCrosstabDimensions);

        final Map<ValueCombination<Number>, Number> valueCombinations = new HashMap<ValueCombination<Number>, Number>();
        // add the partial results
        for (BooleanAnalyzerResult partialResult : partialResults) {
            final Crosstab<Number> partialColumnStatisticsCrosstab = partialResult.getColumnStatisticsCrosstab();
            final Crosstab<Number> partialValueCombinationCrosstab = partialResult.getValueCombinationCrosstab();
            if (partialColumnStatisticsCrosstab != null) {
                CrosstabReducerHelper.addData(newResultColumnStatistics, partialColumnStatisticsCrosstab);
            }
            // gather the sum of all possible value combinations found in the
            // partial crosstabs
            if (partialValueCombinationCrosstab != null) {
                CrosstabReducerHelper.addValueCombinationsCrosstabDimension(valueCombinations,
                        partialValueCombinationCrosstab);
            }
        }
        // create a new measure dimension for Value Combination crosstab
        if (valueCombinations != null && columnValueCombinationCrosstabDimensions != null) {
            CrosstabReducerHelper.createMeasureDimensionValueCombinationCrosstab(valueCombinations,
                    newResultColumnValueCombination);
        }

        return new BooleanAnalyzerResult(newResultColumnStatistics, newResultColumnValueCombination);

    }

}
