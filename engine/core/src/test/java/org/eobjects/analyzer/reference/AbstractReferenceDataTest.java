/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.reference;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.SerializationUtils;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.test.TestHelper;

public class AbstractReferenceDataTest extends TestCase {

	private ReferenceDataCatalogImpl referenceDataCatalog;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		if (referenceDataCatalog == null) {
			Datastore ds = TestHelper.createSampleDatabaseDatastore("my_jdbc_connection");

			List<Dictionary> dictionaries = new ArrayList<Dictionary>();
			dictionaries.add(new DatastoreDictionary("datastore_dict", "my_jdbc_connection", "EMPLOYEES.LASTNAME"));
			dictionaries.add(new TextFileDictionary("dict_txt", "src/test/resources/lastnames.txt", "UTF-8"));
			dictionaries.add(new SimpleDictionary("valuelist_dict", "hello", "hi", "greetings", "godday"));

			List<SynonymCatalog> synonymCatalogs = new ArrayList<SynonymCatalog>();
			synonymCatalogs.add(new TextFileSynonymCatalog("textfile_syn", "src/test/resources/synonym-countries.txt",
					false, "UTF-8"));
			synonymCatalogs.add(new DatastoreSynonymCatalog("datastore_syn", ds.getName(), "CUSTOMERS.CUSTOMERNAME",
					new String[] { "CUSTOMERS.CUSTOMERNUMBER", "CUSTOMERS.PHONE" }));

			List<StringPattern> stringPatterns = new ArrayList<StringPattern>();
			stringPatterns.add(new RegexStringPattern("regex danish mail", "[a-z]+@[a-z]+\\.dk", true));

			referenceDataCatalog = new ReferenceDataCatalogImpl(dictionaries, synonymCatalogs, stringPatterns);
		}
	}

	public void testSerializationAndDeserializationOfDictionaries() throws Exception {

		String[] dictionaryNames = referenceDataCatalog.getDictionaryNames();

		for (String name : dictionaryNames) {
			Dictionary dict = referenceDataCatalog.getDictionary(name);

			if (dict instanceof AbstractReferenceData) {
				System.out.println("Cloning dictionary: " + dict);
				Object clone = SerializationUtils.clone(dict);
				if (!dict.equals(clone)) {
					dict.equals(clone);
				}
				assertEquals(dict, clone);
			}
		}
	}

	public void testSerializationAndDeserializationOfSynonymCatalogs() throws Exception {
		String[] synonymCatalogNames = referenceDataCatalog.getSynonymCatalogNames();

		for (String name : synonymCatalogNames) {
			SynonymCatalog sc = referenceDataCatalog.getSynonymCatalog(name);

			if (sc instanceof AbstractReferenceData) {
				System.out.println("Cloning synonym catalog: " + sc);
				Object clone = SerializationUtils.clone(sc);
				assertEquals(sc, clone);
			}
		}
	}

	public void testSerializationAndDeserializationOfStringPatterns() throws Exception {
		String[] patternNames = referenceDataCatalog.getStringPatternNames();

		for (String name : patternNames) {
			StringPattern pattern = referenceDataCatalog.getStringPattern(name);

			if (pattern instanceof AbstractReferenceData) {
				System.out.println("Cloning string pattern: " + pattern);
				Object clone = SerializationUtils.clone(pattern);
				if (!pattern.equals(clone)) {
					System.out.println();
				}
				assertEquals(pattern, clone);
			}
		}
	}
}
