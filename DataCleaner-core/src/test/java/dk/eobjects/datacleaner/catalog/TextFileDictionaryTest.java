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
package dk.eobjects.datacleaner.catalog;

import java.io.File;

import org.apache.commons.lang.ArrayUtils;

import dk.eobjects.datacleaner.catalog.IDictionary;
import dk.eobjects.datacleaner.catalog.TextFileDictionary;
import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Column;

public class TextFileDictionaryTest extends DataCleanerTestCase {

	private IDictionary _dictionary;

	@Override
	protected void setUp() throws Exception {
		_dictionary = new TextFileDictionary(getTestResourceAsFile("Dictionary.txt"));
	}

	public void testSingleWord() throws Exception {
		boolean[] valid = _dictionary.isValid("foo", "baaaar");
		assertTrue(valid[0]);
		assertFalse(valid[1]);
	}

	public void testNull() throws Exception {
		boolean[] valid = _dictionary.isValid("foo", null, "foo bar");
		assertTrue(valid[0]);
		assertFalse(valid[1]);
	}

	public void testSentense() throws Exception {
		boolean[] valid = _dictionary.isValid("foo,  bar", "fooo barrr", "for bar", "foo bar");
		assertTrue(valid[0]);
		assertFalse(valid[1]);
		assertFalse(valid[2]);
		assertTrue(valid[3]);
	}

	public void testOnlyNumbers() throws Exception {
		TextFileDictionary dict = new TextFileDictionary("my_dict", getTestResourceAsFile("numbers_dictionary.txt"));
		assertTrue(dict.isValid("2200")[0]);
		assertFalse(dict.isValid("2201")[0]);
	}

	public void testNumbersInText() throws Exception {
		boolean[] valid = _dictionary.isValid("there are 2 brown foxes", "there are two brown foxes",
				"there are 2brown foxes", "");
		assertTrue(valid[0]);
		assertTrue(valid[1]);
		assertFalse(valid[2]);
		assertTrue(valid[3]);
	}

	/**
	 * Tests the spell checker with a somewhat large dictionary file, based on
	 * the english ASpell wordlist
	 */
	public void testAspellDictionary() throws Exception {
		TextFileDictionary dictionary = new TextFileDictionary(getTestResourceAsFile("aspell-english.txt"));
		boolean[] valid = dictionary.isValid("hello world", "hi john", "my name is g. bush and I'm a jerk", "foob bar");
		assertEquals(4, valid.length);
		assertTrue(valid[0]);
		assertTrue(valid[1]);
		assertTrue(valid[2]);
		assertFalse(valid[3]);
	}

	public void testWriteDictionary() throws Exception {
		File outputFile = getTestResourceAsFile("dictionary_write_output.txt");
		File benchmarkFile = getTestResourceAsFile("dictionary_write_bench.txt");

		DataContext dc = getTestDataContext();
		Column cityColumn = dc.getDefaultSchema().getTableByName("CUSTOMERS").getColumnByName("CITY");

		TextFileDictionary dictionary = TextFileDictionary.createTextFileDictionary("foobar", cityColumn, dc,
				outputFile);
		assertEquals("TextFileDictionary[name=foobar]", dictionary.toString());

		assertEqualsFile(benchmarkFile, outputFile);

		assertEquals("{true,false}", ArrayUtils.toString(dictionary.isValid("Brisbane", "uggamomma")));
	}
}