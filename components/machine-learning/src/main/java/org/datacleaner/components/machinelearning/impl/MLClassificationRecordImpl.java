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

import java.util.List;

import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.components.machinelearning.api.MLClassificationRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MLClassificationRecordImpl implements MLClassificationRecord {

    private static final Logger logger = LoggerFactory.getLogger(MLClassificationRecordImpl.class);

    public static MLClassificationRecord forEvaluation(InputRow row, InputColumn<?>[] featureColumns) {
        final List<Object> values = row.getValues(featureColumns);
        final MLClassificationRecord record = new MLClassificationRecordImpl(null, values.toArray(new Object[values
                .size()]));
        return record;
    }

    public static MLClassificationRecord forEvaluation(Object[] values) {
        final MLClassificationRecord record = new MLClassificationRecordImpl(null, values);
        return record;
    }

    public static MLClassificationRecord forTraining(InputRow row, InputColumn<?> classification,
            InputColumn<?>[] featureColumns) {
        final Object classificationValue = row.getValue(classification);
        if (classificationValue == null) {
            logger.warn("Encountered null classification value, skipping row: {}", row);
            return null;
        }
        final List<Object> values = row.getValues(featureColumns);
        final MLClassificationRecord record = new MLClassificationRecordImpl(classificationValue, values.toArray(
                new Object[values.size()]));
        return record;
    }

    private final Object classification;
    private final Object[] featureValues;

    private MLClassificationRecordImpl(Object classification, Object[] recordValues) {
        this.classification = classification;
        this.featureValues = recordValues;
    }

    @Override
    public Object getClassification() {
        return classification;
    }

    @Override
    public Object[] getRecordValues() {
        return featureValues;
    }
}
