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
package org.datacleaner.job.builder;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.metamodel.schema.Column;
import org.datacleaner.api.ExpressionBasedInputColumn;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnyComponentRequirement;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.CompoundComponentRequirement;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.HasFilterOutcomes;
import org.datacleaner.job.InputColumnSourceJob;
import org.datacleaner.job.OutputDataStreamJob;
import org.datacleaner.job.SimpleComponentRequirement;
import org.datacleaner.util.SourceColumnFinder;

/**
 * Helper class to perform the somewhat intricate
 * {@link AnalysisJobBuilder#importJob(AnalysisJob)} operation.
 */
final class AnalysisJobBuilderImportHelper {

    private final AnalysisJobBuilder _builder;

    public AnalysisJobBuilderImportHelper(AnalysisJobBuilder builder) {
        _builder = builder;
    }

    public void importJob(AnalysisJob job) {
        _builder.setDatastore(job.getDatastore());

        _builder.setAnalysisJobMetadata(job.getMetadata());

        final Collection<InputColumn<?>> sourceColumns = job.getSourceColumns();
        for (InputColumn<?> inputColumn : sourceColumns) {
            _builder.addSourceColumn((MetaModelInputColumn) inputColumn);
        }

        final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(job);

        // map that translates original component jobs to their builder objects
        final Map<ComponentJob, ComponentBuilder> componentBuilders = new IdentityHashMap<ComponentJob, ComponentBuilder>();
        addComponentBuilders(job.getFilterJobs(), componentBuilders);
        addComponentBuilders(job.getTransformerJobs(), componentBuilders);
        addComponentBuilders(job.getAnalyzerJobs(), componentBuilders);

        // re-build filter requirements
        for (Entry<ComponentJob, ComponentBuilder> entry : componentBuilders.entrySet()) {
            final ComponentJob componentJob = entry.getKey();
            final ComponentBuilder builder = entry.getValue();
            final ComponentRequirement originalRequirement = componentJob.getComponentRequirement();
            final ComponentRequirement componentRequirement = findImportedRequirement(originalRequirement,
                    componentBuilders);
            builder.setComponentRequirement(componentRequirement);
        }

        // re-build input column dependencies
        for (Entry<ComponentJob, ComponentBuilder> entry : componentBuilders.entrySet()) {
            final ComponentJob componentJob = entry.getKey();
            final ComponentBuilder builder = entry.getValue();
            final Set<ConfiguredPropertyDescriptor> inputColumnProperties = componentJob.getDescriptor()
                    .getConfiguredPropertiesForInput(true);

            for (ConfiguredPropertyDescriptor inputColumnProperty : inputColumnProperties) {
                final Object originalInputColumnValue = componentJob.getConfiguration()
                        .getProperty(inputColumnProperty);
                final Object newInputColumnValue = findImportedInputColumns(originalInputColumnValue,
                        componentBuilders, sourceColumnFinder);
                builder.setConfiguredProperty(inputColumnProperty, newInputColumnValue);
            }
        }

        // re-build output data streams
        for (Entry<ComponentJob, ComponentBuilder> entry : componentBuilders.entrySet()) {
            final ComponentJob componentJob = entry.getKey();
            final ComponentBuilder builder = entry.getValue();
            final OutputDataStreamJob[] outputDataStreamJobs = componentJob.getOutputDataStreamJobs();
            for (OutputDataStreamJob outputDataStreamJob : outputDataStreamJobs) {
                final OutputDataStream outputDataStream = outputDataStreamJob.getOutputDataStream();
                final AnalysisJobBuilder outputDataStreamJobBuilder = builder
                        .getOutputDataStreamJobBuilder(outputDataStream.getName());

                // delegate to a new helper to handle the output data stream
                final AnalysisJobBuilderImportHelper helper = new AnalysisJobBuilderImportHelper(
                        outputDataStreamJobBuilder);
                helper.importJob(outputDataStreamJob.getJob());
            }
        }
    }

    private Object findImportedInputColumns(Object originalInputColumnValue,
            Map<ComponentJob, ComponentBuilder> componentBuilders, SourceColumnFinder sourceColumnFinder) {
        if (originalInputColumnValue == null) {
            return null;
        }

        if (originalInputColumnValue instanceof InputColumn) {
            return findImportedInputColumn((InputColumn<?>) originalInputColumnValue, componentBuilders,
                    sourceColumnFinder);
        }

        if (originalInputColumnValue.getClass().isArray()) {
            int length = Array.getLength(originalInputColumnValue);
            InputColumn<?>[] value = new InputColumn[length];
            for (int i = 0; i < value.length; i++) {
                InputColumn<?> element = (InputColumn<?>) Array.get(originalInputColumnValue, i);
                value[i] = findImportedInputColumn(element, componentBuilders, sourceColumnFinder);
            }
            return value;
        }

        throw new UnsupportedOperationException("Unknown input column value type: " + originalInputColumnValue);
    }

    private InputColumn<?> findImportedInputColumn(InputColumn<?> originalInputColumn,
            Map<ComponentJob, ComponentBuilder> componentBuilders, SourceColumnFinder sourceColumnFinder) {
        if (originalInputColumn.isPhysicalColumn()) {
            Column physicalColumn = originalInputColumn.getPhysicalColumn();
            return _builder.getSourceColumnByName(physicalColumn.getQualifiedLabel());
        }

        if (originalInputColumn instanceof ExpressionBasedInputColumn) {
            // we can use the original here - the expression is independent
            return originalInputColumn;
        }

        final InputColumnSourceJob originalSourceJob = sourceColumnFinder.findInputColumnSource(originalInputColumn);
        if (originalSourceJob == null) {
            throw new IllegalStateException("Could not find source for input column  " + originalInputColumn
                    + " in original job");
        }

        final InputColumnSourceJob newSourceJob = (InputColumnSourceJob) componentBuilders.get(originalSourceJob);

        if (newSourceJob == null) {
            throw new IllegalStateException("Could not find builder corresponding to " + originalSourceJob
                    + " in builder map: " + componentBuilders);
        }

        final String originalColumnName = originalInputColumn.getName();
        final InputColumn<?>[] candidates = newSourceJob.getOutput();
        for (InputColumn<?> candidate : candidates) {
            if (candidate.getName().equals(originalColumnName)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Could not determine a replacement input column for '" + originalColumnName
                + "' in output column candidate set: " + Arrays.toString(candidates));
    }

    private ComponentRequirement findImportedRequirement(ComponentRequirement originalRequirement,
            Map<ComponentJob, ComponentBuilder> componentBuilders) {
        if (originalRequirement == null) {
            return null;
        }

        if (originalRequirement instanceof AnyComponentRequirement) {
            return AnyComponentRequirement.get();
        }

        if (originalRequirement instanceof SimpleComponentRequirement) {
            final FilterOutcome originalFilterOutcome = ((SimpleComponentRequirement) originalRequirement).getOutcome();
            final FilterOutcome newOutcome = findFilterOutcome(originalFilterOutcome, componentBuilders);
            return new SimpleComponentRequirement(newOutcome);
        }

        if (originalRequirement instanceof CompoundComponentRequirement) {
            final Set<FilterOutcome> originalOutcomes = ((CompoundComponentRequirement) originalRequirement)
                    .getOutcomes();
            final Collection<FilterOutcome> newOutcomes = new HashSet<>();
            for (final FilterOutcome originalOutcome : originalOutcomes) {
                FilterOutcome newOutcome = findFilterOutcome(originalOutcome, componentBuilders);
                newOutcomes.add(newOutcome);
            }

            return new CompoundComponentRequirement(newOutcomes);
        }

        throw new UnsupportedOperationException("Unsupported requirement type: " + originalRequirement);
    }

    private FilterOutcome findFilterOutcome(FilterOutcome originalFilterOutcome,
            Map<ComponentJob, ComponentBuilder> componentBuilders) {
        final HasFilterOutcomes source = originalFilterOutcome.getSource();
        final ComponentBuilder builder = componentBuilders.get(source);
        if (builder == null) {
            throw new IllegalStateException("Could not find builder corresponding to " + source + " in builder map: "
                    + componentBuilders);
        }
        final Enum<?> category = originalFilterOutcome.getCategory();

        final FilterComponentBuilder<?, ?> filterJobBuilder = (FilterComponentBuilder<?, ?>) builder;
        final FilterOutcome newOutcome = filterJobBuilder.getFilterOutcome(category);
        return newOutcome;
    }

    private void addComponentBuilders(Collection<? extends ComponentJob> componentJobs,
            Map<ComponentJob, ComponentBuilder> componentBuilders) {
        for (ComponentJob componentJob : componentJobs) {
            ComponentBuilder builder = _builder.addComponent(componentJob);
            componentBuilders.put(componentJob, builder);
        }
    }
}
