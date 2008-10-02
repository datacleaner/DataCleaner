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
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;

import dk.eobjects.datacleaner.catalog.IDictionary;
import dk.eobjects.datacleaner.catalog.TextFileDictionary;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;

public class DictionaryValidationRuleTest extends TestCase {

	public void testSuccesfullProcessing() throws Exception {
		IDictionary dictionaryMock = EasyMock.createMock(IDictionary.class);

		EasyMock.expect(
				dictionaryMock.isValid("data", "cleaner", "dictionary",
						"validation")).andReturn(
				new boolean[] { true, true, true, true });
		EasyMock.expect(dictionaryMock.isValid("rule", "test")).andReturn(
				new boolean[] { true, true });

		EasyMock.replay(dictionaryMock);

		DictionaryValidationRule vr = new DictionaryValidationRule();
		vr.setQueryBufferSize(4);
		vr.setDictionary(dictionaryMock);

		Column col1 = new Column("col1", ColumnType.VARCHAR);
		Column col2 = new Column("col2", ColumnType.VARCHAR);
		Column[] columns = new Column[] { col1, col2 };
		vr.initialize(columns);

		SelectItem[] selectItems = new SelectItem[] { new SelectItem(col1),
				new SelectItem(col2) };

		vr.process(new Row(selectItems, new Object[] { "data", "cleaner" }), 2);
		vr.process(new Row(selectItems, new Object[] { "dictionary",
				"validation" }), 1);
		vr.process(new Row(selectItems, new Object[] { "rule", "test" }), 1);

		IValidationRuleResult result = vr.getResult();
		assertEquals(true, result.isValidated());

		EasyMock.verify(dictionaryMock);
	}

	public void testFailingProcessing() throws Exception {
		IDictionary dictionaryMock = EasyMock.createMock(IDictionary.class);

		EasyMock.expect(
				dictionaryMock.isValid("data", "cleaner", "dictionary",
						"validation", "rule", "test")).andReturn(
				new boolean[] { true, false, true, true, true, false });
		EasyMock.expect(dictionaryMock.isValid("foo", "bar")).andReturn(
				new boolean[] { true, true });

		EasyMock.replay(dictionaryMock);

		DictionaryValidationRule vr = new DictionaryValidationRule();
		vr.setQueryBufferSize(6);
		vr.setDictionary(dictionaryMock);

		Column col1 = new Column("col1", ColumnType.VARCHAR);
		Column col2 = new Column("col2", ColumnType.VARCHAR);
		Column[] columns = new Column[] { col1, col2 };
		vr.initialize(columns);

		SelectItem[] selectItems = new SelectItem[] { new SelectItem(col1),
				new SelectItem(col2) };
		Row row1 = new Row(selectItems, new Object[] { "data", "cleaner" });
		vr.process(row1, 2);
		Row row2 = new Row(selectItems, new Object[] { "dictionary",
				"validation" });
		vr.process(row2, 1);
		Row row3 = new Row(selectItems, new Object[] { "rule", "test" });
		vr.process(row3, 1);
		Row row4 = new Row(selectItems, new Object[] { "foo", "bar" });
		vr.process(row4, 4);

		IValidationRuleResult result = vr.getResult();
		assertEquals(false, result.isValidated());

		List<Row> errorRows = result.getUnvalidatedRows();
		assertEquals(2, errorRows.size());
		assertTrue(errorRows.contains(row1));
		assertFalse(errorRows.contains(row2));
		assertTrue(errorRows.contains(row3));
		assertFalse(errorRows.contains(row4));

		EasyMock.verify(dictionaryMock);
	}

	public void testNoDictionary() throws Exception {
		DictionaryValidationRule vr = new DictionaryValidationRule();
		try {
			vr.process(null, 1);
		} catch (IllegalStateException e) {
			assertEquals("No dictionary provided!", e.getMessage());
		}
	}

	/**
	 * Tests that if the property DICTIONARY_NAME is given, the dictionary will
	 * be resolved using DictionaryManager
	 */
	public void testSetDictionaryByPropertyName() throws Exception {
		DictionaryManager.addDictionary(new TextFileDictionary("english",
				new File("src/test/resources/aspell-english.txt")));

		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put(DictionaryValidationRule.PROPERTY_BUFFER_SIZE, "20");
		properties.put(DictionaryValidationRule.PROPERTY_DICTIONARY_NAME,
				"english");

		DictionaryValidationRule vr = new DictionaryValidationRule();
		vr.setProperties(properties);
		Column column = new Column("column", ColumnType.VARCHAR);
		vr.initialize(column);

		assertEquals(20, vr.getQueryBufferSize());
		IDictionary dictionary = vr.getDictionary();
		assertNotNull(dictionary);
		assertEquals("TextFileDictionary[name=english]", dictionary.toString());

		SelectItem[] selectItems = new SelectItem[] { new SelectItem(column) };
		vr.process(new Row(selectItems, new Object[] { "hello there" }), 1);
		assertTrue(vr.getResult().isValidated());
	}
}