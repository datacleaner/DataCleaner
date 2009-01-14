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
package dk.eobjects.datacleaner.execution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.datacleaner.validator.ValidatorJobConfiguration;
import dk.eobjects.datacleaner.validator.ValidationRuleManagerTest;
import dk.eobjects.datacleaner.validator.condition.JavascriptValidationRule;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

public class ValidationRuleRunnerTest extends DataCleanerTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ValidationRuleManagerTest.initValidationRuleManager();
	}

	public void testSingleValidationRule() throws Exception {
		DataContext dc = getTestDataContext();
		Schema schema = dc.getDefaultSchema();
		Table officesTable = schema.getTableByName("OFFICES");
		Column postalCodeColumn = officesTable.getColumnByName("POSTALCODE");
		Column officeCodeColumn = officesTable.getColumnByName("OFFICECODE");
		ValidationRuleRunner runner = new ValidationRuleRunner();

		ValidatorJobConfiguration conf1 = new ValidatorJobConfiguration(
				ValidationRuleManagerTest.DESCRIPTOR_NOT_NULL);
		conf1.setColumns(postalCodeColumn, officeCodeColumn);

		runner.addJobConfiguration(conf1);

		runner.execute(dc);

		assertEquals(1, runner.getResultTables().length);
		List<IValidationRuleResult> results = runner
				.getResultsForTable(officesTable);
		assertEquals(1, results.size());
		assertEquals("SimpleValidationRuleResult[error=null,errorRows=[]]",
				results.get(0).toString());
	}

	public void testMultipleValidationRules() throws Exception {
		DataContext dc = getTestDataContext();
		Schema schema = dc.getDefaultSchema();
		Table table = schema.getTableByName("PRODUCTS");
		Column productLineColumn = table.getColumnByName("PRODUCTLINE");
		Column quantityColumn = table.getColumnByName("QUANTITYINSTOCK");
		ValidationRuleRunner runner = new ValidationRuleRunner();

		ValidatorJobConfiguration conf1 = new ValidatorJobConfiguration(
				ValidationRuleManagerTest.DESCRIPTOR_JAVASCRIPT);
		Map<String, String> javascriptProperties = new HashMap<String, String>();
		javascriptProperties.put(
				JavascriptValidationRule.PROPERTY_JAVASCRIPT_EXPRESSION,
				"values.get('PRODUCTLINE') != 'Planes'");
		conf1.setValidationRuleProperties(javascriptProperties);
		conf1.setColumns(productLineColumn);
		runner.addJobConfiguration(conf1);

		ValidatorJobConfiguration conf2 = new ValidatorJobConfiguration(
				ValidationRuleManagerTest.DESCRIPTOR_NOT_NULL);
		conf2.setColumns(quantityColumn);
		runner.addJobConfiguration(conf2);

		runner.execute(dc);

		List<IValidationRuleResult> results = runner.getResults();

		assertEquals(2, results.size());

		for (IValidationRuleResult result : results) {
			int numErrorRows = result.getUnvalidatedRows().size();
			System.out.println(numErrorRows);
			assertTrue(numErrorRows == 0 || numErrorRows == 12);
		}
	}
}