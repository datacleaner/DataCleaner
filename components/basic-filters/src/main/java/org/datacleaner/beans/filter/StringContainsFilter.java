/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.beans.filter;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Filter;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.components.categories.FilterCategory;

@Named("String contains filter")
@Description("Check if your string values contain specific substrings in order to filter them in or out.")
@Categorized(FilterCategory.class)
public class StringContainsFilter implements Filter<StringContainsFilter.Category> {

    public enum Category {
        @Description("Outcome when a substring is matched/contained in the input string.")
        MATCHED,

        @Description("Outcome when none of the substrings are matched/contained in the input string.")
        UNMATCHED
    }

    @Configured(order = 1)
    InputColumn<String> column;

    @Configured(order = 2)
    @Description("Substring(s) to look for")
    String[] substrings;

    @Configured(order = 3)
    boolean caseSensitive = false;

    @Override
    public Category categorize(final InputRow inputRow) {
        final String value = inputRow.getValue(column);
        return categorize(value);
    }

    private Category categorize(final String value) {
        if (value == null) {
            return Category.UNMATCHED;
        }
        final String normalizedValue = caseSensitive ? value : value.toLowerCase();

        for (final String substring : substrings) {
            final String containedPart = caseSensitive ? substring : substring.toLowerCase();
            if (normalizedValue.contains(containedPart)) {
                return Category.MATCHED;
            }
        }
        return Category.UNMATCHED;
    }
}
