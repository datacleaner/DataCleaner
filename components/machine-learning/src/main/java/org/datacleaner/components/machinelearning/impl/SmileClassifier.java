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
package org.datacleaner.components.machinelearning.impl;

import org.apache.metamodel.util.SerializableRef;
import org.datacleaner.components.machinelearning.api.MLClassification;
import org.datacleaner.components.machinelearning.api.MLClassificationMetadata;

import smile.classification.Classifier;
import smile.classification.SoftClassifier;

public class SmileClassifier extends AbstractClassifier {

    private static final long serialVersionUID = 1L;
    
    private final SerializableRef<Classifier<double[]>> smileClassifierRef;

    public SmileClassifier(final Classifier<double[]> smileClassifier,
            MLClassificationMetadata classificationMetadata) {
        super(classificationMetadata);
        this.smileClassifierRef = new SerializableRef<>(smileClassifier);
    }

    @Override
    public MLClassification classify(double[] featureValues) {
        final Classifier<double[]> classifier = smileClassifierRef.get();
        if (classifier instanceof SoftClassifier) {
            final SoftClassifier<double[]> softClassifier = (SoftClassifier<double[]>) classifier;

            final double[] posteriori = new double[getMetadata().getClassCount()];
            softClassifier.predict(featureValues, posteriori);
            return new MLConfidenceClassification(posteriori);
        }

        final int prediction = classifier.predict(featureValues);
        return new MLSimpleClassification(prediction);
    }
}
