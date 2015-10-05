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

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.StringProperty;
import org.datacleaner.api.Transformer;
import org.datacleaner.api.Validate;
import org.datacleaner.components.categories.TextCategory;

@Named("Plain search/replace")
@Description("Search and replace text in String values.")
@Categorized(TextCategory.class)
public class PlainSearchReplaceTransformer implements Transformer{

    @Configured(value = "Value", order = 1)
    InputColumn<String> valueColumn;

    @Configured(order = 2)
    String searchString;

    @Configured(order = 3)
    @StringProperty(emptyString = true)
    String replacementString = "";

    @Configured(order = 4)
    @Description("Replace the entire string when the search string is found.")
    boolean replaceEntireString = false;
    
    @Validate
    public void validate() {
        if (!replaceEntireString && replacementString.indexOf(searchString) != -1) {
            throw new IllegalArgumentException("Replacement string cannot contain the search string (implies an infinite replacement loop)");
        }
    }

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, valueColumn.getName() + " (replaced '" + searchString + "')");
    }

    @Override
    public String[] transform(InputRow row) {
        final String[] result = new String[1];
        String value = row.getValue(valueColumn);
        if (value == null) {
            return result;
        }

        if (replaceEntireString) {
            if (matchesSearchString(value)) {
                value = replacementString;
            }
        } else {
            while (matchesSearchString(value)) {
                value = value.replace(searchString, replacementString);
            }
        }
        result[0] = value;

        return result;
    }

    private boolean matchesSearchString(String value) {
        return value.indexOf(searchString) != -1;
    }

}
