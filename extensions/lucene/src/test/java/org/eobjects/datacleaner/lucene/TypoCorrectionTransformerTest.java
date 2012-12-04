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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.metamodel.util.Action;

import junit.framework.TestCase;

public class TypoCorrectionTransformerTest extends TestCase {
    
    public void testSpellingAndCasingScenario() throws Exception {
        InMemorySearchIndex index = new InMemorySearchIndex("names");
        index.write(new Action<IndexWriter>() {
            @Override
            public void run(IndexWriter w) throws Exception {
                w.addDocument(createDoc("nuts"));
                w.addDocument(createDoc("bolts"));
                w.addDocument(createDoc("cuts"));
                w.addDocument(createDoc("user"));
                w.addDocument(createDoc("users"));
                w.addDocument(createDoc("for"));
            }
        });

        MockInputColumn<String> col = new MockInputColumn<String>("name");

        TypoCorrectionTransformer transformer = new TypoCorrectionTransformer();
        transformer.searchField = "name";
        transformer.searchIndex = index;
        transformer.fuzzFactor = 2;
        transformer.searchInput = col;
        transformer.init();

        assertEquals("Nuts and Bolts for EasyDQ users", transformer.transform(new MockInputRow().put(col, "Nuts and Bolts fr EasyDQ users"))[0]);
    }

    public void testNameScenario() throws Exception {
        InMemorySearchIndex index = new InMemorySearchIndex("names");
        index.write(new Action<IndexWriter>() {
            @Override
            public void run(IndexWriter w) throws Exception {
                w.addDocument(createDoc("Kasper"));
                w.addDocument(createDoc("Casper"));
                w.addDocument(createDoc("Ankit"));
                w.addDocument(createDoc("Hans"));
                w.addDocument(createDoc("Manuel"));
                w.addDocument(createDoc("Winfried"));
                w.addDocument(createDoc("Peter"));
                w.addDocument(createDoc("Martin"));
                w.addDocument(createDoc("Michael"));
                w.addDocument(createDoc("Michiel"));
                w.addDocument(createDoc("van den"));
            }
        });

        MockInputColumn<String> col = new MockInputColumn<String>("name");

        TypoCorrectionTransformer transformer = new TypoCorrectionTransformer();
        transformer.searchField = "name";
        transformer.searchIndex = index;
        transformer.fuzzFactor = 1;
        transformer.searchInput = col;
        transformer.init();

        assertEquals("Casper", transformer.transform(new MockInputRow().put(col, "Casper"))[0]);
        assertEquals("Kasper", transformer.transform(new MockInputRow().put(col, "Kapper"))[0]);
        assertEquals("Michael", transformer.transform(new MockInputRow().put(col, "Michel"))[0]);
        assertEquals("Michiel", transformer.transform(new MockInputRow().put(col, "Michil"))[0]);
        assertEquals("Kasper Sørensen", transformer.transform(new MockInputRow().put(col, "Kaser Sørensen"))[0]);
        assertEquals("Kasper. Sørensen", transformer.transform(new MockInputRow().put(col, "Kaper. Sørensen"))[0]);
        assertEquals("Kasper!r Sørensen", transformer.transform(new MockInputRow().put(col, "Kaper!r Sørensen"))[0]);
        assertEquals("Hans Drexler", transformer.transform(new MockInputRow().put(col, "Hans Drexler"))[0]);
        assertEquals("Michael van den Michiel",
                transformer.transform(new MockInputRow().put(col, "Michael vanden Michiel"))[0]);
    }

    protected Iterable<? extends IndexableField> createDoc(String string) {
        final Document doc = new Document();
        doc.add(new StringField("name", string, Field.Store.YES));
        return doc;
    }
}
