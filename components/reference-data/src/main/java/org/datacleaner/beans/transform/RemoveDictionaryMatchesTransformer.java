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

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.datacleaner.api.*;
import org.datacleaner.api.ExternalDocumentation.DocumentationLink;
import org.datacleaner.api.ExternalDocumentation.DocumentationType;
import org.datacleaner.components.categories.MatchingAndStandardizationCategory;
import org.datacleaner.reference.Dictionary;

import javax.inject.Named;

@Named("Remove dictionary matches")
@Description("Removes any part of a string that is matched against a dictionary. Use it to standardize or prepare values, for instance by removing adjectives that make comparison of similar terms difficult.")
@ExternalDocumentation({ @DocumentationLink(title = "Segmenting customers on messy data", url = "https://www.youtube.com/watch?v=iy-j5s-uHz4", type = DocumentationType.VIDEO, version = "4.0") })
@Categorized({ MatchingAndStandardizationCategory.class })
@WSStatelessComponent
public class RemoveDictionaryMatchesTransformer implements Transformer {

    public static final String PROPERTY_DICTIONARY = "Dictionary";
    public static final String PROPERTY_COLUMN = "Column";

    @Configured(value = PROPERTY_DICTIONARY)
    Dictionary _dictionary;

    @Configured(value = PROPERTY_COLUMN)
    InputColumn<String> _column;

    private final Splitter SPLITTER = Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings();

    public RemoveDictionaryMatchesTransformer() {
    }

    public RemoveDictionaryMatchesTransformer(InputColumn<String> column, Dictionary dictionary) {
        this();
        _column = column;
        _dictionary = dictionary;
    }

    @Override
    public OutputColumns getOutputColumns() {
        final String name = _column.getName() + " (" + _dictionary.getName() + " removed)";
        return new OutputColumns(String.class, new String[] { name });
    }

    @Override
    public Object[] transform(InputRow inputRow) {
        final String value = inputRow.getValue(_column);
        final String result = transform(value);
        return new Object[] { result };
    }

    public String transform(final String value) {
        if (Strings.isNullOrEmpty(value)) {
            return value;
        }

        final StringBuilder sb = new StringBuilder();
        final Iterable<String> tokens = SPLITTER.split(value);
        for (String token : tokens) {
            if (!_dictionary.containsValue(token)) {
                if (sb.length() != 0) {
                    sb.append(' ');
                }
                sb.append(token);
            }
        }

        return sb.toString();
    }

}
