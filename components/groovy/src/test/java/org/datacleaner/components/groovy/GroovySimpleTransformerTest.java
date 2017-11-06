package org.datacleaner.components.groovy;

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.components.groovy.GroovySimpleTransformer;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;

public class GroovySimpleTransformerTest extends TestCase {

    public void testScenario() throws Exception {
        GroovySimpleTransformer transformer = new GroovySimpleTransformer();

        InputColumn<String> col1 = new MockInputColumn<String>("foo");
        InputColumn<String> col2 = new MockInputColumn<String>("bar");

        transformer.inputs = new InputColumn[] { col1, col2 };
        transformer.code = "class Transformer {\n" + "void initialize(){println(\"hello\")}\n"
                + "String transform(map){println(map); return \"foo\"}\n" + "}";

        transformer.init();

        String[] result = transformer.transform(new MockInputRow().put(col1, "Kasper").put(col2, "Sørensen"));
        assertEquals(1, result.length);
        assertEquals("foo", result[0]);
        
        transformer.close();
    }
}
