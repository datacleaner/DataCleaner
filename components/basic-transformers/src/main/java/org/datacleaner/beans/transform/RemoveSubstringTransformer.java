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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.TextCategory;
import org.datacleaner.components.convert.ConvertToStringTransformer;

@Named("Remove substring")
@Description("Subtracts one or more substrings from a base text, i.e. [\"Hello world\",\"World\"] would yield \"Hello\".")
@Categorized(TextCategory.class)
public class RemoveSubstringTransformer implements Transformer {

    @Configured("Base text column")
    @Description("Column containing the text to subtract from")
    InputColumn<String> baseColumn;

    @Configured("Substring columns")
    @Description("Columns containing the substrings to remove from the base text")
    InputColumn<?>[] substringColumns;

    @Configured(value = "Match whole words only", required = false)
    @Description("If set, only whole words (surrounded by whitespace or punctuation) will be removed.\n"
            + " This prevents removing partial words.")
    boolean wholeWordsOnly = false;

    @Configured
    @Description("Should substring matching be case-sensitive or not?")
    boolean caseSensitive = true;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, baseColumn.getName() + " (substring removed)");
    }

    @Override
    public String[] transform(final InputRow inputRow) {
        String subtractedString = inputRow.getValue(baseColumn);
        for (final InputColumn<?> inputColumn : substringColumns) {
            final Object value = inputRow.getValue(inputColumn);
            if (value instanceof List) {
                for (final Object element : (List<?>) value) {
                    subtractedString = subtract(subtractedString, element);
                }
            } else {
                subtractedString = subtract(subtractedString, value);
            }
        }

        return new String[] { subtractedString };
    }

    private String subtract(final String subtractedString, final Object element) {
        if (element == null || subtractedString == null) {
            return subtractedString;
        }

        final String substring = (caseSensitive ? ConvertToStringTransformer.transformValue(element)
                : ConvertToStringTransformer.transformValue(element).toLowerCase());
        String resultingString = subtractedString;
        
        if (caseSensitive && !wholeWordsOnly) {
            // special case where we can do a very easy/effective
            // String.replace(..) operation
            return resultingString.replace(substring, "");
        }
        
        String matchedString = (caseSensitive ? resultingString : resultingString.toLowerCase());

        final Pattern substringPattern;
        if (wholeWordsOnly) {
            substringPattern = Pattern.compile("\\b" + Pattern.quote(substring) + "\\b");
        } else {
            substringPattern = Pattern.compile(Pattern.quote(substring));
        }

        Matcher matcher = substringPattern.matcher(matchedString);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            resultingString = resultingString.substring(0, start) + resultingString.substring(end);
            matchedString = (caseSensitive ? resultingString : resultingString.toLowerCase());
            matcher = substringPattern.matcher(matchedString);
        }

        return resultingString;
    }
}
