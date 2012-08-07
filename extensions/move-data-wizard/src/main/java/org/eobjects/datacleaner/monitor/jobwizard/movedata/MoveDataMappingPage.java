/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.jobwizard.movedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.beans.filter.NullCheckFilter;
import org.eobjects.analyzer.beans.filter.NullCheckFilter.NullCheckCategory;
import org.eobjects.analyzer.beans.transform.TableLookupTransformer;
import org.eobjects.analyzer.beans.writers.InsertIntoTableAnalyzer;
import org.eobjects.analyzer.beans.writers.UpdateTableAnalyzer;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardPageController;
import org.eobjects.datacleaner.monitor.jobwizard.common.AbstractFreemarkerWizardPage;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Predicate;

/**
 * Page responsible for mapping of source and target columns.
 */
class MoveDataMappingPage extends AbstractFreemarkerWizardPage {

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final Datastore _targetDatastore;
    private final Table _targetTable;
    private final Table _sourceTable;

    public MoveDataMappingPage(AnalysisJobBuilder analysisJobBuilder, Table sourceTable,
            Datastore targetDatastore, Table targetTable) {
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
        return "MoveDataMappingPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("sourceColumns", _sourceTable.getColumns());
        map.put("targetColumns", _targetTable.getColumns());
        return map;
    }

    @Override
    public JobWizardPageController nextPageController(Map<String, List<String>> formParameters) {
        final List<ColumnMapping> mappings = new ArrayList<ColumnMapping>();

        final Column[] sourceColumns = _sourceTable.getColumns();
        for (int i = 0; i < sourceColumns.length; i++) {
            List<String> formParameter = formParameters.get("mapping_" + i);
            if (formParameter != null && !formParameter.isEmpty()) {
                final String mapping = formParameter.get(0);
                if (!StringUtils.isNullOrEmpty(mapping)) {
                    final Column sourceColumn = sourceColumns[i];
                    final Column targetColumn = _targetTable.getColumnByName(mapping);

                    final boolean isId = getBoolean(formParameters, "id_" + i);

                    mappings.add(new ColumnMapping(sourceColumn, targetColumn, isId));
                }
            }
        }

        if (mappings.isEmpty()) {
            throw new IllegalStateException("No columns mapped!");
        }

        final boolean useLookupAndUpdate = getBoolean(formParameters, "updatePrimaryKeys");

        createAnalyzers(useLookupAndUpdate, mappings);

        // finished
        return null;
    }

    private void createAnalyzers(final boolean useLookupAndUpdate, final List<ColumnMapping> mappings) {
        for (final ColumnMapping mapping : mappings) {
            _analysisJobBuilder.addSourceColumn(mapping.getSourceColumn());
        }

        final List<ColumnMapping> idMappings = CollectionUtils.filter(mappings, new Predicate<ColumnMapping>() {
            @Override
            public Boolean eval(ColumnMapping mapping) {
                return mapping.isId();
            }
        });

        final AnalyzerJobBuilder<InsertIntoTableAnalyzer> insert = buildInsert(mappings);

        if (useLookupAndUpdate && !idMappings.isEmpty()) {
            final TransformerJobBuilder<TableLookupTransformer> tableLookup = buildLookup(idMappings);
            final AnalyzerJobBuilder<UpdateTableAnalyzer> update = buildUpdate(idMappings, mappings);

            // bind UPDATE and INSERT to outcome of a null check on the looked
            // up fields
            final FilterJobBuilder<NullCheckFilter, NullCheckCategory> nullCheck = _analysisJobBuilder
                    .addFilter(NullCheckFilter.class);
            nullCheck.addInputColumns(tableLookup.getOutputColumns());
            update.setRequirement(nullCheck, NullCheckCategory.NOT_NULL);
            insert.setRequirement(nullCheck, NullCheckCategory.NULL);
        }
    }

    private AnalyzerJobBuilder<UpdateTableAnalyzer> buildUpdate(final List<ColumnMapping> idMappings,
            final List<ColumnMapping> mappings) {

        // set the ID conditions of the UPDATE ... WHERE clause
        final InputColumn<?>[] conditionValues = new InputColumn[idMappings.size()];
        final String[] conditionColumns = new String[idMappings.size()];
        for (int i = 0; i < idMappings.size(); i++) {
            final ColumnMapping idMapping = idMappings.get(i);
            conditionValues[i] = _analysisJobBuilder.getSourceColumnByName(idMapping.getSourceColumn()
                    .getQualifiedLabel());
            conditionColumns[i] = idMapping.getTargetColumn().getName();
        }

        // UPDATE those colums which are not IDs
        final InputColumn<?>[] values = new InputColumn[mappings.size() - idMappings.size()];
        final String[] columnNames = new String[mappings.size() - idMappings.size()];
        int i = 0;
        for (ColumnMapping mapping : mappings) {
            if (!idMappings.contains(mapping)) {
                values[i] = _analysisJobBuilder.getSourceColumnByName(mapping.getSourceColumn().getQualifiedLabel());
                columnNames[i] = mapping.getTargetColumn().getName();
                i++;
            }
        }

        final AnalyzerJobBuilder<UpdateTableAnalyzer> update = _analysisJobBuilder
                .addAnalyzer(UpdateTableAnalyzer.class);
        update.setConfiguredProperty("Datastore", _targetDatastore);
        update.setConfiguredProperty("Schema name", _targetTable.getSchema().getName());
        update.setConfiguredProperty("Table name", _targetTable.getName());
        update.setConfiguredProperty("Column names", columnNames);
        update.setConfiguredProperty("Values", values);
        update.setConfiguredProperty("Condition column names", conditionColumns);
        update.setConfiguredProperty("Condition values", conditionValues);
        return update;
    }

    private TransformerJobBuilder<TableLookupTransformer> buildLookup(final List<ColumnMapping> idMappings) {
        final InputColumn<?>[] conditionValues = new InputColumn[idMappings.size()];
        final String[] conditionColumns = new String[idMappings.size()];
        for (int i = 0; i < idMappings.size(); i++) {
            final ColumnMapping idMapping = idMappings.get(i);
            conditionValues[i] = _analysisJobBuilder.getSourceColumnByName(idMapping.getSourceColumn()
                    .getQualifiedLabel());
            conditionColumns[i] = idMapping.getTargetColumn().getName();
        }

        // use the first (ANY) column as output of the lookup
        final String[] outputColumns = new String[1];
        outputColumns[0] = idMappings.get(0).getTargetColumn().getName();

        final TransformerJobBuilder<TableLookupTransformer> tableLookup = _analysisJobBuilder
                .addTransformer(TableLookupTransformer.class);
        tableLookup.setConfiguredProperty("Datastore", _targetDatastore);
        tableLookup.setConfiguredProperty("Schema name", _targetTable.getSchema().getName());
        tableLookup.setConfiguredProperty("Table name", _targetTable.getName());
        tableLookup.setConfiguredProperty("Condition columns", conditionColumns);
        tableLookup.setConfiguredProperty("Condition values", conditionValues);
        tableLookup.setConfiguredProperty("Output columns", outputColumns);
        
        tableLookup.getOutputColumns().get(0).setName("lookup_output");
        
        return tableLookup;
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
        return insert;
    }

    private boolean getBoolean(Map<String, List<String>> formParameters, String key) {
        List<String> result = formParameters.get(key);
        if (result == null || result.isEmpty()) {
            return false;
        }

        final String booleanString = result.get(0);
        return "true".equals(booleanString);
    }

}
