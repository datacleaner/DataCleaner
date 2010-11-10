package org.eobjects.datacleaner.output.beans;

import java.io.File;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.datacleaner.output.OutputRow;
import org.eobjects.datacleaner.output.OutputWriter;
import org.eobjects.datacleaner.output.csv.CsvOutputWriterFactory;

@AnalyzerBean("Write to CSV file")
@HiddenFromMenu
public class CsvOutputAnalyzer extends AbstractOutputAnalyzer {

	private OutputWriter _outputWriter;

	@Configured
	char separatorChar = ',';

	@Configured
	char quoteChar = '"';

	@Configured
	File file;

	@Override
	public OutputWriter getOutputWriter() {
		if (_outputWriter == null) {
			synchronized (this) {
				String[] headers = new String[columns.length];
				for (int i = 0; i < headers.length; i++) {
					headers[i] = columns[i].getName();
				}

				if (_outputWriter == null) {
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