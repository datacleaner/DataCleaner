/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.output;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.metamodel.UpdateCallback;
import org.eobjects.metamodel.UpdateScript;
import org.eobjects.metamodel.UpdateableDataContext;
import org.eobjects.metamodel.insert.RowInsertionBuilder;
import org.eobjects.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract {@link OutputWriter} implementation for all
 * {@link UpdateableDataContext}s.
 * 
 * This implementation holds a buffer of records to write, to avoid hitting the
 * executeUpdate() method for every single record.
 * 
 * @author Kasper Sørensen
 */
public abstract class AbstractMetaModelOutputWriter implements OutputWriter {

	private static final Logger logger = LoggerFactory.getLogger(AbstractMetaModelOutputWriter.class);

	private final UpdateableDataContext _dataContext;
	private final Queue<Object[]> _buffer;
	private final InputColumn<?>[] _columns;

	/**
	 * Creates a new {@link OutputWriter} based on a data context, a set of
	 * columns and a buffer size.
	 * 
	 * @param dataContext
	 * @param columns
	 * @param bufferSize
	 *            the size of the write buffer. If 0 or negative, an unlimited
	 *            buffer will be used, meaning that the complete dataset will be
	 *            held in memory.
	 */
	public AbstractMetaModelOutputWriter(UpdateableDataContext dataContext, InputColumn<?>[] columns, int bufferSize) {
		_dataContext = dataContext;
		_columns = columns;
		if (bufferSize > 0) {
			_buffer = new ArrayBlockingQueue<Object[]>(bufferSize);
		} else {
			_buffer = new ConcurrentLinkedQueue<Object[]>();
		}
	}

	@Override
	public final OutputRow createRow() {
		return new MetaModelOutputRow(this, _columns);
	}

	protected final void addToBuffer(Object[] rowData) {
		while (!_buffer.offer(rowData)) {
			flushBuffer();
		}
	}

	private synchronized final void flushBuffer() {
		if (!_buffer.isEmpty()) {
			logger.info("Flushing {} rows in write buffer", _buffer.size());
			_dataContext.executeUpdate(new UpdateScript() {
				@Override
				public void run(UpdateCallback callback) {
					for (Object[] rowData = _buffer.poll(); rowData != null; rowData = _buffer.poll()) {
						RowInsertionBuilder insertBuilder = callback.insertInto(getTable());
						for (int i = 0; i < _columns.length; i++) {
							InputColumn<?> column = _columns[i];
							Object value = rowData[i];
							String columnName = getTargetColumnName(column);
							insertBuilder = insertBuilder.value(columnName, value);
						}
						insertBuilder.execute();
					}
				}
			});
		}
	}

	protected abstract Table getTable();

	protected String getTargetColumnName(InputColumn<?> column) {
		return column.getName();
	}

	@Override
	public final void close() {
		flushBuffer();
		afterClose();
	}

	protected void afterClose() {
	}
}
