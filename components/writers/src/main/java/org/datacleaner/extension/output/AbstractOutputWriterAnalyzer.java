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
package org.datacleaner.extension.output;

import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.MappedProperty;
import org.datacleaner.beans.writers.WriteDataResult;
import org.datacleaner.desktop.api.PrecedingComponentConsumer;
import org.datacleaner.output.OutputRow;
import org.datacleaner.output.OutputWriter;

public abstract class AbstractOutputWriterAnalyzer implements Analyzer<WriteDataResult>, PrecedingComponentConsumer {

    public static final String PROPERTY_COLUMNS = "Columns";
    public static final String PROPERTY_FIELD_NAMES = "Fields";
    
	private final AtomicInteger rowCount = new AtomicInteger(0);

	@Configured(PROPERTY_COLUMNS)
	InputColumn<?>[] columns;
	
	@Configured(value = PROPERTY_FIELD_NAMES, required = false)
    @MappedProperty(PROPERTY_COLUMNS)
    String[] fields;

	protected OutputWriter outputWriter;

	@Initialize
	public final void init() {
		outputWriter = createOutputWriter();
	}

	@Override
	public final WriteDataResult getResult() {
		outputWriter.close();
		return getResultInternal(rowCount.get());
	}

	protected abstract WriteDataResult getResultInternal(int rowCount);

	public abstract OutputWriter createOutputWriter();

	@Override
	public final void run(InputRow row, int distinctCount) {
		writeRow(row, distinctCount);
		rowCount.incrementAndGet();
	}

	protected void writeRow(InputRow row, int distinctCount) {
		OutputRow outputRow = outputWriter.createRow();
		for (InputColumn<?> col : columns) {
			@SuppressWarnings("unchecked")
			InputColumn<Object> objectCol = (InputColumn<Object>) col;
			outputRow.setValue(objectCol, row.getValue(col));
		}
		outputRow.write();
	}

	public void setColumns(InputColumn<?>[] columns) {
		this.columns = columns;
	}

	public InputColumn<?>[] getColumns() {
		return columns;
	}
}
