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

import org.apache.metamodel.util.HasName;
import org.datacleaner.api.Alias;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.ExternalDocumentation;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.api.ExternalDocumentation.DocumentationLink;
import org.datacleaner.api.ExternalDocumentation.DocumentationType;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.TextCategory;

@Named("Regex parser")
@Description("Parses strings using a regular expression and transforms it into substrings based on regex groups."
        + "A few examples:" + "<ul>"
        + "<li>Match 3-dimensional size specification (e.g. '42x61x3') anywhere in a string:"
        + "<blockquote>.*(\\d+)x(\\d+)x(\\d+).*</blockquote></li>" + "<li>Match two words:"
        + "<blockquote>(\\w+) (\\w+)</blockquote></li>"
        + "<li>Match a hash-sign and 3 pairs of hexadecimal digits (using pseudo-characters of Java regular expressions):"
        + "<blockquote>\\#?(\\p{XDigit}{2})(\\p{XDigit}{2})(\\p{XDigit}{2})</blockquote></li>" + "</ul>")
@ExternalDocumentation({
        @DocumentationLink(title = "Regex parsing with DataCleaner", url = "https://www.youtube.com/watch?v=VA6dw5Nv2AM", type = DocumentationType.VIDEO, version = "3.0"),
        @DocumentationLink(title = "Java Tutorials: Regular Expressions Lesson", url = "https://docs.oracle.com/javase/tutorial/essential/regex/", type = DocumentationType.TECH, version = "3.0") })
@Categorized(TextCategory.class)
public class RegexParserTransformer implements Transformer {

    public static enum Mode implements HasName {
        @Description("Find the first match within the value.") FIND_FIRST("Find first match"),

        @Description("Find all matches of the expression within the value. Each match yields a new row in the data stream.") FIND_ALL(
                "Find all matches"),

        @Description("Match the complete value using the expression.") FULL_MATCH("Match the complete value");

        private final String _name;

        private Mode(String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
        }
    }

    @Configured("Value")
    @Alias("Column")
    InputColumn<String> column;

    @Configured
    @Description("A regular expression containing group tokens, marked by parantheses.\nExample: ([a-z]+)_(\\d*)")
    Pattern pattern;

    @Configured
    @Description("The expression-and-value matching mode employed")
    Mode mode = Mode.FULL_MATCH;

    @Provided
    OutputRowCollector outputRowCollector;

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
        final String value = inputRow.getValue(column);
        final Matcher matcher;
        final boolean match;
        if (value == null) {
            matcher = pattern.matcher("");
            match = false;
        } else {
            matcher = pattern.matcher(value);
            switch (mode) {
            case FULL_MATCH:
                match = matcher.matches();
                break;
            case FIND_FIRST:
            case FIND_ALL:
                match = matcher.find();
                break;
            default:
                throw new UnsupportedOperationException();
            }
        }

        final String[] result = new String[matcher.groupCount() + 1];
        for (int i = 0; i < result.length; i++) {
            result[i] = match ? matcher.group(i) : null;
        }

        if (mode == Mode.FIND_ALL) {
            while (matcher.find()) {
                final Object[] nextResult = new Object[matcher.groupCount() + 1];
                for (int i = 0; i < nextResult.length; i++) {
                    nextResult[i] = matcher.group(i);
                }
                outputRowCollector.putValues(nextResult);
            }
        }

        return result;
    }
}
