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
package org.datacleaner.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.metamodel.schema.Table;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.CompoundComponentRequirement;
import org.datacleaner.job.HasFilterOutcomes;
import org.datacleaner.job.SimpleComponentRequirement;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;

public class PreviewUtils {

    public static final String METADATA_PROPERTY_MARKER = "org.datacleaner.preview.targetcomponent";

    public static void limitJobRows(final AnalysisJobBuilder jobBuilder,
            final Collection<? extends ComponentBuilder> componentBuilders, final int previewRows) {
        {
            final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
            sourceColumnFinder.addSources(jobBuilder);
            final List<Table> sourceTables = jobBuilder.getSourceTables();
            final int maxRows = Double.valueOf(Math.ceil(((double) previewRows) / sourceTables.size())).intValue();
            for (Table table : sourceTables) {
                final String filterName = PreviewUtils.class.getName() + "-" + table.getName() + "-MaxRows";

                final FilterComponentBuilder<?, ?> maxRowFilter =
                        jobBuilder.getFilterComponentBuilderByName(filterName).orElseGet(() -> {
                            final FilterComponentBuilder<MaxRowsFilter, MaxRowsFilter.Category> filter = jobBuilder
                                    .addFilter(MaxRowsFilter.class);
                            filter.setName(filterName);
                            filter.getComponentInstance().setMaxRows(maxRows);
                            filter.getComponentInstance().setApplyOrdering(false);
                            filter.getComponentInstance()
                                    .setOrderColumn(jobBuilder.getSourceColumnsOfTable(table).get(0));
                            return filter;
                        });

                componentBuilders.stream().filter(cb -> cb != maxRowFilter).forEach(componentBuilder -> {
                    final InputColumn<?>[] input = componentBuilder.getInput();
                    if (input.length > 0) {
                        if (componentBuilder.getDescriptor().isMultiStreamComponent() || sourceColumnFinder
                                .findOriginatingTable(input[0]) == table) {
                            final ComponentRequirement existingRequirement = componentBuilder
                                    .getComponentRequirement();
                            if (existingRequirement != null) {
                                if (componentBuilder.getDescriptor().isMultiStreamComponent()) {
                                    componentBuilder.setComponentRequirement(new CompoundComponentRequirement(
                                            existingRequirement, maxRowFilter.getFilterOutcome(
                                            MaxRowsFilter.Category.VALID)));
                                }
                            } else {
                                componentBuilder.setComponentRequirement(new SimpleComponentRequirement(maxRowFilter
                                        .getFilterOutcome(MaxRowsFilter.Category.VALID)));
                            }
                        }
                    }
                });
            }
        }
    }

    public static AnalysisJobBuilder copy(final AnalysisJobBuilder original) {
        final AnalysisJob analysisJob = original.getRootJobBuilder().withoutListeners().toAnalysisJob(false);
        return new AnalysisJobBuilder(original.getConfiguration(), analysisJob);
    }

    public static void sanitizeIrrelevantComponents(AnalysisJobBuilder ajb, TransformerComponentBuilder<?> tjb) {
        final List<AnalysisJobBuilder> relevantAnalysisJobBuilders = createRelevantAnalysisJobBuildersList(ajb);

        for (AnalysisJobBuilder relevantAnalysisJobBuilder : relevantAnalysisJobBuilders) {
            final Collection<ComponentBuilder> componentBuilders = relevantAnalysisJobBuilder.getComponentBuilders();
            for (ComponentBuilder componentBuilder : componentBuilders) {

                // flag to indicate if this component is directly involved in
                // populating data for the previewed component
                boolean importantComponent = componentBuilder == tjb;

                final List<OutputDataStream> streams = componentBuilder.getOutputDataStreams();
                for (OutputDataStream stream : streams) {
                    if (componentBuilder.isOutputDataStreamConsumed(stream)) {
                        final AnalysisJobBuilder childJobBuilder = componentBuilder.getOutputDataStreamJobBuilder(
                                stream);
                        if (relevantAnalysisJobBuilders.contains(childJobBuilder)) {
                            importantComponent = true;
                        } else {
                            // remove irrelevant output data stream job builder
                            childJobBuilder.removeAllComponents();
                        }
                    }
                }

                if (!importantComponent && componentBuilder instanceof AnalyzerComponentBuilder) {
                    // remove analyzers because they are generally more
                    // heavy-weight and they produce no dependencies for other
                    // components
                    relevantAnalysisJobBuilder.removeComponent(componentBuilder);
                }

                if (!importantComponent) {
                    // remove the components that are not configured.
                    if (!componentBuilder.isConfigured(false)) {
                        relevantAnalysisJobBuilder.removeComponent(componentBuilder);
                    }
                }

            }
        }
    }

    /**
     * Creates a list with _just_ the relevant {@link AnalysisJobBuilder}s to
     * include in the preview job
     *
     * @param ajb
     * @return
     */
    private static List<AnalysisJobBuilder> createRelevantAnalysisJobBuildersList(AnalysisJobBuilder ajb) {
        final List<AnalysisJobBuilder> relevantAnalysisJobBuilders = new LinkedList<>();
        relevantAnalysisJobBuilders.add(ajb);
        while (!ajb.isRootJobBuilder()) {
            ajb = ajb.getParentJobBuilder();
        }
        return relevantAnalysisJobBuilders;
    }

    public static AnalysisJobBuilder findAnalysisJobBuilder(AnalysisJobBuilder analysisJobBuilder,
            String jobBuilderIdentifier) {
        if (jobBuilderIdentifier.equals(analysisJobBuilder.getAnalysisJobMetadata().getProperties().get(
                METADATA_PROPERTY_MARKER))) {
            return analysisJobBuilder;
        }

        final List<AnalysisJobBuilder> childJobBuilders = analysisJobBuilder.getConsumedOutputDataStreamsJobBuilders();
        for (AnalysisJobBuilder childJobBuilder : childJobBuilders) {
            final AnalysisJobBuilder result = findAnalysisJobBuilder(childJobBuilder, jobBuilderIdentifier);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public static boolean hasFilterPresent(final SourceColumnFinder scf, final ComponentBuilder acb) {
        return scf.findAllSourceJobs(acb).stream().filter(
                o -> o instanceof HasFilterOutcomes).findAny().isPresent();
    }
}
