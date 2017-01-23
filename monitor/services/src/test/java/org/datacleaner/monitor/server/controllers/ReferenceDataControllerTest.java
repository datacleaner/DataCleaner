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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.job.JobEngineManager;
import org.datacleaner.monitor.server.controllers.referencedata.model.DictionaryModel;
import org.datacleaner.monitor.server.controllers.referencedata.model.StringPatternModel;
import org.datacleaner.monitor.server.controllers.referencedata.model.SynonymCatalogModel;
import org.datacleaner.monitor.server.controllers.referencedata.model.SynonymModel;
import org.datacleaner.monitor.server.dao.ReferenceDataDaoImpl;
import org.datacleaner.monitor.server.job.SimpleJobEngineManager;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ReferenceDataControllerTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private MockMvc _mockMvc;
    private ObjectMapper _objectMapper;

    private TenantContextFactory getTenantContextFactory() throws Exception {
        final File jobTempRepoFolder = tempFolder.newFolder("test");
        FileUtils.copyDirectory(new File("src/test/resources/referencedatatest_repo/tenant1"), jobTempRepoFolder);

        final Repository repository = new FileRepository(tempFolder.getRoot().getAbsoluteFile());
        final JobEngineManager jobEngineManager = new SimpleJobEngineManager();
        return new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(), jobEngineManager);
    }

    @Before
    public void setUp() throws Exception {
        final ReferenceDataController referenceDataController =
                new ReferenceDataController(getTenantContextFactory(), new ReferenceDataDaoImpl());

        _mockMvc = MockMvcBuilders.standaloneSetup(referenceDataController).build();
        _objectMapper = new ObjectMapper();
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
    public void testPutAndDeleteStringPattern() throws Exception {
        final StringPatternModel stringPatternModel =
                new StringPatternModel("stringPattern", "A1a", StringPatternModel.PatternType.STRING);

        final MvcResult mvcResult = _mockMvc.perform(
                put("/test/referencedata/stringPattern/testPattern").contentType(MediaType.APPLICATION_JSON)
                        .content(_objectMapper.writeValueAsString(stringPatternModel))).andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        is("http://localhost/test/referencedata/stringPattern/testPattern"))).andReturn();

        final String location = mvcResult.getResponse().getHeader("Location");
        _mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.name", is("testPattern")))
                .andExpect(jsonPath("$.pattern", is("A1a")))
                .andExpect(jsonPath("$.patternType", is(StringPatternModel.PatternType.STRING.name())));
        _mockMvc.perform(delete(location)).andExpect(status().isNoContent());
        _mockMvc.perform(get(location)).andExpect(status().isNotFound());
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
    public void testPutAndDeleteDictionary() throws Exception {
        final DictionaryModel dictionaryModel =
                new DictionaryModel("dictionary", Arrays.asList("foo", "bar", "baz"), true);

        final MvcResult mvcResult = _mockMvc.perform(
                put("/test/referencedata/dictionary/testDictionary").contentType(MediaType.APPLICATION_JSON)
                        .content(_objectMapper.writeValueAsString(dictionaryModel))).andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        is("http://localhost/test/referencedata/dictionary/testDictionary"))).andReturn();

        final String location = mvcResult.getResponse().getHeader("Location");
        _mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.name", is("testDictionary")))
                .andExpect(jsonPath("$.entries", hasSize(3))).andExpect(jsonPath("$.entries[2]", is("baz")))
                .andExpect(jsonPath("$.caseSensitive", is(true)));
        _mockMvc.perform(delete(location)).andExpect(status().isNoContent());
        _mockMvc.perform(get(location)).andExpect(status().isNotFound());
    }

    @Test
    public void testListSynonymCatalogs() throws Exception {
        _mockMvc.perform(get("/test/referencedata/synonymCatalogs")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[?(@.referenceDataType!='SYNONYM_CATALOG')]", hasSize(0)))
                .andExpect(jsonPath("$[?(@.referenceDataType=='SYNONYM_CATALOG')]", hasSize(1))).andExpect(
                jsonPath("$[0].href", is("http://localhost/test/referencedata/synonymCatalog/job title synonyms")));
    }

    @Test
    public void testPutAndDeleteSynonymCatalog() throws Exception {
        final SynonymCatalogModel dictionaryModel = new SynonymCatalogModel("synonymCatalog",
                Collections.singletonList(new SynonymModel("foo", Arrays.asList("bar", "baz"))), true);

        final MvcResult mvcResult = _mockMvc.perform(
                put("/test/referencedata/synonymCatalog/testCatalog").contentType(MediaType.APPLICATION_JSON)
                        .content(_objectMapper.writeValueAsString(dictionaryModel))).andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        is("http://localhost/test/referencedata/synonymCatalog/testCatalog"))).andReturn();

        final String location = mvcResult.getResponse().getHeader("Location");
        _mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.name", is("testCatalog")))
                .andExpect(jsonPath("$.entries", hasSize(1))).andExpect(jsonPath("$.entries[0].masterTerm", is("foo")))
                .andExpect(jsonPath("$.entries[0].synonyms", hasSize(2)))
                .andExpect(jsonPath("$.entries[0].synonyms[1]", is("baz")))
                .andExpect(jsonPath("$.caseSensitive", is(true)));
        _mockMvc.perform(delete(location)).andExpect(status().isNoContent());
        _mockMvc.perform(get(location)).andExpect(status().isNotFound());

    }
}