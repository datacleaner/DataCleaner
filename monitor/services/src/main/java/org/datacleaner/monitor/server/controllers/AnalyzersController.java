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

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;

import org.datacleaner.Version;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.AnalyzerResult;
import org.apache.metamodel.schema.Column;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.beans.StringAnalyzerResult;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.ImmutableComponentConfiguration;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.tasks.InitializeTask;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.server.crates.ComponentRequestCrate;
import org.datacleaner.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("dc-rest-v1/{tenant}/analyzers")
public class AnalyzersController {
    private static final String JSON = "application/json";
    @Autowired
    Repository _repository;
    @Autowired
    TenantContextFactory _tenantContextFactory;
    private TenantContext tenantContext;
    private DataCleanerConfiguration configuration;
    final Map<String, Object> output = new HashMap<>();

    private void init(String tenant) {
        tenantContext = _tenantContextFactory.getContext(tenant);
        configuration = tenantContext.getConfiguration();
        output.clear();
        output.put("tenant", tenant);
        output.put("version", Version.getVersion());
        output.put("edition", Version.getEdition());
    }

    @RequestMapping(
            value = "/info",
            method = RequestMethod.GET,
            produces = AnalyzersController.JSON
    )
    @ResponseBody
    public Map<String, Object> info(@PathVariable("tenant") final String tenant) {
        init(tenant);
        output.put("crate", getTestCrate());

        return output;
    }

    private ComponentRequestCrate getTestCrate() {
        ComponentRequestCrate componentRequestCrate = new ComponentRequestCrate();
        componentRequestCrate.setColumnList(
                Arrays.asList("id", "name")
        );
        componentRequestCrate.setDataTable(Arrays.asList(
                Arrays.asList("01", "one"),
                Arrays.asList("02", "two")
        ));
        componentRequestCrate.setPropertiesMap(null);

        return componentRequestCrate;
    }

    @RequestMapping(
            method = RequestMethod.GET,
            produces = AnalyzersController.JSON
    )
    @ResponseBody
    public Map<String, Object> getList(@PathVariable("tenant") final String tenant) {
        init(tenant);

        return output;
    }

    @RequestMapping(
            value = "/{type}",
            method = RequestMethod.POST,
            produces = AnalyzersController.JSON
    )
    @ResponseBody
    public Map<String, Object> analyzer(
            @PathVariable("tenant") final String tenant,
            @PathVariable("type") final String type,
            @RequestBody ComponentRequestCrate componentRequestCrate
    ) {
        init(tenant);
         // mytodo add checks (existing type, ...)

        componentRequestCrate.init();

        AnalyzerDescriptor descriptor = configuration.getEnvironment()
                .getDescriptorProvider()
                .getAnalyzerDescriptorByDisplayName(type);
        AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(configuration);
        Column[] columnArray =  componentRequestCrate.getTable().getColumns();
        analysisJobBuilder.addSourceColumns(columnArray);
        AnalyzerComponentBuilder analyzerComponentBuilder = analysisJobBuilder.addAnalyzer(descriptor);

        List<InputColumn> inputColumns = componentRequestCrate.getInputColumns();
        analyzerComponentBuilder.addInputColumns(inputColumns);
        Analyzer analyzer = (Analyzer) analyzerComponentBuilder.getComponentInstance();

        LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(configuration, null, false);
        lifeCycleHelper.initializeReferenceData();

        final Map<ConfiguredPropertyDescriptor, Object> configuredProperties = analyzerComponentBuilder.getConfiguredProperties();
        ImmutableComponentConfiguration analyzerConfig = new ImmutableComponentConfiguration(configuredProperties);

        lifeCycleHelper.assignConfiguredProperties(descriptor, analyzer, analyzerConfig);
        lifeCycleHelper.assignProvidedProperties(descriptor, analyzer);
        lifeCycleHelper.validate(descriptor, analyzer);
        lifeCycleHelper.initialize(descriptor, analyzer);

        List<InputRow> inputRows = componentRequestCrate.getInputRows();
        for (InputRow inputRow : inputRows) {
            analyzer.run(inputRow, 1);
        }

        lifeCycleHelper.close(descriptor, analyzer, true);

        AnalyzerResult analyzerResult = analyzer.getResult();

        output.put("result", analyzerResult);

        return output;
    }
}
