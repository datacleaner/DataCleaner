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
package dk.eobjects.datacleaner.gui.dialogs;

import java.io.File;

import dk.eobjects.datacleaner.testware.DataCleanerTestCase;

public class SettingsDialogTest extends DataCleanerTestCase {

	public void testBeautifyPath() throws Exception {
		assertEquals("" + File.separatorChar + "foobar1" + File.separatorChar
				+ "foobar2" + File.separatorChar + "foobar3"
				+ File.separatorChar + "foobar4" + File.separatorChar
				+ "fooba..." + File.separatorChar + "foobar10", SettingsDialog
				.beautifyPath(File.separatorChar + "foobar1"
						+ File.separatorChar + "foobar2" + File.separatorChar
						+ "foobar3" + File.separatorChar + "foobar4"
						+ File.separatorChar + "foobar5" + File.separatorChar
						+ "foobar6" + File.separatorChar + "foobar7"
						+ File.separatorChar + "foobar8" + File.separatorChar
						+ "foobar9" + File.separatorChar + "foobar10"));
	}
}
