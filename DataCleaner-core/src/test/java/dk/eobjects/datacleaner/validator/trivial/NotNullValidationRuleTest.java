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

import junit.framework.TestCase;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;

public class NotNullValidationRuleTest extends TestCase {

	public void testProcessing() throws Exception {
		Column col1 = new Column("name", ColumnType.VARCHAR);
		Column col2 = new Column("gender", ColumnType.VARCHAR);
		Column[] columns = new Column[] { col1, col2 };
		NotNullValidationRule vr = new NotNullValidationRule();

		SelectItem[] items = new SelectItem[] { new SelectItem(col1),
				new SelectItem(col2) };
		vr.initialize(columns);
		vr.process(new Row(items, new Object[] { "foo", "bar" }), 1);
		vr.process(new Row(items, new Object[] { "some string", "" }), 1);
		vr.process(new Row(items, new Object[] { "yay", null }), 1);
		vr.process(new Row(items, new Object[] { "", "another string" }), 1);
		vr.process(new Row(items, new Object[] { null, "yet another string" }),
				1);

		IValidationRuleResult result = vr.getResult();
		assertFalse(result.isValidated());

		assertSame(columns, result.getEvaluatedColumns());
	}
}