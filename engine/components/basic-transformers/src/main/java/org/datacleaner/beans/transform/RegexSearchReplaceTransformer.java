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

import org.datacleaner.beans.api.Categorized;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Description;
import org.datacleaner.beans.api.OutputColumns;
import org.datacleaner.beans.api.Transformer;
import org.datacleaner.beans.api.TransformerBean;
import org.datacleaner.beans.categories.StringManipulationCategory;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;

@TransformerBean("Regex search/replace")
@Description("Search and replace text in String values using regular expressions.")
@Categorized({ StringManipulationCategory.class })
public class RegexSearchReplaceTransformer implements Transformer<String> {

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
        return new OutputColumns(valueColumn.getName() + " (replaced '" + searchPattern.pattern() + "')");
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
