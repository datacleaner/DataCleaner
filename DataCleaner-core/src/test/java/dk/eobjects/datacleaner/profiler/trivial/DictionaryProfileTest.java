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
package dk.eobjects.datacleaner.profiler.trivial;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.easymock.EasyMock;

import dk.eobjects.datacleaner.catalog.IDictionary;
import dk.eobjects.datacleaner.catalog.TextFileDictionary;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.datacleaner.profiler.ProfileManagerTest;
import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.datacleaner.util.ReflectionHelper;
import dk.eobjects.datacleaner.validator.dictionary.DictionaryManager;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;

public class DictionaryProfileTest extends DataCleanerTestCase {

	public void testInitializationExceptions() throws Exception {
		DictionaryProfile dictionaryProfile = new DictionaryProfile();
		Column strCol1 = new Column("col1", ColumnType.VARCHAR);
		Column strCol2 = new Column("col2", ColumnType.VARCHAR);
		Column[] cols = new Column[] { strCol1, strCol2 };

		Map<String, String> properties = new HashMap<String, String>();
		dictionaryProfile.setProperties(properties);

		try {
			dictionaryProfile.initialize(cols);
		} catch (Exception e) {
			assertEquals("No dictionaries specified", e.getMessage());
		}

		properties.put(DictionaryProfile.PREFIX_PROPERTY_DICTIONARY + 0, "foo_bar_foo_bar");

		try {
			dictionaryProfile.initialize(cols);
		} catch (Exception e) {
			assertEquals("No such dictionary, 'foo_bar_foo_bar'", e.getMessage());
		}
	}

	public void testNumbers() throws Exception {
		TextFileDictionary dict = new TextFileDictionary("my_dict", getTestResourceAsFile("numbers_dictionary.txt"));
		DictionaryProfile profile = new DictionaryProfile();

		Column column = new Column("foobar");

		profile.initialize(column);
		ArrayList<IDictionary> dictionaryList = new ArrayList<IDictionary>();
		dictionaryList.add(dict);
		profile.setDictionaryList(dictionaryList);
		profile.setDetailsEnabled(false);

		SelectItem[] selectItems = new SelectItem[] { new SelectItem(column) };

		profile.process(new Row(selectItems, new Object[] { 2200 }), 1);

		assertEquals("Matrix[columnNames={foobar},my_dict={1},No Matches={0},Multiple Matches={0}]", profile
				.getResult().getMatrices()[0].toString());
		
		profile.process(new Row(selectItems, new Object[] { 2201 }), 1);

		assertEquals("Matrix[columnNames={foobar},my_dict={1},No Matches={1},Multiple Matches={0}]", profile
				.getResult().getMatrices()[0].toString());
	}

	public void testProcessValue() throws Exception {
		ProfileManagerTest.initProfileManager();
		List<IDictionary> dictionaries = new ArrayList<IDictionary>();
		dictionaries.add(new TextFileDictionary("foo", new File("src/test/resources/Dictionary.txt")));
		dictionaries.add(new TextFileDictionary("english", new File("src/test/resources/aspell-english.txt")));

		DictionaryManager.setDictionaries(dictionaries);
		DictionaryProfile dictionaryProfile = new DictionaryProfile();
		Column strCol1 = new Column("col1", ColumnType.VARCHAR);
		Column strCol2 = new Column("col2", ColumnType.VARCHAR);
		Column[] cols = new Column[] { strCol1, strCol2 };
		SelectItem[] selectItems = new SelectItem[] { new SelectItem(strCol1), new SelectItem(strCol2) };

		Map<String, String> properties = new HashMap<String, String>();
		ReflectionHelper.addIteratedProperties(properties, DictionaryProfile.PREFIX_PROPERTY_DICTIONARY, new String[] {
				"foo", "english" });
		dictionaryProfile.setProperties(properties);

		dictionaryProfile.initialize(cols);
		Row row1 = new Row(selectItems, new Object[] { "air", "AR" });
		Row row2 = new Row(selectItems, new Object[] { "ea", "ear" });
		Row row3 = new Row(selectItems, new Object[] { "Ar", "Au" });
		Row row4 = new Row(selectItems, new Object[] { "EEO", "e'er" });
		Row row5 = new Row(selectItems, new Object[] { "Arthur", "dhar" });
		Row row6 = new Row(selectItems, new Object[] { "isas", "rahd" });
		dictionaryProfile.process(row1, 1);
		dictionaryProfile.process(row2, 1);
		dictionaryProfile.process(row3, 1);
		dictionaryProfile.process(row4, 1);
		dictionaryProfile.process(row5, 2);
		dictionaryProfile.process(row6, 1);
		IProfileResult result = dictionaryProfile.getResult();
		IMatrix[] matrices = result.getMatrices();
		assertEquals(1, matrices.length);

		assertEquals(
				"Matrix[columnNames={col1,col2},foo={MatrixValue[value=2,detailQuery=SELECT col1, COUNT(*) FROM  GROUP BY col1],0},"
						+ "english={MatrixValue[value=6,detailQuery=SELECT col1, COUNT(*) FROM  GROUP BY col1],MatrixValue[value=4,detailQuery=SELECT col2, COUNT(*) FROM  GROUP BY col2]},"
						+ "No Matches={MatrixValue[value=1,detailSelectItems={col1},detailRows=1],MatrixValue[value=2,detailSelectItems={col2},detailRows=2]},"
						+ "Multiple Matches={MatrixValue[value=1,detailSelectItems={col1},detailRows=1],0}]",
				matrices[0].toString());

		MatrixValue value = matrices[0].getValue("No Matches", "col1");
		DataSet details = value.getDetails(null);
		List<Object[]> data = details.toObjectArrays();
		assertEquals(1, data.size());
		assertEquals("{isas}", ArrayUtils.toString(data.get(0)));
	}

	public void testMultipleMatches() throws Exception {
		List<IDictionary> dictionaries = new ArrayList<IDictionary>();

		IDictionary dict1 = createMock(IDictionary.class);
		IDictionary dict2 = createMock(IDictionary.class);
		IDictionary dict3 = createMock(IDictionary.class);

		dictionaries.add(dict1);
		dictionaries.add(dict2);
		dictionaries.add(dict3);

		DictionaryManager.setDictionaries(dictionaries);

		EasyMock.expect(dict1.isValid("foo", "bar")).andReturn(new boolean[] { true, true });
		EasyMock.expect(dict2.isValid("foo", "bar")).andReturn(new boolean[] { false, true });
		EasyMock.expect(dict3.isValid("foo", "bar")).andReturn(new boolean[] { false, true });

		EasyMock.expect(dict1.getName()).andReturn("d1").anyTimes();
		EasyMock.expect(dict2.getName()).andReturn("d2").anyTimes();
		EasyMock.expect(dict3.getName()).andReturn("d3").anyTimes();

		replayMocks();

		DictionaryProfile dictionaryProfile = new DictionaryProfile();

		Map<String, String> properties = new HashMap<String, String>();
		ReflectionHelper.addIteratedProperties(properties, DictionaryProfile.PREFIX_PROPERTY_DICTIONARY, new String[] {
				"d1", "d2", "d3" });
		dictionaryProfile.setProperties(properties);

		Column col = new Column("some_column", ColumnType.VARCHAR);
		Column[] cols = new Column[] { col };
		dictionaryProfile.initialize(cols);
		dictionaryProfile.setDetailsEnabled(false);

		SelectItem[] selectItems = new SelectItem[] { new SelectItem(col) };
		dictionaryProfile.process(new Row(selectItems, new Object[] { "foo" }), 1);
		dictionaryProfile.process(new Row(selectItems, new Object[] { "bar" }), 1);

		IProfileResult result = dictionaryProfile.getResult();
		IMatrix[] matrices = result.getMatrices();
		assertEquals(1, matrices.length);

		assertEquals("Matrix[columnNames={some_column},d1={2},d2={1},d3={1},No Matches={0},Multiple Matches={1}]",
				matrices[0].toString());

		verifyMocks();
	}
}