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
package dk.eobjects.datacleaner.gui;

import java.io.File;
import java.io.FileFilter;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.datacleaner.util.DomHelper;

public class VersionTest extends DataCleanerTestCase {

	public void testVersionConsistency() throws Exception {
		DocumentBuilder documentBuilder = DomHelper.getDocumentBuilder();

		File parentPomFile = new File("../pom.xml");
		Document document = documentBuilder.parse(parentPomFile);
		Node projectNode = DomHelper.getChildNodesByName(document, "project")
				.get(0);
		String pomVersion = DomHelper.getChildNodeText(projectNode, "version");

		// Assert that the UI's version number is the same as the build tool's
		// version number
		assertEquals(DataCleanerGui.VERSION, pomVersion);

		File parentDirectory = parentPomFile.getParentFile();
		File[] moduleDirs = parentDirectory.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});

		int verifiedPomFiles = 0;
		for (File moduleDir : moduleDirs) {
			File modulePomFile = new File(moduleDir.getAbsolutePath()
					+ File.separatorChar + "pom.xml");
			if (modulePomFile.exists()) {
				document = documentBuilder.parse(modulePomFile);
				projectNode = DomHelper
						.getChildNodesByName(document, "project").get(0);
				pomVersion = DomHelper.getChildNodeText(projectNode, "version");

				// Assert that all module versions are references to the
				// parent's version
				assertEquals("${parent.version}", pomVersion);

				Node parentNode = DomHelper.getChildNodesByName(projectNode,
						"parent").get(0);
				pomVersion = DomHelper.getChildNodeText(parentNode, "version");

				// Assert that all <parent> entries contain the same version as
				// the parent POM.
				assertEquals(DataCleanerGui.VERSION, pomVersion);

				verifiedPomFiles++;
			}
		}

		assertEquals(4, verifiedPomFiles);
	}
}
