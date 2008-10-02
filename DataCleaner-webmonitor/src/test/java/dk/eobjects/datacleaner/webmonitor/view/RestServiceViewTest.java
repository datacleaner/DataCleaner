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

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletResponse;

import dk.eobjects.datacleaner.profiler.BasicProfileDescriptor;
import dk.eobjects.datacleaner.profiler.trivial.ValueDistributionProfile;

public class RestServiceViewTest extends TestCase {

	public void testRender() throws Exception {
		RestfulServiceView view = new RestfulServiceView();
		BasicProfileDescriptor profileDescriptor = new BasicProfileDescriptor(
				"Repeated values", ValueDistributionProfile.class);
		RestfulNode node = RestfulNode
				.newProfileDescriptorNode(profileDescriptor);
		view.setRootNode(node);

		MockHttpServletResponse response = new MockHttpServletResponse();
		view.render(null, null, response);

		assertEquals("application/xml", response.getContentType());
		String contentAsString = response.getContentAsString();
		assertEquals(
				"<?xml version=_1.0_ encoding=_UTF-8_?>|<profileDescriptor displayName=_Repeated values_ profileClass=_dk.eobjects.datacleaner.profiler.trivial.ValueDistributionProfile_ xmlns=_http://www.eobjects.dk/datacleaner/2.0_ xmlns:xsi=_http://www.w3.org/2001/XMLSchema-instance_ xsi:schemaLocation=_http://www.eobjects.dk/datacleaner/2.0 datacleaner.xsd _>| <propertyName>Top n most frequent values</propertyName>| <propertyName>Bottom n least frequent values</propertyName>|</profileDescriptor>",
				contentAsString.trim().replace('\"', '_').replace('\n', '|'));
	}
}