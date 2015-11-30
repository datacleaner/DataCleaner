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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.metamodel.util.HasName;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Close;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.ExternalDocumentation;
import org.datacleaner.api.ExternalDocumentation.DocumentationLink;
import org.datacleaner.api.ExternalDocumentation.DocumentationType;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.ImproveSuperCategory;
import org.datacleaner.components.categories.ReferenceDataCategory;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.DictionaryConnection;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

@Named("Remove dictionary matches")
@Description("Removes any part of a string that is matched against a dictionary. Use it to standardize or prepare values, for instance by removing adjectives that make comparison of similar terms difficult.")
@ExternalDocumentation({ @DocumentationLink(title = "Segmenting customers on messy data", url = "https://www.youtube.com/watch?v=iy-j5s-uHz4", type = DocumentationType.VIDEO, version = "4.0") })
@Categorized(superCategory = ImproveSuperCategory.class, value = ReferenceDataCategory.class)
public class RemoveDictionaryMatchesTransformer implements Transformer {

    public static enum RemovedMatchesType implements HasName {

        STRING, LIST;

        @Override
        public String getName() {
            if (this == STRING) {
                return "String";
            } else {
                return "List";
            }

        }

    };

    public static final String PROPERTY_DICTIONARY = "Dictionary";
    public static final String PROPERTY_COLUMN = "Column";
    public static final String OUTPUT_COLUMN_REMOVED_MATCHES = "Removed matches";

    @Configured(value = PROPERTY_DICTIONARY)
    Dictionary _dictionary;

    @Configured(value = PROPERTY_COLUMN)
    InputColumn<String> _column;

    @Inject
    @Configured
    @Description("Get the removed matches as String or as a List")
    RemovedMatchesType _removedMatchesType = RemovedMatchesType.STRING;

    @Provided
    DataCleanerConfiguration _configuration;

    private DictionaryConnection dictionaryConnection;

    private final Splitter SPLITTER = Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings();

    public RemoveDictionaryMatchesTransformer() {
    }

    public RemoveDictionaryMatchesTransformer(InputColumn<String> column, Dictionary dictionary,
            DataCleanerConfiguration configuration) {
        this();
        _column = column;
        _dictionary = dictionary;
        _configuration = configuration;
    }

    @Override
    public OutputColumns getOutputColumns() {
        final String name = _column.getName() + " (" + _dictionary.getName() + " removed)";
        switch (_removedMatchesType) {
        case STRING:
            return new OutputColumns(String.class, new String[] { name, OUTPUT_COLUMN_REMOVED_MATCHES });
        case LIST:
            return new OutputColumns(new String[] { name, OUTPUT_COLUMN_REMOVED_MATCHES }, new Class[] { String.class,
                    List.class });
        default:
            return null;
        }
    }

    @Initialize
    public void init() {
        dictionaryConnection = _dictionary.openConnection(_configuration);
    }

    @Close
    public void close() {
        if (dictionaryConnection != null) {
            dictionaryConnection.close();
            dictionaryConnection = null;
        }
    }

    @Override
    public Object[] transform(InputRow inputRow) {
        final String value = inputRow.getValue(_column);
        final Object[] result = transform(value);
        return result;
    }

    public Object[] transform(final String value) {

        if (Strings.isNullOrEmpty(value)) {
            switch (_removedMatchesType) {
            case STRING:
                return new String[] { value, "" };
            case LIST:
                return new Object[] { value, Collections.EMPTY_LIST };
            }
        }
        Object removedStringMatches = null;
        switch (_removedMatchesType) {
        case STRING:
            removedStringMatches = new StringBuilder();
            break;
        case LIST:
            removedStringMatches = new ArrayList<String>();
            break;
        }

        final StringBuilder survivorString = new StringBuilder();
        final Iterable<String> tokens = SPLITTER.split(value);
        for (String token : tokens) {
            if (!dictionaryConnection.containsValue(token)) {
                if (survivorString.length() != 0) {
                    survivorString.append(' ');
                }
                survivorString.append(token);
            } else {
                addRemovedMatches(removedStringMatches, token);
            }
        }

        switch (_removedMatchesType) {
        case STRING:
            return new String[] { survivorString.toString(), removedStringMatches.toString() };
        case LIST:
            return new Object[] { survivorString.toString(), removedStringMatches };
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void addRemovedMatches(Object removedObjects, final String token) {
        if (removedObjects instanceof StringBuilder) {
            StringBuilder removedString = (StringBuilder) removedObjects;
            if (removedString.length() != 0) {
                removedString.append(' ');
            }
            removedString.append(token);
        } else if (removedObjects instanceof ArrayList) {
            ((ArrayList<String>) removedObjects).add(token);
        }
    }
}
