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
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
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

    private static final Logger logger = LoggerFactory.getLogger(PreviewTransformedDataActionListener.class);

    public static final int DEFAULT_PREVIEW_ROWS = 200;

    private final TransformerComponentBuilderPresenter _transformerJobBuilderPresenter;
    private final TransformerComponentBuilder<?> _transformerJobBuilder;
    private final WindowContext _windowContext;
    private final int _previewRows;

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
        DataSetWindow window = new DataSetWindow("Preview of transformed dataset", this, _windowContext);
        window.setVisible(true);
    }

    @Override
    public TableModel call() throws Exception {
        if (_transformerJobBuilderPresenter != null) {
            _transformerJobBuilderPresenter.applyPropertyValues();
        }

        final AnalysisJobBuilder ajb = copy(_transformerJobBuilder.getAnalysisJobBuilder());

        TransformerComponentBuilder<?> tjb = findTransformerComponentBuilder(ajb, _transformerJobBuilder);

        // remove all analyzers, except the dummy
        ajb.removeAllAnalyzers();

        // add the result collector (a dummy analyzer)
        final AnalyzerComponentBuilder<PreviewTransformedDataAnalyzer> rowCollector = ajb
                .addAnalyzer(Descriptors.ofAnalyzer(PreviewTransformedDataAnalyzer.class))
                .addInputColumns(tjb.getInputColumns()).addInputColumns(tjb.getOutputColumns());

        if (tjb.getComponentRequirement() != null) {
            rowCollector.setComponentRequirement(tjb.getComponentRequirement());
        }

        // add a max rows filter
        final FilterComponentBuilder<MaxRowsFilter, MaxRowsFilter.Category> maxRowFilter = ajb.addFilter(MaxRowsFilter.class);
        maxRowFilter.getComponentInstance().setMaxRows(_previewRows);
        ajb.setDefaultRequirement(maxRowFilter, MaxRowsFilter.Category.VALID);

        final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(ajb);

        final String[] columnNames = new String[rowCollector.getInputColumns().size()];
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = rowCollector.getInputColumns().get(i).getName();
        }

        final AnalysisRunner runner = new AnalysisRunnerImpl(ajb.getConfiguration());
        final AnalysisResultFuture resultFuture = runner.run(ajb.toAnalysisJob());

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

        final List<AnalyzerResult> results = resultFuture.getResults();
        assert results.size() == 1;

        final PreviewTransformedDataAnalyzer result = (PreviewTransformedDataAnalyzer) results.get(0);

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

    private TransformerComponentBuilder<?> findTransformerComponentBuilder(AnalysisJobBuilder ajb,
            TransformerComponentBuilder<?> transformerJobBuilder) {
        final AnalysisJobBuilder analysisJobBuilder = _transformerJobBuilder.getAnalysisJobBuilder();
        final int transformerIndex = analysisJobBuilder.getTransformerComponentBuilders().indexOf(_transformerJobBuilder);
        return ajb.getTransformerComponentBuilders().get(transformerIndex);
    }

    private AnalysisJobBuilder copy(final AnalysisJobBuilder original) {
        final AnalysisJob analysisJob = original.withoutListeners().toAnalysisJob(false);
        final AnalysisJobBuilder ajb = new AnalysisJobBuilder(original.getConfiguration(), analysisJob);
        return ajb;
    }
}
