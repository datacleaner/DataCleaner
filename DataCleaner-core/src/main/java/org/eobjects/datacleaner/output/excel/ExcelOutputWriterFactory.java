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
package org.eobjects.datacleaner.output.excel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.datacleaner.output.OutputWriter;
import org.eobjects.metamodel.UpdateCallback;
import org.eobjects.metamodel.UpdateScript;
import org.eobjects.metamodel.UpdateableDataContext;
import org.eobjects.metamodel.create.CreateTableBuilder;
import org.eobjects.metamodel.excel.ExcelDataContext;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.MutableRef;

public final class ExcelOutputWriterFactory {

	private static final Map<String, AtomicInteger> counters = new HashMap<String, AtomicInteger>();
	private static final Map<String, UpdateableDataContext> dataContexts = new HashMap<String, UpdateableDataContext>();

	public static OutputWriter getWriter(String filename, String sheetName, final InputColumn<?>... columns) {
		ExcelOutputWriter outputWriter;
		synchronized (dataContexts) {
			UpdateableDataContext dataContext = dataContexts.get(filename);
			if (dataContext == null) {

				File file = new File(filename);
				dataContext = new ExcelDataContext(file);

				Table table = getTable(dataContext, sheetName, columns);

				dataContexts.put(filename, dataContext);
				counters.put(filename, new AtomicInteger(1));
				outputWriter = new ExcelOutputWriter(dataContext, filename, table, columns);

				// write the headers
			} else {
				Table table = getTable(dataContext, sheetName, columns);
				outputWriter = new ExcelOutputWriter(dataContext, filename, table, columns);
				counters.get(filename).incrementAndGet();
			}
		}

		return outputWriter;
	}

	private static Table getTable(UpdateableDataContext dataContext, final String sheetName, final InputColumn<?>[] columns) {
		final Schema schema = dataContext.getDefaultSchema();
		Table table = schema.getTableByName(sheetName);
		if (table == null) {
			final MutableRef<Table> tableRef = new MutableRef<Table>();
			dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					CreateTableBuilder tableBuilder = callback.createTable(schema, sheetName);
					for (InputColumn<?> inputColumn : columns) {
						tableBuilder.withColumn(inputColumn.getName());
					}
					tableRef.set(tableBuilder.execute());
				}
			});
			table = tableRef.get();
		}
		return table;
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
