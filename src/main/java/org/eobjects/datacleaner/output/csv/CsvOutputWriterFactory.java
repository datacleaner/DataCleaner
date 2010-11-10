package org.eobjects.datacleaner.output.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.datacleaner.output.OutputWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;
import dk.eobjects.metamodel.util.FileHelper;

public final class CsvOutputWriterFactory {

	private static final Logger logger = LoggerFactory.getLogger(CsvOutputWriterFactory.class);

	private static final Map<String, AtomicInteger> counters = new HashMap<String, AtomicInteger>();
	private static final Map<String, CSVWriter> writers = new HashMap<String, CSVWriter>();

	public static OutputWriter getWriter(String filename, List<InputColumn<?>> columns) {
		return getWriter(filename, columns.toArray(new InputColumn<?>[columns.size()]));
	}

	public static OutputWriter getWriter(String filename, InputColumn<?>... columns) {
		String[] headers = new String[columns.length];
		for (int i = 0; i < headers.length; i++) {
			headers[i] = columns[i].getName();
		}
		return getWriter(filename, headers, ',', '"', columns);
	}

	public static OutputWriter getWriter(String filename, String[] headers, char separatorChar, char quoteChar,
			InputColumn<?>... columns) {
		CsvOutputWriter outputWriter;
		synchronized (writers) {
			CSVWriter writer = writers.get(filename);
			if (writer == null) {
				File file = new File(filename);
				if (!file.exists()) {
					File dir = file.getParentFile();
					if (dir != null && !dir.exists() && !dir.mkdirs()) {
						throw new IllegalStateException("Could not create directory for output file: " + filename);
					}
					try {
						file.createNewFile();
					} catch (IOException e) {
						throw new IllegalStateException("Could not create output file: " + filename, e);
					}
				}
				BufferedWriter fileWriter = FileHelper.getBufferedWriter(file);
				writer = new CSVWriter(fileWriter, separatorChar, quoteChar);
				writers.put(filename, writer);
				counters.put(filename, new AtomicInteger(1));

				outputWriter = new CsvOutputWriter(writer, filename, columns);
				outputWriter.createHeader(headers);

				// write the headers
			} else {
				outputWriter = new CsvOutputWriter(writer, filename, columns);
				counters.get(filename).incrementAndGet();
			}
		}

		return outputWriter;
	}

	protected static void release(String filename) {
		int count = counters.get(filename).decrementAndGet();
		if (count == 0) {
			synchronized (writers) {
				CSVWriter writer = writers.get(filename);
				try {
					writer.close();
				} catch (IOException e) {
					logger.error("Could not correctly close CSV file");
				}
			}
		}
	}

}
