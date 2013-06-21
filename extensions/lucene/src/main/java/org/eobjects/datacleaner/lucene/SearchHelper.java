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

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.eobjects.analyzer.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SearchHelper {

    private static final Logger logger = LoggerFactory.getLogger(SearchHelper.class);

    public static Iterable<? extends IndexableField> createSimpleDoc(String value) {
        final Document doc = new Document();
        doc.add(new TextField(Constants.SEARCH_FIELD_NAME, value, Field.Store.YES));
        return doc;
    }

    public static TopDocs search(IndexSearcher indexSearcher, String searchText) {
        if (StringUtils.isNullOrEmpty(searchText)) {
            return null;
        }

        final Query query;
        try {
            final Analyzer analyzer = new SimpleAnalyzer(Constants.VERSION);
            final QueryParser queryParser = new QueryParser(Constants.VERSION, Constants.SEARCH_FIELD_NAME, analyzer);
            query = queryParser.parse(searchText);
        } catch (ParseException e) {
            logger.error("An error occurred while parsing query: " + searchText, e);
            return null;
        }

        final TopDocs searchResult;
        try {
            searchResult = indexSearcher.search(query, 1);
        } catch (IOException e) {
            throw new IllegalStateException("Searching index threw exception", e);
        }
        return searchResult;
    }
}
