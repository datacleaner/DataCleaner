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

import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.reference.TextFileSynonymCatalog;

public class SynonymLookupTransformerTest extends TestCase {

    private final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();
    private final SynonymCatalog sc = new TextFileSynonymCatalog("my synonyms",
            "src/test/resources/synonym-countries.txt", true, "UTF8");

    public void testTransformWithCompleteInput() throws Exception {
        MockInputColumn<String> col = new MockInputColumn<>("my col", String.class);

        // with retain original value
        SynonymLookupTransformer transformer = new SynonymLookupTransformer(col, sc, true, configuration);
        assertEquals(1, transformer.getOutputColumns().getColumnCount());
        assertEquals("my col (synonyms replaced)", transformer.getOutputColumns().getColumnName(0));
        
        transformer.init();

        assertEquals("hello", transformer.transform(new MockInputRow().put(col, "hello"))[0]);
        assertEquals("ALB", transformer.transform(new MockInputRow().put(col, "Albania"))[0]);
        assertEquals("I come from Albania!",
                transformer.transform(new MockInputRow().put(col, "I come from Albania!"))[0]);
        
        transformer.close();

        // without retain original value
        transformer = new SynonymLookupTransformer(col, sc, false, configuration);
        assertEquals(1, transformer.getOutputColumns().getColumnCount());
        assertEquals("my col (synonyms replaced)", transformer.getOutputColumns().getColumnName(0));
        transformer.init();

        assertNull(transformer.transform(new MockInputRow().put(col, "hello"))[0]);
        assertEquals("ALB", transformer.transform(new MockInputRow().put(col, "Albania"))[0]);
        transformer.close();
    }

    public void testTransformWithEveryToken() throws Exception {
        MockInputColumn<String> col = new MockInputColumn<>("my col", String.class);

        // with retain original value
        SynonymLookupTransformer transformer = new SynonymLookupTransformer(col, sc, true, configuration);
        transformer.lookUpEveryToken = true;
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
        assertEquals("I come from GBR!", transformer.transform(new MockInputRow().put(col, "I come from Great Britain!"))[0]);
        final Object[] result  =
                transformer.transform(new MockInputRow().put(col, "I come from Great Great Britain Albania!"));
        assertEquals("I come from Great GBR ALB!", result [0]);
        @SuppressWarnings("unchecked") List<String> synonyms = (List<String>) result[1];
        @SuppressWarnings("unchecked") List<String> masterTerms  = (List<String>) result[2];
        assertEquals("Great Britain", synonyms.get(0));
        assertEquals("Albania", synonyms.get(1));

        assertEquals("GBR", masterTerms.get(0));
        assertEquals("ALB", masterTerms.get(1));

        transformer.close();

        // without retain original value
        transformer = new SynonymLookupTransformer(col, sc, false, configuration);
        transformer.init();
        assertEquals(1, transformer.getOutputColumns().getColumnCount());
        assertEquals("my col (synonyms replaced)", transformer.getOutputColumns().getColumnName(0));

        assertNull(transformer.transform(new MockInputRow().put(col, "hello"))[0]);
        assertEquals("ALB", transformer.transform(new MockInputRow().put(col, "Albania"))[0]);
        transformer.close();
    }
}
