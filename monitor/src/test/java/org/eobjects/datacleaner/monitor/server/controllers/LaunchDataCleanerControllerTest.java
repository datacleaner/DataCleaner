/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.monitor.server.controllers;

import java.io.File;

import junit.framework.TestCase;

import org.eobjects.metamodel.util.FileHelper;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

public class LaunchDataCleanerControllerTest extends TestCase {

    public void testCreateJnlpOutput() throws Exception {
        LaunchDataCleanerController controller = new LaunchDataCleanerController();

        FileSystemResourceLoader resourceLoader = new FileSystemResourceLoader();
        MockServletContext servletContext = new MockServletContext(resourceLoader);
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        MockHttpServletResponse response = new MockHttpServletResponse();
        String tenant = "DC";
        String jobName = "my_job";

        controller.launchDataCleaner(request, response, tenant, jobName);

        String expected = FileHelper.readFileAsString(new File("src/test/resources/expected_launch_file.xml"), "UTF-8");
        expected = expected.replaceAll("\r\n", "\n");

        assertEquals(expected, response.getContentAsString());
    }
}
