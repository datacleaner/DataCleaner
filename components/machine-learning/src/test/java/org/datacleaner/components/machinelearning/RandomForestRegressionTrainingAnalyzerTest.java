package org.datacleaner.components.machinelearning;

import org.junit.Test;

public class RandomForestRegressionTrainingAnalyzerTest {

    private final TrainingAnalyzerTestHelper testHelper = new TrainingAnalyzerTestHelper();

    @Test
    public void testScenario() {
        final MLRegressionTrainingAnalyzer analyzer = new RandomForestRegressionTrainingAnalyzer();
        testHelper.runScenario(analyzer);
    }
}
