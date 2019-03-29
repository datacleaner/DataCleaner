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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.datacleaner.components.machinelearning.api.MLClassificationRecord;
import org.datacleaner.components.machinelearning.api.MLFeatureModifier;
import org.datacleaner.components.machinelearning.api.MLRecord;
import org.datacleaner.components.machinelearning.api.MLRegressionRecord;
import org.datacleaner.components.machinelearning.api.MLTrainingConstraints;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public class MLFeatureUtils {

    public static List<Object> toClassifications(Iterable<MLClassificationRecord> data) {
        final Set<Object> set = new LinkedHashSet<>();
        for (MLClassificationRecord record : data) {
            set.add(record.getClassification());
        }
        return new ArrayList<Object>(set);
    }

    /**
     * Generates a matrix of feature values for each record.
     * 
     * @param data
     * @param featureModifiers
     * @return
     */
    public static double[][] toFeatureVector(Iterable<? extends MLRecord> data,
            List<MLFeatureModifier> featureModifiers) {
        final List<double[]> trainingInstances = new ArrayList<>();
        for (MLRecord record : data) {
            final double[] features = generateFeatureValues(record, featureModifiers);
            trainingInstances.add(features);
        }
        final double[][] x = trainingInstances.toArray(new double[trainingInstances.size()][]);
        return x;
    }

    /**
     * Generates a vector of classifications for each record.
     * 
     * @param data
     * @return
     */
    public static int[] toClassificationVector(Iterable<MLClassificationRecord> data) {
        final List<Integer> responseVariables = new ArrayList<>();
        final List<Object> classifications = new ArrayList<>();

        for (MLClassificationRecord record : data) {
            final Object classification = record.getClassification();
            int classificationIndex = classifications.indexOf(classification);
            if (classificationIndex == -1) {
                classifications.add(classification);
                classificationIndex = classifications.size() - 1;
            }
            responseVariables.add(classificationIndex);
        }

        final int[] y = responseVariables.stream().mapToInt(i -> i).toArray();
        return y;
    }

    /**
     * Generates a vector of regression outputs for every record
     * 
     * @param data
     * @return
     */
    public static double[] toRegressionOutputVector(Iterable<MLRegressionRecord> data) {
        final Stream<MLRegressionRecord> stream = StreamSupport.stream(data.spliterator(), false);
        return stream.mapToDouble(MLRegressionRecord::getRegressionOutput).toArray();
    }

    /**
     * Ensures that a feature value is in the valid range (from 0 to 1).
     * 
     * @param scaled
     * @return
     */
    public static double ensureFeatureInRange(double value) {
        return Math.max(0d, Math.min(1d, value));
    }

    public static double[] generateFeatureValues(MLRecord record, List<MLFeatureModifier> featureModifiers) {
        final Object[] recordValues = record.getRecordValues();
        assert featureModifiers.size() == recordValues.length;

        final double[] featureValues = new double[getFeatureCount(featureModifiers)];

        int offset = 0;
        for (int i = 0; i < recordValues.length; i++) {
            final Object value = recordValues[i];
            final MLFeatureModifier featureModifier = featureModifiers.get(i);
            final double[] vector = featureModifier.generateFeatureValues(value);
            System.arraycopy(vector, 0, featureValues, offset, vector.length);
            offset += vector.length;
        }
        return featureValues;
    }

    public static int getFeatureCount(Collection<MLFeatureModifier> featureModifiers) {
        return featureModifiers.stream().mapToInt(f -> f.getFeatureCount()).sum();
    }

    public static Set<String> sanitizeFeatureVectorSet(Multiset<String> values, MLTrainingConstraints constraints) {
        final Set<String> resultSet;

        final int maxFeatures = constraints.getMaxFeatures();
        if (maxFeatures > 0) {
            resultSet = new TreeSet<>();
            final Iterator<String> highestCountFirst = Multisets.copyHighestCountFirst(values).elementSet().iterator();
            // populate "resultSet" using "highestCountFirst"
            for (int i = 0; i < maxFeatures; i++) {
                if (highestCountFirst.hasNext()) {
                    final String value = highestCountFirst.next();
                    resultSet.add(value);
                }
            }
        } else {
            resultSet = new TreeSet<>(values.elementSet());
        }

        final boolean includeFeaturesForUniqueValues = constraints.isIncludeFeaturesForUniqueValues();
        if (!includeFeaturesForUniqueValues) {
            // remove uniques in "values" from "resultSet".
            for (Iterator<String> it = resultSet.iterator(); it.hasNext();) {
                final String value = it.next();
                if (values.count(value) == 1) {
                    it.remove();
                }
            }
        }

        // TODO
        System.out.println("Reduced " + values.size() + " multiset to " + resultSet.size() + " values");

        return resultSet;
    }
}
