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