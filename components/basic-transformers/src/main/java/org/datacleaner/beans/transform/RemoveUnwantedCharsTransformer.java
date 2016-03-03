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
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.TextCategory;
import org.datacleaner.util.CharIterator;

@Named("Remove unwanted characters")
@Description("Removes characters from strings that are not wanted. Use it to cleanse codes and identifiers that may have additional dashes, punctuations, unwanted letters etc.")
@Categorized(TextCategory.class)
public class RemoveUnwantedCharsTransformer implements Transformer {

    @Configured
    InputColumn<String> column;

    @Configured(order = 1)
    boolean removeWhitespaces = true;

    @Configured(order = 2)
    boolean removeLetters = true;

    @Configured(order = 3)
    boolean removeDigits = false;

    @Configured(order = 4)
    @Description("Remove additional signs, such as dashes, punctuations, slashes and more?")
    boolean removeSigns = true;

    public RemoveUnwantedCharsTransformer() {
    }

    public RemoveUnwantedCharsTransformer(InputColumn<String> inputColumn) {
        column = inputColumn;
    }

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, column.getName() + " (cleansedß)");
    }

    @Override
    public Object[] transform(InputRow row) {
        final String value = row.getValue(column);
        return transform(value);
    }

    public Object[] transform(String value) {
        if (value == null) {
            return new Object[1];
        }
        final CharIterator it = new CharIterator(value);
        while (it.hasNext()) {
            it.next();
            if (it.isWhitespace()) {
                if (removeWhitespaces) {
                    it.remove();
                }
            } else if (it.isLetter()) {
                if (removeLetters) {
                    it.remove();
                }
            } else if (it.isDigit()) {
                if (removeDigits) {
                    it.remove();
                }
            } else if (removeSigns) {
                it.remove();
            }
        }
        return new Object[] { it.toString() };
    }
}
