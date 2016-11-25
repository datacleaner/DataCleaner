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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.HasName;
import org.apache.metamodel.util.HasNameMapper;

public class ReferenceDataCatalogImpl implements ReferenceDataCatalog {

    private static final long serialVersionUID = 1L;
    private final Collection<Dictionary> _dictionaries;
    private final Collection<SynonymCatalog> _synonymCatalogs;
    private final Collection<StringPattern> _stringPatterns;

    public ReferenceDataCatalogImpl() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public ReferenceDataCatalogImpl(final Collection<Dictionary> dictionaries,
            final Collection<SynonymCatalog> synonymCatalogs, final Collection<StringPattern> stringPatterns) {
        if (dictionaries == null) {
            throw new IllegalArgumentException("dictionaries cannot be null");
        }
        final Set<String> uniqueNames = new HashSet<>();
        for (final Dictionary dictionary : dictionaries) {
            final String name = dictionary.getName();
            if (uniqueNames.contains(name)) {
                throw new IllegalStateException("Duplicate dictionary names: " + name);
            } else {
                uniqueNames.add(name);
            }
        }

        if (synonymCatalogs == null) {
            throw new IllegalArgumentException("synonymCatalogs cannot be null");
        }
        uniqueNames.clear();
        for (final SynonymCatalog synonymCatalog : synonymCatalogs) {
            final String name = synonymCatalog.getName();
            if (uniqueNames.contains(name)) {
                throw new IllegalStateException("Duplicate synonym catalog names: " + name);
            } else {
                uniqueNames.add(name);
            }
        }

        if (stringPatterns == null) {
            throw new IllegalArgumentException("stringPatterns cannot be null");
        }
        uniqueNames.clear();
        for (final StringPattern stringPattern : stringPatterns) {
            final String name = stringPattern.getName();
            if (uniqueNames.contains(name)) {
                throw new IllegalStateException("Duplicate string pattern names: " + name);
            } else {
                uniqueNames.add(name);
            }
        }
        _dictionaries = dictionaries;
        _synonymCatalogs = synonymCatalogs;
        _stringPatterns = stringPatterns;
    }

    @Override
    public String[] getDictionaryNames() {
        return getNames(_dictionaries);
    }

    private String[] getNames(final Collection<? extends HasName> items) {
        final List<String> names = CollectionUtils.map(items, new HasNameMapper());
        Collections.sort(names);
        return names.toArray(new String[names.size()]);
    }

    @Override
    public Dictionary getDictionary(final String name) {
        if (name != null) {
            for (final Dictionary d : _dictionaries) {
                if (name.equals(d.getName())) {
                    return d;
                }
            }
        }
        return null;
    }

    @Override
    public String[] getSynonymCatalogNames() {
        return getNames(_synonymCatalogs);
    }

    @Override
    public SynonymCatalog getSynonymCatalog(final String name) {
        if (name != null) {
            for (final SynonymCatalog sc : _synonymCatalogs) {
                if (name.equals(sc.getName())) {
                    return sc;
                }
            }
        }
        return null;
    }

    @Override
    public StringPattern getStringPattern(final String name) {
        if (name != null) {
            for (final StringPattern sp : _stringPatterns) {
                if (name.equals(sp.getName())) {
                    return sp;
                }
            }
        }
        return null;
    }

    @Override
    public String[] getStringPatternNames() {
        return getNames(_stringPatterns);
    }
}
