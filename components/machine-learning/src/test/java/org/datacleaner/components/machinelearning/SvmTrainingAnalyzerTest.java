package org.datacleaner.components.machinelearning;

import org.junit.Test;

public class SvmTrainingAnalyzerTest {

    private final TrainingAnalyzerTestHelper testHelper = new TrainingAnalyzerTestHelper();

    @Test
    public void testScenario() {
        final MLTrainingAnalyzer analyzer = new SvmTrainingAnalyzer();
        testHelper.runScenario(analyzer);
    }
}
