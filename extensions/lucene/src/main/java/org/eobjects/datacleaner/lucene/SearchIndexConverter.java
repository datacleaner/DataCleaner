/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Converter;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.lucene.ui.SearchIndexCatalogFactory;
import org.eobjects.datacleaner.user.UserPreferences;

/**
 * Converter used to convert a search index reference when DC components gets
 * serialized as XML.
 */
public class SearchIndexConverter implements Converter<SearchIndex> {

    @Inject
    UserPreferences userPreferences;

    @Override
    public SearchIndex fromString(Class<?> cls, String str) {
        final SearchIndexCatalog catalog = SearchIndexCatalogFactory.getInstance(userPreferences);
        return catalog.getSearchIndex(str);
    }

    @Override
    public String toString(SearchIndex index) {
        return index.getName();
    }

    @Override
    public boolean isConvertable(Class<?> cls) {
        return ReflectionUtils.is(cls, SearchIndex.class);
    }

}
