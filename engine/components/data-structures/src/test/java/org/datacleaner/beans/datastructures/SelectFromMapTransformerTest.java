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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.datacleaner.data.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;

public class SelectFromMapTransformerTest extends TestCase {

    public void testTransform() throws Exception {
        final SelectFromMapTransformer trans = new SelectFromMapTransformer();
        final InputColumn<Map<String, ?>> col = new MockInputColumn<Map<String, ?>>("foo");
        trans.mapColumn = col;
        trans.keys = new String[] { "id", "Name.GivenName", "email.address", "Name.FamilyName",
                "Name.Something.That.Does.Not.Exist", "Addresses[1].street" };
        trans.types = new Class[] { Integer.class, String.class, String.class, String.class, String.class, String.class };
        trans.verifyTypes = true;

        assertEquals(
                "OutputColumns[id, Name.GivenName, email.address, Name.FamilyName, Name.Something.That.Does.Not.Exist, Addresses[1].street]",
                trans.getOutputColumns().toString());

        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", 1001);
        map.put("email.address", "foo@bar.com");

        final Map<String, Object> nestedMap = new HashMap<String, Object>();
        nestedMap.put("GivenName", "John");
        nestedMap.put("FamilyName", "Doe");
        nestedMap.put("Titulation", "Mr");
        map.put("Name", nestedMap);

        final List<Map<String, Object>> nestedList = new ArrayList<Map<String, Object>>();
        final Map<String, Object> address1 = new HashMap<String, Object>();
        address1.put("street", "Warwick Avenue");
        nestedList.add(address1);
        final Map<String, Object> address2 = new HashMap<String, Object>();
        address2.put("street", "Fifth Avenue");
        nestedList.add(address2);
        map.put("Addresses", nestedList);

        Object[] result = trans.transform(new MockInputRow().put(col, map));

        assertEquals(6, result.length);
        assertEquals("[1001, John, foo@bar.com, Doe, null, Fifth Avenue]", Arrays.toString(result));

        nestedList.remove(0);

        result = trans.transform(new MockInputRow().put(col, map));

        assertEquals(6, result.length);
        assertEquals("[1001, John, foo@bar.com, Doe, null, null]", Arrays.toString(result));
    }

    public void testFindWithKeyIncludingDotAndNestedMap() throws Exception {
        final Map<String, Object> nestedMap = new HashMap<String, Object>();
        nestedMap.put("GivenName", "John");
        nestedMap.put("FamilyName", "Doe");
        nestedMap.put("Titulation", "Mr");

        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("Person.Name", nestedMap);

        assertEquals("John", SelectFromMapTransformer.find(map, "Person.Name.GivenName").toString());
    }
}
