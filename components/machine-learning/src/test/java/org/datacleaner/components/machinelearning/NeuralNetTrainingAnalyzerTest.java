package org.datacleaner.components.machinelearning;

import org.junit.Test;

public class NeuralNetTrainingAnalyzerTest {

    private final TrainingAnalyzerTestHelper testHelper = new TrainingAnalyzerTestHelper();

    @Test
    public void testScenario() {
        final MLTrainingAnalyzer analyzer = new NeuralNetTrainingAnalyzer();
        testHelper.runScenario(analyzer);
    }
}
