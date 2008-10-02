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
package dk.eobjects.datacleaner.webmonitor;

import java.io.File;

import org.springframework.context.support.FileSystemXmlApplicationContext;

import junit.framework.TestCase;

public class TestWebApplication extends TestCase {

	public void testFileExists() throws Exception {
		File file = new File("src/main/webapp/WEB-INF/web.xml");
		assertTrue(file.exists());
	}

	public void testDataCleanerConfig() throws Exception {
		FileSystemXmlApplicationContext appCtx = new FileSystemXmlApplicationContext(
				"src/main/webapp/WEB-INF/datacleaner-config.xml");
		assertNotNull(appCtx);
	}
}