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
package org.datacleaner.spark;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.spark.api.java.function.Function;

import au.com.bytecode.opencsv.CSVParser;

public final class CsvParserMapper implements Function<String, Object[]> {

    private static final long serialVersionUID = 1L;

    private final CsvConfiguration _csvConfiguration;

    public CsvParserMapper(final CsvConfiguration csvConfiguration) {
        if (csvConfiguration.isMultilineValues()) {
            throw new IllegalStateException("Multiline CSV files are not supported");
        }
        if (!csvConfiguration.getEncoding().equalsIgnoreCase("UTF-8")) {
            throw new IllegalStateException("CSV files must be UTF-8 encoded");
        }

        _csvConfiguration = csvConfiguration;
    }

    @Override
    public Object[] call(String csvLine) throws Exception {
        final CSVParser csvParser = new CSVParser(_csvConfiguration.getSeparatorChar(),
                _csvConfiguration.getQuoteChar(), _csvConfiguration.getEscapeChar());
        final String[] values = csvParser.parseLine(csvLine);
        return values;
    }

}