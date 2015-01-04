/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.beans.transform;

import java.util.StringTokenizer;

import org.eobjects.analyzer.beans.api.Alias;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.util.HasLabelAdvice;

/**
 * A simple transformer that uses a synonym catalog to replace a synonym with
 * it's master term.
 * 
 * 
 */
@TransformerBean("Synonym lookup")
@Alias("Synonym replacement")
@Description("Replaces strings with their synonyms")
public class SynonymLookupTransformer implements Transformer<String>, HasLabelAdvice {

    @Configured
    InputColumn<String> column;

    @Configured
    SynonymCatalog synonymCatalog;

    @Configured
    @Description("Retain original value in case no synonym is found (otherwise null)")
    boolean retainOriginalValue = true;

    @Configured
    @Description("Tokenize and look up every token of the input, rather than looking up the complete input string?")
    boolean lookUpEveryToken = false;

    public SynonymLookupTransformer() {
    }

    public SynonymLookupTransformer(InputColumn<String> column, SynonymCatalog synonymCatalog,
            boolean retainOriginalValue) {
        this();
        this.column = column;
        this.synonymCatalog = synonymCatalog;
        this.retainOriginalValue = retainOriginalValue;
    }

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(new String[] { column.getName() + " (synonyms replaced)" });
    }
    
    @Override
    public String getSuggestedLabel() {
        if (synonymCatalog == null) {
            return null;
        }
        return "Lookup: " + synonymCatalog.getName();
    }

    @Override
    public String[] transform(InputRow inputRow) {
        final String originalValue = inputRow.getValue(column);

        if (originalValue == null) {
            return new String[1];
        }

        if (lookUpEveryToken) {
            final String delim = " \t\n\r\f.,!?\"'+-_:;/\\\\()%@";
            final StringBuilder sb = new StringBuilder();
            final StringTokenizer tokenizer = new StringTokenizer(originalValue, delim, true);
            final int numTokens = tokenizer.countTokens();
            for (int i = 0; i < numTokens; i++) {
                final String token = tokenizer.nextToken();
                if (token.matches(delim)) {
                    // add the delim as-is
                    sb.append(token);
                } else {
                    // look up the token
                    String replacedToken = lookup(token);
                    if (replacedToken == null) {
                        sb.append(token);
                    } else {
                        sb.append(replacedToken);
                    }
                }
            }
            return new String[] { sb.toString() };

        } else {
            final String replacedValue = lookup(originalValue);
            return new String[] { replacedValue };
        }
    }

    private String lookup(String originalValue) {
        final String replacedValue = synonymCatalog.getMasterTerm(originalValue);
        if (retainOriginalValue && replacedValue == null) {
            return originalValue;
        }
        return replacedValue;
    }
}
