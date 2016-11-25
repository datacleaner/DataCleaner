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
package org.datacleaner.reference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.SerializationUtils;
import org.datacleaner.util.ChangeAwareObjectInputStream;
import org.junit.Ignore;

import junit.framework.TestCase;

public class ReferenceDataDeserializationTest extends TestCase {

    public void testDeserializeSimpleDictionary() throws Exception {
        final SimpleDictionary obj = deserialize("src/test/resources/serialized_ref_data/dc_4_dictionary_simple.dat");
        assertEquals("simple dictionary", obj.getName());
        assertEquals("[bar, baz, foo]", new TreeSet<>(obj.getValueSet()).toString());
        assertTrue(obj.isCaseSensitive());
    }

    public void testDeserializeTextFileDictionary() throws Exception {
        final TextFileDictionary obj =
                deserialize("src/test/resources/serialized_ref_data/dc_4_dictionary_text_file.dat");
        assertEquals("text file dictionary", obj.getName());
        assertEquals("dictionary.txt", obj.getFilename());
        assertEquals("UTF8", obj.getEncoding());
        assertTrue(obj.isCaseSensitive());
    }

    public void testDeserializeDatastoreDictionary() throws Exception {
        final DatastoreDictionary obj =
                deserialize("src/test/resources/serialized_ref_data/dc_4_dictionary_datastore.dat");
        assertEquals("datastore dictionary", obj.getName());
        assertEquals("orderdb", obj.getDatastoreName());
        assertEquals("dictionary.term", obj.getQualifiedColumnName());
    }

    public void testDeserializeSimpleSynonymCatalog() throws Exception {
        final SimpleSynonymCatalog obj =
                deserialize("src/test/resources/serialized_ref_data/dc_4_synonym_catalog_simple.dat");
        assertEquals("simple synonym catalog", obj.getName());
        assertEquals("{DK=DK, DNK=DK, Danmark=DK, Denmark=DK}", new TreeMap<>(obj.getSynonymMap()).toString());
        assertTrue(obj.isCaseSensitive());
    }

    public void testDeserializeTextFileSynonymCatalog() throws Exception {
        final TextFileSynonymCatalog obj =
                deserialize("src/test/resources/serialized_ref_data/dc_4_synonym_catalog_text_file.dat");
        assertEquals("text file synonym catalog", obj.getName());
        assertEquals("synonyms.txt", obj.getFilename());
        assertEquals("UTF8", obj.getEncoding());
        assertTrue(obj.isCaseSensitive());
    }

    public void testDeserializeDatastoreSynonymCatalog() throws Exception {
        final DatastoreSynonymCatalog obj =
                deserialize("src/test/resources/serialized_ref_data/dc_4_synonym_catalog_datastore.dat");
        assertEquals("datastore synonym catalog", obj.getName());
        assertEquals("orderdb", obj.getDatastoreName());
        assertEquals("synonyms.master", obj.getMasterTermColumnPath());
        assertEquals("[synonyms.syn1, synonyms.syn2]", Arrays.toString(obj.getSynonymColumnPaths()));
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize(final String path) throws Exception {
        try (FileInputStream in = new FileInputStream(new File(path))) {
            try (ChangeAwareObjectInputStream objectInputStream = new ChangeAwareObjectInputStream(in)) {
                return (T) objectInputStream.readObject();
            }
        }
    }

    @Ignore
    public void testSerialize() throws Exception {
        final Dictionary dict1 = new SimpleDictionary("simple dictionary", "foo", "bar", "baz");
        serialize(dict1, "target/dc_4_dictionary_simple.dat");

        final Dictionary dict2 = new TextFileDictionary("text file dictionary", "dictionary.txt", "UTF8");
        serialize(dict2, "target/dc_4_dictionary_text_file.dat");

        final Dictionary dict3 = new DatastoreDictionary("datastore dictionary", "orderdb", "dictionary.term");
        serialize(dict3, "target/dc_4_dictionary_datastore.dat");

        final Synonym synonym = new SimpleSynonym("DK", "Denmark", "Danmark", "DNK");
        final SynonymCatalog sc1 = new SimpleSynonymCatalog("simple synonym catalog", synonym);
        serialize(sc1, "target/dc_4_synonym_catalog_simple.dat");

        final SynonymCatalog sc2 =
                new TextFileSynonymCatalog("text file synonym catalog", "synonyms.txt", true, "UTF8");
        serialize(sc2, "target/dc_4_synonym_catalog_text_file.dat");

        final SynonymCatalog sc3 =
                new DatastoreSynonymCatalog("datastore synonym catalog", "orderdb", "synonyms.master",
                        new String[] { "synonyms.syn1", "synonyms.syn2" });
        serialize(sc3, "target/dc_4_synonym_catalog_datastore.dat");
    }

    private void serialize(final Serializable obj, final String path) throws Exception {
        try (OutputStream out = new FileOutputStream(new File(path))) {
            SerializationUtils.serialize(obj, out);
        }
    }
}
