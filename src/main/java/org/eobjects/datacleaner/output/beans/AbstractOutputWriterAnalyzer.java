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

import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.output.OutputRow;
import org.eobjects.datacleaner.output.OutputWriter;

public abstract class AbstractOutputWriterAnalyzer implements RowProcessingAnalyzer<OutputAnalyzerResult> {

	private final AtomicInteger rowCount = new AtomicInteger(0);
	
	@Configured
	InputColumn<?>[] columns;

	@Override
	public final OutputAnalyzerResult getResult() {
		getOutputWriter().close();
		return getResultInternal(rowCount.get());
	}
	
	protected abstract OutputAnalyzerResult getResultInternal(int rowCount);

	public abstract OutputWriter getOutputWriter();

	/**
	 * Subclasses should implement this method with any configuration logic such
	 * as setting filenames etc. when the output writer analyzer is being
	 * created.
	 * 
	 * @param analysisJobBuilder
	 *            the job builder being used to build this component
	 * @param descriptor
	 *            the descriptor of the filter that succeeds this component
	 * @param categoryName
	 *            the outcome category of the filter that succeeds this
	 *            component
	 */
	public abstract void configureForOutcome(AnalysisJobBuilder analysisJobBuilder, FilterBeanDescriptor<?, ?> descriptor,
			String categoryName);

	@Override
	public final void run(InputRow row, int distinctCount) {
		writeRow(row, distinctCount);
		rowCount.incrementAndGet();
	}
	
	protected void writeRow(InputRow row, int distinctCount) {
		OutputRow outputRow = getOutputWriter().createRow();
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
