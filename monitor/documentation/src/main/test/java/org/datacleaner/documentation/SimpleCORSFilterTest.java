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

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.junit.Test;

public class SimpleCORSFilterTest {
    @Test
    public void testDoFilterInternal() throws Exception {
        final SimpleCORSFilter simpleCORSFilter = new SimpleCORSFilter();
        final HttpServletRequest httpServletRequest = getHttpServletRequestMock();
        final HttpServletResponse httpServletResponse = getHttpServletResponseMock();
        final FilterChain filterChain = getFilterChainMock();
        simpleCORSFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
    }

    private HttpServletRequest getHttpServletRequestMock() {
        final HttpServletRequest httpServletRequestMock = EasyMock.createNiceMock(HttpServletRequest.class);
        EasyMock.replay(httpServletRequestMock);

        return httpServletRequestMock;
    }

    private HttpServletResponse getHttpServletResponseMock() {
        final HttpServletResponse httpServletResponseMock = EasyMock.createNiceMock(HttpServletResponse.class);
        EasyMock.replay(httpServletResponseMock);

        return httpServletResponseMock;
    }

    private FilterChain getFilterChainMock() {
        final FilterChain filterChain = EasyMock.createNiceMock(FilterChain.class);
        EasyMock.replay(filterChain);

        return filterChain;
    }
}