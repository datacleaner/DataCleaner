/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.beans.transform;

import static org.junit.Assert.*;

import java.util.List;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.reference.TextFileSynonymCatalog;
import org.junit.Test;

public class SynonymLookupTransformerTest {

    private final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();
    private final SynonymCatalog sc = new TextFileSynonymCatalog("my synonyms",
            "src/test/resources/synonym-countries.txt", true, "UTF8");

    @Test
    public void testCaseInsensitiveMathingOnEveryToken() throws Exception {
        final SynonymCatalog sc = new TextFileSynonymCatalog("my synonyms", "src/test/resources/synonym-countries.txt",
                false, "UTF8");

        MockInputColumn<String> col = new MockInputColumn<>("my col", String.class);

        SynonymLookupTransformer transformer = new SynonymLookupTransformer(col, sc, true, configuration);
        transformer.replaceInlinedSynonyms = true;
        transformer.init();

        assertEquals("Hello DNK DNK DNK!", transformer.transform(new MockInputRow().put(col,
                "Hello denmark dnk dk!"))[0]);
        assertEquals("DNK", transformer.transform(new MockInputRow().put(col, "dk"))[0]);
        assertEquals("Hello GBR DNK GBR.", transformer.transform(new MockInputRow().put(col,
                "Hello United KINGDOM danmark uk."))[0]);

        transformer.close();
    }

    @Test
    public void testCaseInsensitiveMathingOnCompleteExpression() throws Exception {
        final SynonymCatalog sc = new TextFileSynonymCatalog("my synonyms", "src/test/resources/synonym-countries.txt",
                false, "UTF8");

        MockInputColumn<String> col = new MockInputColumn<>("my col", String.class);

        SynonymLookupTransformer transformer = new SynonymLookupTransformer(col, sc, true, configuration);
        transformer.replaceInlinedSynonyms = false;
        transformer.init();

        assertEquals("Hello denmark dnk dk!", transformer.transform(new MockInputRow().put(col,
                "Hello denmark dnk dk!"))[0]);
        assertNull(transformer.transform(new MockInputRow().put(col, "Hello denmark dnk dk!"))[1]);
        assertNull(transformer.transform(new MockInputRow().put(col, "Hello denmark dnk dk!"))[2]);
        assertEquals("DNK", transformer.transform(new MockInputRow().put(col, "dk"))[0]);
        assertEquals("dk", transformer.transform(new MockInputRow().put(col, "dk"))[1]);
        assertEquals("DNK", transformer.transform(new MockInputRow().put(col, "dk"))[2]);
        assertEquals("Hello United KINGDOM danmark uk.", transformer.transform(new MockInputRow().put(col,
                "Hello United KINGDOM danmark uk."))[0]);
        assertNull(transformer.transform(new MockInputRow().put(col, "Hello United KINGDOM danmark uk."))[1]);
        assertNull(transformer.transform(new MockInputRow().put(col, "Hello United KINGDOM danmark uk."))[2]);

        transformer.close();
    }

    @Test
    public void testTransformWithCompleteInput() throws Exception {
        MockInputColumn<String> col = new MockInputColumn<>("my col", String.class);

        // with retain original value
        SynonymLookupTransformer transformer = new SynonymLookupTransformer(col, sc, true, configuration);
        transformer.replaceInlinedSynonyms = false;
        assertEquals(3, transformer.getOutputColumns().getColumnCount());
        assertEquals("my col (synonyms replaced)", transformer.getOutputColumns().getColumnName(0));

        transformer.init();

        assertEquals("hello", transformer.transform(new MockInputRow().put(col, "hello"))[0]);
        assertEquals("ALB", transformer.transform(new MockInputRow().put(col, "Albania"))[0]);
        assertEquals("I come from Albania!", transformer.transform(new MockInputRow().put(col,
                "I come from Albania!"))[0]);

        transformer.close();

        // without retain original value
        transformer = new SynonymLookupTransformer(col, sc, false, configuration);
        transformer.replaceInlinedSynonyms = false;
        assertEquals(3, transformer.getOutputColumns().getColumnCount());
        assertEquals("my col (synonyms replaced)", transformer.getOutputColumns().getColumnName(0));
        assertEquals("my col (synonyms found)", transformer.getOutputColumns().getColumnName(1));
        assertEquals("my col (master terms found)", transformer.getOutputColumns().getColumnName(2));

        transformer.init();

        assertNull(transformer.transform(new MockInputRow().put(col, "hello"))[0]);
        assertEquals("ALB", transformer.transform(new MockInputRow().put(col, "Albania"))[0]);
        assertEquals("Albania", transformer.transform(new MockInputRow().put(col, "Albania"))[1]);
        assertEquals("ALB", transformer.transform(new MockInputRow().put(col, "Albania"))[2]);

        assertNull(transformer.transform(new MockInputRow().put(col, "foo"))[0]);
        assertNull(transformer.transform(new MockInputRow().put(col, "foo"))[1]);
        assertNull(transformer.transform(new MockInputRow().put(col, "foo"))[2]);

        transformer.close();
    }

    @Test
    public void testTransformWithEveryToken() throws Exception {
        MockInputColumn<String> col = new MockInputColumn<>("my col", String.class);

        // with retain original value
        SynonymLookupTransformer transformer = new SynonymLookupTransformer(col, sc, true, configuration);
        transformer.replaceInlinedSynonyms = true;
        transformer.replacedSynonymsType = SynonymLookupTransformer.ReplacedSynonymsType.LIST;
        transformer.init();
        assertEquals(3, transformer.getOutputColumns().getColumnCount());
        assertEquals("my col (synonyms replaced)", transformer.getOutputColumns().getColumnName(0));
        assertEquals("my col (synonyms found)", transformer.getOutputColumns().getColumnName(1));
        assertEquals("my col (master terms found)", transformer.getOutputColumns().getColumnName(2));

        assertEquals("hello", transformer.transform(new MockInputRow().put(col, "hello"))[0]);
        assertEquals("ALB", transformer.transform(new MockInputRow().put(col, "Albania"))[0]);
        assertEquals("I come from ALB!", transformer.transform(new MockInputRow().put(col, "I come from ALB!"))[0]);
        assertEquals("I come from GBR!", transformer.transform(new MockInputRow().put(col, "I come from Britain!"))[0]);
        assertEquals("I come from GBR!", transformer.transform(new MockInputRow().put(col,
                "I come from Great Britain!"))[0]);
        final Object[] result = transformer.transform(new MockInputRow().put(col,
                "I come from Great Great Britain Albania!"));
        assertEquals("I come from Great GBR ALB!", result[0]);
        @SuppressWarnings("unchecked")
        List<String> synonyms = (List<String>) result[1];
        @SuppressWarnings("unchecked")
        List<String> masterTerms = (List<String>) result[2];
        assertEquals("Great Britain", synonyms.get(0));
        assertEquals("Albania", synonyms.get(1));

        assertEquals("GBR", masterTerms.get(0));
        assertEquals("ALB", masterTerms.get(1));

        transformer.close();

        // without retain original value
        transformer = new SynonymLookupTransformer(col, sc, false, configuration);
        transformer.replaceInlinedSynonyms = false;
        transformer.init();
        assertEquals(3, transformer.getOutputColumns().getColumnCount());
        assertEquals("my col (synonyms replaced)", transformer.getOutputColumns().getColumnName(0));

        assertNull(transformer.transform(new MockInputRow().put(col, "hello"))[0]);
        assertNull(transformer.transform(new MockInputRow().put(col, "hello"))[1]);
        assertNull(transformer.transform(new MockInputRow().put(col, "hello"))[2]);
        assertEquals("ALB", transformer.transform(new MockInputRow().put(col, "Albania"))[0]);
        assertEquals("Albania", transformer.transform(new MockInputRow().put(col, "Albania"))[1]);
        assertEquals("ALB", transformer.transform(new MockInputRow().put(col, "Albania"))[2]);
        transformer.close();
    }
}
