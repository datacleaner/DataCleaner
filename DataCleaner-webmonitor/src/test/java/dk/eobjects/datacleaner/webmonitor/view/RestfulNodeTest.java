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
package dk.eobjects.datacleaner.webmonitor.view;

import dk.eobjects.datacleaner.profiler.BasicProfileDescriptor;
import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.profiler.pattern.PatternFinderProfile;
import dk.eobjects.datacleaner.profiler.trivial.ValueDistributionProfile;
import junit.framework.TestCase;

public class RestfulNodeTest extends TestCase {

	public void testToString() throws Exception {
		RestfulNode node = RestfulNode.newModulesNode();
		assertEquals("<modules/>", node.toString());

		node.addAttribute("foo", "bar");
		assertEquals("<modules foo=\"bar\"/>", node.toString());

		RestfulNode foobarInnerNode = RestfulNode.newModulesNode();
		node.addInnerNode(foobarInnerNode);
		node.addInnerNode(RestfulNode.newModulesNode());
		assertEquals(
				"<modules foo=\"bar\">\n <modules/>\n <modules/>\n</modules>",
				node.toString());

		foobarInnerNode.addInnerNode(RestfulNode.newModulesNode());
		assertEquals(
				"<modules foo=\"bar\">\n <modules>\n  <modules/>\n </modules>\n <modules/>\n</modules>",
				node.toString());
	}

	public void testNewProfileDescriptorNode() throws Exception {
		IProfileDescriptor profileDescriptor = new BasicProfileDescriptor(
				"Pattern finder", PatternFinderProfile.class);
		RestfulNode node = RestfulNode
				.newProfileDescriptorNode(profileDescriptor);
		assertEquals(
				"<profileDescriptor displayName=\"Pattern finder\" profileClass=\"dk.eobjects.datacleaner.profiler.pattern.PatternFinderProfile\"/>",
				node.toString());

		profileDescriptor = new BasicProfileDescriptor("Value distribution",
				ValueDistributionProfile.class);
		node = RestfulNode.newProfileDescriptorNode(profileDescriptor);
		assertEquals(
				"<profileDescriptor displayName=_Value distribution_ profileClass=_dk.eobjects.datacleaner.profiler.trivial.ValueDistributionProfile_>| <propertyName>Top n most frequent values</propertyName>| <propertyName>Bottom n least frequent values</propertyName>|</profileDescriptor>",
				node.toString().replace('\"', '_').replace('\n', '|'));
	}
}