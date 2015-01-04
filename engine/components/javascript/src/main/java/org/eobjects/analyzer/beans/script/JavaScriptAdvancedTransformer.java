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
package org.eobjects.analyzer.beans.script;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Close;
import org.eobjects.analyzer.beans.api.Concurrent;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.OutputRowCollector;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.beans.api.StringProperty;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.ScriptingCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A transformer that uses userwritten JavaScript to generate a transformer
 * object
 */
@TransformerBean("JavaScript transformer (advanced)")
@Description("Supply your own piece of JavaScript to do a custom transformation")
@Categorized({ ScriptingCategory.class })
@Concurrent(false)
public class JavaScriptAdvancedTransformer implements Transformer<Object> {

    private static final Logger logger = LoggerFactory.getLogger(JavaScriptAdvancedTransformer.class);

    @Inject
    @Configured
    InputColumn<?>[] columns;

    @Inject
    @Configured
    Class<?>[] returnTypes = new Class[] { String.class, Object.class };

    @Inject
    @Configured
    @StringProperty(multiline = true, mimeType = { "text/javascript", "application/x-javascript" })
    String sourceCode = "var transformerObj = {\n"
            + "\tinitialize: function() {\n\t\tlogger.info('Initializing advanced JavaScript transformer...');\n\t},\n\n"
            + "\ttransform: function(columns,values,outputCollector) {\n\t\tlogger.debug('transform({},{},{}) invoked', columns, values, outputCollector);\n\t\tfor (var i=0;i<columns.length;i++) {\n\t\t\toutputCollector.putValues(columns[i],values[i])\n\t\t}\n\t},\n\n"
            + "\tclose: function() {\n\t\tlogger.info('Closing advanced JavaScript transformer...');\n\t}\n}";

    @Inject
    @Provided
    OutputRowCollector rowCollector;

    private ContextFactory _contextFactory;
    private Script _script;
    private ScriptableObject _sharedScope;
    private NativeObject _transformerObj;

    private Function _initializeFunction;
    private Function _transformFunction;
    private Function _closeFunction;

    @Override
    public OutputColumns getOutputColumns() {
        String[] names = new String[returnTypes.length];
        Class<?>[] types = new Class[returnTypes.length];
        for (int i = 0; i < returnTypes.length; i++) {
            names[i] = "JavaScript output " + (i + 1);
            types[i] = returnTypes[i];
        }
        OutputColumns outputColumns = new OutputColumns(names, types);
        return outputColumns;
    }

    @Initialize
    public void init() {
        _contextFactory = new ContextFactory();

        Context context = _contextFactory.enterContext();
        try {
            _script = context.compileString(sourceCode, this.getClass().getSimpleName(), 1, null);
            _sharedScope = context.initStandardObjects();

            JavaScriptUtils.addToScope(_sharedScope, logger, "logger", "log");
            JavaScriptUtils.addToScope(_sharedScope, System.out, "out");

            _script.exec(context, _sharedScope);
            _transformerObj = (NativeObject) _sharedScope.get("transformerObj");
            if (_transformerObj == null) {
                throw new IllegalStateException("Required JS object 'transformerObj' not found!");
            }

            _initializeFunction = (Function) _transformerObj.get("initialize");
            _transformFunction = (Function) _transformerObj.get("transform");
            _closeFunction = (Function) _transformerObj.get("close");

            _initializeFunction.call(context, _sharedScope, _sharedScope, new Object[0]);
        } finally {
            Context.exit();
        }
    }

    @Close
    public void close() {
        Context context = _contextFactory.enterContext();
        try {
            _closeFunction.call(context, _sharedScope, _sharedScope, new Object[0]);
        } finally {
            Context.exit();
        }
    }

    @Override
    public Object[] transform(InputRow inputRow) {
        Context context = _contextFactory.enterContext();
        try {
            String[] columnNames = new String[columns.length];
            Object[] values = new Object[columns.length];
            for (int i = 0; i < columns.length; i++) {
                columnNames[i] = columns[i].getName();
                values[i] = inputRow.getValue(columns[i]);
            }
            Object[] args = { columnNames, values, rowCollector };
            _transformFunction.call(context, _sharedScope, _sharedScope, args);
            return null;
        } finally {
            Context.exit();
        }
    }
}
