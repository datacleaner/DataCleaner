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
package dk.eobjects.datacleaner.validator.trivial;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.joda.time.DateTime;

import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;

public class ValueRangeValidationRuleTest extends TestCase {

	public void testNumbers() throws Exception {
		ValueRangeValidationRule vr = new ValueRangeValidationRule();
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(ValueRangeValidationRule.PROPERTY_HIGHEST_VALUE, "8");
		properties.put(ValueRangeValidationRule.PROPERTY_LOWEST_VALUE, "3");
		vr.setProperties(properties);
		Column column = new Column("My int column", ColumnType.INTEGER);
		vr.initialize(column);

		SelectItem[] items = new SelectItem[] { new SelectItem(column) };
		Row errorRow1 = new Row(items, new Object[] { 1 });
		vr.process(errorRow1, 1);
		Row errorRow2 = new Row(items, new Object[] { 2 });
		vr.process(errorRow2, 1);
		vr.process(new Row(items, new Object[] { 3 }), 1);
		vr.process(new Row(items, new Object[] { 4 }), 1);
		vr.process(new Row(items, new Object[] { 5 }), 1);
		vr.process(new Row(items, new Object[] { 6 }), 1);
		vr.process(new Row(items, new Object[] { 7 }), 1);
		vr.process(new Row(items, new Object[] { 8 }), 1);
		Row errorRow3 = new Row(items, new Object[] { 9 });
		vr.process(errorRow3, 1);
		Row errorRow4 = new Row(items, new Object[] { 10 });
		vr.process(errorRow4, 1);

		IValidationRuleResult result = vr.getResult();

		List<Row> errorRows = result.getUnvalidatedRows();
		assertTrue(errorRows.contains(errorRow1));
		assertTrue(errorRows.contains(errorRow2));
		assertTrue(errorRows.contains(errorRow3));
		assertTrue(errorRows.contains(errorRow4));
	}

	public void testNull() throws Exception {
		ValueRangeValidationRule vr = new ValueRangeValidationRule();
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(ValueRangeValidationRule.PROPERTY_HIGHEST_VALUE,
				"2010-01-01");
		properties.put(ValueRangeValidationRule.PROPERTY_LOWEST_VALUE,
				"2000-01-01");
		vr.setProperties(properties);
		Column column = new Column("My date column", ColumnType.DATE);
		vr.initialize(column);

		SelectItem[] items = new SelectItem[] { new SelectItem(column) };
		vr.process(new Row(items, new Object[] { new DateTime(2005, 1, 1, 0, 0,
				0, 0).toDate() }), 1);
		vr.process(new Row(items, new Object[] { null }), 1);
		vr.process(new Row(items, new Object[] { new DateTime(2001, 1, 1, 0, 0,
				0, 0).toDate() }), 1);
		vr.process(new Row(items, new Object[] { new DateTime(2009, 1, 1, 0, 0,
				0, 0).toDate() }), 1);

		IValidationRuleResult result = vr.getResult();

		assertEquals(1, result.getUnvalidatedRows().size());
	}

	public void testLowerThan() throws Exception {
		ValueRangeValidationRule vr = new ValueRangeValidationRule();
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(ValueRangeValidationRule.PROPERTY_HIGHEST_VALUE, "10");
		vr.setProperties(properties);
		Column column = new Column("My int column", ColumnType.INTEGER);
		vr.initialize(column);

		SelectItem[] items = new SelectItem[] { new SelectItem(column) };
		vr.process(new Row(items, new Object[] { -5 }), 1);
		vr.process(new Row(items, new Object[] { 6 }), 1);
		vr.process(new Row(items, new Object[] { 10 }), 1);
		vr.process(new Row(items, new Object[] { 4000 }), 1);
		vr.process(new Row(items, new Object[] { 54353796 }), 1);

		IValidationRuleResult result = vr.getResult();
		assertEquals(2, result.getUnvalidatedRows().size());
	}

	public void testGreaterThan() throws Exception {
		ValueRangeValidationRule vr = new ValueRangeValidationRule();
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(ValueRangeValidationRule.PROPERTY_LOWEST_VALUE, "3");
		vr.setProperties(properties);
		Column column = new Column("My int column", ColumnType.INTEGER);
		vr.initialize(column);
		SelectItem[] items = new SelectItem[] { new SelectItem(column) };

		Row errorRow1 = new Row(items, new Object[] { 1 });
		vr.process(errorRow1, 1);
		Row errorRow2 = new Row(items, new Object[] { 2 });
		vr.process(errorRow2, 1);
		vr.process(new Row(items, new Object[] { 3 }), 1);
		vr.process(new Row(items, new Object[] { 4000 }), 1);
		vr.process(new Row(items, new Object[] { 54353796 }), 1);

		IValidationRuleResult result = vr.getResult();
		assertEquals(2, result.getUnvalidatedRows().size());

		List<Row> errorRows = result.getUnvalidatedRows();
		assertTrue(errorRows.contains(errorRow1));
		assertTrue(errorRows.contains(errorRow2));
	}

	public void testMixedColumns() throws Exception {
		ValueRangeValidationRule vr = new ValueRangeValidationRule();
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(ValueRangeValidationRule.PROPERTY_HIGHEST_VALUE, "8");
		properties.put(ValueRangeValidationRule.PROPERTY_LOWEST_VALUE, "3");
		vr.setProperties(properties);
		Column intColumn = new Column("My int column", ColumnType.INTEGER);
		Column stringColumn = new Column("My string column", ColumnType.VARCHAR);
		Column[] columns = new Column[] { intColumn, stringColumn };
		vr.initialize(columns);

		assertEquals("NumberComparable[number=8.0]", vr.getHighestValue(
				intColumn).toString());
		assertEquals("ToStringComparable[string=8]", vr.getHighestValue(
				stringColumn).toString());

		assertEquals("NumberComparable[number=3.0]", vr.getLowestValue(
				intColumn).toString());
		assertEquals("ToStringComparable[string=3]", vr.getLowestValue(
				stringColumn).toString());
	}

	public void testIncompatibleColumn() throws Exception {
		ValueRangeValidationRule vr = new ValueRangeValidationRule();
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(ValueRangeValidationRule.PROPERTY_HIGHEST_VALUE, "foo");
		properties.put(ValueRangeValidationRule.PROPERTY_LOWEST_VALUE, "bar");
		vr.setProperties(properties);
		Column intColumn = new Column("My int column", ColumnType.INTEGER);
		Column stringColumn = new Column("My string column", ColumnType.VARCHAR);
		Column[] columns = new Column[] { intColumn, stringColumn };
		vr.initialize(columns);

		assertEquals(1, vr.getColumns().length);
		assertSame(stringColumn, vr.getColumns()[0]);
	}

	public void testGetEvaluatedColumns() throws Exception {
		ValueRangeValidationRule vr = new ValueRangeValidationRule();
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(ValueRangeValidationRule.PROPERTY_LOWEST_VALUE, "3");
		vr.setProperties(properties);
		Column usedColumn = new Column("My int column", ColumnType.INTEGER);
		Column unusedColumn = new Column("My unused column", ColumnType.VARCHAR);
		vr.initialize(usedColumn);

		SelectItem[] items = new SelectItem[] { new SelectItem(usedColumn),
				new SelectItem(unusedColumn) };
		Row errorRow1 = new Row(items, new Object[] { 1, "foobar" });
		vr.process(errorRow1, 1);

		IValidationRuleResult result = vr.getResult();
		Column[] evaluatedColumns = result.getEvaluatedColumns();
		assertEquals(1, evaluatedColumns.length);
	}
}