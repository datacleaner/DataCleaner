/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.widgets.result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.valuedist.ValueCount;

public class PieSliceGroupTest extends TestCase {
	
	public void testShrinkingCollectionFixedCount() throws Exception {
		List<String> values = new ArrayList<String>();
		values.add("1");
		values.add("2");
		values.add("3");
		values.add("4");
		values.add("5");
		PieSliceGroup pieSliceGroup = new PieSliceGroup("foo", 5, values , 1);
		
		assertEquals(5, pieSliceGroup.getTotalCount());
		
		// simulate a weak/soft collection shrinking
		values.remove("3");
		values.remove("2");
		assertEquals(3, values.size());
		assertEquals(5, pieSliceGroup.getTotalCount());
		
		Iterator<ValueCount> valueCounts = pieSliceGroup.getValueCounts();
		assertTrue(valueCounts.hasNext());
		assertEquals("1", valueCounts.next().getValue());
		assertTrue(valueCounts.hasNext());
		assertEquals("4", valueCounts.next().getValue());
		assertTrue(valueCounts.hasNext());
		assertEquals("5", valueCounts.next().getValue());
		assertFalse(valueCounts.hasNext());
	}

}
