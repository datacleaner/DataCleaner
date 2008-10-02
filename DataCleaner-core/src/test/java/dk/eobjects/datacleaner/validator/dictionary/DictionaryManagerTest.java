/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.validator.dictionary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;

import dk.eobjects.datacleaner.catalog.IDictionary;
import dk.eobjects.datacleaner.catalog.TextFileDictionary;

public class DictionaryManagerTest extends TestCase {

	public void testGetDictionaryByName() throws Exception {
		List<IDictionary> dictionaries = new ArrayList<IDictionary>();
		dictionaries.add(new TextFileDictionary("foo", new File(
				"src/test/resources/Dictionary.txt")));
		dictionaries.add(new TextFileDictionary("english", new File(
				"src/test/resources/aspell-english.txt")));
		DictionaryManager.setDictionaries(dictionaries);

		assertEquals(
				"{TextFileDictionary[name=foo],TextFileDictionary[name=english]}",
				ArrayUtils.toString(DictionaryManager.getDictionaries()));

		assertEquals("TextFileDictionary[name=foo]", DictionaryManager
				.getDictionaryByName("foo").toString());
		assertNull(DictionaryManager.getDictionaryByName("bar"));
	}
}