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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.Percentage;

@TransformerBean("Search in Lucene search index")
public class SearchTransformer implements Transformer<Object> {

    @Configured
    InputColumn<String>[] values;

    // TODO: Add converter
    @Configured
    SearchIndex searchIndex;

    @Configured
    String[] searchFields;

    @Configured
    Percentage minimumSimilarity = new Percentage(40);

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
        final BooleanQuery outerQuery = new BooleanQuery(true);

        for (int i = 0; i < searchFields.length; i++) {
            final String field = searchFields[i];
            final String searchText = row.getValue(values[i]);
            
            if (searchText != null) {
                final Query query;
                
                if (searchText.indexOf(" ") != -1) {
                    PhraseQuery phraseQuery = new PhraseQuery();
                    phraseQuery.add(new Term(field, searchText));
                    
                    query = phraseQuery;
                } else {
                    final Term term = new Term(field, searchText);
                    query = new FuzzyQuery(term, minimumSimilarity.floatValue());
                }
                outerQuery.add(query, Occur.SHOULD);
            }

        }

        final TopDocs searchResult;
        try {
            searchResult = indexSearcher.search(outerQuery, 1);
        } catch (IOException e) {
            throw new IllegalStateException("Searching index threw exception", e);
        }

        final Object[] result = new Object[2];

        if (searchResult == null || searchResult.totalHits == 0) {
            result[1] = 0;
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
        final List<Fieldable> fields = document.getFields();
        for (Fieldable fieldable : fields) {
            final String name = fieldable.name();
            final String value = document.get(name);
            result.put(name, value);
        }
        return result;
    }

}
