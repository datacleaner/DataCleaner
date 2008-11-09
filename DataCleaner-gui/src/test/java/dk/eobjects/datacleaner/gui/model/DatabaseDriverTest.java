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
package dk.eobjects.datacleaner.gui.model;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;

import junit.framework.TestCase;

public class DatabaseDriverTest extends TestCase {

	public void testLoadDriver() throws Exception {
		File jarFile = new File("src/test/resources/testjar.jar");
		assertTrue(jarFile.exists());
		DatabaseDriver driver = new DatabaseDriver(jarFile,
				"dk.eobjects.test.DummyDriver");
		assertFalse(driver.isLoaded());

		driver.loadDriver();

		assertTrue(driver.isLoaded());

		try {
			DriverManager.getConnection("jdbc:eobjects-dummy:foobar");
			fail("Exception should have been thrown");
		} catch (SQLException e) {
			assertEquals("This is just a dummy driver, but you got through!", e
					.getMessage());
		}

		driver.unloadDriver();

		try {
			DriverManager.getConnection("jdbc:eobjects-dummy:foobar");
			fail("Exception should have been thrown");
		} catch (SQLException e) {
			assertTrue(e.getMessage().startsWith("No suitable driver"));
		}
	}
}