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
package org.datacleaner.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.ToStringComparator;

public class CollectionUtilsTest extends TestCase {

    public void testArray1() throws Exception {
        String[] result = CollectionUtils.array(new String[] { "foo", "bar" }, "hello", "world");
        assertEquals("[foo, bar, hello, world]", Arrays.toString(result));
    }

    public void testArray2() throws Exception {
        Object existingArray = new Object[] { 'c' };
        Object[] result = CollectionUtils2.array(Object.class, existingArray, "foo", 1, "bar");

        assertEquals("[c, foo, 1, bar]", Arrays.toString(result));
    }

    public void testFilterOnClass() throws Exception {
        Collection<Object> superTypeList = new ArrayList<Object>();
        superTypeList.add(123);
        superTypeList.add("foo");
        superTypeList.add(123);
        superTypeList.add("bar");
        List<String> result = CollectionUtils2.filterOnClass(superTypeList, String.class);
        assertEquals("[foo, bar]", result.toString());
    }

    public void testSorted() throws Exception {
        ArrayList<String> list1 = new ArrayList<String>();
        list1.add("4");
        list1.add("1");
        list1.add("3");
        list1.add("2");

        List<String> list2 = CollectionUtils2.sorted(list1, ToStringComparator.getComparator());
        assertEquals("[4, 1, 3, 2]", list1.toString());
        assertEquals("[1, 2, 3, 4]", list2.toString());
    }

    public void testArrayRemove() throws Exception {
        String[] arr = new String[] { "a", "b", "c", "d", "e" };
        arr = CollectionUtils.arrayRemove(arr, "c");
        assertEquals("[a, b, d, e]", Arrays.toString(arr));

        arr = CollectionUtils.arrayRemove(arr, "e");
        assertEquals("[a, b, d]", Arrays.toString(arr));

        arr = CollectionUtils.arrayRemove(arr, "e");
        assertEquals("[a, b, d]", Arrays.toString(arr));

        arr = CollectionUtils.arrayRemove(arr, "a");
        assertEquals("[b, d]", Arrays.toString(arr));
    }
}
