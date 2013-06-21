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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Convertable;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Search Lucene index (return map)")
@Description("Searches a Lucene search index and returns the top result, if any. This transformer returns the search result as a map, which can then be post-processed eg. using the 'data structures' transformation options.")
@Categorized(LuceneSearchCategory.class)
public class SearchToMapTransformer implements LuceneTransformer<Object> {

    @Configured
    @Description("Column containing search term(s) to fire.")
    InputColumn<String> searchInput;

    @Configured
    @Convertable(SearchIndexConverter.class)
    @Description("Search index to fire searches on.")
    SearchIndex searchIndex;

    private IndexSearcher indexSearcher;

    @Override
    public OutputColumns getOutputColumns() {
        final String[] columnNames = new String[] { "Search result", "Score" };
        final Class<?>[] columnTypes = new Class[] { Map.class, Number.class };
        return new OutputColumns(columnNames, columnTypes);
    }

    @Initialize
    public void init() {
        indexSearcher = searchIndex.getSearcher();
    }

    @Override
    public Object[] transform(InputRow row) {
        final Object[] result = new Object[2];
        result[1] = 0;

        final String searchText = row.getValue(searchInput);

        TopDocs searchResult = SearchHelper.search(indexSearcher, searchText);
        if (searchResult == null) {
            result[1] = -1;
            return result;
        }

        if (searchResult.totalHits == 0) {
            return result;
        }

        final ScoreDoc scoreDoc = searchResult.scoreDocs[0];
        // add score
        final Document document;
        try {
            document = indexSearcher.doc(scoreDoc.doc);
        } catch (Exception e) {
            throw new IllegalStateException("Fetching document from index threw exception", e);
        }

        result[0] = toMap(document);
        result[1] = scoreDoc.score;

        return result;
    }

    protected static Map<String, String> toMap(Document document) {
        final Map<String, String> result = new LinkedHashMap<String, String>();
        final List<IndexableField> fields = document.getFields();
        for (IndexableField field : fields) {
            final String name = field.name();
            final String value = document.get(name);
            result.put(name, value);
        }
        return result;
    }

}
