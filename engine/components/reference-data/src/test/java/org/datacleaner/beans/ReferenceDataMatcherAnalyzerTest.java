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
package org.datacleaner.beans;

import junit.framework.TestCase;

import org.datacleaner.data.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.SimpleDictionary;
import org.datacleaner.reference.SimpleStringPattern;
import org.datacleaner.reference.SimpleSynonym;
import org.datacleaner.reference.SimpleSynonymCatalog;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.result.renderer.CrosstabTextRenderer;

public class ReferenceDataMatcherAnalyzerTest extends TestCase {

	private final MockInputColumn<String> column2 = new MockInputColumn<String>(
			"PERSON", String.class);
	private final MockInputColumn<String> column1 = new MockInputColumn<String>(
			"GREETING", String.class);
	private final Dictionary dict1 = new SimpleDictionary("Greetings", "Hi",
			"Hello", "Howdy");
	private final Dictionary dict2 = new SimpleDictionary("Male names", "John",
			"Joe");
	private final Dictionary dict3 = new SimpleDictionary("Female names",
			"Barbara", "Jane");
	private final StringPattern sp1 = new SimpleStringPattern(
			"Correct case word", "Aaaaaa");
	private final StringPattern sp2 = new SimpleStringPattern("Lowercase word",
			"aaaaaa");
	private final SynonymCatalog sc1 = new SimpleSynonymCatalog(
			"Geek language", new SimpleSynonym("foobar", "foo", "bar", "baz"),
			new SimpleSynonym("hello world", "hello", "world"),
			new SimpleSynonym("lorem ipsum", "lorem", "ipsum", "dolor"));
	private final SynonymCatalog sc2 = new SimpleSynonymCatalog("Name genders",
			new SimpleSynonym("male", "jack", "joe", "kim"), new SimpleSynonym(
					"female", "jane", "barbara", "kim"));

	public void testMultipleColumnsDictionariesStringPatterns()
			throws Exception {
		final InputColumn<?>[] columns = { column1, column2 };
		final Dictionary[] dictionaries = { dict1, dict2, dict3 };
		final StringPattern[] stringPatterns = { sp1, sp2 };
		final ReferenceDataMatcherAnalyzer analyzer = new ReferenceDataMatcherAnalyzer(
				columns, dictionaries, null, stringPatterns);

		analyzer.validate();
		analyzer.init();

		analyzer.run(
				new MockInputRow().put(column1, "Hey").put(column2, "Joe"), 1);
		analyzer.run(
				new MockInputRow().put(column1, "Hi").put(column2, "John"), 1);
		analyzer.run(
				new MockInputRow().put(column1, "Hello").put(column2, "World"),
				1);
		analyzer.run(
				new MockInputRow().put(column1, "Hello").put(column2, "Jane"),
				1);

		BooleanAnalyzerResult result = analyzer.getResult();

		String[] resultLines = new CrosstabTextRenderer().render(
				result.getColumnStatisticsCrosstab()).split("\n");
		assertEquals(5, resultLines.length);
		assertEquals(
				"                 GREETING in 'Greetings'     GREETING in 'Male names'   GREETING in 'Female names' GREETING 'Correct case word'    GREETING 'Lowercase word'        PERSON in 'Greetings'       PERSON in 'Male names'     PERSON in 'Female names'   PERSON 'Correct case word'      PERSON 'Lowercase word' ",
				resultLines[0]);
		assertEquals(
				"Row count                              4                            4                            4                            4                            4                            4                            4                            4                            4                            4 ",
				resultLines[1]);
		assertEquals(
				"Null count                             0                            0                            0                            0                            0                            0                            0                            0                            0                            0 ",
				resultLines[2]);
		assertEquals(
				"True count                             3                            0                            0                            4                            0                            0                            2                            1                            4                            0 ",
				resultLines[3]);
		assertEquals(
				"False count                            1                            4                            4                            0                            4                            4                            2                            3                            0                            4 ",
				resultLines[4]);
	}

	public void testDictionariesAndSynonymCatalogs() throws Exception {
		ReferenceDataMatcherAnalyzer analyzer = new ReferenceDataMatcherAnalyzer(
				new InputColumn[] { column1, column2 },
				new Dictionary[] { dict1 }, new SynonymCatalog[] { sc1, sc2 },
				new StringPattern[] { sp1 });

		analyzer.validate();
		analyzer.init();

		BooleanAnalyzerResult result = analyzer.getResult();

		analyzer.run(
				new MockInputRow().put(column1, "hello").put(column2, "foo"), 1);

		result = analyzer.getResult();

		String[] resultLines = new CrosstabTextRenderer().render(
				result.getColumnStatisticsCrosstab()).split("\n");
		assertEquals(5, resultLines.length);
		assertEquals(
				"                 GREETING in 'Greetings'    GREETING in Geek language     GREETING in Name genders GREETING 'Correct case word'        PERSON in 'Greetings'      PERSON in Geek language       PERSON in Name genders   PERSON 'Correct case word' ",
				resultLines[0]);
		assertEquals(
				"Row count                              1                            1                            1                            1                            1                            1                            1                            1 ",
				resultLines[1]);
		assertEquals(
				"Null count                             0                            0                            0                            0                            0                            0                            0                            0 ",
				resultLines[2]);
		assertEquals(
				"True count                             0                            1                            0                            0                            0                            1                            0                            0 ",
				resultLines[3]);
		assertEquals(
				"False count                            1                            0                            1                            1                            1                            0                            1                            1 ",
				resultLines[4]);
	}

	public void testNoReferenceData() throws Exception {
		ReferenceDataMatcherAnalyzer analyzer = new ReferenceDataMatcherAnalyzer();

		try {
			analyzer.validate();
			fail("Exception expected");
		} catch (IllegalStateException e) {
			assertEquals(
					"No dictionaries, synonym catalogs or string patterns selected",
					e.getMessage());
		}
	}
}
