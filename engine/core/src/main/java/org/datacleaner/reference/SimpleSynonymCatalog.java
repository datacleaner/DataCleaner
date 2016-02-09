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
package org.datacleaner.reference;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.util.ReadObjectBuilder;
import org.datacleaner.util.ReadObjectBuilder.Adaptor;
import org.elasticsearch.common.base.Objects;

/**
 * The simplest implementation of {@link SynonymCatalog}. Based on an in-memory
 * {@link Map} of values.
 */
public final class SimpleSynonymCatalog extends AbstractReferenceData implements SynonymCatalog {

    private static final long serialVersionUID = 1L;

    private final Map<String, String> _synonymMap;

    private final boolean _caseSensitive;

    public SimpleSynonymCatalog(String name) {
        this(name, new HashMap<>());
    }

    public SimpleSynonymCatalog(String name, Map<String, String> synonyms) {
        this(name, synonyms, true);
    }

    public SimpleSynonymCatalog(String name, Map<String, String> synonyms, boolean caseSensitive) {
        super(name);
        _caseSensitive = caseSensitive;
        _synonymMap = synonyms;
    }

    private SortedMap<String, String> createSortedSynonymMap() {
        SortedMap<String, String> synonymMap = new TreeMap<>(Comparator.comparingInt(String::length).reversed().thenComparing(String::compareTo));
        if (_caseSensitive) {
            synonymMap.putAll(_synonymMap);
        } else {
            final Set<Entry<String, String>> entries = _synonymMap.entrySet();
            for (Entry<String, String> entry : entries) {
                final String key = entry.getKey().toLowerCase();
                synonymMap.put(key, entry.getValue());
            }
        }
        return synonymMap;
    }

    public SimpleSynonymCatalog(String name, Synonym... synonyms) {
        this(name);
        for (Synonym synonym : synonyms) {
            addSynonym(synonym);
        }
    }

    public SimpleSynonymCatalog(String name, List<Synonym> synonyms) {
        this(name);
        for (Synonym synonym : synonyms) {
            addSynonym(synonym);
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        Adaptor adaptor = new Adaptor() {
            @Override
            public void deserialize(ObjectInputStream.GetField getField, Serializable serializable) throws Exception {
                final boolean caseSensitive = getField.get("_caseSensitive", true);
                Field field = SimpleSynonymCatalog.class.getDeclaredField("_caseSensitive");
                field.setAccessible(true);
                field.set(serializable, caseSensitive);
            }
        };
        ReadObjectBuilder.create(this, SimpleSynonymCatalog.class).readObject(stream, adaptor);
    }

    private void addSynonym(Synonym synonym) {
        final String masterTerm = synonym.getMasterTerm();
        {
            final String key = _caseSensitive ? masterTerm : masterTerm.toLowerCase();
            _synonymMap.put(key, masterTerm);
        }
        final Collection<String> values = synonym.getSynonyms();
        for (String value : values) {
            final String key = _caseSensitive ? value : value.toLowerCase();
            _synonymMap.put(key, masterTerm);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            SimpleSynonymCatalog other = (SimpleSynonymCatalog) obj;
            return Objects.equal(_synonymMap, other._synonymMap) && Objects.equal(_caseSensitive, other._caseSensitive);
        }
        return false;
    }

    @Override
    public SynonymCatalogConnection openConnection(DataCleanerConfiguration configuration) {
        return new SynonymCatalogConnection() {


            SortedMap<String, String> _sortedSynonymMap = createSortedSynonymMap();

            @Override
            public Collection<Synonym> getSynonyms() {
                final Map<String, Synonym> synonyms = new TreeMap<>();
                for (Entry<String, String> synonymEntry : _sortedSynonymMap.entrySet()) {
                    final String masterTerm = synonymEntry.getValue();
                    final String synonymValue = synonymEntry.getKey();

                    MutableSynonym synonym = (MutableSynonym) synonyms.get(masterTerm);
                    if (synonym == null) {
                        synonym = new MutableSynonym(masterTerm);
                        synonyms.put(masterTerm, synonym);
                    }

                    synonym.addSynonym(synonymValue);
                }
                return synonyms.values();
            }

            @Override
            public String getMasterTerm(String term) {
                if (term == null) {
                    return null;
                }
                final String key = _caseSensitive ? term : term.toLowerCase();
                return _sortedSynonymMap.get(key);
            }

            @Override
            public Replacement replaceInline(String sentence) {
                final List<String> synonyms = new ArrayList<>();
                final List<String> masterTerms = new ArrayList<>();

                if(!_caseSensitive){
                    sentence = sentence.toLowerCase();
                }

                for(String synonym : _sortedSynonymMap.keySet()) {
                    if(masterTerms.contains(synonym)){
                        continue;
                    }

                    final Matcher matcher = Pattern.compile("\\b" + synonym + "\\b").matcher(sentence);
                    while(matcher.find()){
                        final String masterTerm = _sortedSynonymMap.get(synonym);
                        sentence =
                                sentence.substring(0, matcher.start()) + masterTerm + sentence.substring(matcher.end());
                        synonyms.add(synonym);
                        masterTerms.add(masterTerm);
                    }
                }

                final String finalSentence = sentence;
                return new Replacement() {
                    @Override
                    public String getReplacedString() {
                        return finalSentence;
                    }

                    @Override
                    public List<String> getSynonyms() {
                        return synonyms;
                    }

                    @Override
                    public List<String> getMasterTerms() {
                        return masterTerms;
                    }
                };
            }

            @Override
            public void close() {
            }
        };
    }

    public boolean isCaseSensitive() {
        return _caseSensitive;
    }

    public Map<String, String> getSynonymMap() {
        return _synonymMap;
    }
}
