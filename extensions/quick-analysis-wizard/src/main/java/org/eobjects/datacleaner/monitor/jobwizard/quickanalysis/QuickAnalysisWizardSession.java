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
package org.eobjects.datacleaner.monitor.jobwizard.quickanalysis;

import java.util.List;

import org.eobjects.analyzer.beans.stringpattern.PatternFinderAnalyzer;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.datacleaner.monitor.server.wizard.JobNameWizardPage;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.SelectColumnsWizardPage;
import org.eobjects.datacleaner.monitor.wizard.common.SelectTableWizardPage;
import org.eobjects.datacleaner.monitor.wizard.job.DataCleanerJobWizardSession;
import org.eobjects.datacleaner.monitor.wizard.job.JobWizardContext;
import org.eobjects.datacleaner.user.QuickAnalysisStrategy;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;

/**
 * Session implementation for the Quick Analysis wizard.
 */
final class QuickAnalysisWizardSession extends DataCleanerJobWizardSession {

    private final AnalysisJobBuilder _analysisJobBuilder;
    private int _pageCount;

    public QuickAnalysisWizardSession(JobWizardContext context) {
        super(context);
        _analysisJobBuilder = new AnalysisJobBuilder(context.getTenantContext().getConfiguration());
        _analysisJobBuilder.setDatastore(context.getSourceDatastore());
        _pageCount = 5;
    }

    @Override
    public WizardPageController firstPageController() {
        return new SelectTableWizardPage(getWizardContext(), 0) {
            @Override
            protected WizardPageController nextPageController(final Table selectedTable) {
                final boolean hasStringColumns = selectedTable.getLiteralColumns().length > 0;
                if (!hasStringColumns) {
                    _pageCount = 4;
                }

                final JobNameWizardPage lastPage = new JobNameWizardPage(getWizardContext(), _pageCount - 1,
                        "Quick analysis of " + selectedTable.getName()) {
                    @Override
                    protected WizardPageController nextPageController(String name) {
                        setJobName(name);
                        return null;
                    }
                };

                return new SelectColumnsWizardPage(1, selectedTable) {

                    @Override
                    protected String getHeaderHtml() {
                        return "<p>Please select columns to check for <b>standard data quality metrics</b>, based on data type:</p>";
                    }

                    @Override
                    protected WizardPageController nextPageController(final List<Column> selectedColumns) {
                        _analysisJobBuilder.addSourceColumns(selectedColumns);

                        final QuickAnalysisStrategy quickAnalysisStrategy = new QuickAnalysisStrategy(5, false, false);
                        quickAnalysisStrategy.configureAnalysisJobBuilder(_analysisJobBuilder);

                        return new SelectValueDistributionColumnsPage(2, selectedTable) {
                            @Override
                            protected WizardPageController nextPageController(List<Column> selectedColumns) {
                                for (Column selectedColumn : selectedColumns) {
                                    _analysisJobBuilder.addSourceColumn(selectedColumn);
                                    final InputColumn<?> sourceColumn = _analysisJobBuilder
                                            .getSourceColumnByName(selectedColumn.getName());
                                    final AnalyzerJobBuilder<ValueDistributionAnalyzer> valueDistribution = _analysisJobBuilder
                                            .addAnalyzer(ValueDistributionAnalyzer.class);
                                    valueDistribution.setName("Value distribution of " + selectedColumn.getName());
                                    valueDistribution.addInputColumn(sourceColumn);
                                }
                                
                                if (!hasStringColumns) {
                                    return lastPage;
                                }
                                return new SelectPatternFinderColumnsPage(3, selectedTable) {
                                    @Override
                                    protected WizardPageController nextPageController(List<Column> selectedColumns) {
                                        for (Column selectedColumn : selectedColumns) {
                                            _analysisJobBuilder.addSourceColumn(selectedColumn);
                                            final InputColumn<?> sourceColumn = _analysisJobBuilder
                                                    .getSourceColumnByName(selectedColumn.getName());
                                            final AnalyzerJobBuilder<PatternFinderAnalyzer> patternFinder = _analysisJobBuilder
                                                    .addAnalyzer(PatternFinderAnalyzer.class);
                                            patternFinder.setName("Patterns of " + selectedColumn.getName());
                                            patternFinder.addInputColumn(sourceColumn);
                                        }

                                        return lastPage;
                                    }
                                };
                            }
                        };
                    }
                };
            }
        };
    }

    @Override
    public Integer getPageCount() {
        return _pageCount;
    }

    @Override
    public AnalysisJobBuilder createJob() {
        return _analysisJobBuilder;
    }
}
