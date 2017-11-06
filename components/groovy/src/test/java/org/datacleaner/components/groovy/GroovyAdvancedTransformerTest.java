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
        GroovyAdvancedTransformer transformer = new GroovyAdvancedTransformer();

        InputColumn<String> col1 = new MockInputColumn<String>("foo");
        InputColumn<String> col2 = new MockInputColumn<String>("bar");

        final AtomicInteger rowsCollected = new AtomicInteger(0);
        
        ThreadLocalOutputRowCollector outputRowCollector = new ThreadLocalOutputRowCollector();
        outputRowCollector.setListener(new Listener() {
            public void onValues(Object[] values) {
                rowsCollected.incrementAndGet();
                
                assertEquals(2, values.length);
                
                String str = Arrays.toString(values);
                assertTrue(str, "[foo, Kasper]".equals(str) || "[bar, Sørensen]".equals(str));
            }
        });

        transformer.inputs = new InputColumn[] { col1, col2 };
        transformer._outputRowCollector = outputRowCollector;
        transformer.init();

        String[] result = transformer.transform(new MockInputRow().put(col1, "Kasper").put(col2, "Sørensen"));
        assertNull(result);
        
        assertEquals(2, rowsCollected.get());
        
        transformer.close();
        
    }
}
