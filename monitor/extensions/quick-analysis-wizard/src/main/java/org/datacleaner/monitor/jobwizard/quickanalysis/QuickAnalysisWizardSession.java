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
package org.datacleaner.monitor.jobwizard.quickanalysis;

import java.util.List;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.stringpattern.PatternFinderAnalyzer;
import org.datacleaner.beans.valuedist.ValueDistributionAnalyzer;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.monitor.server.wizard.JobNameWizardPage;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.common.SelectColumnsWizardPage;
import org.datacleaner.monitor.wizard.common.SelectTableWizardPage;
import org.datacleaner.monitor.wizard.job.DataCleanerJobWizardSession;
import org.datacleaner.monitor.wizard.job.JobWizardContext;

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
                
                // add primary key columns for reference
                final Column[] primaryKeys = selectedTable.getPrimaryKeys();
                if (primaryKeys != null && primaryKeys.length > 0) {
                    for (Column primaryKeyColumn : primaryKeys) {
                        _analysisJobBuilder.addSourceColumn(primaryKeyColumn);
                    }
                }
                
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

                        final QuickAnalysisBuilder builder = new QuickAnalysisBuilder(5, false, false);
                        builder.configureAnalysisJobBuilder(_analysisJobBuilder);

                        return new SelectValueDistributionColumnsPage(2, selectedTable) {
                            @Override
                            protected WizardPageController nextPageController(List<Column> selectedColumns) {
                                for (Column selectedColumn : selectedColumns) {
                                    _analysisJobBuilder.addSourceColumn(selectedColumn);
                                    final InputColumn<?> sourceColumn = _analysisJobBuilder
                                            .getSourceColumnByName(selectedColumn.getName());
                                    final AnalyzerComponentBuilder<ValueDistributionAnalyzer> valueDistribution = _analysisJobBuilder
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
                                            final AnalyzerComponentBuilder<PatternFinderAnalyzer> patternFinder = _analysisJobBuilder
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
