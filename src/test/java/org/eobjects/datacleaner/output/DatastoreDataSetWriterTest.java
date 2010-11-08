package org.eobjects.datacleaner.output;

import junit.framework.TestCase;

public class DatastoreDataSetWriterTest extends TestCase {

	public void testSafeName() throws Exception {
		assertEquals("hello_world", DatastoreDataSetWriter.safeName("hello, world"));
		assertEquals("hello_world_", DatastoreDataSetWriter.safeName("hello (world)"));
	}
}
