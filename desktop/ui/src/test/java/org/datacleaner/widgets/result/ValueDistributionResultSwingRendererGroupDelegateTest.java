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
package org.datacleaner.widgets.result;

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.valuedist.SingleValueDistributionResult;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.result.SingleValueFrequency;
import org.datacleaner.result.ValueCountListImpl;
import org.datacleaner.storage.RowAnnotationImpl;

public class ValueDistributionResultSwingRendererGroupDelegateTest extends
		TestCase {

	private InputColumn<String> column = new MockInputColumn<String>("col",
			String.class);

	public void testNoGrouping() throws Exception {
		ValueCountListImpl topValueCount = ValueCountListImpl.createFullList();
		for (int i = 0; i < 40; i++) {
			// 40 values with unique counts
			topValueCount.register(new SingleValueFrequency("v" + i, i + 1));
		}

		ValueDistributionResultSwingRendererGroupDelegate r = new ValueDistributionResultSwingRendererGroupDelegate(
				"foo", 50, null, null);
		r.renderGroupResult(new SingleValueDistributionResult(column.getName(),
				topValueCount, null, 0, 0, 0, null, new RowAnnotationImpl(), null, null));

		assertEquals(40, r.getDataSetItemCount());
	}

	public void testGroupEverythingOrNothing() throws Exception {
		ValueCountListImpl topValueCount = ValueCountListImpl.createFullList();

		topValueCount.register(new SingleValueFrequency("a", 2));
		topValueCount.register(new SingleValueFrequency("b", 2));
		topValueCount.register(new SingleValueFrequency("c", 2));
		topValueCount.register(new SingleValueFrequency("d", 2));
		topValueCount.register(new SingleValueFrequency("e", 2));
		topValueCount.register(new SingleValueFrequency("f", 3));
		topValueCount.register(new SingleValueFrequency("g", 3));
		topValueCount.register(new SingleValueFrequency("h", 3));
		topValueCount.register(new SingleValueFrequency("i", 4));
		topValueCount.register(new SingleValueFrequency("j", 4));
		topValueCount.register(new SingleValueFrequency("k", 4));
		topValueCount.register(new SingleValueFrequency("l", 4));
		topValueCount.register(new SingleValueFrequency("m", 5));
		topValueCount.register(new SingleValueFrequency("n", 5));
		topValueCount.register(new SingleValueFrequency("o", 6));
		topValueCount.register(new SingleValueFrequency("p", 6));
		topValueCount.register(new SingleValueFrequency("q", 7));
		topValueCount.register(new SingleValueFrequency("r", 7));
		topValueCount.register(new SingleValueFrequency("s", 8));
		topValueCount.register(new SingleValueFrequency("t", 8));
		topValueCount.register(new SingleValueFrequency("u", 9));
		topValueCount.register(new SingleValueFrequency("v", 9));

		// even though it is not nescesary for the slice threshold to group
		// these 4 values, we do so for the sake of "grouping consistency"
		// (because they all have count=9).
		topValueCount.register(new SingleValueFrequency("hi", 10));
		topValueCount.register(new SingleValueFrequency("hello", 10));
		topValueCount.register(new SingleValueFrequency("howdy", 10));
		topValueCount.register(new SingleValueFrequency("mjellow", 10));

		// preferred size is 13, which would earlier on mean that all the 4
		// values above could be individually included in the dataset.
		ValueDistributionResultSwingRendererGroupDelegate r = new ValueDistributionResultSwingRendererGroupDelegate(
				"foo", 13, null, null);
		RowAnnotationImpl nullValueAnnotation = new RowAnnotationImpl();
        r.renderGroupResult(new SingleValueDistributionResult(column.getName(),
				topValueCount, null, 10, 0, 0, null, nullValueAnnotation, null, null));

		assertEquals(13, r.getDataSetItemCount());

		assertEquals(10, r.getDataSetValue("<count=2>"));
		assertEquals(40, r.getDataSetValue("<count=10>"));
		assertEquals(10, r.getDataSetValue("<unique>"));
	}

	public void testMaxSizeGrouping() throws Exception {
		ValueCountListImpl topValueCount = ValueCountListImpl.createFullList();
		for (int i = 0; i < 5000; i++) {
			// 5000 values with 10 different counts
			topValueCount.register(new SingleValueFrequency("v" + i, 2 + (int) (Math
					.random() * 10)));
		}

		// 10 additional values that will be grouped in 3 range-groups
		topValueCount.register(new SingleValueFrequency("r1", 100));
		topValueCount.register(new SingleValueFrequency("r2", 110));
		topValueCount.register(new SingleValueFrequency("r3", 130));
		topValueCount.register(new SingleValueFrequency("r4", 160));
		topValueCount.register(new SingleValueFrequency("r5", 210));
		topValueCount.register(new SingleValueFrequency("r6", 340));
		topValueCount.register(new SingleValueFrequency("r7", 520));
		topValueCount.register(new SingleValueFrequency("r8", 525));
		topValueCount.register(new SingleValueFrequency("r9", 530));

		ValueDistributionResultSwingRendererGroupDelegate r = new ValueDistributionResultSwingRendererGroupDelegate(
				"foo", 10, null, null);
		r.renderGroupResult(new SingleValueDistributionResult("foo",
				topValueCount, null, 0, 0, 0, null, new RowAnnotationImpl(), null, null));

		assertEquals(19, r.getDataSetItemCount());
	}
}
