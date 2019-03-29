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
import org.datacleaner.components.machinelearning.api.MLRegressionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MLRegressionRecordImpl implements MLRegressionRecord {

    private static final Logger logger = LoggerFactory.getLogger(MLRegressionRecordImpl.class);

    public static MLRegressionRecord forTraining(InputRow row, InputColumn<Number> regressionOutputColumn,
            InputColumn<?>[] featureColumns) {
        final Number regressionOutputValue = row.getValue(regressionOutputColumn);
        if (regressionOutputValue == null) {
            logger.warn("Encountered null regression output value, skipping row: {}", row);
            return null;
        }
        final List<Object> values = row.getValues(featureColumns);
        final MLRegressionRecord record = new MLRegressionRecordImpl(regressionOutputValue.doubleValue(),
                values.toArray(new Object[values.size()]));
        return record;
    }

    private final double regressionOutput;
    private final Object[] featureValues;

    private MLRegressionRecordImpl(double regressionOutput, Object[] recordValues) {
        this.regressionOutput = regressionOutput;
        this.featureValues = recordValues;
    }

    @Override
    public double getRegressionOutput() {
        return regressionOutput;
    }

    @Override
    public Object[] getRecordValues() {
        return featureValues;
    }
}
