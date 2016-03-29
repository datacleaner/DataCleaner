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
package org.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.metamodel.schema.Table;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.descriptors.Descriptors;
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
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.panels.TransformerComponentBuilderPresenter;
import org.datacleaner.util.PreviewTransformedDataAnalyzer;
import org.datacleaner.util.SourceColumnFinder;
import org.datacleaner.windows.DataSetWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ActionListener responsible for previewing transformed data in a
 * {@link DataSetWindow}.
 */
public final class PreviewTransformedDataActionListener implements ActionListener, Callable<TableModel> {

    public static class PreviewJob {
        public final AnalysisJobBuilder analysisJobBuilder;
        public final AnalyzerComponentBuilder<?> rowCollectorAnalyzer;
        public final TransformerComponentBuilder<?> previewedTransformer;

        public PreviewJob(AnalysisJobBuilder analysisJobBuilder, AnalyzerComponentBuilder<?> rowCollectorAnalyzer,
                TransformerComponentBuilder<?> previewedTransformer) {
            this.analysisJobBuilder = analysisJobBuilder;
            this.rowCollectorAnalyzer = rowCollectorAnalyzer;
            this.previewedTransformer = previewedTransformer;
        }
    }

    public static final String METADATA_PROPERTY_MARKER = "org.datacleaner.preview.targetcomponent";

    private static final Logger logger = LoggerFactory.getLogger(PreviewTransformedDataActionListener.class);

    public static final int DEFAULT_PREVIEW_ROWS = 200;

    private final TransformerComponentBuilderPresenter _transformerJobBuilderPresenter;
    private final TransformerComponentBuilder<?> _transformerJobBuilder;
    private final WindowContext _windowContext;
    private final int _previewRows;
    private DataSetWindow _latestWindow;

    public PreviewTransformedDataActionListener(WindowContext windowContext,
            TransformerComponentBuilder<?> transformerJobBuilder) {
        this(windowContext, null, transformerJobBuilder);
    }

    public PreviewTransformedDataActionListener(WindowContext windowContext,
            TransformerComponentBuilderPresenter transformerJobBuilderPresenter,
            TransformerComponentBuilder<?> transformerJobBuilder) {
        this(windowContext, transformerJobBuilderPresenter, transformerJobBuilder, DEFAULT_PREVIEW_ROWS);
    }

    public PreviewTransformedDataActionListener(WindowContext windowContext,
            TransformerComponentBuilderPresenter transformerJobBuilderPresenter,
            TransformerComponentBuilder<?> transformerJobBuilder, int previewRows) {
        _windowContext = windowContext;
        _transformerJobBuilderPresenter = transformerJobBuilderPresenter;
        _transformerJobBuilder = transformerJobBuilder;
        _previewRows = previewRows;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // prevent multiple preview windows from the same preview button
        DataSetWindow existingWindow = _latestWindow;
        if (existingWindow != null) {
            existingWindow.close();
            _latestWindow = null;
        }
        DataSetWindow window = new DataSetWindow("Preview of transformed dataset", this, _windowContext);
        window.open();
        _latestWindow = window;
    }

    protected PreviewJob createPreviewJob() {
        if (_transformerJobBuilderPresenter != null) {
            _transformerJobBuilderPresenter.applyPropertyValues();
        }

        final String jobBuilderIdentifier = UUID.randomUUID().toString();

        final AnalysisJobBuilder originalAnalysisJobBuilder = _transformerJobBuilder.getAnalysisJobBuilder();
        // put a marker metadata property on the AnalysisJobBuilder to make
        // it easy to identify it's equivalent object from the copy later.
        originalAnalysisJobBuilder.getAnalysisJobMetadata().getProperties().put(METADATA_PROPERTY_MARKER,
                jobBuilderIdentifier);
        final AnalysisJobBuilder ajb;
        try {
            final AnalysisJobBuilder copyAnalysisJobBuilder = copy(originalAnalysisJobBuilder.getRootJobBuilder());
            ajb = findAnalysisJobBuilder(copyAnalysisJobBuilder, jobBuilderIdentifier);
        } finally {
            // remove the marker metadata
            originalAnalysisJobBuilder.getAnalysisJobMetadata().getProperties().remove(METADATA_PROPERTY_MARKER);
        }

        if (ajb == null) {
            throw new IllegalStateException(
                    "Could not find AnalysisJobBuilder copy which is equivalent to the original");
        }

        final TransformerComponentBuilder<?> tjb = findTransformerComponentBuilder(ajb, _transformerJobBuilder);
        sanitizeIrrelevantComponents(ajb, tjb);

        // represents if the transformer is already filtered (also may be transitively)
        final boolean alreadyFiltered;
        
        // remove irrelevant source tables
        {
            final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
            sourceColumnFinder.addSources(ajb);

            
            final List<Table> tables = ajb.getSourceTables();
            if (tables.size() > 1) {
                final Table originatingTable = sourceColumnFinder.findOriginatingTable(tjb.getOutputColumns().get(0));
                tables.remove(originatingTable);
                for (Table otherTable : tables) {
                    ajb.removeSourceTable(otherTable);
                }
            }
            alreadyFiltered = sourceColumnFinder.findAllSourceJobs(tjb).stream().filter(
                    o -> o instanceof HasFilterOutcomes).findAny().isPresent();
        }

        final List<MetaModelInputColumn> sourceColumns = ajb.getSourceColumns();
        if (sourceColumns.isEmpty()) {
            logger.error("No source columns left after removing irrelevant source tables. Component: {}",
                    _transformerJobBuilder);
            return null;
        }
        

        // add the result collector (a dummy analyzer)
        final AnalyzerComponentBuilder<PreviewTransformedDataAnalyzer> rowCollector = ajb.addAnalyzer(Descriptors
                .ofAnalyzer(PreviewTransformedDataAnalyzer.class)).addInputColumns(tjb.getInputColumns())
                .addInputColumns(tjb.getOutputColumns());

        if (tjb.getComponentRequirement() != null) {
            rowCollector.setComponentRequirement(tjb.getComponentRequirement());
        }

        // add a max rows filter to the source of the job
        final AnalysisJobBuilder rootJobBuilder = ajb.getRootJobBuilder();
        {
            final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
            sourceColumnFinder.addSources(rootJobBuilder);
            final List<Table> sourceTables = rootJobBuilder.getSourceTables();
            final int maxRows = Double.valueOf(Math.ceil(((double) _previewRows) / sourceTables.size())).intValue();
            for (Table table : sourceTables) {
                final FilterComponentBuilder<MaxRowsFilter, MaxRowsFilter.Category> maxRowFilter = rootJobBuilder
                        .addFilter(MaxRowsFilter.class);
                maxRowFilter.setName(getClass().getName() + "-" + table.getName() + "-MaxRows");
                maxRowFilter.getComponentInstance().setMaxRows(maxRows);
                maxRowFilter.getComponentInstance().setApplyOrdering(false);
                maxRowFilter.getComponentInstance().setOrderColumn(rootJobBuilder.getSourceColumnsOfTable(table).get(
                        0));
                final Collection<? extends ComponentBuilder> componentBuilders;
                if (alreadyFiltered) {
                    // if there are already filters in place, only apply the max rows filter on the other filters.
                    componentBuilders = rootJobBuilder.getFilterComponentBuilders();
                } else {
                    componentBuilders = rootJobBuilder.getComponentBuilders();
                }
                
                for (ComponentBuilder componentBuilder : componentBuilders) {
                    if (componentBuilder != maxRowFilter) {
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
                    }
                }
            }
        }

        return new PreviewJob(rootJobBuilder, rowCollector, tjb);
    }

    @Override
    public TableModel call() throws Exception {
        final PreviewJob previewJob = createPreviewJob();

        if (previewJob == null) {
            return new DefaultTableModel(0, 0);
        }

        final AnalyzerComponentBuilder<?> rowCollector = previewJob.rowCollectorAnalyzer;

        final String[] columnNames = new String[rowCollector.getInputColumns().size()];
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = rowCollector.getInputColumns().get(i).getName();
        }

        final AnalysisRunner runner = new AnalysisRunnerImpl(previewJob.analysisJobBuilder.getConfiguration());
        final AnalysisResultFuture resultFuture = runner.run(previewJob.analysisJobBuilder.toAnalysisJob());

        resultFuture.await();

        if (resultFuture.isErrornous()) {
            List<Throwable> errors = resultFuture.getErrors();
            Throwable firstError = errors.get(0);
            logger.error("Error occurred while running preview data job: {}", firstError.getMessage());
            for (Throwable throwable : errors) {
                logger.info("Preview data error", throwable);
            }
            if (firstError instanceof Exception) {
                throw (Exception) firstError;
            }
            throw new IllegalStateException(firstError);
        }

        final List<? extends PreviewTransformedDataAnalyzer> results = resultFuture.getResults(
                PreviewTransformedDataAnalyzer.class);
        assert results.size() == 1;

        final PreviewTransformedDataAnalyzer result = results.get(0);

        final List<Object[]> rows = result.getList();
        final DefaultTableModel tableModel = new DefaultTableModel(columnNames, rows.size());
        int rowIndex = 0;
        for (Object[] row : rows) {
            if (row != null) {
                for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
                    tableModel.setValueAt(row[columnIndex], rowIndex, columnIndex);
                }
            }
            rowIndex++;
        }

        return tableModel;
    }

    private void sanitizeIrrelevantComponents(AnalysisJobBuilder ajb, TransformerComponentBuilder<?> tjb) {
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
    private List<AnalysisJobBuilder> createRelevantAnalysisJobBuildersList(AnalysisJobBuilder ajb) {
        final List<AnalysisJobBuilder> relevantAnalysisJobBuilders = new LinkedList<>();
        relevantAnalysisJobBuilders.add(ajb);
        while (!ajb.isRootJobBuilder()) {
            ajb = ajb.getParentJobBuilder();
        }
        return relevantAnalysisJobBuilders;
    }

    private AnalysisJobBuilder findAnalysisJobBuilder(AnalysisJobBuilder analysisJobBuilder,
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

    private TransformerComponentBuilder<?> findTransformerComponentBuilder(AnalysisJobBuilder ajb,
            TransformerComponentBuilder<?> transformerJobBuilder) {
        final AnalysisJobBuilder analysisJobBuilder = _transformerJobBuilder.getAnalysisJobBuilder();
        final int transformerIndex = analysisJobBuilder.getTransformerComponentBuilders().indexOf(
                _transformerJobBuilder);
        return ajb.getTransformerComponentBuilders().get(transformerIndex);
    }

    private AnalysisJobBuilder copy(final AnalysisJobBuilder original) {
        final AnalysisJob analysisJob = original.getRootJobBuilder().withoutListeners().toAnalysisJob(false);
        final AnalysisJobBuilder copy = new AnalysisJobBuilder(original.getConfiguration(), analysisJob);
        return copy;
    }
}
