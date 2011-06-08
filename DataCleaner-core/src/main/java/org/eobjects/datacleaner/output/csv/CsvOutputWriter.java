/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.output.csv;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.datacleaner.output.OutputRow;
import org.eobjects.datacleaner.output.OutputWriter;

import au.com.bytecode.opencsv.CSVWriter;

final class CsvOutputWriter implements OutputWriter {

	private final InputColumn<?>[] _columns;
	private final CSVWriter _csvWriter;
	private final String _filename;

	public CsvOutputWriter(CSVWriter csvWriter, String filename, 
			InputColumn<?>... columns) {
		_csvWriter = csvWriter;
		_filename = filename;
		_columns = columns;
	}

	@Override
	public void close() {
		CsvOutputWriterFactory.release(_filename);
	}

	public void createHeader(String... headers) {
		if (_columns.length != headers.length) {
			throw new IllegalStateException("Columns and header length doesn't match. Expected " + _columns.length
					+ " but found " + headers.length);
		}
		_csvWriter.writeNext(headers);
	}

	@Override
	public OutputRow createRow() {
		return new CsvOutputRow(_csvWriter, _columns);
	}

}
