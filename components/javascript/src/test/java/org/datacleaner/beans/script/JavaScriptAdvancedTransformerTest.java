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
package org.datacleaner.beans.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.job.AbstractOutputRowCollector;

public class JavaScriptAdvancedTransformerTest extends TestCase {

    public void testCompileScript() throws Exception {
        final InputColumn<String> col1 = new MockInputColumn<String>("col1");
        final InputColumn<String> col2 = new MockInputColumn<String>("col2");

        final List<String> output = new ArrayList<String>();
        final OutputRowCollector collector = new AbstractOutputRowCollector() {
            @Override
            public void putValues(Object... arg0) {
                output.add(Arrays.toString(arg0));
            }
        };

        JavaScriptAdvancedTransformer transformer = new JavaScriptAdvancedTransformer();
        transformer.rowCollector = collector;
        transformer.columns = new InputColumn[] { col1, col2 };
        transformer.init();

        assertNull(transformer.transform(new MockInputRow().put(col1, "foo").put(col2, "bar")));
        assertEquals("[[col1, foo], [col2, bar]]", output.toString());
        output.clear();
        transformer.close();
    }
}
