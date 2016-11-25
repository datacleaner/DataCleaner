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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;
import org.datacleaner.connection.Datastore;
import org.datacleaner.test.TestHelper;

import junit.framework.TestCase;

public class AbstractReferenceDataTest extends TestCase {

    private ReferenceDataCatalogImpl referenceDataCatalog;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        if (referenceDataCatalog == null) {
            final Datastore ds = TestHelper.createSampleDatabaseDatastore("my_jdbc_connection");

            final List<Dictionary> dictionaries = new ArrayList<>();
            dictionaries.add(new DatastoreDictionary("datastore_dict", "my_jdbc_connection", "EMPLOYEES.LASTNAME"));
            dictionaries.add(new TextFileDictionary("dict_txt", "src/test/resources/lastnames.txt", "UTF-8"));
            dictionaries.add(new SimpleDictionary("valuelist_dict", "hello", "hi", "greetings", "godday"));

            final List<SynonymCatalog> synonymCatalogs = new ArrayList<>();
            synonymCatalogs
                    .add(new TextFileSynonymCatalog("textfile_syn", "src/test/resources/synonym-countries.txt", false,
                            "UTF-8"));
            synonymCatalogs.add(new DatastoreSynonymCatalog("datastore_syn", ds.getName(), "CUSTOMERS.CUSTOMERNAME",
                    new String[] { "CUSTOMERS.CUSTOMERNUMBER", "CUSTOMERS.PHONE" }));

            final List<StringPattern> stringPatterns = new ArrayList<>();
            stringPatterns.add(new RegexStringPattern("regex danish mail", "[a-z]+@[a-z]+\\.dk", true));

            referenceDataCatalog = new ReferenceDataCatalogImpl(dictionaries, synonymCatalogs, stringPatterns);
        }
    }

    public void testSerializationAndDeserializationOfDictionaries() throws Exception {

        final String[] dictionaryNames = referenceDataCatalog.getDictionaryNames();

        for (final String name : dictionaryNames) {
            final Dictionary dict = referenceDataCatalog.getDictionary(name);

            if (dict instanceof AbstractReferenceData) {
                System.out.println("Cloning dictionary: " + dict);
                final Object clone = SerializationUtils.clone(dict);
                if (!dict.equals(clone)) {
                    dict.equals(clone);
                }
                assertEquals(dict, clone);
            }
        }
    }

    public void testSerializationAndDeserializationOfSynonymCatalogs() throws Exception {
        final String[] synonymCatalogNames = referenceDataCatalog.getSynonymCatalogNames();

        for (final String name : synonymCatalogNames) {
            final SynonymCatalog sc = referenceDataCatalog.getSynonymCatalog(name);

            if (sc instanceof AbstractReferenceData) {
                System.out.println("Cloning synonym catalog: " + sc);
                final Object clone = SerializationUtils.clone(sc);
                assertEquals(sc, clone);
            }
        }
    }

    public void testSerializationAndDeserializationOfStringPatterns() throws Exception {
        final String[] patternNames = referenceDataCatalog.getStringPatternNames();

        for (final String name : patternNames) {
            final StringPattern pattern = referenceDataCatalog.getStringPattern(name);

            if (pattern instanceof AbstractReferenceData) {
                System.out.println("Cloning string pattern: " + pattern);
                final Object clone = SerializationUtils.clone(pattern);
                if (!pattern.equals(clone)) {
                    System.out.println();
                }
                assertEquals(pattern, clone);
            }
        }
    }
}
