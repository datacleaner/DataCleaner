/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.analyzer.job.JaxbJobWriter;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPresenter;
import org.eobjects.datacleaner.util.PreviewTransformedDataAnalyzer;
import org.eobjects.datacleaner.windows.DataSetWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ActionListener responsible for previewing transformed data in a
 * {@link DataSetWindow}.
 */
public final class PreviewTransformedDataActionListener implements ActionListener, Callable<TableModel> {

    private static final Logger logger = LoggerFactory.getLogger(PreviewTransformedDataActionListener.class);

    public static final int DEFAULT_PREVIEW_ROWS = 200;

    private final TransformerJobBuilderPresenter _transformerJobBuilderPresenter;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final TransformerJobBuilder<?> _transformerJobBuilder;
    private final WindowContext _windowContext;
    private final AnalyzerBeansConfiguration _configuration;
    private final int _previewRows;

    public PreviewTransformedDataActionListener(WindowContext windowContext,
            TransformerJobBuilderPresenter transformerJobBuilderPresenter, AnalysisJobBuilder analysisJobBuilder,
            TransformerJobBuilder<?> transformerJobBuilder, AnalyzerBeansConfiguration configuration) {
        this(windowContext, transformerJobBuilderPresenter, analysisJobBuilder, transformerJobBuilder, configuration,
                DEFAULT_PREVIEW_ROWS);
    }

    public PreviewTransformedDataActionListener(WindowContext windowContext,
            TransformerJobBuilderPresenter transformerJobBuilderPresenter, AnalysisJobBuilder analysisJobBuilder,
            TransformerJobBuilder<?> transformerJobBuilder, AnalyzerBeansConfiguration configuration, int previewRows) {
        _windowContext = windowContext;
        _transformerJobBuilderPresenter = transformerJobBuilderPresenter;
        _analysisJobBuilder = analysisJobBuilder;
        _transformerJobBuilder = transformerJobBuilder;
        _configuration = configuration;
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

        final AnalysisJobBuilder ajb = copy(_analysisJobBuilder);

        TransformerJobBuilder<?> tjb = findTransformerJobBuilder(ajb, _transformerJobBuilder);

        // remove all analyzers, except the dummy
        ajb.removeAllAnalyzers();

        // add the result collector (a dummy analyzer)
        final AnalyzerJobBuilder<PreviewTransformedDataAnalyzer> rowCollector = ajb
                .addAnalyzer(Descriptors.ofAnalyzer(PreviewTransformedDataAnalyzer.class))
                .addInputColumns(tjb.getInputColumns()).addInputColumns(tjb.getOutputColumns());

        if (tjb.getRequirement() != null) {
            rowCollector.setRequirement(tjb.getRequirement());
        }

        // add a max rows filter
        final FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> maxRowFilter = ajb.addFilter(MaxRowsFilter.class);
        maxRowFilter.getConfigurableBean().setMaxRows(_previewRows);
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
            for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
                tableModel.setValueAt(row[columnIndex], rowIndex, columnIndex);
            }
            rowIndex++;
        }

        return tableModel;
    }

    private TransformerJobBuilder<?> findTransformerJobBuilder(AnalysisJobBuilder ajb,
            TransformerJobBuilder<?> transformerJobBuilder) {
        int transformerIndex = _analysisJobBuilder.getTransformerJobBuilders().indexOf(_transformerJobBuilder);
        return ajb.getTransformerJobBuilders().get(transformerIndex);
    }

    private AnalysisJobBuilder copy(final AnalysisJobBuilder original) {
        // the easiest/safest way to copy a job is by writing and reading it
        // using the JAXB reader/writers.
        final AnalysisJob analysisJob = original.withoutListeners().toAnalysisJob(false);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new JaxbJobWriter(_configuration).write(analysisJob, baos);
        AnalysisJobBuilder ajb = new JaxbJobReader(original.getConfiguration()).create(new ByteArrayInputStream(baos
                .toByteArray()));
        return ajb;
    }
}
