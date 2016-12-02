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

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Filter;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.StringProperty;
import org.datacleaner.components.categories.ScriptingCategory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

@Named("JavaScript filter")
@Description(
        "Supply your own piece of JavaScript that evaluates whether rows should be included or excluded from processing.")
@Categorized(ScriptingCategory.class)
public class JavaScriptFilter implements Filter<JavaScriptFilter.Category> {

    public enum Category {
        VALID, INVALID
    }

    @Configured
    InputColumn<?>[] columns;

    @Configured
    @Description("Available variables:\nvalues[0..]: Array of values\nvalues[\"my_col\"]: Map of values\nmy_col: "
            + "Each column value has it's own variable\nout: Print to console using out.println('hello')\n"
            + "logger: Print to log using log.info(...), log.warn(...), log.error(...)")
    @StringProperty(multiline = true, mimeType = { "text/javascript", "application/x-javascript" })
    String sourceCode = "function eval() {\n  return values[0] != null;\n}\n\neval();";

    private ContextFactory _contextFactory;
    private Script _script;

    // this scope is shared between all threads
    private ScriptableObject _sharedScope;

    @Initialize
    public void init() {
        _contextFactory = new ContextFactory();
        final Context context = _contextFactory.enterContext();

        try {
            _script = context.compileString(sourceCode, this.getClass().getSimpleName(), 1, null);
            _sharedScope = context.initStandardObjects();

            JavaScriptUtils.addToScope(_sharedScope, new JavaScriptLogger(), "logger", "log");
            JavaScriptUtils.addToScope(_sharedScope, System.out, "out");
        } finally {
            Context.exit();
        }
    }

    @Override
    public Category categorize(final InputRow inputRow) {
        final Context context = _contextFactory.enterContext();

        try {

            // this scope is local to the execution of a single row
            final Scriptable scope = context.newObject(_sharedScope);
            scope.setPrototype(_sharedScope);
            scope.setParentScope(null);

            JavaScriptUtils.addToScope(scope, inputRow, columns, "values");

            final Object result = _script.exec(context, scope);
            final boolean booleanResult = Context.toBoolean(result);

            if (booleanResult) {
                return JavaScriptFilter.Category.VALID;
            }
            return JavaScriptFilter.Category.INVALID;
        } finally {
            Context.exit();
        }
    }

    public void setSourceCode(final String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public void setColumns(final InputColumn<?>[] columns) {
        this.columns = columns;
    }
}
