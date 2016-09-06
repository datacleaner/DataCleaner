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

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.NumberProperty;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.TextCategory;
import org.apache.metamodel.util.HasName;

/**
 * Tokenizes values into a configurable amount of tokens.
 *
 *
 */
@Named("Tokenizer")
@Description("Tokenizes a String value (splits into substrings).")
@Categorized(TextCategory.class)
public class TokenizerTransformer implements Transformer {

    public static enum TokenTarget implements HasName {
        COLUMNS, ROWS;

        @Override
        public String getName() {
            if (this == COLUMNS) {
                return "Columns";
            } else {
                return "Rows";
            }
        }
    }

    @Inject
    @Configured("Number of tokens")
    @Description("Defines the max amount of tokens to expect")
    @NumberProperty(zero = false, negative = false)
    Integer numTokens = 2;

    @Inject
    @Configured
    InputColumn<String> column;

    @Inject
    @Configured
    @Description("Characters to tokenize by")
    char[] delimiters = new char[] { ' ', '\t', '\n', '\r', '\f' };

    @Inject
    @Configured
    @Description("Add tokens as columns or as separate rows?")
    TokenTarget tokenTarget = TokenTarget.COLUMNS;

    @Inject
    @Provided
    OutputRowCollector outputRowCollector;

    public TokenizerTransformer() {
    }

    public TokenizerTransformer(InputColumn<String> column, Integer numTokens) {
        this.column = column;
        this.numTokens = numTokens;
    }

    @Override
    public OutputColumns getOutputColumns() {
        if (tokenTarget == TokenTarget.COLUMNS) {
            String[] names = new String[numTokens];
            for (int i = 0; i < names.length; i++) {
                names[i] = column.getName() + " (token " + (i + 1) + ")";
            }
            return new OutputColumns(String.class, names);
        } else {
            return new OutputColumns(String.class, column.getName() + " (token)");
        }
    }

    @Override
    public String[] transform(InputRow inputRow) {
        String value = inputRow.getValue(column);
        String[] result = new String[numTokens];

        if (value != null) {
            int i = 0;
            StringTokenizer st = new StringTokenizer(value, new String(delimiters));
            while (i < result.length && st.hasMoreTokens()) {
                result[i] = st.nextToken();
                i++;
            }
        }

        if (tokenTarget == TokenTarget.COLUMNS) {
            return result;
        } else {
            for (int i = 0; i < result.length; i++) {
                if (result[i] != null) {
                    outputRowCollector.putValues(result[i]);
                }
            }
            return null;
        }
    }

}
