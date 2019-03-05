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
package org.datacleaner.components.machinelearning;

import java.util.List;
import java.util.stream.Collectors;

import org.datacleaner.components.machinelearning.api.MLClassification;
import org.datacleaner.components.machinelearning.api.MLClassificationMetadata;
import org.datacleaner.components.machinelearning.api.MLClassificationRecord;
import org.datacleaner.components.machinelearning.api.MLClassifier;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.CrosstabNavigator;

public class MLConfusionMatrixBuilder {

    private final MLClassifier classifier;
    private final Crosstab<Integer> crosstab;
    private final CrosstabDimension expectedDimension;
    private final CrosstabDimension actualDimension;

    public MLConfusionMatrixBuilder(MLClassifier classifier) {
        this.classifier = classifier;
        this.crosstab = new Crosstab<>(Integer.class, "Expected", "Actual");

        this.expectedDimension = crosstab.getDimension(0);
        this.actualDimension = crosstab.getDimension(1);
        final List<String> classificationLabels = classifier.getMetadata().getClassifications().stream()
                .map(this::getClassificationLabel).collect(Collectors.toList());
        this.expectedDimension.addCategories(classificationLabels);
        this.actualDimension.addCategories(classificationLabels);

        // set all counts to 0
        for (String label1 : classificationLabels) {
            final CrosstabNavigator<Integer> nav = crosstab.where(expectedDimension, label1);
            for (String label2 : classificationLabels) {
                nav.where(actualDimension, label2).put(0);
            }
        }
    }

    public void append(MLClassificationRecord record) {
        final MLClassificationMetadata metadata = classifier.getMetadata();

        final MLClassification result = classifier.classify(record);
        final String actual = getClassificationLabel(metadata.getClassification(result.getBestClassificationIndex()));
        final String expected = getClassificationLabel(record.getClassification());

        final CrosstabNavigator<Integer> crosstabPath =
                crosstab.navigate().where(expectedDimension, expected).where(actualDimension, actual);
        final Integer valueBefore = crosstabPath.get();
        if (valueBefore == null) {
            crosstabPath.put(1);
        } else {
            crosstabPath.put(valueBefore.intValue() + 1);
        }
    }

    private String getClassificationLabel(Object classification) {
        return classification.toString();
    }

    public Crosstab<Integer> build() {
        return crosstab;
    }
}
