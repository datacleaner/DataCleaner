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
package org.datacleaner.beans.coalesce;

import junit.framework.TestCase;

import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;

public class CoalesceNumbersTransformerTest extends TestCase {

	public void testTransform() throws Exception {
		MockInputColumn<Number> col1 = new MockInputColumn<Number>("col1", Number.class);
		MockInputColumn<Number> col2 = new MockInputColumn<Number>("col2", Number.class);
		MockInputColumn<Number> col3 = new MockInputColumn<Number>("col3", Number.class);

		@SuppressWarnings("unchecked")
		CoalesceNumbersTransformer t = new CoalesceNumbersTransformer(col1, col2, col3);
		assertEquals(1, t.getOutputColumns().getColumnCount());

		assertEquals(1, t.transform(new MockInputRow().put(col2, 1).put(col3, 2))[0]);
		assertEquals(1, t.transform(new MockInputRow().put(col2, 2).put(col1, 1))[0]);
		assertEquals(54, t.transform(new MockInputRow().put(col2, 0).put(col1, 54))[0]);

		assertNull(t.transform(new MockInputRow().put(col2, null).put(col1, null))[0]);
	}
}
