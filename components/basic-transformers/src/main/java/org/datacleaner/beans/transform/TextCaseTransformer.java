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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.metamodel.util.HasName;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.TextCategory;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.DictionaryConnection;
import org.datacleaner.reference.ReferenceDataCatalog;

import com.google.common.collect.Iterables;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.BreakIterator;

@Named("Text case transformer")
@Description("Modifies the text case/capitalization of Strings.")
@Categorized(TextCategory.class)
public class TextCaseTransformer implements Transformer {
    private static final String DEFAULT_START_CASE_DICTIONARY = "TextCaseTransformer start case dictionary";
    private static final String DEFAULT_END_CASE_DICTIONARY = "TextCaseTransformer end case dictionary";
    private static final String DEFAULT_WORD_CASE_DICTIONARY = "TextCaseTransformer word case dictionary";
    private static final String DEFAULT_COMPLETE_CASE_DICTIONARY = "TextCaseTransformer fixed case dictionary";

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

    public static final String VALUE_PROPERTY = "Value";
    public static final String MODE_PROPERTY = "Mode";
    public static final String ALL_WORDS_DICTIONARY_PROPERTY = "Dictionaries for casing all words";
    public static final String WORD_DICTIONARY_PROPERTY = "Dictionaries for casing complete words";
    public static final String BEGIN_WORD_DICTIONARY_PROPERTY = "Dictionaries for casing beginning of words";
    public static final String END_WORD_DICTIONARY_PROPERTY = "Dictionaries for casing ending of words";

    public TextCaseTransformer() {
        this(null);
    }

    @Inject
    public TextCaseTransformer(final ReferenceDataCatalog catalog) {
        if (catalog != null) {
            if (catalog.containsDictionary(DEFAULT_COMPLETE_CASE_DICTIONARY)) {
                allWordsDictionaries = new Dictionary[] { catalog.getDictionary(DEFAULT_COMPLETE_CASE_DICTIONARY) };
            }

            if (catalog.containsDictionary(DEFAULT_WORD_CASE_DICTIONARY)) {
                wordDictionaries = new Dictionary[] { catalog.getDictionary(DEFAULT_WORD_CASE_DICTIONARY) };
            }

            if (catalog.containsDictionary(DEFAULT_START_CASE_DICTIONARY)) {
                wordStartDictionaries = new Dictionary[] { catalog.getDictionary(DEFAULT_START_CASE_DICTIONARY) };
            }

            if (catalog.containsDictionary(DEFAULT_END_CASE_DICTIONARY)) {
                wordEndDictionaries = new Dictionary[] { catalog.getDictionary(DEFAULT_END_CASE_DICTIONARY) };
            }
        }
    }

    @Configured(VALUE_PROPERTY)
    InputColumn<String> valueColumn;

    @Configured(MODE_PROPERTY)
    TransformationMode mode = TransformationMode.UPPER_CASE;

    @Configured(value = ALL_WORDS_DICTIONARY_PROPERTY,required = false, order = 11)
    Dictionary[] allWordsDictionaries = {};

    @Configured(value = WORD_DICTIONARY_PROPERTY,required = false, order = 12)
    Dictionary[] wordDictionaries = {};

    @Configured(value = BEGIN_WORD_DICTIONARY_PROPERTY,required = false, order = 13)
    Dictionary[] wordStartDictionaries = {};

    @Configured(value = END_WORD_DICTIONARY_PROPERTY,required = false, order = 14)
    Dictionary[] wordEndDictionaries = {};

    @Provided
    DataCleanerConfiguration _configuration;

    private DictionaryConnection[] allWordsDictionaryConnections = {};
    private DictionaryConnection[] wordDictionaryConnections = {};
    private DictionaryConnection[] wordStartDictionaryConnections = {};
    private DictionaryConnection[] wordEndDictionaryConnections = {};

    private DictionaryConnection[] openConnections(final Dictionary[] dictionaries) {
        return Stream.of(dictionaries).map(d -> d.openConnection(_configuration)).toArray(DictionaryConnection[]::new);
    }

    @Initialize
    public void init() {
        allWordsDictionaryConnections = openConnections(allWordsDictionaries);
        wordDictionaryConnections = openConnections(wordDictionaries);
        wordStartDictionaryConnections = openConnections(wordStartDictionaries);
        wordEndDictionaryConnections = openConnections(wordEndDictionaries);
    }

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
            return capitalizeWords(value);
        default:
            throw new UnsupportedOperationException("Unsupported mode: " + mode);
        }
    }

    private String capitalizeWords(final String value) {
        final String preparedString = UCharacter.toTitleCase(value, BreakIterator.getWordInstance());

        return getAllWords(preparedString).stream().map(this::capitalizeWord).collect(Collectors.joining());
    }

    private String capitalizeWord(String input) {
        return input;
    }

    private List<String> getAllWords(final String preparedString) {
        final List<String> words = new ArrayList<>();
        final BreakIterator breakIterator = BreakIterator.getWordInstance();
        breakIterator.setText(preparedString);
        int start = breakIterator.first();

        for (int end = breakIterator.next(); end != BreakIterator.DONE; start = end, end = breakIterator.next()) {
            words.add(preparedString.substring(start, end));
        }
        return words;
    }

    private Iterable<String> getSortedDictionaryWords(final DictionaryConnection[] dictionaryConnections) {
        final List<Iterable<String>> iterables = new ArrayList<>(dictionaryConnections.length);

        for (final DictionaryConnection dictionaryConnection : dictionaryConnections) {
            iterables.add(dictionaryConnection::getLengthSortedValues);
        }

        return Iterables.mergeSorted(iterables, Comparator.comparingInt(String::length));
    }
}

