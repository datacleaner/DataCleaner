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
package org.eobjects.datacleaner.extension.output;

import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.writers.WriteDataResult;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.output.OutputRow;
import org.eobjects.datacleaner.output.OutputWriter;

public abstract class AbstractOutputWriterAnalyzer implements Analyzer<WriteDataResult> {

	private final AtomicInteger rowCount = new AtomicInteger(0);

	@Configured
	InputColumn<?>[] columns;

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
	public abstract void configureForFilterOutcome(AnalysisJobBuilder analysisJobBuilder, FilterBeanDescriptor<?, ?> descriptor,
			String categoryName);

	/**
	 * Subclasses should implement this method with any configuration logic such
	 * as setting filenames etc. when the output writer analyzer is being
	 * created.
	 * 
	 * @param analysisJobBuilder
	 *            the job builder being used to build this component
	 * @param descriptor
	 *            the descriptor of the transformer that succeeds this component
	 */
	public abstract void configureForTransformedData(AnalysisJobBuilder analysisJobBuilder,
			TransformerBeanDescriptor<?> descriptor);

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
