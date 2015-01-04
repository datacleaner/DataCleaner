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
package org.datacleaner.monitor.jobwizard.movedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.beans.writers.InsertIntoTableAnalyzer;
import org.datacleaner.connection.Datastore;
import org.datacleaner.data.InputColumn;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerJobBuilder;
import org.datacleaner.util.StringUtils;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;
import org.datacleaner.monitor.wizard.job.DataCleanerJobWizardSession;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;

/**
 * Page responsible for mapping of source and target columns.
 */
class ColumnMappingPage extends AbstractFreemarkerWizardPage {

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final Datastore _targetDatastore;
    private final Table _targetTable;
    private final Table _sourceTable;
    private final DataCleanerJobWizardSession _session;

    public ColumnMappingPage(DataCleanerJobWizardSession session, AnalysisJobBuilder analysisJobBuilder,
            Table sourceTable, Datastore targetDatastore, Table targetTable) {
        _session = session;
        _analysisJobBuilder = analysisJobBuilder;
        _sourceTable = sourceTable;
        _targetDatastore = targetDatastore;
        _targetTable = targetTable;
    }

    @Override
    public Integer getPageIndex() {
        return 3;
    }

    @Override
    protected String getTemplateFilename() {
        return "ColumnMappingPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("sourceColumns", _sourceTable.getColumns());
        map.put("targetColumns", _targetTable.getColumns());
        return map;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters) {
        final List<ColumnMapping> mappings = new ArrayList<ColumnMapping>();

        final Column[] sourceColumns = _sourceTable.getColumns();
        for (int i = 0; i < sourceColumns.length; i++) {
            List<String> formParameter = formParameters.get("mapping_" + i);
            if (formParameter != null && !formParameter.isEmpty()) {
                final String mapping = formParameter.get(0);
                if (!StringUtils.isNullOrEmpty(mapping)) {
                    final Column sourceColumn = sourceColumns[i];
                    final Column targetColumn = _targetTable.getColumnByName(mapping);

                    mappings.add(new ColumnMapping(sourceColumn, targetColumn));
                }
            }
        }

        if (mappings.isEmpty()) {
            throw new IllegalStateException("No columns mapped!");
        }

        AnalyzerJobBuilder<InsertIntoTableAnalyzer> insert = createAnalyzer(mappings);

        return new SelectUpdateStrategyWizardPage(_session, _analysisJobBuilder, _targetDatastore, _targetTable,
                insert, mappings);
    }

    private AnalyzerJobBuilder<InsertIntoTableAnalyzer> createAnalyzer(final List<ColumnMapping> mappings) {
        for (final ColumnMapping mapping : mappings) {
            _analysisJobBuilder.addSourceColumn(mapping.getSourceColumn());
        }

        final AnalyzerJobBuilder<InsertIntoTableAnalyzer> insert = buildInsert(mappings);
        return insert;
    }

    private AnalyzerJobBuilder<InsertIntoTableAnalyzer> buildInsert(final List<ColumnMapping> mappings) {
        final InputColumn<?>[] values = new InputColumn[mappings.size()];
        final String[] columnNames = new String[mappings.size()];

        for (int i = 0; i < mappings.size(); i++) {
            final ColumnMapping mapping = mappings.get(i);
            values[i] = _analysisJobBuilder.getSourceColumnByName(mapping.getSourceColumn().getQualifiedLabel());
            columnNames[i] = mapping.getTargetColumn().getName();
        }

        final AnalyzerJobBuilder<InsertIntoTableAnalyzer> insert = _analysisJobBuilder
                .addAnalyzer(InsertIntoTableAnalyzer.class);
        insert.setConfiguredProperty("Datastore", _targetDatastore);
        insert.setConfiguredProperty("Schema name", _targetTable.getSchema().getName());
        insert.setConfiguredProperty("Table name", _targetTable.getName());
        insert.setConfiguredProperty("Column names", columnNames);
        insert.setConfiguredProperty("Values", values);

        // set an empty array, or else JaxbJobWriter will fail (Ticket #900)
        insert.setConfiguredProperty("Additional error log values", new InputColumn[0]);
        return insert;
    }
}
