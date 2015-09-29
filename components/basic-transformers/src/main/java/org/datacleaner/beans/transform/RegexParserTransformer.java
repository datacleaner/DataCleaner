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
package org.datacleaner.beans.transform;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.ExternalDocumentation;
import org.datacleaner.api.ExternalDocumentation.DocumentationLink;
import org.datacleaner.api.ExternalDocumentation.DocumentationType;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.TextCategory;

@Named("Regex parser")
@Description("Parses strings using a regular expression and transforms it into substrings based on regex groups")
@ExternalDocumentation({ @DocumentationLink(title = "Regex parsing with DataCleaner", url = "https://www.youtube.com/watch?v=VA6dw5Nv2AM", type = DocumentationType.VIDEO, version = "3.0") })
@Categorized(TextCategory.class)
public class RegexParserTransformer implements Transformer {

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
        return new OutputColumns(String.class, column.getName() + " (matched part)", columns);
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
