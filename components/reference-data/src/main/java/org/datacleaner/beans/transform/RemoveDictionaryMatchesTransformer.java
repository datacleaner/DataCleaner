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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.datacleaner.util.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

@Named("Remove dictionary matches")
@Description("Removes any part of a string that is matched against a dictionary. Use it to standardize or prepare values, for instance by removing adjectives that make comparison of similar terms difficult.")
@ExternalDocumentation({ @DocumentationLink(title = "Segmenting customers on messy data", url = "https://www.youtube.com/watch?v=iy-j5s-uHz4", type = DocumentationType.VIDEO, version = "4.0") })
@Categorized(superCategory = ImproveSuperCategory.class, value = ReferenceDataCategory.class)
public class RemoveDictionaryMatchesTransformer implements Transformer {
    public enum RemovedMatchesType implements HasName {
        STRING("String"), LIST("List");

        private final String _name;

        RemovedMatchesType(String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
        }
    }

    public static final String PROPERTY_DICTIONARY = "Dictionary";
    public static final String PROPERTY_COLUMN = "Column";
    public static final String OUTPUT_COLUMN_REMOVED_MATCHES = "Removed matches";

    @Inject
    @Configured(value = PROPERTY_DICTIONARY)
    Dictionary _dictionary;

    @Inject
    @Configured(value = PROPERTY_COLUMN)
    InputColumn<String> _column;

    @Inject
    @Configured
    @Description("How should the 'Removed matches' be returned? Get the removed matches as a concatenated String or as a List.")
    RemovedMatchesType _removedMatchesType = RemovedMatchesType.STRING;

    @Inject
    @Provided
    DataCleanerConfiguration _configuration;

    private DictionaryConnection _dictionaryConnection;
    private Map<String, Pattern> multiWordDictionaryPatterns;

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
        final String[] columnNames = new String[2];
        final Class<?>[] columnTypes = new Class[2];

        columnNames[0] = _column.getName() + " (" + _dictionary.getName() + " removed)";
        columnTypes[0] = String.class;

        columnNames[1] = OUTPUT_COLUMN_REMOVED_MATCHES;

        switch (_removedMatchesType) {
        case STRING:
            columnTypes[1] = String.class;
            break;
        case LIST:
            columnTypes[1] = List.class;
            break;
        default:
            throw new UnsupportedOperationException("Unsupported output type: " + _removedMatchesType);
        }

        return new OutputColumns(columnNames, columnTypes);
    }

    @Initialize
    public void init() {
        _dictionaryConnection = _dictionary.openConnection(_configuration);
        multiWordDictionaryPatterns = new LinkedHashMap<>();
        
        final Iterator<String> allValues = _dictionaryConnection.getLengthSortedValues();
        while (allValues.hasNext()) {
            final String value = allValues.next();
            if (!StringUtils.isSingleWord(value)) {
                final Pattern pattern;
                if (_dictionary.isCaseSensitive()) {
                    pattern = Pattern.compile("\\b" + Pattern.quote(value) + "\\b");
                } else {
                    pattern = Pattern.compile("\\b" + Pattern.quote(value.toLowerCase()) + "\\b");
                }
                multiWordDictionaryPatterns.put(value, pattern);
            }
        }
    }

    @Close
    public void close() {
        if (_dictionaryConnection != null) {
            _dictionaryConnection.close();
            _dictionaryConnection = null;
        }
    }

    @Override
    public Object[] transform(InputRow inputRow) {
        final String value = inputRow.getValue(_column);
        return transform(value);
    }

    public Object[] transform(String value) {
        final List<String> removedParts = new ArrayList<>(2);
        if (!Strings.isNullOrEmpty(value)) {
            for (Entry<String, Pattern> entry : multiWordDictionaryPatterns.entrySet()) {
                final Pattern pattern = entry.getValue();
                final Matcher matcher;
                if (_dictionary.isCaseSensitive()) {
                    matcher = pattern.matcher(value);
                } else {
                    matcher = pattern.matcher(value.toLowerCase());
                }
                while(matcher.find()){
                    final int start;
                    final int end;
                    if (matcher.start() > 0 && value.charAt(matcher.start() - 1) == ' ') {
                        start = matcher.start() - 1;
                        end = matcher.end();
                    } else if ( matcher.end() < value.length() && value.charAt(matcher.end()) == ' ') {
                        start = matcher.start();
                        end = matcher.end() + 1;
                    } else {
                        start = matcher.start();
                        end = matcher.end();
                    }

                    value = value.substring(0, start) + value.substring(end);
                    removedParts.add(entry.getKey());
                }
            }
            
            // do word-by-word dictionary lookups
            final StringBuilder sb = new StringBuilder();
            final List<String> tokens = StringUtils.splitOnWordBoundaries(value, true);
            for (String token : tokens) {
                if (StringUtils.isSingleWord(token))  {
                    if (_dictionaryConnection.containsValue(token)) {
                        removedParts.add(token);
                    } else {
                        sb.append(token);
                    }
                } else {
                    // this is a delim - just add it
                    sb.append(token);
                }
            }
            value = sb.toString();
        }

        switch (_removedMatchesType) {
        case STRING:
            final String removedPartsString = Joiner.on(' ').join(removedParts);
            return new String[] { value, removedPartsString };
        case LIST:
            return new Object[] { value, removedParts };
        default:
            throw new UnsupportedOperationException("Unsupported output type: " + _removedMatchesType);
        }
    }
}
