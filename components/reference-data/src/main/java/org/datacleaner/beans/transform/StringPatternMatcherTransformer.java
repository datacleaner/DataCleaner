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
package org.datacleaner.beans.transform;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Close;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.ImproveSuperCategory;
import org.datacleaner.components.categories.ReferenceDataCategory;
import org.datacleaner.components.convert.ConvertToStringTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.StringPatternConnection;

@Named("String pattern matcher")
@Description("Matches string values against a set of string patterns, producing a corresponding set "
        + "of output columns specifying whether or not the values matched those string patterns")
@Categorized(superCategory = ImproveSuperCategory.class, value = ReferenceDataCategory.class)
public class StringPatternMatcherTransformer implements Transformer {

    @Configured
    StringPattern[] _stringPatterns;

    @Configured
    InputColumn<?> _column;

    @Configured
    MatchOutputType _outputType = MatchOutputType.TRUE_FALSE;

    @Provided
    DataCleanerConfiguration _configuration;

    private StringPatternConnection[] stringPatternConnections;

    public StringPatternMatcherTransformer(final InputColumn<?> column, final StringPattern[] stringPatterns,
            final DataCleanerConfiguration configuration) {
        this();
        _column = column;
        _stringPatterns = stringPatterns;
        _configuration = configuration;
    }

    public StringPatternMatcherTransformer() {
    }

    @Initialize
    public void init() {
        stringPatternConnections = new StringPatternConnection[_stringPatterns.length];
        for (int i = 0; i < _stringPatterns.length; i++) {
            stringPatternConnections[i] = _stringPatterns[i].openConnection(_configuration);
        }
    }

    @Close
    public void close() {
        if (stringPatternConnections != null) {
            for (final StringPatternConnection stringPatternConnection : stringPatternConnections) {
                stringPatternConnection.close();
            }
            stringPatternConnections = null;
        }
    }

    @Override
    public OutputColumns getOutputColumns() {
        final String columnName = _column.getName();
        final String[] names = new String[_stringPatterns.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = columnName + " '" + _stringPatterns[i].getName() + "'";
        }
        final Class<?>[] types = new Class[_stringPatterns.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = _outputType.getOutputClass();
        }
        return new OutputColumns(names, types);
    }

    @Override
    public Object[] transform(final InputRow inputRow) {
        final Object value = inputRow.getValue(_column);
        return doMatching(value);
    }

    public Object[] doMatching(final Object value) {
        final Object[] result = new Object[stringPatternConnections.length];
        final String stringValue = ConvertToStringTransformer.transformValue(value);

        for (int i = 0; i < result.length; i++) {
            final boolean matches = stringPatternConnections[i].matches(stringValue);
            if (_outputType == MatchOutputType.TRUE_FALSE) {
                result[i] = matches;
            } else if (_outputType == MatchOutputType.INPUT_OR_NULL) {
                if (matches) {
                    result[i] = stringValue;
                } else {
                    result[i] = null;
                }
            }
        }
        return result;
    }

    public void setStringPatterns(final StringPattern[] stringPatterns) {
        _stringPatterns = stringPatterns;
    }

    public void setColumn(final InputColumn<?> column) {
        _column = column;
    }
}
