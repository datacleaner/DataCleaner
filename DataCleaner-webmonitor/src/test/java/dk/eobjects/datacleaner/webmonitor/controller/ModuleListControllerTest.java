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

import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.View;

import dk.eobjects.datacleaner.webmonitor.WebmonitorBootstrap;
import dk.eobjects.datacleaner.webmonitor.WebmonitorTestCase;

public class ModuleListControllerTest extends WebmonitorTestCase {

	public void testSiteModules() throws Exception {
		FileSystemXmlApplicationContext appCtx = new FileSystemXmlApplicationContext(
				"src/main/webapp/WEB-INF/datacleaner-config.xml");
		WebmonitorBootstrap bootstrap = new WebmonitorBootstrap();
		bootstrap.setApplicationContext(appCtx);
		bootstrap.initDataCleanerManagers();

		ModuleListController moduleListController = new ModuleListController();
		ModelMap model = new ModelMap();
		String result = moduleListController.siteModules(model);
		assertEquals("modules", result);
		assertNotNull(model.get("profileDescriptors"));
		assertNotNull(model.get("validationRuleDescriptors"));
	}

	public void testRestModules() throws Exception {
		initApplicationContext();

		ModuleListController moduleListController = new ModuleListController();
		View view = moduleListController.restModules();

		MockHttpServletResponse response = new MockHttpServletResponse();
		view.render(null, null, response);

		assertEquals("application/xml", response.getContentType());

		String content = response.getContentAsString();
		validateXml(content);
	}
}