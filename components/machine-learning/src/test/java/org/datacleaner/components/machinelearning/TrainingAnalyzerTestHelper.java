/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.datacleaner.api.InputColumn;
import org.datacleaner.components.machinelearning.api.MLClassificationMetadata;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierType;
import org.datacleaner.components.machinelearning.impl.MLClassificationRecordImpl;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;

public class TrainingAnalyzerTestHelper {

    private static final String[] RANDOM_WORDS = ("Lorem ipsum dolor sit amet, consectetur "
            + "adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna "
            + "aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi "
            + "ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in "
            + "voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint "
            + "occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
                    .split(" ");

    private static final String RED = "red";
    private static final String GREEN = "green";
    private static final String BLUE = "blue";
    private final Random random = new Random();

    public void runScenario(MLClassificationTrainingAnalyzer analyzer) {
        final MockInputColumn<String> color = new MockInputColumn<>("color");
        final MockInputColumn<String> desc = new MockInputColumn<>("Description");
        final MockInputColumn<Double> red = new MockInputColumn<>(RED);
        final MockInputColumn<Double> green = new MockInputColumn<>(GREEN);
        final MockInputColumn<Double> blue = new MockInputColumn<>(BLUE);

        analyzer.classification = color;
        analyzer.featureColumns = new InputColumn[] { desc, red, green, blue };
        analyzer.featureModifierTypes =
                new MLFeatureModifierType[] { MLFeatureModifierType.VECTOR_3_GRAM, MLFeatureModifierType.SCALED_MIN_MAX,
                        MLFeatureModifierType.SCALED_MIN_MAX, MLFeatureModifierType.SCALED_MIN_MAX };
        analyzer.init();

        for (int i = 0; i < 1000; i++) {
            analyzer.run(new MockInputRow().put(desc, randomDesc()).put(color, RED).put(red, randomHigh())
                    .put(green, randomLow()).put(blue, randomLow()), 1);
            analyzer.run(new MockInputRow().put(desc, randomDesc()).put(color, BLUE).put(red, randomLow())
                    .put(green, randomLow()).put(blue, randomHigh()), 1);
            analyzer.run(new MockInputRow().put(desc, randomDesc()).put(color, GREEN).put(red, randomLow())
                    .put(green, randomHigh()).put(blue, randomLow()), 1);
        }

        final MLClassificationAnalyzerResult result = analyzer.getResult();
        final MLClassificationMetadata metadata = result.getTrainedClassifier().getMetadata();

        assertEquals(RED,
                metadata.getClassification(result.getTrainedClassifier()
                        .classify(MLClassificationRecordImpl.forEvaluation(new Object[] { randomDesc(), 255, 0, 0 }))
                        .getBestClassificationIndex()));
        assertEquals(GREEN,
                metadata.getClassification(result.getTrainedClassifier()
                        .classify(MLClassificationRecordImpl.forEvaluation(new Object[] { randomDesc(), 0, 255, 0 }))
                        .getBestClassificationIndex()));
        assertEquals(BLUE,
                metadata.getClassification(result.getTrainedClassifier()
                        .classify(MLClassificationRecordImpl.forEvaluation(new Object[] { randomDesc(), 0, 0, 255 }))
                        .getBestClassificationIndex()));

    }

    private String randomDesc() {
        final String randomWord1 = RANDOM_WORDS[random.nextInt(RANDOM_WORDS.length)];
        final String randomWord2 = RANDOM_WORDS[random.nextInt(RANDOM_WORDS.length)];
        return randomWord1 + ' ' + randomWord2;
    }

    private double randomLow() {
        return random.nextInt(255) / 3d;
    }

    private double randomHigh() {
        return 255 * Math.max(0.6, random.nextDouble());
    }
}
