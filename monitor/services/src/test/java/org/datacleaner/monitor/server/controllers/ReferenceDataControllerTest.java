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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.job.JobEngineManager;
import org.datacleaner.monitor.server.controllers.referencedata.model.StringPatternModel;
import org.datacleaner.monitor.server.dao.ReferenceDataDaoImpl;
import org.datacleaner.monitor.server.job.SimpleJobEngineManager;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ReferenceDataControllerTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private MockMvc _mockMvc;
    private ObjectMapper _objectMapper;

    private TenantContextFactory getTenantContextFactory() throws Exception {
        final File jobTempRepoFolder = tempFolder.newFolder("test");
        FileUtils.copyDirectory(new File("src/test/resources/example_repo/tenant1"), jobTempRepoFolder);

        final Repository repository = new FileRepository(tempFolder.getRoot().getAbsoluteFile());
        final JobEngineManager jobEngineManager = new SimpleJobEngineManager();
        return new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(), jobEngineManager);
    }

    @Before
    public void setUp() throws Exception {
        ReferenceDataController referenceDataController =
                new ReferenceDataController(getTenantContextFactory(), new ReferenceDataDaoImpl());

        _mockMvc = MockMvcBuilders.standaloneSetup(referenceDataController).build();
        _objectMapper = new ObjectMapper();
    }

    @Test
    public void testPutStringPattern() throws Exception {
        StringPatternModel stringPatternModel =
                new StringPatternModel("stringPattern", "A1a", StringPatternModel.PatternType.STRING);

        _mockMvc.perform(put("/test/referencedata/stringPattern/test").contentType(MediaType.APPLICATION_JSON)
                .content(_objectMapper.writeValueAsString(stringPatternModel))).andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        Matchers.endsWith("http://localhost/test/referencedata/stringPattern/test")));
    }

    @Test
    public void testListReferenceData() throws Exception {
        _mockMvc.perform(get("/test/referencedata")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[?(@.referenceDataType=='DICTIONARY')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.referenceDataType=='SYNONYM_CATALOG')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.referenceDataType=='STRING_PATTERN')]", hasSize(3))).andExpect(
                jsonPath("$[0].href", is("http://localhost/test/referencedata/dictionary/vendor whitelist")));
    }

    @Test
    public void testListStringPatterns() throws Exception {
        _mockMvc.perform(get("/test/referencedata/stringPatterns")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.referenceDataType!='STRING_PATTERN')]", hasSize(0)))
                .andExpect(jsonPath("$[?(@.referenceDataType=='STRING_PATTERN')]", hasSize(3))).andExpect(
                jsonPath("$[0].href", is("http://localhost/test/referencedata/stringPattern/All lowercase")));
    }

    @Test
    public void testListDictionaries() throws Exception {

        _mockMvc.perform(get("/test/referencedata/dictionaries")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[?(@.referenceDataType!='DICTIONARY')]", hasSize(0)))
                .andExpect(jsonPath("$[?(@.referenceDataType=='DICTIONARY')]", hasSize(1))).andExpect(
                jsonPath("$[0].href", is("http://localhost/test/referencedata/dictionary/vendor whitelist")));
    }

    @Test
    public void testListSynonymCatalogs() throws Exception {

        _mockMvc.perform(get("/test/referencedata/synonymCatalogs")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[?(@.referenceDataType!='SYNONYM_CATALOG')]", hasSize(0)))
                .andExpect(jsonPath("$[?(@.referenceDataType=='SYNONYM_CATALOG')]", hasSize(1))).andExpect(
                jsonPath("$[0].href", is("http://localhost/test/referencedata/synonymCatalog/job title synonyms")));
    }


}