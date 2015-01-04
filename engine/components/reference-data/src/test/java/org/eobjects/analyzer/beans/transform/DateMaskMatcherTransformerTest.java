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
package org.eobjects.analyzer.beans.transform;

import java.util.Arrays;

import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

import junit.framework.TestCase;

public class DateMaskMatcherTransformerTest extends TestCase {

	public void testSimpleScenario() throws Exception {
		MockInputColumn<String> col = new MockInputColumn<String>("foo", String.class);
		DateMaskMatcherTransformer t = new DateMaskMatcherTransformer(col);
		
		t.setDateMasks(new String[] { "yyyy-MM-dd", "yyyy-dd-MM" });
		t.init();

		OutputColumns outputColumns = t.getOutputColumns();
		assertEquals(2, outputColumns.getColumnCount());
		assertEquals("foo 'yyyy-MM-dd'", outputColumns.getColumnName(0));
		assertEquals("foo 'yyyy-dd-MM'", outputColumns.getColumnName(1));

		assertEquals("[true, false]", Arrays.toString(t.transform(new MockInputRow().put(col, "2010-03-21"))));
		assertEquals("[false, false]", Arrays.toString(t.transform(new MockInputRow().put(col, "hello world"))));
		assertEquals("[false, false]", Arrays.toString(t.transform(new MockInputRow().put(col, null))));
		assertEquals("[true, true]", Arrays.toString(t.transform(new MockInputRow().put(col, "2010-03-11"))));
		assertEquals("[false, false]", Arrays.toString(t.transform(new MockInputRow().put(col, "2010/03/21"))));
	}
}
