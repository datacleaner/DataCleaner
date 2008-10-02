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

import java.sql.Connection;
import java.util.HashMap;
import java.util.StringTokenizer;

import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class RegexProfileTest extends DataCleanerTestCase {

	public void testProfileSingleColumn() throws Exception {
		RegexProfile profile = new RegexProfile();
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put(RegexProfile.PREFIX_PROPERTY_LABEL + 0,
				"only ab, aab, aaab etc.");
		properties.put(RegexProfile.PREFIX_PROPERTY_LABEL + 1,
				"any char string");
		properties.put(RegexProfile.PREFIX_PROPERTY_REGEX + 0, "a*b");
		properties.put(RegexProfile.PREFIX_PROPERTY_REGEX + 1, "[a-zA-Z]*");
		profile.setProperties(properties);
		Column column = new Column("column");
		SelectItem[] selectItems = new SelectItem[] { new SelectItem(column) };
		profile.initialize(column);
		profile.setDetailsEnabled(false);

		profile.process(new Row(selectItems, new Object[] { "mrrrh" }), 32);

		IMatrix matrix = profile.getResult().getMatrices()[0];
		assertEquals(
				"Matrix[columnNames={column},only ab, aab, aaab etc.={0},any char string={32},No Matches={0},Multiple Matches={0}]",
				matrix.toString());

		profile.process(new Row(selectItems, new Object[] { "w00p" }), 16);
		profile.process(new Row(selectItems, new Object[] { "ab" }), 8);
		profile.process(new Row(selectItems, new Object[] { "123" }), 4);
		profile.process(new Row(selectItems, new Object[] { "foobar" }), 2);
		profile.process(new Row(selectItems, new Object[] { "aab" }), 1);

		matrix = profile.getResult().getMatrices()[0];
		assertEquals(
				"Matrix[columnNames={column},only ab, aab, aaab etc.={9},any char string={43},"
						+ "No Matches={20},Multiple Matches={9}]", matrix
						.toString());
	}

	public void testSeveralColumns() throws Exception {
		Connection connection = getTestDbConnection();
		DataContext dc = new DataContext(connection);

		Table employeesTable = dc.getDefaultSchema()
				.getTableByName("EMPLOYEES");
		Column lastNameColumn = employeesTable.getColumnByName("LASTNAME");
		Column emailColumn = employeesTable.getColumnByName("EMAIL");
		Column jobtitleColumn = employeesTable.getColumnByName("JOBTITLE");
		Column[] columns = { lastNameColumn, emailColumn, jobtitleColumn };

		RegexProfile profile = new RegexProfile();
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put(RegexProfile.PREFIX_PROPERTY_LABEL + 0, "email");
		properties.put(RegexProfile.PREFIX_PROPERTY_LABEL + 1, "a single word");
		properties.put(RegexProfile.PREFIX_PROPERTY_REGEX + 0,
				"[a-zA-Z0-9._%+-]*@[a-zA-Z0-9._%+-]*\\.[a-z]{2,4}");
		properties.put(RegexProfile.PREFIX_PROPERTY_REGEX + 1, "[a-zA-Z]*");
		profile.setProperties(properties);

		profile.initialize(columns);

		DataSet data = dc.executeQuery(new Query().select(columns).from(
				employeesTable));
		while (data.next()) {
			Row row = data.getRow();
			profile.process(row, 1);
		}
		data.close();

		IMatrix[] matrices = profile.getResult().getMatrices();
		assertEquals(1, matrices.length);

		assertEquals(
				"Matrix[columnNames={LASTNAME,EMAIL,JOBTITLE},"
						+ "email={0,MatrixValue[value=23,detailQuery=SELECT _EMPLOYEES_._EMPLOYEENUMBER_, _EMPLOYEES_._LASTNAME_, _EMPLOYEES_._FIRSTNAME_, _EMPLOYEES_._EXTENSION_, _EMPLOYEES_._EMAIL_, _EMPLOYEES_._OFFICECODE_, _EMPLOYEES_._REPORTSTO_, _EMPLOYEES_._JOBTITLE_ FROM APP._EMPLOYEES_],0},"
						+ "a single word={MatrixValue[value=23,detailQuery=SELECT _EMPLOYEES_._EMPLOYEENUMBER_, _EMPLOYEES_._LASTNAME_, _EMPLOYEES_._FIRSTNAME_, _EMPLOYEES_._EXTENSION_, _EMPLOYEES_._EMAIL_, _EMPLOYEES_._OFFICECODE_, _EMPLOYEES_._REPORTSTO_, _EMPLOYEES_._JOBTITLE_ FROM APP._EMPLOYEES_],0,MatrixValue[value=1,detailQuery=SELECT _EMPLOYEES_._EMPLOYEENUMBER_, _EMPLOYEES_._LASTNAME_, _EMPLOYEES_._FIRSTNAME_, _EMPLOYEES_._EXTENSION_, _EMPLOYEES_._EMAIL_, _EMPLOYEES_._OFFICECODE_, _EMPLOYEES_._REPORTSTO_, _EMPLOYEES_._JOBTITLE_ FROM APP._EMPLOYEES_]},"
						+ "No Matches={0,0,MatrixValue[value=22,detailQuery=SELECT _EMPLOYEES_._EMPLOYEENUMBER_, _EMPLOYEES_._LASTNAME_, _EMPLOYEES_._FIRSTNAME_, _EMPLOYEES_._EXTENSION_, _EMPLOYEES_._EMAIL_, _EMPLOYEES_._OFFICECODE_, _EMPLOYEES_._REPORTSTO_, _EMPLOYEES_._JOBTITLE_ FROM APP._EMPLOYEES_]},"
						+ "Multiple Matches={0,0,0}]", matrices[0].toString()
						.replace('"', '_'));

		// Test the the number of rows in detail data are the same as presented
		// in the matrix
		DataSet detailData = matrices[0].getValue("a single word", "LASTNAME")
				.getDetails(dc);
		SelectItem countAllItem = SelectItem.getCountAllItem();
		int count = 0;
		while (detailData.next()) {
			Row row = detailData.getRow();
			Number countValue = (Number) row.getValue(countAllItem);
			if (countValue != null) {
				count += countValue.intValue();
			} else {
				count++;
			}

			// While we're at it, we might just as well test that the
			// "single word" regex worked ;)
			String lastName = row.getValue(lastNameColumn).toString();
			assertEquals(1, new StringTokenizer(lastName).countTokens());
		}
		detailData.close();
		assertEquals(23, count);

		count = 0;
		detailData = matrices[0].getValue("a single word", "JOBTITLE")
				.getDetails(dc);
		while (detailData.next()) {
			Row row = detailData.getRow();
			Number countValue = (Number) row.getValue(countAllItem);
			if (countValue != null) {
				count += countValue.intValue();
			} else {
				count++;
			}
		}
		detailData.close();
		assertEquals(1, count);

		count = 0;
		detailData = matrices[0].getValue("No Matches", "JOBTITLE").getDetails(
				dc);
		while (detailData.next()) {
			Row row = detailData.getRow();
			Number countValue = (Number) row.getValue(countAllItem);
			if (countValue != null) {
				count += countValue.intValue();
			} else {
				count++;
			}
		}
		detailData.close();
		assertEquals(22, count);

		connection.close();
	}
}