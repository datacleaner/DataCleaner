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
package org.datacleaner.components.groovy;

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.components.groovy.GroovySimpleTransformer;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;

public class GroovySimpleTransformerTest extends TestCase {

    public void testScenario() throws Exception {
        final GroovySimpleTransformer transformer = new GroovySimpleTransformer();

        final InputColumn<String> col1 = new MockInputColumn<String>("foo");
        final InputColumn<String> col2 = new MockInputColumn<String>("bar");

        transformer.inputs = new InputColumn[] { col1, col2 };
        transformer.code = "class Transformer {\n" + "void initialize(){println(\"hello\")}\n"
                + "String transform(map){println(map); return \"foo\"}\n" + "}";

        transformer.init();

        final String[] result = transformer.transform(new MockInputRow().put(col1, "Kasper").put(col2, "Sørensen"));
        assertEquals(1, result.length);
        assertEquals("foo", result[0]);

        transformer.close();
    }
}
