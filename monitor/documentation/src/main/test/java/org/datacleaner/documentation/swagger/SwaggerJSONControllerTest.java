/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.documentation.swagger;

import javax.servlet.http.HttpServletRequest;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

public class SwaggerJSONControllerTest {
    private final SwaggerJSONController controller = new SwaggerJSONController();
    private static final String url = "http://localhost/repository/swagger.json";

    @Test
    public void testGenerateSwaggerJSON() throws Exception {
        final HttpServletRequest httpServletRequest = getHttpServletRequestMock();
        final SwaggerConfiguration configuration = controller.generateSwaggerJSON(httpServletRequest);
        Assert.assertNotNull(configuration);
        Assert.assertTrue(url.contains(configuration.getHost()));
    }

    private HttpServletRequest getHttpServletRequestMock() {
        final HttpServletRequest httpServletRequest = EasyMock.createNiceMock(HttpServletRequest.class);
        final StringBuffer requestUrl = new StringBuffer();
        requestUrl.append(url);
        EasyMock.expect(httpServletRequest.getRequestURL()).andReturn(requestUrl).anyTimes();
        EasyMock.replay(httpServletRequest);

        return httpServletRequest;
    }
}