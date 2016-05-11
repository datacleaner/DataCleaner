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

import org.datacleaner.api.Alias;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.api.Validate;
import org.datacleaner.components.categories.CompositionCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("Fuse / Coalesce fields")
@Alias("Coalesce multiple fields")
@Description("Lets you combine multiple fields into one, selecting the first value that is non-null.\n\n"
        + "Use it to fuse data streams coming from different filter requirements. You can define new fields whose values represent whatever is available from one of the input streams.\n\n"
        + "Or use it to identify the most accurate or most recent observation, if multiple entries have been recorded in separate columns.")
@Categorized(CompositionCategory.class)
public class CoalesceMultipleFieldsTransformer implements Transformer {
    private static final Logger logger = LoggerFactory.getLogger(CoalesceMultipleFieldsTransformer.class);
    public static final String PROPERTY_UNITS = "Units";
    
    @Configured
    InputColumn<?>[] _input;

    @Configured(value = PROPERTY_UNITS)
    CoalesceUnit[] _units;

    @Configured
    @Description("Consider empty strings (\"\") as null also?")
    boolean considerEmptyStringAsNull = true;

    private CoalesceFunction _coalesceFunction;

    private CoalesceUnit[] _initializedUnits;

    public CoalesceMultipleFieldsTransformer() {
    }

    public CoalesceMultipleFieldsTransformer(CoalesceUnit... units) {
        this();
        this._units = units;
    }

    @Initialize
    public void init() {
        _coalesceFunction = new CoalesceFunction(considerEmptyStringAsNull);
        _initializedUnits = new CoalesceUnit[_units.length];
        for (int i = 0; i < _units.length; i++) {
            _initializedUnits[i] = _units[i].updateInputColumns(_input);
        }
    }

    @Validate
    public void validate() {
        for (final CoalesceUnit _unit : _units) {
            // Ensure that initialization is actually possible.
            _unit.updateInputColumns(_input);
        }
    }

    /**
     * Configures the transformer using the coalesce units provided
     * 
     * @param units
     */
    public void configureUsingCoalesceUnits(CoalesceUnit... units) {
        final List<InputColumn<?>> input = new ArrayList<>();
        for (CoalesceUnit coalesceUnit : units) {
            final InputColumn<?>[] inputColumns = coalesceUnit.getInputColumns();
            Collections.addAll(input, inputColumns);
        }

        _input = input.toArray(new InputColumn[input.size()]);
        _units = units;
    }

    @Override
    public OutputColumns getOutputColumns() {
        final OutputColumns outputColumns = new OutputColumns(_units.length, Object.class);
        for (int i = 0; i < _units.length; i++) {
            // Not necessarily initialized yet, so no _initializedUnits available
            final CoalesceUnit unit = _units[i].updateInputColumns(_input);
            final Class<?> dataType = unit.getOutputDataType();
            outputColumns.setColumnType(i, dataType);
        }
        return outputColumns;
    }

    @Override
    public Object[] transform(InputRow inputRow) {
        final Object[] result = new Object[_initializedUnits.length];
        for (int i = 0; i < _initializedUnits.length; i++) {
            final CoalesceUnit unit = _initializedUnits[i];
            final InputColumn<?>[] inputColumns = unit.getInputColumns();
            final List<Object> values = inputRow.getValues(inputColumns);
            final Object value = _coalesceFunction.coalesce(values);
            result[i] = value;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Coalesced values for row {}: {}", inputRow.getId(), Arrays.toString(result));
        }
        return result;
    }
}