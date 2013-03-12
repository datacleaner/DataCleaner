/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
import java.util.TreeSet;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.valuedist.SingleValueDistributionResult;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.result.ValueCount;
import org.eobjects.analyzer.result.ValueCountListImpl;
import org.eobjects.analyzer.storage.RowAnnotationImpl;
import org.jfree.data.general.DefaultPieDataset;

public class ValueDistributionResultSwingRendererGroupDelegateTest extends
		TestCase {

	private InputColumn<String> column = new MockInputColumn<String>("col",
			String.class);

	public void testNoGrouping() throws Exception {
		ValueCountListImpl topValueCount = ValueCountListImpl.createFullList();
		for (int i = 0; i < 40; i++) {
			// 40 values with unique counts
			topValueCount.register(new ValueCount("v" + i, i + 1));
		}

		ValueDistributionResultSwingRendererGroupDelegate r = new ValueDistributionResultSwingRendererGroupDelegate(
				"foo", 50, 60, null, null);
		r.renderGroupResult(new SingleValueDistributionResult(column.getName(),
				topValueCount, null, 0, 0, 0, null, new RowAnnotationImpl(), null, null));

		assertTrue(r.getGroups().isEmpty());
		assertEquals(40, r.getDataset().getItemCount());
	}

	public void testGroupEverythingOrNothing() throws Exception {
		ValueCountListImpl topValueCount = ValueCountListImpl.createFullList();

		topValueCount.register(new ValueCount("a", 2));
		topValueCount.register(new ValueCount("b", 2));
		topValueCount.register(new ValueCount("c", 2));
		topValueCount.register(new ValueCount("d", 2));
		topValueCount.register(new ValueCount("e", 2));
		topValueCount.register(new ValueCount("f", 3));
		topValueCount.register(new ValueCount("g", 3));
		topValueCount.register(new ValueCount("h", 3));
		topValueCount.register(new ValueCount("i", 4));
		topValueCount.register(new ValueCount("j", 4));
		topValueCount.register(new ValueCount("k", 4));
		topValueCount.register(new ValueCount("l", 4));
		topValueCount.register(new ValueCount("m", 5));
		topValueCount.register(new ValueCount("n", 5));
		topValueCount.register(new ValueCount("o", 6));
		topValueCount.register(new ValueCount("p", 6));
		topValueCount.register(new ValueCount("q", 7));
		topValueCount.register(new ValueCount("r", 7));
		topValueCount.register(new ValueCount("s", 8));
		topValueCount.register(new ValueCount("t", 8));
		topValueCount.register(new ValueCount("u", 9));
		topValueCount.register(new ValueCount("v", 9));

		// even though it is not nescesary for the slice threshold to group
		// these 4 values, we do so for the sake of "grouping consistency"
		// (because they all have count=9).
		topValueCount.register(new ValueCount("hi", 10));
		topValueCount.register(new ValueCount("hello", 10));
		topValueCount.register(new ValueCount("howdy", 10));
		topValueCount.register(new ValueCount("mjellow", 10));

		// preferred size is 13, which would earlier on mean that all the 4
		// values above could be individually included in the dataset.
		ValueDistributionResultSwingRendererGroupDelegate r = new ValueDistributionResultSwingRendererGroupDelegate(
				"foo", 13, 100, null, null);
		RowAnnotationImpl nullValueAnnotation = new RowAnnotationImpl();
        r.renderGroupResult(new SingleValueDistributionResult(column.getName(),
				topValueCount, null, 10, 0, 0, null, nullValueAnnotation, null, null));

		Map<String, PieSliceGroup> groups = r.getGroups();
		DefaultPieDataset dataset = r.getDataset();
		assertEquals(10, dataset.getItemCount());
		assertEquals(9, groups.size());

		assertEquals(10, dataset.getValue("<unique>").intValue());

		assertTrue(groups.containsKey("<count=2>"));
		assertTrue(groups.containsKey("<count=3>"));
		assertTrue(groups.containsKey("<count=4>"));
		assertTrue(groups.containsKey("<count=5>"));
		assertTrue(groups.containsKey("<count=6>"));
		assertTrue(groups.containsKey("<count=7>"));
		assertTrue(groups.containsKey("<count=8>"));
		assertTrue(groups.containsKey("<count=9>"));
		assertTrue(groups.containsKey("<count=10>"));
	}

	public void testPreferredSizeGrouping() throws Exception {
		ValueCountListImpl topValueCount = ValueCountListImpl.createFullList();
		for (int i = 0; i < 2000; i++) {
			// 2000 values but with only 10 different counts - should yield 10
			// groups
			topValueCount.register(new ValueCount("v" + i, 2 + (int) (Math
					.random() * 10)));
		}

		ValueDistributionResultSwingRendererGroupDelegate r = new ValueDistributionResultSwingRendererGroupDelegate(
				"foo", 17, 20, null, null);
		r.renderGroupResult(new SingleValueDistributionResult("foo",
				topValueCount, null, 0, 0, 0, null, new RowAnnotationImpl(), null, null));

		assertEquals(
				"[<count=10>, <count=11>, <count=2>, <count=3>, <count=4>, <count=5>, <count=6>, <count=7>, <count=8>, <count=9>]",
				new TreeSet<String>(r.getGroups().keySet()).toString());

		Map<String, PieSliceGroup> groups = r.getGroups();
		assertEquals(10, groups.size());
		for (String groupName : groups.keySet()) {
			assertTrue(groupName.startsWith("<count="));
		}
	}

	public void testMaxSizeGrouping() throws Exception {
		ValueCountListImpl topValueCount = ValueCountListImpl.createFullList();
		for (int i = 0; i < 5000; i++) {
			// 5000 values with 10 different counts
			topValueCount.register(new ValueCount("v" + i, 2 + (int) (Math
					.random() * 10)));
		}

		// 10 additional values that will be grouped in 3 range-groups
		topValueCount.register(new ValueCount("r1", 100));
		topValueCount.register(new ValueCount("r2", 110));
		topValueCount.register(new ValueCount("r3", 130));
		topValueCount.register(new ValueCount("r4", 160));
		topValueCount.register(new ValueCount("r5", 210));
		topValueCount.register(new ValueCount("r6", 340));
		topValueCount.register(new ValueCount("r7", 520));
		topValueCount.register(new ValueCount("r8", 525));
		topValueCount.register(new ValueCount("r9", 530));

		ValueDistributionResultSwingRendererGroupDelegate r = new ValueDistributionResultSwingRendererGroupDelegate(
				"foo", 10, 13, null, null);
		r.renderGroupResult(new SingleValueDistributionResult("foo",
				topValueCount, null, 0, 0, 0, null, new RowAnnotationImpl(), null, null));

		assertEquals(13, r.getDataset().getItemCount());

		Map<String, PieSliceGroup> groups = r.getGroups();
		assertEquals(13, groups.size());
		for (String groupName : groups.keySet()) {
			assertTrue(groupName.startsWith("<count="));
		}

		assertTrue(groups.containsKey("<count=[100-130]>"));
		assertTrue(groups.containsKey("<count=[160-340]>"));
		assertTrue(groups.containsKey("<count=[520-530]>"));
	}
}
