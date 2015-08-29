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

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.junit.Before;
import org.junit.Test;

public class DatastoreSynonymCatalogTest {

    private DatastoreSynonymCatalog _synonymCatalog;
    private DataCleanerConfigurationImpl _configuration;

    @Before
    public void createCsvDataStore() {
        final CsvDatastore csvDatastore = new CsvDatastore("region datastore",
                "src/test/resources/datastore-synonym-countries.csv");
        final DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(csvDatastore);
        _synonymCatalog = new DatastoreSynonymCatalog("my synonym catalog", "region datastore", "region", new String[] {
                "firstsynonym", "secondsynonym", "thirdsynonym" });
        _configuration = new DataCleanerConfigurationImpl().withDatastoreCatalog(datastoreCatalog);
    }

    @Test
    public void shouldReturnCorrectMasterTerm() {
        final SynonymCatalogConnection connection = _synonymCatalog.openConnection(_configuration);
        assertEquals(null, connection.getMasterTerm("region"));
        assertEquals("DNK", connection.getMasterTerm("Denmark"));
        assertEquals("GBR", connection.getMasterTerm("Great Britain"));
        assertEquals("DNK", connection.getMasterTerm("DK"));
        connection.close();
    }

    @Test
    public void shouldReturnAllSynonyms() {
        final SynonymCatalogConnection connection = _synonymCatalog.openConnection(_configuration);
        final Collection<Synonym> synonyms = connection.getSynonyms();
        connection.close();
        org.junit.Assert.assertEquals(3, synonyms.size());
    }

    @Test
    public void shouldReturnNameOfTheCatalog() {
        org.junit.Assert.assertSame("my synonym catalog", _synonymCatalog.getName());
    }
}
