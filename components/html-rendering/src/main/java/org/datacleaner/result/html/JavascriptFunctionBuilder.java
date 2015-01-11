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
package org.datacleaner.result.html;

/**
 * Helper class for building javascript elements to be included in a
 * {@link HtmlFragment}, typically a subclass of
 * {@link AbstractScriptHeadElement}.
 */
public class JavascriptFunctionBuilder {

    private final String _functionName;
    private final StringBuilder _functionBody;

    public JavascriptFunctionBuilder(String functionName) {
        _functionName = functionName;
        _functionBody = new StringBuilder();
    }

    public String toHeadElementHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<script type=\"text/javascript\">//<![CDATA[\n");
        if (_functionName.indexOf('.') == -1) {
            sb.append("function ");
            sb.append(_functionName);
            sb.append("() {");
        } else {
            sb.append(_functionName);
            sb.append("= function() {");
        }
        sb.append(createFunctionBody());
        sb.append("}\n");
        sb.append("//]]</script>");
        return sb.toString();
    }

    public StringBuilder getFunctionBody() {
        return _functionBody;
    }

    public void append(String code) {
        _functionBody.append(code);
    }

    private String createFunctionBody() {
        return _functionBody.toString();
    }
}
