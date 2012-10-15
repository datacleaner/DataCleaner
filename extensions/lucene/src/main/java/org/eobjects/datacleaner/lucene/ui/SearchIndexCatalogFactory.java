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
package org.eobjects.datacleaner.lucene.ui;

import java.util.WeakHashMap;

import org.eobjects.datacleaner.lucene.DefaultSearchIndexCatalog;
import org.eobjects.datacleaner.lucene.SearchIndexCatalog;
import org.eobjects.datacleaner.user.UserPreferences;

/**
 * A factory for {@link SearchIndexCatalog} instances.
 */
public class SearchIndexCatalogFactory {

    private static final WeakHashMap<UserPreferences, SearchIndexCatalog> instances = new WeakHashMap<UserPreferences, SearchIndexCatalog>();
    
    private SearchIndexCatalogFactory() {
        // prevent instantiation
    }

    public static SearchIndexCatalog getInstance(UserPreferences userPreferences) {
        SearchIndexCatalog instance = instances.get(userPreferences);
        if (instance == null) {
            instance = new DefaultSearchIndexCatalog(userPreferences);
            instances.put(userPreferences, instance);
        }
        return instance;
    }
}
