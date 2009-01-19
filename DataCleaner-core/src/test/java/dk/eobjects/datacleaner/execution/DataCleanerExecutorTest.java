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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;

import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

public class DataCleanerExecutorTest extends DataCleanerTestCase {

	DataCleanerExecutor<MockConfiguration, MockResult, MockProcessor> _executor;
	private DataContextSelection _dataContextSelection;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Class.forName("org.hsqldb.jdbcDriver");
		_executor = new DataCleanerExecutor<MockConfiguration, MockResult, MockProcessor>(
				new MockCallback());

		// Create a DataContext that is duplicatable
		_dataContextSelection = new DataContextSelection();
		_dataContextSelection.selectDatabase(
				DataCleanerTestCase.CONNECTION_STRING, null,
				DataCleanerTestCase.USERNAME, DataCleanerTestCase.PASSWORD,
				null);
	}

	public void testMultithreading() throws Exception {
		ExecutionConfiguration executionConfiguration = new ExecutionConfiguration(
				200l, false, false, 4, 4);
		_executor.setExecutionConfiguration(executionConfiguration);

		BenchmarkProgressObserver benchmarkProgressObserver = new BenchmarkProgressObserver();
		_executor.addProgressObserver(benchmarkProgressObserver);

		assertTrue(_dataContextSelection.isDuplicatable());

		Schema schema = _dataContextSelection.getDataContext()
				.getDefaultSchema();
		Table orderDetailsTable = schema.getTableByName("ORDERDETAILS");
		Table orderFactTable = schema.getTableByName("ORDERFACT");

		Column[] detailsColumns = orderDetailsTable.getColumns();
		Column[] factColumns = orderFactTable.getColumns();
		MockConfiguration conf1 = new MockConfiguration("details",
				new Column[] { detailsColumns[0], detailsColumns[1] });

		MockConfiguration conf2 = new MockConfiguration("fact", new Column[] {
				factColumns[0], factColumns[1] });

		MockConfiguration conf3 = new MockConfiguration("both", new Column[] {
				detailsColumns[2], factColumns[2] });

		_executor.addJobConfiguration(conf1);
		_executor.addJobConfiguration(conf2);
		_executor.addJobConfiguration(conf3);

		_executor.execute(_dataContextSelection);

		List<MockResult> resultsForFact = _executor
				.getResultsForTable(orderFactTable);
		assertEquals(2, resultsForFact.size());
		String[] results = new String[] { resultsForFact.get(0).getOutput(),
				resultsForFact.get(1).getOutput() };
		Arrays.sort(results);
		assertEquals("{both-result-2996,fact-result-2996}", ArrayUtils
				.toString(results));

		List<MockResult> resultsForDetails = _executor
				.getResultsForTable(orderDetailsTable);
		assertEquals(2, resultsForDetails.size());
		results = new String[] { resultsForDetails.get(0).getOutput(),
				resultsForDetails.get(1).getOutput() };
		Arrays.sort(results);
		assertEquals("{both-result-2996,details-result-2996}", ArrayUtils
				.toString(results));

		benchmarkProgressObserver.validateSimultaniousExecution();
		benchmarkProgressObserver.validateRowCount();

		Map<Table, List<Long>> progressTimes = benchmarkProgressObserver
				.getProgressTimes();
		List<Long> progressTimesForDetails = progressTimes
				.get(orderDetailsTable);
		assertEquals(5, progressTimesForDetails.size());

		List<Long> progressTimesForFact = progressTimes.get(orderFactTable);
		assertEquals(5, progressTimesForFact.size());
	}

	class MockCallback implements
			IExecutorCallback<MockConfiguration, MockResult, MockProcessor> {

		private Map<MockProcessor, Long> _counter = new HashMap<MockProcessor, Long>();

		public MockResult getResult(MockProcessor processor) {
			return new MockResult(processor.getInput() + "-result-"
					+ _counter.get(processor));
		}

		public List<MockProcessor> initProcessors(
				Map<MockConfiguration, Column[]> jobConfigurations,
				ExecutionConfiguration executionConfiguration) {
			List<MockProcessor> processors = new ArrayList<MockProcessor>();
			for (Entry<MockConfiguration, Column[]> entry : jobConfigurations
					.entrySet()) {
				MockConfiguration configuration = entry.getKey();
				processors.add(new MockProcessor(configuration.getInput()));
			}
			return processors;
		}

		public synchronized void processRow(Row row, long count,
				MockProcessor processor) {
			Long totalCount = _counter.get(processor);
			if (totalCount == null) {
				totalCount = 0l;
			}
			totalCount += count;
			_counter.put(processor, totalCount);
		}
	}

	class MockResult {
		private String _output;

		public MockResult(String output) {
			_output = output;
		}

		public String getOutput() {
			return _output;
		}
	}

	class MockProcessor {
		private String _input;

		public MockProcessor(String input) {
			_input = input;
		}

		public String getInput() {
			return _input;
		}
	}

	class MockConfiguration implements IJobConfiguration {

		private String _input;
		private Column[] _columns;

		public MockConfiguration(String input, Column[] columns) {
			_input = input;
			_columns = columns;
		}

		private static final long serialVersionUID = 1201317836520299637L;

		public Column[] getColumns() {
			return _columns;
		}

		public String getInput() {
			return _input;
		}
	}
}
