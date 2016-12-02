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

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.spark.api.java.function.Function;

import au.com.bytecode.opencsv.CSVParser;

public final class CsvParserFunction implements Function<String, Object[]> {

    private static final long serialVersionUID = 1L;

    private final CsvConfiguration _csvConfiguration;

    public CsvParserFunction(final CsvConfiguration csvConfiguration) {
        if (csvConfiguration.isMultilineValues()) {
            throw new IllegalStateException("Multiline CSV files are not supported");
        }

        final String encoding = csvConfiguration.getEncoding();
        switch (encoding.toUpperCase()) {
        case "UTF-8":
        case "UTF8":
            // supported
            break;
        default:
            throw new IllegalStateException(
                    "Unsupported encoding: '" + encoding + "'. CSV files on Hadoop must be UTF-8 encoded.");
        }

        _csvConfiguration = csvConfiguration;
    }

    @Override
    public Object[] call(final String csvLine) throws Exception {
        final CSVParser csvParser =
                new CSVParser(_csvConfiguration.getSeparatorChar(), _csvConfiguration.getQuoteChar(),
                        _csvConfiguration.getEscapeChar());
        return csvParser.parseLine(csvLine);
    }

}
