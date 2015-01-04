/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.beans.transform;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.MatchingAndStandardizationCategory;
import org.eobjects.analyzer.beans.categories.ScriptingCategory;
import org.eobjects.analyzer.beans.categories.StringManipulationCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Regex parser")
@Description("Parses strings using a regular expression and transforms it into substrings based on regex groups")
@Categorized({ StringManipulationCategory.class, ScriptingCategory.class, MatchingAndStandardizationCategory.class })
public class RegexParserTransformer implements Transformer<String> {

    @Configured
    InputColumn<String> column;

    @Configured
    @Description("A regular expression containing\ngroup tokens, marked by parantheses.\n\nFor example:\n([a-z]+)_(\\d*)")
    Pattern pattern;

    @Override
    public OutputColumns getOutputColumns() {
        String[] columns = new String[pattern.matcher("").groupCount()];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = column.getName() + " (group " + (i + 1) + ")";
        }
        return new OutputColumns(column.getName() + " (matched part)", columns);
    }

    @Override
    public String[] transform(InputRow inputRow) {
        final Matcher matcher = pattern.matcher("");
        final String value = inputRow.getValue(column);
        final boolean match = value != null && matcher.reset(value).matches();

        String[] result = new String[matcher.groupCount() + 1];
        for (int i = 0; i < result.length; i++) {
            result[i] = match ? matcher.group(i) : null;
        }
        return result;
    }
}
