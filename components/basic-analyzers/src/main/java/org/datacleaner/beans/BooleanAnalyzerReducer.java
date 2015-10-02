package org.datacleaner.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.util.CrosstabReducerHelper;

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
            CrosstabReducerHelper.createDimensions(columnStatisticCrosstabDimensions, partialColumnStatisticsCrosstab);
            CrosstabReducerHelper.createDimensions(columnValueCombinationCrosstabDimensions, partialValueCombinationCrosstab);
        }
        final Crosstab<Number> newResultColumnStatistics = new Crosstab<Number>(Number.class,
                columnStatisticCrosstabDimensions);
        final Crosstab<Number> newResultColumnValueCombination = new Crosstab<Number>(Number.class,
                columnValueCombinationCrosstabDimensions);

        // add the partial results
        for (BooleanAnalyzerResult partialResult : partialResults) {
            final Crosstab<Number> partialColumnStatisticsCrosstab = partialResult.getColumnStatisticsCrosstab();
            final Crosstab<Number> partialValueCombinationCrosstab = partialResult.getValueCombinationCrosstab();
            CrosstabReducerHelper.addData(newResultColumnStatistics, partialColumnStatisticsCrosstab);
            CrosstabReducerHelper.addData(newResultColumnValueCombination, partialValueCombinationCrosstab);
        }

        return new BooleanAnalyzerResult(newResultColumnStatistics, newResultColumnValueCombination);

    }

}
