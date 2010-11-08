package org.eobjects.datacleaner.output;

import java.util.List;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

public interface DataSetWriter {

	public void write(List<InputColumn<?>> columns, InputRow[] rows);
}
