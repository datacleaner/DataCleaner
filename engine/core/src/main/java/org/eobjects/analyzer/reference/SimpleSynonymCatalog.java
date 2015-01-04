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
package org.eobjects.analyzer.reference;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eobjects.analyzer.util.ReadObjectBuilder;
import org.eobjects.analyzer.util.ReadObjectBuilder.Adaptor;
import org.eobjects.analyzer.util.StringUtils;

public final class SimpleSynonymCatalog extends AbstractReferenceData implements SynonymCatalog {

    private static final long serialVersionUID = 1L;

    private final Map<String, String> _synonymMap;

    public SimpleSynonymCatalog(String name) {
        super(name);
        _synonymMap = new HashMap<String, String>();
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
        final Collection<String> values = synonym.getSynonyms().getValues();
        for (String value : values) {
            _synonymMap.put(value, masterTerm);
        }
    }

    @Override
    protected void decorateIdentity(List<Object> identifiers) {
        super.decorateIdentity(identifiers);
        identifiers.add(_synonymMap);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ReadObjectBuilder.create(this, SimpleSynonymCatalog.class).readObject(stream, new Adaptor() {
            @Override
            public void deserialize(GetField getField, Serializable serializable) throws IOException {
                // Get the old List<Synonym> field '_synonyms'.
                Object synonyms = getField.get("_synonyms", null);
                if (synonyms instanceof List) {
                    try {
                        Field field = SimpleSynonymCatalog.class.getDeclaredField("_synonymMap");
                        field.setAccessible(true);
                        field.set(SimpleSynonymCatalog.this, new HashMap<String, String>());
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }

                    @SuppressWarnings("unchecked")
                    List<Synonym> synonymsList = (List<Synonym>) synonyms;
                    for (Synonym synonym : synonymsList) {
                        addSynonym(synonym);
                    }
                }
            }
        });
    }

    @Override
    public String getMasterTerm(String term) {
        if (StringUtils.isNullOrEmpty(term)) {
            return null;
        }
        String masterTerm = _synonymMap.get(term);
        return masterTerm;
    }

    @Override
    public Collection<? extends Synonym> getSynonyms() {
        Map<String, MutableSynonym> synonyms = new TreeMap<String, MutableSynonym>();
        for (Entry<String, String> synonymEntry : _synonymMap.entrySet()) {
            String masterTerm = synonymEntry.getValue();
            String synonymValue = synonymEntry.getKey();

            MutableSynonym synonym = synonyms.get(masterTerm);
            if (synonym == null) {
                synonym = new MutableSynonym(masterTerm);
                synonyms.put(masterTerm, synonym);
            }

            synonym.addSynonym(synonymValue);
        }
        return synonyms.values();
    }

}
