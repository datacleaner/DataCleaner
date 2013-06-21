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

import java.util.StringTokenizer;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Convertable;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.MatchingAndStandardizationCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Search index matcher")
@Description("Finds the best search index match for the search input using multiple search indices and marks the output with the best scoring index name.")
@Categorized({ LuceneSearchCategory.class, MatchingAndStandardizationCategory.class })
public class SearchIndexMatcherTransformer implements LuceneTransformer<String> {

    private static final char[] DELIMITERS = new char[] { ' ', '\t', '\n', '\r', '\f', ',', ';', ':', '-' };

    @Configured
    @Description("Column(s) containing search term(s).")
    InputColumn<String>[] searchInputs;

    @Configured
    @Convertable(SearchIndexConverter.class)
    @Description("Search indices to fire searches on.")
    SearchIndex[] searchIndices;

    @Configured
    boolean tokenizeInput = true;

    private IndexSearcher[] indexSearchers;

    @Override
    public OutputColumns getOutputColumns() {
        final String[] columnNames = new String[searchInputs.length];
        for (int i = 0; i < columnNames.length; i++) {
            if (tokenizeInput) {
                columnNames[i] = searchInputs[i].getName() + " (matches)";
            } else {
                columnNames[i] = searchInputs[i].getName() + " (match)";
            }
        }

        return new OutputColumns(columnNames);
    }

    @Initialize
    public void init() {
        indexSearchers = new IndexSearcher[searchIndices.length];
        for (int i = 0; i < searchIndices.length; i++) {
            indexSearchers[i] = searchIndices[i].getSearcher();
        }
    }

    @Override
    public String[] transform(InputRow row) {
        final String[] result = new String[searchInputs.length];
        for (int i = 0; i < searchInputs.length; i++) {
            String value = row.getValue(searchInputs[i]);
            if (value == null) {
                result[i] = null;
            } else if (tokenizeInput) {
                final StringBuilder sb = new StringBuilder();
                final StringTokenizer st = new StringTokenizer(value, new String(DELIMITERS), true);
                while (i < result.length && st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (isDelimitor(token)) {
                        sb.append(token);
                    } else {
                        sb.append(getBestIndexName(token));
                    }
                }
                result[i] = sb.toString();
            } else {
                result[i] = getBestIndexName(value);
            }
        }
        return result;
    }

    private boolean isDelimitor(String token) {
        for (int i = 0; i < DELIMITERS.length; i++) {
            if (token.indexOf(DELIMITERS[i]) != -1) {
                return true;
            }
        }
        return false;
    }

    private String getBestIndexName(String searchInput) {
        String bestIndexName = null;
        float maxScore = -1f;
        for (int i = 0; i < indexSearchers.length; i++) {
            final TopDocs result = SearchHelper.search(indexSearchers[i], searchInput);
            if (result != null) {
                final float score = result.getMaxScore();
                if (score > maxScore) {
                    bestIndexName = searchIndices[i].getName();
                    maxScore = score;
                }
            }
        }
        if (bestIndexName == null) {
            return "<?>";
        }
        return "<" + bestIndexName + ">";
    }

}
