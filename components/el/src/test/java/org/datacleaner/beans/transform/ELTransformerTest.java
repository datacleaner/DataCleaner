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
package org.datacleaner.beans.transform;

import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;

import junit.framework.TestCase;

public class ELTransformerTest extends TestCase {

    public void testTransform() throws Exception {
        final ELTransformer transformer = new ELTransformer();
        transformer._expression = "Hello #{name}";
        transformer.init();

        String[] result;

        result = transformer
                .transform(new MockInputRow().put(new MockInputColumn<>("name", String.class), "Donald Duck"));

        assertEquals(1, result.length);
        assertEquals("Hello Donald Duck", result[0]);

        result = transformer.transform(new MockInputRow().put(new MockInputColumn<>("name", String.class), null));

        assertEquals(1, result.length);
        assertEquals("Hello ", result[0]);

        result = transformer.transform(new MockInputRow());

        assertEquals(1, result.length);
        assertEquals(null, result[0]);
    }
}
