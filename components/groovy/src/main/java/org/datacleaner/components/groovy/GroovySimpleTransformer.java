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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Close;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.StringProperty;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.ScriptingCategory;
import org.datacleaner.components.convert.ConvertToStringTransformer;
import org.datacleaner.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("Groovy transformer (simple)")
@Categorized(ScriptingCategory.class)
@Description("Perform a data transformation with the use of the Groovy language.")
public class GroovySimpleTransformer implements Transformer {

    private static final Logger logger = LoggerFactory.getLogger(GroovySimpleTransformer.class);

    @Configured(order = 1)
    InputColumn<?>[] inputs;

    @Configured(order = 3)
    @StringProperty(multiline = true, mimeType = { "application/x-groovy", "text/x-groovy", "text/groovy" })
    String code = "class Transformer {\n\tString transform(map) {\n"
            + "\t\t// Example: Finds the first value of a column with the word 'NAME' in it\n"
            + "\t\treturn \"Hello \" + map.find{\n\t\t\tit.key.toUpperCase().indexOf(\"NAME\")!=-1\n"
            + "\t\t}?.value\n\t}\n}";

    private GroovyObject _groovyObject;
    private GroovyClassLoader _groovyClassLoader;

    @Initialize
    public void init() {
        final ClassLoader parent = getClass().getClassLoader();
        _groovyClassLoader = new GroovyClassLoader(parent);
        logger.debug("Compiling Groovy code:\n{}", code);
        final Class<?> groovyClass = _groovyClassLoader.parseClass(code);
        _groovyObject = (GroovyObject) ReflectionUtils.newInstance(groovyClass);
    }

    @Close
    public void close() {
        _groovyObject = null;
        _groovyClassLoader.clearCache();
        _groovyClassLoader = null;
    }

    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, "Groovy output");
    }

    public String[] transform(final InputRow inputRow) {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (InputColumn<?> input : inputs) {
            map.put(input.getName(), inputRow.getValue(input));
        }
        final Object[] args = new Object[] { map };
        final Object result;
        result = _groovyObject.invokeMethod("transform", args);

        logger.debug("Transformation result: {}", result);
        final String stringResult = ConvertToStringTransformer.transformValue(result);
        return new String[] { stringResult };
    }

}
