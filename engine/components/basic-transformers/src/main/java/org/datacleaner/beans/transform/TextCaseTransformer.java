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

import org.datacleaner.beans.api.Categorized;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Description;
import org.datacleaner.beans.api.OutputColumns;
import org.datacleaner.beans.api.Transformer;
import org.datacleaner.beans.api.TransformerBean;
import org.datacleaner.beans.categories.StringManipulationCategory;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;
import org.apache.metamodel.util.HasName;

@TransformerBean("Text case transformer")
@Description("Modifies the text case/capitalization of Strings.")
@Categorized({ StringManipulationCategory.class })
public class TextCaseTransformer implements Transformer<String> {

    /**
     * Enum depicting the modes of operation for the text case modifications.
     */
    public static enum TransformationMode implements HasName {

        LOWER_CASE("Lower case"),

        UPPER_CASE("Upper case"),

        CAPITALIZE_SENTENCES("Capitalize sentences"),

        CAPITALIZE_WORDS("Capitalize every word");

        private final String _name;

        private TransformationMode(String name) {
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
    TransformationMode mode;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(valueColumn.getName() + " (" + mode.getName() + ")");
    }

    @Override
    public String[] transform(InputRow row) {
        final String value = row.getValue(valueColumn);
        final String[] result = new String[1];
        result[0] = transform(value);
        return result;
    }

    public String transform(String value) {
        if (value == null) {
            return null;
        }

        switch (mode) {
        case UPPER_CASE:
            return value.toUpperCase();
        case LOWER_CASE:
            return value.toLowerCase();
        case CAPITALIZE_SENTENCES:
        case CAPITALIZE_WORDS:
            return capitalizeWords(value);
        default:
            throw new UnsupportedOperationException("Unsupported mode: " + mode);
        }
    }

    private String capitalizeWords(String value) {
        final StringBuilder sb = new StringBuilder();
        final StringTokenizer tokenizer = new StringTokenizer(value, " \t\n\r\f", true);
        boolean capitalizeNext = true;
        while (tokenizer.hasMoreTokens()) {
            final String nextToken = tokenizer.nextToken();
            final String lowerCasedToken = nextToken.toLowerCase();
            if (capitalizeNext) {
                final String capitalizedToken = Character.toUpperCase(lowerCasedToken.charAt(0))
                        + lowerCasedToken.substring(1);
                sb.append(capitalizedToken);
            } else {
                sb.append(lowerCasedToken);
            }
            final char lastChar = lowerCasedToken.charAt(lowerCasedToken.length() - 1);
            capitalizeNext = (mode == TransformationMode.CAPITALIZE_WORDS) || isCapitalizeTrigger(lastChar);
        }

        return sb.toString();
    }

    private boolean isCapitalizeTrigger(char c) {
        return c == '.' || c == '!' || c == '?' || c == ':';
    }
}
