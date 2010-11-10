package org.eobjects.datacleaner.output.beans;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.datacleaner.output.OutputRow;
import org.eobjects.datacleaner.output.OutputWriter;

public abstract class AbstractOutputAnalyzer implements RowProcessingAnalyzer<OutputAnalyzerResult> {

	@Configured
	InputColumn<?>[] columns;

	@Override
	public OutputAnalyzerResult getResult() {
		getOutputWriter().close();
		return new OutputAnalyzerResult();
	}

	public abstract OutputWriter getOutputWriter();

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

	public void setColumns(InputColumn<?>[] columns) {
		this.columns = columns;
	}

	public InputColumn<?>[] getColumns() {
		return columns;
	}
}
