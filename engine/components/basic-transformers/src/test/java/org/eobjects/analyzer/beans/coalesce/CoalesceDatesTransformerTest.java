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

import java.util.Date;

import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

import org.apache.metamodel.util.DateUtils;
import org.apache.metamodel.util.Month;

import junit.framework.TestCase;

public class CoalesceDatesTransformerTest extends TestCase {

	public void testTransform() throws Exception {
		MockInputColumn<Date> col1 = new MockInputColumn<Date>("col1", Date.class);
		MockInputColumn<Date> col2 = new MockInputColumn<Date>("col2", Date.class);
		MockInputColumn<Date> col3 = new MockInputColumn<Date>("col3", Date.class);

		CoalesceDatesTransformer t = new CoalesceDatesTransformer(col1, col2, col3);
		assertEquals(1, t.getOutputColumns().getColumnCount());

		Date april1 = DateUtils.get(2000, Month.APRIL, 1);
		Date may1 = DateUtils.get(2000, Month.MAY, 1);
		Date june1 = DateUtils.get(2000, Month.JUNE, 1);
		assertEquals(april1, t.transform(new MockInputRow().put(col2, april1).put(col3, may1))[0]);
		assertEquals(may1, t.transform(new MockInputRow().put(col2, april1).put(col1, may1))[0]);
		assertEquals(may1, t.transform(new MockInputRow().put(col2, june1).put(col1, may1))[0]);

		assertNull(t.transform(new MockInputRow().put(col2, null).put(col1, null))[0]);
	}
}
