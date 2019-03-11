package org.datacleaner.components.machinelearning.api;

import java.io.Serializable;

public class MLTrainingOptions implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int maxFeatures;
    private final boolean includeFeaturesForUniqueValues;

    public MLTrainingOptions(int maxFeatures, boolean includeFeaturesForUniqueValues) {
        this.maxFeatures = maxFeatures;
        this.includeFeaturesForUniqueValues = includeFeaturesForUniqueValues;
    }

    public int getMaxFeatures() {
        return maxFeatures;
    }

    public boolean isIncludeFeaturesForUniqueValues() {
        return includeFeaturesForUniqueValues;
    }

}
