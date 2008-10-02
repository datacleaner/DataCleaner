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
package dk.eobjects.datacleaner.webmonitor.controller;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.View;

import junit.framework.TestCase;

public class SchemaControllerTest extends TestCase {

	public void testGetSchema() throws Exception {
		View view = new SchemaController().getSchema();
		MockHttpServletResponse response = new MockHttpServletResponse();
		view.render(null, null, response);
		String content = response.getContentAsString();
		assertTrue(content
				.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") != -1);
		assertTrue(content
				.indexOf("<schema xmlns=\"http://www.w3.org/2001/XMLSchema\"") != -1);
		assertTrue(content
				.indexOf("targetNamespace=\"http://www.eobjects.dk/datacleaner/2.0\"") != -1);
	}
}