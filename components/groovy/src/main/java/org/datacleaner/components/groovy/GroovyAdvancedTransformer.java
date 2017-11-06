package org.datacleaner.components.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Close;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.NumberProperty;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.api.Provided;
import org.datacleaner.api.StringProperty;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.ScriptingCategory;
import org.datacleaner.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("Groovy transformer (advanced)")
@Categorized(ScriptingCategory.class)
@Description("Perform almost any kind of data transformation with the use of the Groovy language. This transformer includes advanced options to map records to multiple (or no) output records and more.")
public class GroovyAdvancedTransformer implements Transformer {

    private static final Logger logger = LoggerFactory.getLogger(GroovyAdvancedTransformer.class);

    @Configured(order = 1)
    InputColumn<?>[] inputs;

    @Configured(order = 2)
    @Description("Execute the transformation code in a concurrent manner?")
    boolean concurrent = true;

    @Configured(order = 3)
    @NumberProperty(negative = false)
    @Description("The number of field to expect in the output")
    int outputFields = 2;

    @Configured(order = 4)
    @StringProperty(multiline = true, mimeType = { "application/x-groovy", "text/x-groovy", "text/groovy" })
    String code = "class Transformer {\n\tvoid initialize() {\n\t\t// Optional initializer\n\t}\n\n\tvoid transform(map, outputCollector) {\n\t\t// Example: Makes an output record for each field+value in input\n\t\tmap.each{\n\t\t\tk, v -> outputCollector.putValues(k, v)\n\t\t};\n\t}\n\n\tvoid close() {\n\t\t// Optional destroyer\n\t}\n}";

    @Inject
    @Provided
    OutputRowCollector _outputRowCollector;

    private GroovyObject _groovyObject;
    private GroovyClassLoader _groovyClassLoader;

    @Initialize
    public void init() {
        ClassLoader parent = getClass().getClassLoader();
        _groovyClassLoader = new GroovyClassLoader(parent);
        logger.debug("Compiling Groovy code:\n{}", code);
        final Class<?> groovyClass = _groovyClassLoader.parseClass(code);
        _groovyObject = (GroovyObject) ReflectionUtils.newInstance(groovyClass);
        _groovyObject.invokeMethod("initialize", new Object[] {});
    }

    @Close
    public void close() {
        _groovyObject.invokeMethod("close", new Object[] {});
        _groovyObject = null;
        _groovyClassLoader.clearCache();
        _groovyClassLoader = null;
    }

    public OutputColumns getOutputColumns() {
        String[] names = new String[outputFields];
        for (int i = 0; i < outputFields; i++) {
            names[i] = "Groovy output (" + (i + 1) + ")";
        }
        return new OutputColumns(String.class, names);
    }

    public String[] transform(InputRow inputRow) {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (InputColumn<?> input : inputs) {
            map.put(input.getName(), inputRow.getValue(input));
        }
        final Object[] args = new Object[] { map, _outputRowCollector };
        if (concurrent) {
            _groovyObject.invokeMethod("transform", args);
        } else {
            synchronized (_groovyObject) {
                _groovyObject.invokeMethod("transform", args);
            }
        }
        return null;
    }

}
