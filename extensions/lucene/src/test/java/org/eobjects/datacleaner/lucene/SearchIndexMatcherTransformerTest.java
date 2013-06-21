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

import java.util.Arrays;

import org.apache.lucene.index.IndexWriter;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.metamodel.util.Action;

import junit.framework.TestCase;

public class SearchIndexMatcherTransformerTest extends TestCase {

    private InMemorySearchIndex companyWords = new InMemorySearchIndex("company word");
    private InMemorySearchIndex givenNames = new InMemorySearchIndex("given name");
    private InMemorySearchIndex familyNames = new InMemorySearchIndex("family name");

    @SuppressWarnings("unchecked")
    public void testScenario() throws Exception {
        InputColumn<String> column1 = new MockInputColumn<String>("foo");
        InputColumn<String> column2 = new MockInputColumn<String>("bar");

        SearchIndexMatcherTransformer transformer = new SearchIndexMatcherTransformer();
        transformer.searchIndices = new SearchIndex[] { companyWords, givenNames, familyNames };
        transformer.searchInputs = new InputColumn[] { column1, column2 };

        transformer.init();

        assertEquals("[<?> <?> <company word>, null]",
                Arrays.toString(transformer.transform(new MockInputRow().put(column1, "Human Inference B.V."))));

        assertEquals("[<given name> <family name>, null]",
                Arrays.toString(transformer.transform(new MockInputRow().put(column1, "Kasper Sørensen"))));

        assertEquals("[<given name>, <family name>]",
                Arrays.toString(transformer.transform(new MockInputRow().put(column1, "Ankit").put(column2, "Kumar"))));

        assertEquals("[<family name>, <given name>, null]",
                Arrays.toString(transformer.transform(new MockInputRow().put(column1, "Drexler, Hans"))));
    }

    @Override
    protected void setUp() throws Exception {
        companyWords.write(new Action<IndexWriter>() {
            @Override
            public void run(IndexWriter w) throws Exception {
                w.addDocument(SearchHelper.createSimpleDoc("Corporation"));
                w.addDocument(SearchHelper.createSimpleDoc("Corp"));
                w.addDocument(SearchHelper.createSimpleDoc("Co"));
                w.addDocument(SearchHelper.createSimpleDoc("Co."));
                w.addDocument(SearchHelper.createSimpleDoc("Ltd"));
                w.addDocument(SearchHelper.createSimpleDoc("Limited"));
                w.addDocument(SearchHelper.createSimpleDoc("Aps"));
                w.addDocument(SearchHelper.createSimpleDoc("B.V."));
                w.addDocument(SearchHelper.createSimpleDoc("BV"));
                w.addDocument(SearchHelper.createSimpleDoc("A/S"));
                w.addDocument(SearchHelper.createSimpleDoc("GmbH"));
                w.commit();
            }
        });

        givenNames.write(new Action<IndexWriter>() {
            @Override
            public void run(IndexWriter w) throws Exception {
                w.addDocument(SearchHelper.createSimpleDoc("Kasper"));
                w.addDocument(SearchHelper.createSimpleDoc("Ankit"));
                w.addDocument(SearchHelper.createSimpleDoc("Manuel"));
                w.addDocument(SearchHelper.createSimpleDoc("Hans"));
                w.addDocument(SearchHelper.createSimpleDoc("Winfried"));
                w.addDocument(SearchHelper.createSimpleDoc("Andre"));
                w.addDocument(SearchHelper.createSimpleDoc("Vincent"));
                w.commit();
            }
        });

        familyNames.write(new Action<IndexWriter>() {
            @Override
            public void run(IndexWriter w) throws Exception {
                w.addDocument(SearchHelper.createSimpleDoc("Sørensen"));
                w.addDocument(SearchHelper.createSimpleDoc("Kumar"));
                w.addDocument(SearchHelper.createSimpleDoc("van den Berg"));
                w.addDocument(SearchHelper.createSimpleDoc("Drexler"));
                w.addDocument(SearchHelper.createSimpleDoc("van Holland"));
                w.addDocument(SearchHelper.createSimpleDoc("Velthoen"));
                w.addDocument(SearchHelper.createSimpleDoc("van Hunnik"));
                w.commit();
            }
        });
    }
}
