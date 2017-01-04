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

import java.util.StringTokenizer;

import javax.inject.Named;

import org.apache.metamodel.util.HasName;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.TextCategory;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.BreakIterator;

@Named("Text case transformer")
@Description("Modifies the text case/capitalization of Strings.")
@Categorized(TextCategory.class)
public class TextCaseTransformer implements Transformer {

    /**
     * Enum depicting the modes of operation for the text case modifications.
     */
    public enum TransformationMode implements HasName {

        LOWER_CASE("Lower case"),

        UPPER_CASE("Upper case"),

        CAPITALIZE_SENTENCES("Capitalize sentences"),

        CAPITALIZE_WORDS("Capitalize every word");

        private final String _name;

        TransformationMode(final String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
        }

    }

    @Configured("Value")
    InputColumn<String> valueColumn;

    @Configured
    TransformationMode mode = TransformationMode.UPPER_CASE;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, valueColumn.getName() + " (" + mode.getName() + ")");
    }

    @Override
    public String[] transform(final InputRow row) {
        final String value = row.getValue(valueColumn);
        final String[] result = new String[1];
        result[0] = transform(value);
        return result;
    }

    public String transform(final String value) {
        if (value == null) {
            return null;
        }

        switch (mode) {
        case UPPER_CASE:
            return UCharacter.toUpperCase(value);
        case LOWER_CASE:
            return UCharacter.toLowerCase(value);
        case CAPITALIZE_SENTENCES:
            return UCharacter.toTitleCase(value, BreakIterator.getSentenceInstance());
        case CAPITALIZE_WORDS:
            return UCharacter.toTitleCase(value, BreakIterator.getWordInstance());
        default:
            throw new UnsupportedOperationException("Unsupported mode: " + mode);
        }
    }
}

