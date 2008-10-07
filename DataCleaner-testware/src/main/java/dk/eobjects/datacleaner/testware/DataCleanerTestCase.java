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
package dk.eobjects.datacleaner.testware;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.easymock.EasyMock;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.JdbcDataContextFactory;
import dk.eobjects.metamodel.util.FileHelper;

/**
 * Testcase implementation with some additional functionality suited for testing
 * in DataCleaner
 */
public abstract class DataCleanerTestCase extends TestCase {

	private static final String CONNECTION_STRING = "jdbc:hsqldb:res:database/datacleaner";
	private static final String USERNAME = "SA";
	private static final String PASSWORD = "";
	private List<Object> _mocks = new ArrayList<Object>();
	private Connection _connection;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_mocks.clear();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (_connection != null) {
			if (!_connection.isClosed()) {
				_connection.close();
			}
			_connection = null;
		}
	}

	public <T extends Object> T createMock(Class<T> clazz) {
		T mock = EasyMock.createMock(clazz);
		_mocks.add(mock);
		return mock;
	}

	public void verifyMocks() {
		EasyMock.verify(_mocks.toArray());
	}

	public void replayMocks() {
		EasyMock.replay(_mocks.toArray());
	}

	public Connection getTestDbConnection() throws Exception {
		if (_connection == null || _connection.isClosed()) {
			Class.forName("org.hsqldb.jdbcDriver");
			_connection = DriverManager.getConnection(CONNECTION_STRING,
					USERNAME, PASSWORD);
			_connection.setReadOnly(true);
		}
		return _connection;
	}

	public DataContext getTestDataContext() throws Exception {
		return JdbcDataContextFactory.getDataContext(getTestDbConnection());
	}

	public void assertEqualsIgnoreCase(String expected, String actual) {
		assertEqualsIgnoreCase(null, expected, actual);
	}

	public void assertEqualsIgnoreCase(String message, String expected,
			String actual) {
		if (expected != null) {
			boolean result = expected.equalsIgnoreCase(actual);
			if (!result) {
				assertEquals(message, expected, actual);
			}
		} else {
			if (actual != null) {
				assertEquals(message, expected, actual);
			}
		}
	}

	public void assertEqualsArray(Object[] arr1, Object[] arr2,
			boolean respectSequence) {
		if (arr1 == null || arr2 == null) {
			assertNotNull(arr1);
			assertNotNull(arr2);
		} else {
			assertEquals(arr1.length, arr2.length);
			if (respectSequence) {
				assertEquals(ArrayUtils.toString(arr1), ArrayUtils
						.toString(arr2));
			} else {
				for (int i = 0; i < arr1.length; i++) {
					boolean found = false;
					Object obj1 = arr1[i];
					for (int j = 0; j < arr2.length && !found; j++) {
						Object obj2 = arr1[j];
						if (obj1.equals(obj2)) {
							found = true;
						}
					}
					if (!found) {
						fail("Couldn't find " + obj1 + " in second array");
					}
				}
			}
		}
	}

	protected File getTestResourceAsFile(String filename) {
		return new File("src/test/resources/" + filename);
	}

	public void assertEqualsFile(File benchmarkFile, File outputFile) {
		assertTrue("Benchmark file does not exist", benchmarkFile.exists());
		assertTrue("Output file does not exist", outputFile.exists());
		assertEquals(FileHelper.readFileAsString(benchmarkFile), FileHelper
				.readFileAsString(outputFile));
	}
}
