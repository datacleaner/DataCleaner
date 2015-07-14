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
package org.datacleaner.monitor.server.components;

import org.apache.metamodel.schema.Column;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.ImmutableComponentConfiguration;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.monitor.server.crates.ComponentDataInput;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This class handles an execution of an analyzer and provides its results.
 * @author j.horcicka (GMC)
 * @since 14. 07. 2015
 */
public class AnalyzerProvider implements ComponentResultsProvider {
    private DataCleanerConfiguration dataCleanerConfiguration;
    private ComponentDataInput componentDataInput;

    public AnalyzerProvider(DataCleanerConfiguration dataCleanerConfiguration, ComponentDataInput componentDataInput) {
        this.dataCleanerConfiguration = dataCleanerConfiguration;
        this.componentDataInput = componentDataInput;
    }

    public boolean exists(String name) {
        AnalyzerDescriptor analyzerDescriptor = dataCleanerConfiguration.getEnvironment()
            .getDescriptorProvider()
            .getAnalyzerDescriptorByDisplayName(name);

        return analyzerDescriptor != null;
    }

    public Serializable getComponentResults() {
        String name = componentDataInput.getConfiguration().getComponentName();
        AnalyzerDescriptor descriptor = dataCleanerConfiguration.getEnvironment()
            .getDescriptorProvider()
            .getAnalyzerDescriptorByDisplayName(name);

        AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(dataCleanerConfiguration);
        Column[] columnArray =  componentDataInput.getTable().getColumns();
        analysisJobBuilder.addSourceColumns(columnArray);
        AnalyzerComponentBuilder analyzerComponentBuilder = analysisJobBuilder.addAnalyzer(descriptor);
        List<InputColumn> inputColumns = componentDataInput.getInputColumns();
        analyzerComponentBuilder.addInputColumns(inputColumns);
        Analyzer analyzer = (Analyzer) analyzerComponentBuilder.getComponentInstance();

        LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(dataCleanerConfiguration, null, false);
        lifeCycleHelper.initializeReferenceData();

        final Map<ConfiguredPropertyDescriptor, Object> configuredProperties = analyzerComponentBuilder.getConfiguredProperties();
        ImmutableComponentConfiguration analyzerConfiguration = new ImmutableComponentConfiguration(configuredProperties);

        lifeCycleHelper.assignConfiguredProperties(descriptor, analyzer, analyzerConfiguration);
        lifeCycleHelper.assignProvidedProperties(descriptor, analyzer);
        lifeCycleHelper.validate(descriptor, analyzer);
        lifeCycleHelper.initialize(descriptor, analyzer);

        for (InputRow inputRow : componentDataInput.getInputRows()) {
            analyzer.run(inputRow, 1);
        }

        lifeCycleHelper.close(descriptor, analyzer, true);

        return analyzer.getResult();
    }
}
