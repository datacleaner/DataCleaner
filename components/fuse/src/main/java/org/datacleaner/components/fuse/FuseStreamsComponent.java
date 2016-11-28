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
package org.datacleaner.components.fuse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;

import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.ColumnTypeImpl;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.MultiStreamComponent;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.components.categories.CompositionCategory;
import org.datacleaner.job.output.OutputDataStreamBuilder;
import org.datacleaner.job.output.OutputDataStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("Union")
@Description("Lets you combine multiple streams into one. Providing what is equivalent to a union of tables.\n\n"
        + "Use it to fuse data streams coming from different source tables. "
        + "You can define new fields whose values represent whatever is available from one of the input streams.")
@Categorized(CompositionCategory.class)
public class FuseStreamsComponent extends MultiStreamComponent {

    public static final String OUTPUT_DATA_STREAM_NAME = "output";
    public static final String PROPERTY_INPUTS = "Inputs";
    public static final String PROPERTY_UNITS = "Units";
    private static final Logger logger = LoggerFactory.getLogger(FuseStreamsComponent.class);
    @Configured(PROPERTY_INPUTS)
    InputColumn<?>[] _inputs;

    @Configured(PROPERTY_UNITS)
    CoalesceUnit[] _units;

    private OutputRowCollector _outputRowCollector;
    private CoalesceFunction _coalesceFunction;
    private CoalesceUnit[] _initializedUnits;

    public FuseStreamsComponent() {
    }

    public FuseStreamsComponent(final CoalesceUnit... units) {
        this();
        this._units = units;
    }

    @Initialize
    public void init() {
        _coalesceFunction = new CoalesceFunction(false);
        _initializedUnits = new CoalesceUnit[_units.length];
        for (int i = 0; i < _units.length; i++) {
            _initializedUnits[i] = _units[i].updateInputColumns(_inputs);
        }
    }

    /**
     * Configures the transformer using the coalesce units provided
     *
     * @param units
     */
    public void configureUsingCoalesceUnits(final CoalesceUnit... units) {
        final List<InputColumn<?>> input = new ArrayList<>();
        for (final CoalesceUnit coalesceUnit : units) {
            final InputColumn<?>[] inputColumns = coalesceUnit.getInputColumns();
            Collections.addAll(input, inputColumns);
        }

        _inputs = input.toArray(new InputColumn[input.size()]);
        _units = units;
        init();
    }

    @Override
    public void run(final InputRow inputRow) {
        final Object[] output = new Object[_initializedUnits.length];
        for (int i = 0; i < _initializedUnits.length; i++) {
            final CoalesceUnit unit = _initializedUnits[i];
            final InputColumn<?>[] inputColumns = unit.getInputColumns();
            final List<Object> values = inputRow.getValues(inputColumns);
            final Object value = _coalesceFunction.coalesce(values);
            output[i] = value;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Fused values for row: {}", Arrays.toString(output));
        }

        _outputRowCollector.putValues(output);
    }

    @Override
    public OutputDataStream[] getOutputDataStreams() {
        final OutputDataStreamBuilder builder = OutputDataStreams.pushDataStream(OUTPUT_DATA_STREAM_NAME);
        boolean foundOutputDataStream = false;
        for (final CoalesceUnit unit : _units) {
            // Not necessarily initialized yet, so no _initializedUnits available
            final InputColumn<?>[] updatedInputColumns = unit.getUpdatedInputColumns(_inputs, false);
            if (unit.getInputColumnNames().length == updatedInputColumns.length) {
                // Valid Unit
                foundOutputDataStream = true;
                final CoalesceUnit updatedCoalesceUnit = unit.getUpdatedCoalesceUnit(updatedInputColumns);
                final Class<?> dataType = updatedCoalesceUnit.getOutputDataType();
                final String columnName = updatedCoalesceUnit.getSuggestedOutputColumnName();
                final ColumnType columnType = ColumnTypeImpl.convertColumnType(dataType);
                builder.withColumn(columnName, columnType);
            } else {
                logger.info("Missing columns detected, skipping coalesce unit");
            }
        }
        if (!foundOutputDataStream) {
            return new OutputDataStream[0];
        }
        return new OutputDataStream[] { builder.toOutputDataStream() };
    }

    @Override
    public void initializeOutputDataStream(final OutputDataStream outputDataStream, final Query query,
            final OutputRowCollector outputRowCollector) {
        _outputRowCollector = outputRowCollector;
    }

}
