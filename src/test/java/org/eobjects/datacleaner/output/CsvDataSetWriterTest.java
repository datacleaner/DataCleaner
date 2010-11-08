package org.eobjects.datacleaner.output;

import junit.framework.TestCase;

public class CsvDataSetWriterTest extends TestCase {

	public void testEscape() throws Exception {
		assertEquals("I said \\\"Hello\\\" to 'world'", CsvDataSetWriter.escape("I said \"Hello\" to 'world'"));
		assertEquals("I said \\\"Hello\\\", to 'world'", CsvDataSetWriter.escape("I said \"Hello\", to 'world'"));
	}
}
