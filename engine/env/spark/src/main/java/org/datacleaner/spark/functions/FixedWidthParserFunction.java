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
package org.datacleaner.spark.functions;

import org.apache.metamodel.fixedwidth.FixedWidthConfiguration;
import org.apache.metamodel.fixedwidth.FixedWidthLineParser;
import org.apache.spark.api.java.function.Function;

public class FixedWidthParserFunction implements Function<String, Object[]> {

    private static final long serialVersionUID = 1L;
    private final FixedWidthConfiguration _fixedWidthConfiguration;
    private final int _expectedLineLength;

    public FixedWidthParserFunction(final FixedWidthConfiguration fixedWidthConfiguration) {
        _fixedWidthConfiguration = fixedWidthConfiguration;
        int expectedLineLength = 0;
        if (_fixedWidthConfiguration.getFixedValueWidth() == -1) {
            for (int i = 0; i < _fixedWidthConfiguration.getValueWidths().length; i++) {
                expectedLineLength += _fixedWidthConfiguration.getValueWidth(i);
            }
        }
        _expectedLineLength = expectedLineLength;

    }

    @Override
    public Object[] call(final String line) throws Exception {
        final FixedWidthLineParser fixedWidthParser =
                new FixedWidthLineParser(_fixedWidthConfiguration, _expectedLineLength, 0);
        return fixedWidthParser.parseLine(line);
    }
}
