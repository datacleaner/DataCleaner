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

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import dk.eobjects.datacleaner.profiler.BasicProfileDescriptor;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.profiler.OutOfMemoryProfile;
import dk.eobjects.datacleaner.profiler.ProfilerJobConfiguration;
import dk.eobjects.datacleaner.profiler.ProfileManagerTest;
import dk.eobjects.datacleaner.profiler.ProfilerManager;
import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.IDataContextStrategy;
import dk.eobjects.metamodel.JdbcDataContextFactory;
import dk.eobjects.metamodel.MetaModelHelper;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.IDataSetStrategy;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;
import dk.eobjects.metamodel.schema.TableType;

public class ProfileRunnerTest extends DataCleanerTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ProfileManagerTest.initProfileManager();
	}

	public void testSplitQueries() throws Exception {
		final String result = "Matrix[columnNames={ORDERNUMBER,PRODUCTCODE},Row count={2996,2996},Null values={0,0},Empty values={0,0},Highest value={10425,S72_3212},Lowest value={10100,S10_1678}]";

		Connection connection = getTestDbConnection();
		DataContext dataContext = JdbcDataContextFactory
				.getDataContext(connection);
		Schema schema = dataContext.getDefaultSchema();

		Table orderFactTable = schema.getTableByName("ORDERFACT");
		assertEquals(2996, MetaModelHelper.executeSingleRowQuery(dataContext,
				new Query().selectCount().from(orderFactTable)).getValue(0));

		ProfilerJobConfiguration conf = new ProfilerJobConfiguration(
				ProfileManagerTest.DESCRIPTOR_STANDARD_MEASURES);
		conf.setColumns(orderFactTable.getColumns()[0], orderFactTable
				.getColumns()[1]);

		ProfileRunner profileRunner = new ProfileRunner();
		profileRunner.addJobConfiguration(conf);
		profileRunner.execute(dataContext);

		assertEquals(result, profileRunner.getResults().get(0).getMatrices()[0]
				.toString());

		profileRunner = new ProfileRunner();
		profileRunner.addJobConfiguration(conf);
		profileRunner.setExecutionConfiguration(new ExecutionConfiguration(
				800l, false, false));
		profileRunner.execute(dataContext);

		assertEquals(result, profileRunner.getResults().get(0).getMatrices()[0]
				.toString());
	}

	public void testMultipleProfileDefinitions() throws Exception {
		Connection connection = getTestDbConnection();
		DataContext dataContext = JdbcDataContextFactory
				.getDataContext(connection);
		Schema schema = dataContext.getDefaultSchema();

		ProfileRunner profileRunner = new ProfileRunner();

		// Create profile definition for a single column
		Table customersTable = schema.getTableByName("CUSTOMERS");
		Column addressLine2Column = customersTable
				.getColumnByName("ADDRESSLINE2");
		ProfilerJobConfiguration conf1 = new ProfilerJobConfiguration(
				ProfileManagerTest.DESCRIPTOR_PATTERN_FINDER);
		conf1.setColumns(addressLine2Column);
		profileRunner.addJobConfiguration(conf1);

		// Create profile definition for multiple columns
		Table officesTable = schema.getTableByName("OFFICES");
		Column postalCodeColumn = officesTable.getColumnByName("POSTALCODE");
		Column officeCodeColumn = officesTable.getColumnByName("OFFICECODE");
		ProfilerJobConfiguration conf2 = new ProfilerJobConfiguration(
				ProfileManagerTest.DESCRIPTOR_STANDARD_MEASURES);
		conf2.setColumns(postalCodeColumn, officeCodeColumn);
		profileRunner.addJobConfiguration(conf2);

		IProgressObserver po = new IProgressObserver() {
			long _time = 0;
			boolean _begun = false;

			public void init(Table[] tablesToProcess) {
				assertEquals(2, tablesToProcess.length);
			}

			public void notifyBeginning(Table tableToProcess, long numRows) {
				assertFalse(_begun);
				_begun = true;
				_time = System.currentTimeMillis();
			}

			public void notifyFailure(Table processedTable,
					Throwable throwable, long lastRow) {
				fail("Execution should not have failed");
			}

			public void notifyProgress(long numRowsProcessed) {
			}

			public void notifySuccess(Table processedTable,
					long numRowsProcessed) {
				assertTrue(_begun);
				_begun = false;
				assertTrue("CurrentTime was less than when execution began.",
						_time <= System.currentTimeMillis());
			}
		};
		profileRunner.addProgressObserver(po);
		profileRunner.execute(dataContext);
		Table[] profileTables = profileRunner.getResultTables();
		assertEquals(2, profileTables.length);

		List<IProfileResult> profileResultsForTable = profileRunner
				.getResultsForTable(customersTable);
		assertEquals(1, profileResultsForTable.size());
		profileResultsForTable = profileRunner.getResultsForTable(officesTable);
		assertEquals(1, profileResultsForTable.size());

		List<IProfileResult> results = profileRunner.getResults();

		String[] expectations = {
				"ProfileResult[profileDescriptor=BasicProfileDescriptor[displayName=Pattern finder,profileClass=class dk.eobjects.datacleaner.profiler.pattern.PatternFinderProfile],matrices={Matrix[columnNames={ADDRESSLINE2},aaaaa 999={MatrixValue[value=11,detailQuery=SELECT \"CUSTOMERS\".\"ADDRESSLINE2\", COUNT(*) FROM PUBLIC.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"ADDRESSLINE2\"]},??? aaaaa={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"ADDRESSLINE2\", COUNT(*) FROM PUBLIC.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"ADDRESSLINE2\"]},aaaaa aa. 9={MatrixValue[value=1,detailQuery=SELECT \"CUSTOMERS\".\"ADDRESSLINE2\", COUNT(*) FROM PUBLIC.\"CUSTOMERS\" GROUP BY \"CUSTOMERS\".\"ADDRESSLINE2\"]}]}]",
				"ProfileResult[profileDescriptor=BasicProfileDescriptor[displayName=Standard measures,profileClass=class dk.eobjects.datacleaner.profiler.trivial.StandardMeasuresProfile],matrices={Matrix[columnNames={POSTALCODE,OFFICECODE},Row count={7,7},Null values={0,0},Empty values={0,0},Highest value={NSW 2010,7},Lowest value={02107,1}]}]" };

		assertEquals(expectations.length, results.size());
		assertEquals(results, expectations);
	}

	private void assertEquals(List<IProfileResult> results,
			String[] expectations) {
		for (int j = 0; j < results.size(); j++) {
			String string = results.get(j).toString();
			boolean result = ArrayUtils.indexOf(expectations, string) != -1;
			if (!result) {
				System.err
						.println("Could not find the following string in expectations array:\n"
								+ string.replaceAll("\\\"", "\\\\\""));
				System.err.println("Expectations array is:"
						+ ArrayUtils.toString(expectations));
				assertEquals("?", string);
			}
		}
	}

	public void testProfileDefinitionsWithSameTable() throws Exception {
		DataContext dc = getTestDataContext();
		Schema schema = dc.getDefaultSchema();

		ProfileRunner profileRunner = new ProfileRunner();
		profileRunner.setExecutionConfiguration(new ExecutionConfiguration(
				false, true));

		// Create profile definition for a single column
		final Table customersTable = schema.getTableByName("CUSTOMERS");
		Column customerNameColumn = customersTable
				.getColumnByName("CUSTOMERNAME");
		ProfilerJobConfiguration conf1 = new ProfilerJobConfiguration(
				ProfileManagerTest.DESCRIPTOR_STANDARD_MEASURES);
		conf1.setColumns(customerNameColumn);
		profileRunner.addJobConfiguration(conf1);

		// Create profile definition for multiple columns
		Column countryColumn = customersTable.getColumnByName("COUNTRY");
		ProfilerJobConfiguration conf2 = new ProfilerJobConfiguration(
				ProfileManagerTest.DESCRIPTOR_PATTERN_FINDER);
		conf2.setColumns(countryColumn, customerNameColumn);
		profileRunner.addJobConfiguration(conf2);

		IProgressObserver po = new IProgressObserver() {
			private int _notifications = 0;

			public void init(Table[] tablesToProcess) {
				assertEquals(1, tablesToProcess.length);
				assertSame(customersTable, tablesToProcess[0]);
			}

			public void notifyBeginning(Table tableToProcess, long numRows) {
				_notifications++;
				assertEquals(1, _notifications);
			}

			public void notifyFailure(Table processedTable,
					Throwable throwable, long lastRow) {
				fail("Execution should not have failed");
			}

			public void notifyProgress(long numRowsProcessed) {
			}

			public void notifySuccess(Table processedTable,
					long numRowsProcessed) {
				_notifications++;
				assertEquals(2, _notifications);
			}
		};

		profileRunner.addProgressObserver(po);
		profileRunner.execute(dc);
		List<IProfileResult> results = profileRunner.getResults();

		String[] expectations = {
				"ProfileResult[profileDescriptor=BasicProfileDescriptor[displayName=Pattern finder,profileClass=class dk.eobjects.datacleaner.profiler.pattern.PatternFinderProfile],matrices={Matrix[columnNames={COUNTRY},aaaaaaaaaaa={116},aaaaa aaaaaaa={6}],Matrix[columnNames={CUSTOMERNAME},aaaaaaaaaa aaaaaaaaaaaa={22},aaaaaaaaaaaa aaaaaaaaaaa aaaaaaaaaaaa={15},aaaaaaaaa aaaaaaaaaaaa aaaa.={13},aaaaaaaaaa aaaaaaaaaaaaa, aaa={9},aaaaaaaaaaaa aaaaaaa aaaaaaaaaaaa aaa.={9},aaaaaaaaaa aaaaaaaaaaaa, aaa.={8},aaaaaaaa aaaaaaaaaaaa aaaaaaaaaaaa, aaaa.={8},aaaaaaaaaa aaaaaaaa aaaaaaaaaaa, aaa={8},aaaaaaaa aaaaa aaaaaaaa aaaaaaaaa={3},aaaaa aaaaaaaaa & aaa.={3},?????????????.aaa={2},aaaaaaaaaaaa.aaa={2},aaaaaaaaaa aaa.={2},aaaaaa & aaaa aa.={2},aaaaaaaaaaa.aa.aa={1},aaaa-aaaa aaaaaaaa aaa.={1},aaaa+ aaaaaaaa aaaaaaa={1},aaaa'a aaaaaaaaaaa, aaa={1},aaaaa'a aaaaaaaa aa.={1},aaaaa'a aaaa aaaa={1},a'aaaaaa aaaaaaaaaa={1},aa&a aaaaaaaaaaaa={1},aaaa aaaaa+ aaaaa={1},aaaaaa aaaaa& aa={1},aa aaaaa a'aaaaaaaaa, aa.={1},aaaaaa aaaaa aa aaaa, aa.={1},aaaaaa aaaaaa aaaa aaaaaa, aaa={1},aaa 'a' aa aaaaaaaaa, aaa.={1},aaaaaaa & aaaaaaa, aa.={1},aaaaa & aaaaaaa aa={1}]}]",
				"ProfileResult[profileDescriptor=BasicProfileDescriptor[displayName=Standard measures,profileClass=class dk.eobjects.datacleaner.profiler.trivial.StandardMeasuresProfile],matrices={Matrix[columnNames={CUSTOMERNAME},Row count={122},Null values={0},Empty values={0},Highest value={giftsbymail.co.uk},Lowest value={ANG Resellers}]}]" };

		assertEquals(2, results.size());
		assertEquals(results, expectations);
	}

	public void testColumnProfiles() throws Exception {
		DataContext dc = getTestDataContext();
		Schema schema = dc.getDefaultSchema();

		ProfileRunner profileRunner = new ProfileRunner();
		profileRunner.setExecutionConfiguration(new ExecutionConfiguration(
				false, true));

		// Create profile definition for a single column
		final Table customersTable = schema.getTableByName("CUSTOMERS");
		final Table employeesTable = schema.getTableByName("EMPLOYEES");
		ProfilerJobConfiguration conf1 = new ProfilerJobConfiguration(
				ProfileManagerTest.DESCRIPTOR_PATTERN_FINDER);
		conf1.setColumns(employeesTable.getColumns()[0], customersTable
				.getColumns()[0]);
		profileRunner.addJobConfiguration(conf1);

		// Create profile definition for multiple columns
		Column postalCodeColumn = customersTable
				.getColumnByName("ADDRESSLINE1");
		Column officeCodeColumn = customersTable
				.getColumnByName("ADDRESSLINE2");
		ProfilerJobConfiguration conf2 = new ProfilerJobConfiguration(
				ProfileManagerTest.DESCRIPTOR_STANDARD_MEASURES);
		conf2.setColumns(postalCodeColumn, officeCodeColumn);
		profileRunner.addJobConfiguration(conf2);

		profileRunner.execute(dc);

		List<IProfileResult> results = profileRunner.getResults();

		String[] expectations = {
				"ProfileResult[profileDescriptor=BasicProfileDescriptor[displayName=Standard measures,profileClass=class dk.eobjects.datacleaner.profiler.trivial.StandardMeasuresProfile],matrices={Matrix[columnNames={ADDRESSLINE1,ADDRESSLINE2},Row count={122,122},Null values={0,109},Empty values={0,0},Highest value={ul. Filtrowa 68,Suite 750},Lowest value={1 rue Alsace-Lorraine,2nd Floor}]}]",
				"ProfileResult[profileDescriptor=BasicProfileDescriptor[displayName=Pattern finder,profileClass=class dk.eobjects.datacleaner.profiler.pattern.PatternFinderProfile],matrices={Matrix[columnNames={CUSTOMERNUMBER},999={122}]}]",
				"ProfileResult[profileDescriptor=BasicProfileDescriptor[displayName=Pattern finder,profileClass=class dk.eobjects.datacleaner.profiler.pattern.PatternFinderProfile],matrices={Matrix[columnNames={EMPLOYEENUMBER},9999={23}]}]" };

		assertEquals(expectations.length, results.size());
		assertEquals(results, expectations);
	}

	public void testOutOfMemoryError() throws Exception {
		Table table = new Table("table1", TableType.TABLE);
		final Column column = new Column("col1", ColumnType.VARCHAR, table, 0,
				true);
		table.addColumn(column);

		ProfileRunner profileRunner = new ProfileRunner();
		BasicProfileDescriptor descriptor = new BasicProfileDescriptor(
				"Memory aggregator", OutOfMemoryProfile.class);
		ProfilerManager.addProfileDescriptor(descriptor);
		ProfilerJobConfiguration conf1 = new ProfilerJobConfiguration(
				descriptor);
		conf1.setColumns(column);
		profileRunner.addJobConfiguration(conf1);

		IDataContextStrategy dcStrategy = createMock(IDataContextStrategy.class);
		IDataSetStrategy dsStrategy = createMock(IDataSetStrategy.class);

		EasyMock.expect(dcStrategy.executeQuery((Query) EasyMock.notNull()))
				.andReturn(new DataSet(dsStrategy)).times(2);

		EasyMock.expect(dsStrategy.next()).andReturn(true);
		EasyMock.expect(dsStrategy.next()).andReturn(false);
		dsStrategy.close();

		EasyMock.expect(dsStrategy.getRow()).andStubAnswer(new IAnswer<Row>() {
			public Row answer() throws Throwable {
				return new Row(
						new SelectItem[] { SelectItem.getCountAllItem() },
						new Object[] { Long.MAX_VALUE });
			}
		});

		EasyMock.expect(dsStrategy.next()).andReturn(true).anyTimes();
		IAnswer<Row> stubRowAnswer = new IAnswer<Row>() {
			public Row answer() throws Throwable {
				Row row = new Row(new SelectItem[] { new SelectItem(column),
						SelectItem.getCountAllItem() }, new Object[] {
						new Date().toString(), 1l });
				return row;
			}
		};
		EasyMock.expect(dsStrategy.getRow()).andStubAnswer(stubRowAnswer);

		replayMocks();

		try {
			profileRunner.execute(new DataContext(dcStrategy));
			fail("Exception should have been thrown");
		} catch (OutOfMemoryError e) {
			assertEquals("Java heap space", e.getMessage());
		}

		verifyMocks();
	}
}