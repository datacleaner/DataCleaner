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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.elasticsearch.common.base.Objects;

import com.google.common.base.Strings;

/**
 * The simplest implementation of {@link SynonymCatalog}. Based on an in-memory
 * {@link Map} of values.
 */
public final class SimpleSynonymCatalog extends AbstractReferenceData implements SynonymCatalog {

    private static final long serialVersionUID = 1L;

    private final Map<String, String> _synonymMap;

    public SimpleSynonymCatalog(String name) {
        super(name);
        _synonymMap = new HashMap<String, String>();
    }

    public SimpleSynonymCatalog(String name, Map<String, String> synonyms) {
        super(name);
        _synonymMap = synonyms;
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

    private void addSynonym(Synonym synonym) {
        final String masterTerm = synonym.getMasterTerm();
        _synonymMap.put(masterTerm, masterTerm);
        final Collection<String> values = synonym.getSynonyms();
        for (String value : values) {
            _synonymMap.put(value, masterTerm);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            SimpleSynonymCatalog other = (SimpleSynonymCatalog) obj;
            return Objects.equal(_synonymMap, other._synonymMap);
        }
        return false;
    }

    @Override
    public SynonymCatalogConnection openConnection(DataCleanerConfiguration configuration) {
        return new SynonymCatalogConnection() {

            @Override
            public Collection<? extends Synonym> getSynonyms() {
                final Map<String, MutableSynonym> synonyms = new TreeMap<String, MutableSynonym>();
                for (Entry<String, String> synonymEntry : _synonymMap.entrySet()) {
                    final String masterTerm = synonymEntry.getValue();
                    final String synonymValue = synonymEntry.getKey();

                    MutableSynonym synonym = synonyms.get(masterTerm);
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
                if (Strings.isNullOrEmpty(term)) {
                    return null;
                }
                final String masterTerm = _synonymMap.get(term);
                return masterTerm;
            }

            @Override
            public void close() {
            }
        };
    }
}
