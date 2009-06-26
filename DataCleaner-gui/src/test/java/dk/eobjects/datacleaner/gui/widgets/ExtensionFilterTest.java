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
package dk.eobjects.datacleaner.gui.widgets;

import java.io.File;

import dk.eobjects.datacleaner.gui.model.ExtensionFilter;

import junit.framework.TestCase;

public class ExtensionFilterTest extends TestCase {

	public void testExtension() throws Exception {
		ExtensionFilter extensionFilter = new ExtensionFilter("My filter",
				"csv");
		File file = new File("src/test/resources/customers.csv");
		assertTrue(extensionFilter.accept(file));
		
		file = new File("src/test/resources/EmptyFile.CSV");
		assertTrue(extensionFilter.accept(file));

		file = new File("src/test/resources/images/dialog_banner.png");
		assertFalse(extensionFilter.accept(file));
	}
}