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

import org.apache.commons.lang.ArrayUtils;
import org.apache.metamodel.schema.Column;
import org.datacleaner.api.Transformer;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.ImmutableComponentConfiguration;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.monitor.server.crates.ComponentDataInput;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This class handles a transformer execution and provides its results.
 * @author j.horcicka (GMC)
 * @since 14. 07. 2015
 */
public class TransformerProvider implements ComponentResultsProvider {
    private DataCleanerConfiguration dataCleanerConfiguration;
    private ComponentDataInput componentDataInput;

    public TransformerProvider(DataCleanerConfiguration dataCleanerConfiguration, ComponentDataInput componentDataInput) {
        this.dataCleanerConfiguration = dataCleanerConfiguration;
        this.componentDataInput = componentDataInput;
    }

    public boolean exists(String name) {
        TransformerDescriptor transformerDescriptor = dataCleanerConfiguration.getEnvironment()
                .getDescriptorProvider()
                .getTransformerDescriptorByDisplayName(name);

        return transformerDescriptor != null;
    }

    public Serializable getComponentResults() {
        String name = componentDataInput.getConfiguration().getComponentName();
        TransformerDescriptor descriptor = dataCleanerConfiguration.getEnvironment()
                .getDescriptorProvider()
                .getTransformerDescriptorByDisplayName(name);

        AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(dataCleanerConfiguration);
        Column[] columnArray =  componentDataInput.getTable().getColumns();
        analysisJobBuilder.addSourceColumns(columnArray);
        TransformerComponentBuilder transformerComponentBuilder = analysisJobBuilder.addTransformer(descriptor);
        List<InputColumn> inputColumns = componentDataInput.getInputColumns();
        transformerComponentBuilder.addInputColumns(inputColumns);
        Transformer transformer = (Transformer) transformerComponentBuilder.getComponentInstance();

        LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(dataCleanerConfiguration, null, false);
        lifeCycleHelper.initializeReferenceData();

        final Map<ConfiguredPropertyDescriptor, Object> configuredProperties = transformerComponentBuilder.getConfiguredProperties();
        ImmutableComponentConfiguration transformerConfiguration = new ImmutableComponentConfiguration(configuredProperties);

        lifeCycleHelper.assignConfiguredProperties(descriptor, transformer, transformerConfiguration);
        lifeCycleHelper.assignProvidedProperties(descriptor, transformer);
        lifeCycleHelper.validate(descriptor, transformer);
        lifeCycleHelper.initialize(descriptor, transformer);
        Object[] results = null;

        for (InputRow inputRow : componentDataInput.getInputRows()) {
            results = ArrayUtils.addAll(results, transformer.transform(inputRow));
        }

        lifeCycleHelper.close(descriptor, transformer, true);

        return results;
    }
}
