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
package org.datacleaner.beans.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;

public class SelectFromListTransformerTest extends TestCase {

	public void testTransform() throws Exception {
		final SelectFromListTransformer trans = new SelectFromListTransformer();
		final InputColumn<List<?>> col = new MockInputColumn<List<?>>("foo");
		trans.listColumn = col;
		trans.indices = new Number[] { 0, 3, 50, 1, -1 };
		trans.elementType = Integer.class;
		trans.verifyTypes = true;

		List<Number> list = new ArrayList<Number>();
		list.add(1000);
		list.add(1001);
		list.add(1003);
		list.add(1004);

		Object[] result = trans.transform(new MockInputRow().put(col, list));

		assertEquals(5, result.length);
		assertEquals("[1000, 1004, null, 1001, null]", Arrays.toString(result));
	}

}
