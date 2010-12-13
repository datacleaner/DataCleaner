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

import java.util.Map;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.valuedist.ValueCount;
import org.eobjects.analyzer.beans.valuedist.ValueCountListImpl;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.result.ValueDistributionResult;

public class ValueDistributionResultSwingRendererTest extends TestCase {

	private InputColumn<String> column = new MockInputColumn<String>("col", String.class);

	public void testNoGrouping() throws Exception {
		ValueCountListImpl topValueCount = ValueCountListImpl.createFullList();
		for (int i = 0; i < 40; i++) {
			// 40 values with unique counts
			topValueCount.register(new ValueCount("v" + i, i + 1));
		}

		ValueDistributionResultSwingRenderer r = new ValueDistributionResultSwingRenderer(50, 60);
		r.render(new ValueDistributionResult(column, topValueCount, null, 0, 0));

		assertTrue(r.getGroups().isEmpty());
		assertEquals(40, r.getDataset().getItemCount());
	}

	public void testPreferredSizeGrouping() throws Exception {
		ValueCountListImpl topValueCount = ValueCountListImpl.createFullList();
		for (int i = 0; i < 1000; i++) {
			// 1000 values but with only 10 different counts - should yield 10
			// groups
			topValueCount.register(new ValueCount("v" + i, (int) (Math.random() * 10)));
		}

		ValueDistributionResultSwingRenderer r = new ValueDistributionResultSwingRenderer(17, 20);
		r.render(new ValueDistributionResult(column, topValueCount, null, 0, 0));

		assertEquals(10, r.getDataset().getItemCount());

		Map<String, PieSliceGroup> groups = r.getGroups();
		assertEquals(10, groups.size());
		for (String groupName : groups.keySet()) {
			assertTrue(groupName.startsWith("<count="));
		}
	}

	public void testMaxSizeGrouping() throws Exception {
		ValueCountListImpl topValueCount = ValueCountListImpl.createFullList();
		for (int i = 0; i < 5000; i++) {
			// 5000 values with 40 different counts
			topValueCount.register(new ValueCount("v" + i, (int) (Math.random() * 40)));
		}

		ValueDistributionResultSwingRenderer r = new ValueDistributionResultSwingRenderer(16, 20);
		r.render(new ValueDistributionResult(column, topValueCount, null, 0, 0));

		assertEquals(20, r.getDataset().getItemCount());

		Map<String, PieSliceGroup> groups = r.getGroups();
		assertEquals(20, groups.size());
		for (String groupName : groups.keySet()) {
			assertTrue(groupName.startsWith("<count="));
		}

		assertNotNull(groups.get("<count=[0-8]>"));
		assertNotNull(groups.get("<count=[9-18]>"));
		assertNotNull(groups.get("<count=[19-28]>"));
		assertNotNull(groups.get("<count=[29-39]>"));
	}
}
