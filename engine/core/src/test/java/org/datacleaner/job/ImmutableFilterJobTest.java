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
package org.datacleaner.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.beans.filter.MaxRowsFilter;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.FilterBeanDescriptor;

public class ImmutableFilterJobTest extends TestCase {

	public void testGetOutcomes() throws Exception {
		FilterBeanDescriptor<?, ?> descriptor = Descriptors.ofFilterUnbound(MaxRowsFilter.class);
		BeanConfiguration configuration = new ImmutableBeanConfiguration(new HashMap<ConfiguredPropertyDescriptor, Object>());

		ImmutableFilterJob job = new ImmutableFilterJob("foo", descriptor, configuration, null, null);
		assertEquals("foo", job.getName());
		assertEquals(null, job.getComponentRequirement());

		List<FilterOutcome> outcomes1 = new ArrayList<>( job.getFilterOutcomes());
		assertEquals(2, outcomes1.size());
		assertEquals("FilterOutcome[category=VALID]", outcomes1.get(0).toString());
		assertEquals("FilterOutcome[category=INVALID]", outcomes1.get(1).toString());

		List<FilterOutcome> outcomes2 = new ArrayList<>(  job.getFilterOutcomes());
		assertEquals(2, outcomes2.size());
		assertEquals("FilterOutcome[category=VALID]", outcomes2.get(0).toString());
		assertEquals("FilterOutcome[category=INVALID]", outcomes2.get(1).toString());

		// the arrays are not the same, but their contents are equal
		assertNotSame(outcomes1, outcomes2);

		assertNotSame(outcomes1.get(0), outcomes2.get(0));
		assertEquals(outcomes1.get(0), outcomes2.get(0));
		assertNotSame(outcomes1.get(1), outcomes2.get(1));
		assertEquals(outcomes1.get(1), outcomes2.get(1));
	}
}
