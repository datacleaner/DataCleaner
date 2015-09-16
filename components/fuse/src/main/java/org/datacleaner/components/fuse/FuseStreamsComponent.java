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

@Named("Fuse streams")
@Description("Lets you combine multiple streams into one.\n\n"
        + "Use it to fuse data streams coming from different filter requirements, different output data streams or different source tables. "
        + "You can define new fields whose values represent whatever is available from one of the input streams.")
@Categorized(CompositionCategory.class)
public class FuseStreamsComponent extends MultiStreamComponent {

    private static final Logger logger = LoggerFactory.getLogger(FuseStreamsComponent.class);

    public static final String OUTPUT_DATA_STREAM_NAME = "output";
    public static final String PROPERTY_UNITS = "Units";

    @Configured
    InputColumn<?>[] _input;

    @Configured(PROPERTY_UNITS)
    CoalesceUnit[] _units;

    @Configured
    @Description("Consider empty strings (\"\") as null also?")
    boolean considerEmptyStringAsNull = true;

    private OutputRowCollector _outputRowCollector;
    private CoalesceFunction _coalesceFunction;

    public FuseStreamsComponent() {
    }

    public FuseStreamsComponent(CoalesceUnit... units) {
        this();
        this._units = units;
    }

    @Initialize
    public void init() {
        _coalesceFunction = new CoalesceFunction(considerEmptyStringAsNull);
    }

    /**
     * Configures the transformer using the coalesce units provided
     * 
     * @param units
     */
    public void configureUsingCoalesceUnits(CoalesceUnit... units) {
        final List<InputColumn<?>> input = new ArrayList<InputColumn<?>>();
        for (CoalesceUnit coalesceUnit : units) {
            final InputColumn<?>[] inputColumns = coalesceUnit.getInputColumns(null);
            for (final InputColumn<?> inputColumn : inputColumns) {
                input.add(inputColumn);
            }
        }

        _input = input.toArray(new InputColumn[input.size()]);
        _units = units;
    }

    @Override
    public void run(InputRow inputRow) {
        final Object[] output = new Object[_units.length];
        for (int i = 0; i < _units.length; i++) {
            final CoalesceUnit unit = _units[i];
            final InputColumn<?>[] inputColumns = unit.getInputColumns(_input);
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
        for (int i = 0; i < _units.length; i++) {
            final CoalesceUnit unit = _units[i];
            final Class<?> dataType = unit.getOutputDataType(_input);
            final String columnName = unit.getSuggestedOutputColumnName();
            final ColumnType columnType = ColumnTypeImpl.convertColumnType(dataType);
            builder.withColumn(columnName, columnType);
        }
        return new OutputDataStream[] { builder.toOutputDataStream() };
    }

    @Override
    public void initializeOutputDataStream(OutputDataStream outputDataStream, Query query,
            OutputRowCollector outputRowCollector) {
        _outputRowCollector = outputRowCollector;
    }

}