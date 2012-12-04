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
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Convertable;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.NumberProperty;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.stringpattern.DefaultTokenizer;
import org.eobjects.analyzer.beans.stringpattern.Token;
import org.eobjects.analyzer.beans.stringpattern.TokenType;
import org.eobjects.analyzer.beans.stringpattern.TokenizerConfiguration;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.StringUtils;

@TransformerBean("Typo correction using search index")
@Description("Uses a search index field containing correct spellings of words to search/replace for typos and minor mistakes within strings.")
@Categorized(LuceneSearchCategory.class)
public class TypoCorrectionTransformer implements LuceneTransformer<String> {

    @Configured
    @Description("Column containing search term(s) to fire.")
    InputColumn<String> searchInput;

    @Configured
    @Convertable(SearchIndexConverter.class)
    @Description("Search index to fire searches on.")
    SearchIndex searchIndex;

    @Configured
    @Description("Search field name")
    String searchField;

    @Configured
    @NumberProperty(negative = false)
    int fuzzFactor = 1;

    private DefaultTokenizer tokenizer;
    private IndexSearcher indexSearcher;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(searchInput.getName() + " (typos corrected)");
    }

    @Initialize
    public void init() {
        indexSearcher = searchIndex.getSearcher();

        TokenizerConfiguration tokenizerConfiguration = new TokenizerConfiguration(false);
        tokenizerConfiguration.setDiscriminateTextCase(false);

        tokenizer = new DefaultTokenizer(tokenizerConfiguration);
    }

    @Override
    public String[] transform(InputRow row) {
        final String searchText = row.getValue(searchInput);

        if (StringUtils.isNullOrEmpty(searchText)) {
            return new String[1];
        }

        final StringBuilder result = new StringBuilder();

        final List<Token> tokens = tokenizer.tokenize(searchText);

        int textTokens = 0;
        for (Token token : tokens) {
            if (token.getType() == TokenType.TEXT) {
                textTokens++;
            }
        }

        // first try a full-text search
        final String fullResult = searchToken(searchText, textTokens * fuzzFactor);
        if (fullResult != null) {
            return new String[] { fullResult };
        }

        for (Token token : tokens) {
            final String string = token.getString();
            if (token.getType() == TokenType.TEXT) {
                String outputToken = searchToken(string, fuzzFactor);
                if (outputToken == null || string.equalsIgnoreCase(outputToken)) {
                    result.append(string);
                } else {
                    result.append(outputToken);
                }
            } else {
                result.append(string);
            }
        }

        return new String[] { result.toString() };
    }

    private String searchToken(String inputToken, int fuzzFactor) {
        if (fuzzFactor > 2) {
            // max supported fuzz factor in Lucene is 2
            fuzzFactor = 2;
        }
        final Query query = new FuzzyQuery(new Term(searchField, inputToken), fuzzFactor);

        final TopDocs searchResult;
        try {
            searchResult = indexSearcher.search(query, 1);
        } catch (IOException e) {
            throw new IllegalStateException("Searching index threw exception", e);
        }

        if (searchResult == null || searchResult.totalHits == 0) {
            return null;
        }

        final ScoreDoc scoreDoc = searchResult.scoreDocs[0];
        // add score
        final Document document;
        try {
            document = indexSearcher.doc(scoreDoc.doc);
        } catch (Exception e) {
            throw new IllegalStateException("Fetching document from index threw exception", e);
        }

        return document.get(searchField);
    }
}
