/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.lucene;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.datacleaner.user.UserPreferences;

/**
 * Default implementation of {@link SearchIndexCatalog}. Saves the
 * {@link SearchIndex} information in the {@link UserPreferences} of
 * DataCleaner.
 */
public class DefaultSearchIndexCatalog implements SearchIndexCatalog {

    private static final String PROPERTY_PREFIX = "datacleaner.lucene.";

    private final Map<String, SearchIndex> _searchIndices;
    private final Map<String, String> _properties;

    protected DefaultSearchIndexCatalog(UserPreferences userPreferences) {
        this(userPreferences.getAdditionalProperties());
    }

    protected DefaultSearchIndexCatalog(Map<String, String> properties) {
        _properties = properties;
        _searchIndices = new TreeMap<String, SearchIndex>();

        initialize();
    }

    protected void initialize() {
        Number count = getNumber(PROPERTY_PREFIX + "index_count");
        if (count == null) {
            // no indices at all
            return;
        }

        for (int i = 0; i < count.intValue(); i++) {
            final SearchIndex searchIndex = initializeSearchIndex(i);
            _searchIndices.put(searchIndex.getName(), searchIndex);
        }
    }

    private SearchIndex initializeSearchIndex(int i) {
        final String name = getString(PROPERTY_PREFIX + i + ".name");
        final String description = getString(PROPERTY_PREFIX + i + ".description");
        final String path = getString(PROPERTY_PREFIX + i + ".path");
        final File directory = new File(path);

        final FileSystemSearchIndex searchIndex = new FileSystemSearchIndex(name, directory);
        searchIndex.setDescription(description);

        return searchIndex;
    }

    private String getString(String key) {
        return _properties.get(key);
    }

    private Number getNumber(String key) {
        final String value = getString(key);
        return ConvertToNumberTransformer.transformValue(value);
    }

    private void setString(String key, String value) {
        _properties.put(key, value);
    }

    @Override
    public String[] getSearchIndexNames() {
        return _searchIndices.keySet().toArray(new String[_searchIndices.size()]);
    }

    @Override
    public SearchIndex getSearchIndex(String name) {
        return _searchIndices.get(name);
    }

    @Override
    public void addSearchIndex(SearchIndex searchIndex) {
        _searchIndices.put(searchIndex.getName(), searchIndex);
        updateMap();
    }

    private void updateMap() {
        setString(PROPERTY_PREFIX + "index_count", _searchIndices.size() + "");

        int i = 0;
        for (SearchIndex searchIndex : _searchIndices.values()) {
            FileSystemSearchIndex fileSystemSearchIndex = (FileSystemSearchIndex) searchIndex;

            setString(PROPERTY_PREFIX + i + ".name", searchIndex.getName());
            setString(PROPERTY_PREFIX + i + ".description", searchIndex.getDescription());
            setString(PROPERTY_PREFIX + i + ".path", fileSystemSearchIndex.getFile().getPath());

            i++;
        }
    }
}