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
package org.eobjects.analyzer.beans.filter;

import java.util.Set;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.ValidateMethodDescriptor;

import junit.framework.TestCase;

public class NumberRangeFilterTest extends TestCase {

    public void testDescriptorInheritance() throws Exception {
        FilterBeanDescriptor<NumberRangeFilter, RangeFilterCategory> filter = Descriptors
                .ofFilter(NumberRangeFilter.class);
        
        assertTrue(filter.isDistributable());

        Set<ConfiguredPropertyDescriptor> inputs = filter.getConfiguredPropertiesForInput();
        assertEquals(1, inputs.size());
        ConfiguredPropertyDescriptor input = inputs.iterator().next();
        assertEquals(
                "ConfiguredPropertyDescriptorImpl[name=Column]",
                input.toString());

        assertEquals("java.lang.Number", input.getTypeArgument(0).getName());

        Set<ValidateMethodDescriptor> validateMethods = filter.getValidateMethods();
        assertEquals(1, validateMethods.size());
    }

    public void testFilter() throws Exception {
        NumberRangeFilter filter = new NumberRangeFilter(5d, 10d);
        filter.validate();
        assertEquals(RangeFilterCategory.LOWER, filter.categorize((Number) null));
        assertEquals(RangeFilterCategory.LOWER, filter.categorize(0));
        assertEquals(RangeFilterCategory.LOWER, filter.categorize(-200));
        assertEquals(RangeFilterCategory.VALID, filter.categorize(5));
        assertEquals(RangeFilterCategory.VALID, filter.categorize(5.0));
        assertEquals(RangeFilterCategory.VALID, filter.categorize(5.0f));
        assertEquals(RangeFilterCategory.VALID, filter.categorize(10));
        assertEquals(RangeFilterCategory.VALID, filter.categorize(10.0));
        assertEquals(RangeFilterCategory.VALID, filter.categorize(10.0f));
        assertEquals(RangeFilterCategory.HIGHER, filter.categorize(11));
    }

    public void testSameMaxAndMin() throws Exception {
        NumberRangeFilter filter = new NumberRangeFilter(18d, 18d);
        filter.validate();
        assertEquals(RangeFilterCategory.VALID, filter.categorize(18));
        assertEquals(RangeFilterCategory.VALID, filter.categorize(18.0));
        assertEquals(RangeFilterCategory.VALID, filter.categorize(18.0f));
        assertEquals(RangeFilterCategory.LOWER, filter.categorize(17));
        assertEquals(RangeFilterCategory.HIGHER, filter.categorize(19));
    }
}
