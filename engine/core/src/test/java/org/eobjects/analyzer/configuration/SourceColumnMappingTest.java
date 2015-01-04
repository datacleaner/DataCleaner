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
package org.eobjects.analyzer.configuration;

import junit.framework.TestCase;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.test.TestHelper;
import org.eobjects.analyzer.test.mock.MockDatastore;
import org.apache.metamodel.schema.MutableColumn;

public class SourceColumnMappingTest extends TestCase {

	public void testIsSatisfied() throws Exception {
		SourceColumnMapping columnMapping = new SourceColumnMapping("foo.bar.col1", "foo.bar.col2");
		assertFalse(columnMapping.isSatisfied());

		columnMapping.setDatastore(new MockDatastore());
		assertFalse(columnMapping.isSatisfied());
		
		columnMapping.setColumn("foo.bar.col1", new MutableColumn("col1"));
		assertFalse(columnMapping.isSatisfied());

		columnMapping.setColumn("foo.bar.col2", new MutableColumn("col2"));
		assertTrue(columnMapping.isSatisfied());
		
		
	}

	public void testAutoMapAllMatches() throws Exception {
		Datastore datastore = TestHelper.createSampleDatabaseDatastore("testdb");

		SourceColumnMapping columnMapping = new SourceColumnMapping("PUBLIC.EMPLOYEES.FIRSTNAME",
				"PUBLIC.EMPLOYEES.LASTNAME");
		assertFalse(columnMapping.isSatisfied());
		columnMapping.autoMap(datastore);

		assertTrue(columnMapping.isSatisfied());
	}

	public void testAutoMapPartialMatches() throws Exception {
		Datastore datastore = TestHelper.createSampleDatabaseDatastore("testdb");

		SourceColumnMapping columnMapping = new SourceColumnMapping("PUBLIC.EMPLOYEES.FIRSTNAME", "foo.bar.col1",
				"PUBLIC.EMPLOYEES.LASTNAME", "foo.bar.col2");

		columnMapping.setColumn("foo.bar.col1", new MutableColumn("col1"));
		assertFalse(columnMapping.isSatisfied());
		assertEquals("[PUBLIC.EMPLOYEES.FIRSTNAME, PUBLIC.EMPLOYEES.LASTNAME, foo.bar.col2]", columnMapping
				.getUnmappedPaths().toString());

		columnMapping.autoMap(datastore);
		assertFalse(columnMapping.isSatisfied());
		assertEquals("[foo.bar.col2]", columnMapping.getUnmappedPaths().toString());

		columnMapping.setColumn("foo.bar.col2", new MutableColumn("col2"));
		assertTrue(columnMapping.isSatisfied());
	}
}
