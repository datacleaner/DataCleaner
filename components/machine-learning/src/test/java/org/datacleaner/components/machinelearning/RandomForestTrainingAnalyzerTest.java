package org.datacleaner.components.machinelearning;

import org.junit.Test;

public class RandomForestTrainingAnalyzerTest {

    private final TrainingAnalyzerTestHelper testHelper = new TrainingAnalyzerTestHelper();

    @Test
    public void testScenario() {
        final MLTrainingAnalyzer analyzer = new RandomForestTrainingAnalyzer();
        testHelper.runScenario(analyzer);
    }
}
