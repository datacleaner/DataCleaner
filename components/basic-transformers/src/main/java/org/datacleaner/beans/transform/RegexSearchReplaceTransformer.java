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
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.TextCategory;

@Named("Regex search/replace")
@Description("Search and replace text in String values using regular expressions.")
@Categorized(TextCategory.class)
public class RegexSearchReplaceTransformer implements Transformer {

    @Configured(value = "Value", order = 1)
    InputColumn<String> valueColumn;

    @Configured(order = 2)
    @Description("Regular expression pattern used for searching. Eg. 'Mr\\. (\\w+)'")
    Pattern searchPattern;

    @Configured(order = 3)
    @Description("Regular expression pattern used for replacement. Eg. 'Mister $1'")
    Pattern replacementPattern;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, valueColumn.getName() + " (replaced '" + searchPattern.pattern() + "')");
    }

    @Override
    public String[] transform(InputRow row) {
        final String[] result = new String[1];
        final String value = row.getValue(valueColumn);
        if (value == null) {
            return result;
        }

        final Matcher matcher = searchPattern.matcher(value);
        final String replacedString = matcher.replaceAll(replacementPattern.pattern());
        result[0] = replacedString;

        return result;
    }

}
