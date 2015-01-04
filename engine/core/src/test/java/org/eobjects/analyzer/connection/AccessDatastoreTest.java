/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.connection;

import java.util.Arrays;

import org.apache.metamodel.DataContext;

import junit.framework.TestCase;

public class AccessDatastoreTest extends TestCase {

	public void testGetDatastoreConnection() throws Exception {
		AccessDatastore ds = new AccessDatastore("foobar", "src/test/resources/developers.mdb");
		assertEquals("foobar", ds.getName());

		DatastoreConnection con = ds.openConnection();
		DataContext dataContext = con.getDataContext();

		assertEquals("[information_schema, developers.mdb]", Arrays.toString(dataContext.getSchemaNames()));
		String[] tableNames = dataContext.getDefaultSchema().getTableNames();
		assertEquals("[developer, product]", Arrays.toString(tableNames));
	}
}
