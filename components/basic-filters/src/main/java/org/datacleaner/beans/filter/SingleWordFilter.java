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
package org.datacleaner.beans.filter;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Filter;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.components.categories.FilterCategory;
import org.datacleaner.util.CharIterator;

@Named("Single word")
@Description("Filters single word values from multiple word values.")
@Categorized(FilterCategory.class)
@Deprecated
public class SingleWordFilter implements Filter<ValidationCategory> {

    @Configured
    InputColumn<String> input;

    @Override
    public ValidationCategory categorize(final InputRow inputRow) {
        final String value = inputRow.getValue(input);
        return filter(value);
    }

    protected ValidationCategory filter(final String value) {
        if (value == null || value.length() == 0) {
            return ValidationCategory.INVALID;
        }
        final CharIterator it = new CharIterator(value);
        while (it.hasNext()) {
            it.next();
            if (!it.isLetter()) {
                return ValidationCategory.INVALID;
            }
        }
        return ValidationCategory.VALID;
    }

}
