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

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.apache.metamodel.csv.CsvConfiguration;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.data.MockInputRow;

import au.com.bytecode.opencsv.CSVParser;

public class CsvParser implements Serializable {

    private static final long serialVersionUID = 1L;

    public static InputRow prepareInputRow(Collection<InputColumn<?>> jobColumns, CsvConfiguration csvConfiguration, String csvLine) throws IOException {
        CSVParser openCsvParser = new CSVParser(csvConfiguration.getSeparatorChar(), csvConfiguration.getQuoteChar(), csvConfiguration.getEscapeChar());
        
        String[] values = openCsvParser.parseLine(csvLine);
        
        if (values.length != jobColumns.size()) {
            throw new IllegalStateException("The number of values in the row (" + values.length
                    + " did not match the number of columns defined in the job (" + jobColumns.size() + ")");
        }

        Iterator<InputColumn<?>> jobColumnsIterator = jobColumns.iterator();

        MockInputRow row = new MockInputRow();
        for (String value : values) {
            InputColumn<?> inputColumn = jobColumnsIterator.next();
            row.put(inputColumn, value);
        }
        return row;
    }

}
