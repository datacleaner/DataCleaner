package org.eobjects.datacleaner.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

import dk.eobjects.metamodel.util.FileHelper;

public class CsvDataSetWriter implements DataSetWriter {

	private File _file;

	public CsvDataSetWriter(File file) {
		_file = file;
	}

	@Override
	public void write(List<InputColumn<?>> columns, InputRow[] rows) {
		if (_file.exists()) {
			try {
				_file.createNewFile();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

		BufferedWriter writer = FileHelper.getBufferedWriter(_file);
		try {
			for (int i = 0; i < columns.size(); i++) {
				InputColumn<?> column = columns.get(i);
				if (i != 0) {
					writer.write(',');
				}
				writer.write('"');
				writer.write(escape(column.getName()));
				writer.write('"');
			}

			for (InputRow inputRow : rows) {
				writer.write('\n');
				for (int i = 0; i < columns.size(); i++) {
					InputColumn<?> column = columns.get(i);
					if (i != 0) {
						writer.write(',');
					}
					Object value = inputRow.getValue(column);
					if (value != null) {
						boolean requiresQuote = requiresQuote(value);
						if (requiresQuote) {
							writer.write('"');
						}
						writer.write(escape(value.toString()));
						if (requiresQuote) {
							writer.write('"');
						}
					}
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}
	}

	private boolean requiresQuote(Object value) {
		return value instanceof String || value instanceof Character || value instanceof Date;
	}

	public static String escape(String str) {
		return str.replaceAll("\\\"", "\\\\\"");
	}

}
