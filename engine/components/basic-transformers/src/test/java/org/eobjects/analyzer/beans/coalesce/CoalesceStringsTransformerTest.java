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
package org.eobjects.analyzer.beans.coalesce;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

public class CoalesceStringsTransformerTest extends TestCase {

	public void testTransform() throws Exception {
		MockInputColumn<String> col1 = new MockInputColumn<String>("col1", String.class);
		MockInputColumn<String> col2 = new MockInputColumn<String>("col2", String.class);
		MockInputColumn<String> col3 = new MockInputColumn<String>("col3", String.class);

		@SuppressWarnings("unchecked")
		CoalesceStringsTransformer t = new CoalesceStringsTransformer(col1, col2, col3);
		assertEquals(1, t.getOutputColumns().getColumnCount());

		assertEquals("hello", t.transform(new MockInputRow().put(col2, "hello").put(col3, "world"))[0]);
		assertEquals("world", t.transform(new MockInputRow().put(col2, "hello").put(col1, "world"))[0]);
		assertEquals("hello", t.transform(new MockInputRow().put(col1, "hello").put(col2, "world"))[0]);
		assertEquals("world", t.transform(new MockInputRow().put(col1, "").put(col2, "world"))[0]);
		assertEquals("world", t.transform(new MockInputRow().put(col1, null).put(col2, "world"))[0]);

		assertNull(t.transform(new MockInputRow().put(col2, null).put(col1, null))[0]);

		t.considerEmptyStringAsNull = false;
		assertEquals("", t.transform(new MockInputRow().put(col1, "").put(col2, "world"))[0]);
	}
}
