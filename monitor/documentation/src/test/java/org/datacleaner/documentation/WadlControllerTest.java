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
package org.datacleaner.documentation;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class WadlControllerTest {
    private WadlController wadlController = new WadlController();

    @Before
    public void setUp() {
        wadlController.handlerMapping = getRequestMappingHandlerMappingMock();
    }

    private RequestMappingHandlerMapping getRequestMappingHandlerMappingMock() {
        RequestMappingHandlerMapping requestMappingHandlerMapping = createNiceMock(RequestMappingHandlerMapping.class);
        expect(requestMappingHandlerMapping.getHandlerMethods()).andReturn(getHandlerMethods()).anyTimes();
        replay(requestMappingHandlerMapping);

        return requestMappingHandlerMapping;
    }

    private Map<RequestMappingInfo, HandlerMethod> getHandlerMethods() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = new HashMap<>();
        handlerMethods.put(getRequestMappingInfo(), getHandlerMethodMock());

        return handlerMethods;
    }

    private RequestMappingInfo getRequestMappingInfo() {
        RequestMappingInfo requestMappingInfo = new RequestMappingInfo("mappingInfo", null, null, null, null, null, null, null);

        return requestMappingInfo;
    }

    private HandlerMethod getHandlerMethodMock() {
        HandlerMethod handlerMethod = createNiceMock(HandlerMethod.class);
        replay(handlerMethod);

        return handlerMethod;
    }

    @Test
    public void testGenerateWadl() throws Exception {
        wadlController.generateWadl(getServletRequestMock());
    }

    private HttpServletRequest getServletRequestMock() {
        HttpServletRequest httpServletRequest = createNiceMock(HttpServletRequest.class);
        replay(httpServletRequest);

        return httpServletRequest;
    }
}