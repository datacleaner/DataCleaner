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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.components.groovy.GroovyAdvancedTransformer;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.job.concurrent.ThreadLocalOutputRowCollector;
import org.datacleaner.job.concurrent.ThreadLocalOutputRowCollector.Listener;

public class GroovyAdvancedTransformerTest extends TestCase {

    public void testScenario() throws Exception {
        final GroovyAdvancedTransformer transformer = new GroovyAdvancedTransformer();

        final InputColumn<String> col1 = new MockInputColumn<String>("foo");
        final InputColumn<String> col2 = new MockInputColumn<String>("bar");

        final AtomicInteger rowsCollected = new AtomicInteger(0);

        final ThreadLocalOutputRowCollector outputRowCollector = new ThreadLocalOutputRowCollector();
        outputRowCollector.setListener(new Listener() {
            public void onValues(Object[] values) {
                rowsCollected.incrementAndGet();

                assertEquals(2, values.length);

                final String str = Arrays.toString(values);
                assertTrue(str, "[foo, Kasper]".equals(str) || "[bar, Sørensen]".equals(str));
            }
        });

        transformer.inputs = new InputColumn[] { col1, col2 };
        transformer._outputRowCollector = outputRowCollector;
        transformer.init();

        final String[] result = transformer.transform(new MockInputRow().put(col1, "Kasper").put(col2, "Sørensen"));
        assertNull(result);

        assertEquals(2, rowsCollected.get());

        transformer.close();

    }
}
