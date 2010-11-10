package org.eobjects.datacleaner.output;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * Represents a row that is being written to some output (typically a file or a database)
 * 
 * @author Kasper Sørensen
 */
public interface OutputRow {

	public <E> OutputRow setValue(InputColumn<E> inputColumn, E value);

	public OutputRow setValues(InputRow row);

	public void write();
}
