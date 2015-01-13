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

import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.junit.Before;
import org.junit.Test;

public class DatastoreSynonymCatalogTest {

	private DatastoreSynonymCatalog _dataStoreBasedSynonymCatalog;

	@Before
	public void createCsvDataStore() {
		CsvDatastore csvDatastore = new CsvDatastore("region datastore",
				"src/test/resources/datastore-synonym-countries.csv");
		DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(csvDatastore);
		_dataStoreBasedSynonymCatalog = new DatastoreSynonymCatalog("my synonym catalog", "region datastore", "region",
				new String[] { "firstsynonym", "secondsynonym", "thirdsynonym" });
		_dataStoreBasedSynonymCatalog._datastoreCatalog = datastoreCatalog;
	}

	@Test
	public void shouldReturnCorrectMasterTerm() {
		assertEquals(null, _dataStoreBasedSynonymCatalog.getMasterTerm("region"));
		assertEquals("DNK", _dataStoreBasedSynonymCatalog.getMasterTerm("Denmark"));
		assertEquals("GBR", _dataStoreBasedSynonymCatalog.getMasterTerm("Great Britain"));
		assertEquals("DNK", _dataStoreBasedSynonymCatalog.getMasterTerm("DK"));
	}

	@Test
	public void shouldReturnAllSynonyms() {
		Collection<Synonym> synonyms = _dataStoreBasedSynonymCatalog.getSynonyms();
		org.junit.Assert.assertEquals(3, synonyms.size());
	}

	@Test
	public void shouldReturnNameOfTheCatalog() {
		org.junit.Assert.assertSame("my synonym catalog", _dataStoreBasedSynonymCatalog.getName());
	}
}
