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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.datacleaner.output.OutputWriter;
import org.eobjects.metamodel.UpdateCallback;
import org.eobjects.metamodel.UpdateScript;
import org.eobjects.metamodel.UpdateableDataContext;
import org.eobjects.metamodel.create.TableCreationBuilder;
import org.eobjects.metamodel.csv.CsvConfiguration;
import org.eobjects.metamodel.csv.CsvDataContext;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.FileHelper;

public final class CsvOutputWriterFactory {

	private static final Map<String, AtomicInteger> counters = new HashMap<String, AtomicInteger>();
	private static final Map<String, UpdateableDataContext> dataContexts = new HashMap<String, UpdateableDataContext>();

	public static OutputWriter getWriter(String filename, List<InputColumn<?>> columns) {
		return getWriter(filename, columns.toArray(new InputColumn<?>[columns.size()]));
	}

	public static OutputWriter getWriter(String filename, InputColumn<?>... columns) {
		return getWriter(filename, ',', '"', columns);
	}

	public static OutputWriter getWriter(String filename, char separatorChar, char quoteChar, InputColumn<?>... columns) {
		String[] headers = new String[columns.length];
		for (int i = 0; i < headers.length; i++) {
			headers[i] = columns[i].getName();
		}
		return getWriter(filename, headers, separatorChar, quoteChar, columns);
	}

	public static OutputWriter getWriter(String filename, final String[] headers, char separatorChar, char quoteChar,
			final InputColumn<?>... columns) {
		CsvOutputWriter outputWriter;
		synchronized (dataContexts) {
			UpdateableDataContext dataContext = dataContexts.get(filename);
			if (dataContext == null) {

				File file = new File(filename);
				File parentFile = file.getParentFile();
				if (parentFile != null && !parentFile.exists()) {
					parentFile.mkdirs();
				}
				dataContext = new CsvDataContext(file, getConfiguration(separatorChar, quoteChar));

				final Schema schema = dataContext.getDefaultSchema();
				dataContext.executeUpdate(new UpdateScript() {
					@Override
					public void run(UpdateCallback callback) {
						TableCreationBuilder tableBuilder = callback.createTable(schema, "table");
						for (String header : headers) {
							tableBuilder.withColumn(header);
						}
						tableBuilder.execute();
					}
				});

				Table table = dataContext.getDefaultSchema().getTables()[0];

				dataContexts.put(filename, dataContext);
				counters.put(filename, new AtomicInteger(1));
				outputWriter = new CsvOutputWriter(dataContext, filename, table, columns);

				// write the headers
			} else {
				Table table = dataContext.getDefaultSchema().getTables()[0];
				outputWriter = new CsvOutputWriter(dataContext, filename, table, columns);
				counters.get(filename).incrementAndGet();
			}
		}

		return outputWriter;
	}

	private static CsvConfiguration getConfiguration(char separatorChar, char quoteChar) {
		return new CsvConfiguration(CsvConfiguration.DEFAULT_COLUMN_NAME_LINE, FileHelper.DEFAULT_ENCODING, separatorChar,
				quoteChar, '\\');
	}

	protected static void release(String filename) {
		int count = counters.get(filename).decrementAndGet();
		if (count == 0) {
			synchronized (dataContexts) {
				dataContexts.remove(filename);
			}
		}
	}

}
