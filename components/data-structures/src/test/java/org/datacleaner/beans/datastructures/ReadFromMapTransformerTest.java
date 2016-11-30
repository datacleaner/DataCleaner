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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.job.AbstractOutputRowCollector;

import junit.framework.TestCase;

public class ReadFromMapTransformerTest extends TestCase {

    public void testTransform() throws Exception {
        final List<Object[]> result = new ArrayList<>();
        final ReadFromMapTransformer trans = new ReadFromMapTransformer();
        final InputColumn<Map<String, ?>> col = new MockInputColumn<>("foo");
        trans.mapColumn = col;
        trans.valueType = Integer.class;
        trans.verifyTypes = true;
        trans.outputRowCollector = new AbstractOutputRowCollector() {
            @Override
            public void putValues(final Object... values) {
                result.add(values);
            }
        };

        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("foo", 1);
        map.put("bar", 2);
        map.put("baz", 3);

        final Object[] transformerOutput = trans.transform(new MockInputRow().put(col, map));

        assertNull(transformerOutput);
        assertEquals(3, result.size());
        assertEquals("[foo, 1]", Arrays.toString(result.get(0)));
        assertEquals("[bar, 2]", Arrays.toString(result.get(1)));
        assertEquals("[baz, 3]", Arrays.toString(result.get(2)));
    }

}
