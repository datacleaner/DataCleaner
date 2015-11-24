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
package org.datacleaner.monitor.server.controllers;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.repository.file.FileRepository;
import org.datacleaner.repository.file.FileRepositoryFile;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationFileControllerTest extends EasyMockSupport {

    private final ConfigurationFileController controller = new ConfigurationFileController();

    @Test
    public void testGetFileContents() throws Exception {
        final TenantContextFactory tenantContextFactory = createMock(TenantContextFactory.class);
        final TenantContext tenantContext = createMock(TenantContext.class);

        controller._contextFactory = tenantContextFactory;

        EasyMock.expect(tenantContextFactory.getContext("tenant1")).andReturn(tenantContext);
        File file = new File("target/ConfigurationFileControllerTest.testGetFileContents.conf.xml");
        FileHelper.writeString(new FileOutputStream(file), "<configuration></configuration>",
                FileHelper.DEFAULT_ENCODING);

        FileRepositoryFile configurationFile = new FileRepositoryFile(new FileRepository("target"), file);
        EasyMock.expect(tenantContext.getConfigurationFile()).andReturn(configurationFile);

        replayAll();

        final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        mockMvc.perform(MockMvcRequestBuilders.get("/tenant1/conf.xml")).andExpect(status().isOk())
                .andExpect(content().string("<configuration></configuration>"));
    }

    @Test
    public void testUploadFile() throws Exception {
        final TenantContextFactory tenantContextFactory = createMock(TenantContextFactory.class);
        final TenantContext tenantContext = createMock(TenantContext.class);

        controller._contextFactory = tenantContextFactory;

        EasyMock.expect(tenantContextFactory.getContext("tenant1")).andReturn(tenantContext);
        File file = new File("target/ConfigurationFileControllerTest.testUploadFile.conf.xml");
        FileRepositoryFile configurationFile = new FileRepositoryFile(new FileRepository("target"), file);
        EasyMock.expect(tenantContext.getConfigurationFile()).andReturn(configurationFile);

        replayAll();

        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON));
        ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();
        RequestResponseBodyMethodProcessor returnValueHandler = new RequestResponseBodyMethodProcessor(
                Arrays.<HttpMessageConverter<?>> asList(messageConverter), contentNegotiationManager);

        InputStream inputStream = FileHelper
                .getInputStream(new File("src/test/resources/example_repo/tenant1/conf.xml"));
        MockMultipartFile uploadedFile = new MockMultipartFile("file", inputStream);

        final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomReturnValueHandlers(returnValueHandler).build();

        // note that in this test we have to use the "{tenant}/configuration"
        // url because MockMvc does some strange magic regarding the ".xml"
        // suffix which it interprets as a XML response then.
        final String content = mockMvc
                .perform(MockMvcRequestBuilders.fileUpload("/tenant1/configuration").file(uploadedFile)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        final Map<?, ?> map = new ObjectMapper().readValue(content, Map.class);
        assertEquals("Success", map.get("status"));

        verifyAll();
    }
}
