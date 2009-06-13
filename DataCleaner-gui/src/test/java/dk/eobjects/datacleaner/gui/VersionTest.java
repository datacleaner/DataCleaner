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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.datacleaner.util.DomHelper;

public class VersionTest extends DataCleanerTestCase {

	public void testVersionConsistency() throws Exception {
		File pomFile = new File("../pom.xml");
		Document document = DomHelper.getDocumentBuilder().parse(pomFile);
		Node projectNode = DomHelper.getChildNodesByName(document, "project").get(0);
		Node propertiesNode = DomHelper.getChildNodesByName(projectNode, "properties").get(0);
		String pomVersion = DomHelper.getChildNodeText(propertiesNode, "datacleaner.version");
		
		assertEquals(DataCleanerGui.VERSION, pomVersion);
	}
}
