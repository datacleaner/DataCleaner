package org.eobjects.datacleaner.output;

/**
 * Represents an object which can be used to write rows of data to some output
 * (typically a database or a file).
 * 
 * @author Kasper Sørensen
 */
public interface OutputWriter {

	/**
	 * Creates a new row in the output.
	 * 
	 * @return
	 */
	public OutputRow createRow();

	/**
	 * Closes the output writing sequence. Implementing classes should flush and
	 * close any outputstreams etc. here.
	 */
	public void close();
}