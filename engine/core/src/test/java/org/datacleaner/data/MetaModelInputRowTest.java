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
package org.datacleaner.data;

import junit.framework.TestCase;

import org.apache.metamodel.data.DataSetHeader;
import org.apache.metamodel.data.DefaultRow;
import org.apache.metamodel.data.SimpleDataSetHeader;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.MutableColumn;

public class MetaModelInputRowTest extends TestCase {

	public void testContainsInputColumn() throws Exception {
		SelectItem[] items = new SelectItem[] { new SelectItem(new MutableColumn("foo")),
				new SelectItem(new MutableColumn("bar")) };
		Object[] values = new Object[] { "baz", null };

		DataSetHeader header = new SimpleDataSetHeader(items);
        MetaModelInputRow row = new MetaModelInputRow(1, new DefaultRow(header, values));

		assertTrue(row.containsInputColumn(new MetaModelInputColumn(new MutableColumn("foo"))));
		assertTrue(row.containsInputColumn(new MetaModelInputColumn(new MutableColumn("bar"))));
		assertFalse(row.containsInputColumn(new MetaModelInputColumn(new MutableColumn("baz"))));
	}
}
