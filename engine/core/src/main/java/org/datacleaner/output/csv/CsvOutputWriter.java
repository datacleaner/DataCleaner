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
package org.datacleaner.output.csv;

import java.io.OutputStream;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvWriter;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.LazyRef;
import org.apache.metamodel.util.Resource;
import org.datacleaner.api.InputColumn;
import org.datacleaner.output.OutputRow;
import org.datacleaner.output.OutputWriter;

final class CsvOutputWriter implements OutputWriter {

    private final LazyRef<OutputStream> _outputStreamRef;
    private final InputColumn<?>[] _columns;
    private final CsvConfiguration _csvConfiguration;

    public CsvOutputWriter(final Resource resource, final CsvConfiguration csvConfiguration,
            final String[] columnNames, final InputColumn<?>[] columns) {
        _csvConfiguration = csvConfiguration;
        _columns = columns;
        _outputStreamRef = new LazyRef<OutputStream>() {
            @Override
            protected OutputStream fetch() throws Throwable {
                final OutputStream outputStream = resource.write();
                if (csvConfiguration.getColumnNameLineNumber() != CsvConfiguration.NO_COLUMN_NAME_LINE) {
                    final CsvWriter csvWriter = new CsvWriter(_csvConfiguration);
                    final String headerLine = csvWriter.buildLine(columnNames);
                    final byte[] bytes = headerLine.getBytes(csvConfiguration.getEncoding());
                    outputStream.write(bytes);
                }
                return outputStream;
            }
        };
    }

    @Override
    public OutputRow createRow() {
        return new CsvOutputRow(_outputStreamRef, _csvConfiguration, _columns);
    }

    @Override
    public void close() {
        if (_outputStreamRef.isFetched()) {
            FileHelper.safeClose(_outputStreamRef.get());
        }
    }

}
