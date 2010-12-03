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
package org.eobjects.datacleaner.output.beans;

import java.io.File;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.datacleaner.output.OutputRow;
import org.eobjects.datacleaner.output.OutputWriter;
import org.eobjects.datacleaner.output.csv.CsvOutputWriterFactory;

@AnalyzerBean("Write to CSV file")
@OutputWriterAnalyzer
public class CsvOutputAnalyzer extends AbstractOutputWriterAnalyzer {

	private OutputWriter _outputWriter;

	@Configured
	char separatorChar = ',';

	@Configured
	char quoteChar = '"';

	@Configured
	File file;

	@Override
	public void configureForOutcome(FilterBeanDescriptor<?, ?> descriptor, String categoryName) {
		file = new File("output-" + descriptor.getDisplayName() + "-" + categoryName + ".csv");
	}

	@Override
	public OutputWriter getOutputWriter() {
		if (_outputWriter == null) {
			synchronized (this) {
				if (_outputWriter == null) {
					String[] headers = new String[columns.length];
					for (int i = 0; i < headers.length; i++) {
						headers[i] = columns[i].getName();
					}

					_outputWriter = CsvOutputWriterFactory.getWriter(file.getPath(), headers, separatorChar, quoteChar,
							columns);
				}
			}
		}
		return _outputWriter;
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		OutputRow outputRow = getOutputWriter().createRow();
		for (InputColumn<?> col : columns) {
			@SuppressWarnings("unchecked")
			InputColumn<Object> objectCol = (InputColumn<Object>) col;
			outputRow.setValue(objectCol, row.getValue(col));
		}
		outputRow.write();
	}

	public void setFile(File file) {
		this.file = file;
	}
}