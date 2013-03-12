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
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;

/**
 * Page that allows the user to specify how source data updates is handled by
 * the job.
 */
public class SelectUpdateStrategyWizardPage extends
		AbstractFreemarkerWizardPage {

	private final Datastore _targetDatastore;
	private final Table _targetTable;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final AnalyzerJobBuilder<InsertIntoTableAnalyzer> _insert;
	private final List<ColumnMapping> _columnMappings;

	public SelectUpdateStrategyWizardPage(AnalysisJobBuilder jobBuilder,
			Datastore targetDatastore, Table targetTable,
			AnalyzerJobBuilder<InsertIntoTableAnalyzer> insert,
			List<ColumnMapping> columnMappings) {
		_analysisJobBuilder = jobBuilder;
		_targetDatastore = targetDatastore;
		_targetTable = targetTable;
		_insert = insert;
		_columnMappings = columnMappings;
	}

	@Override
	public Integer getPageIndex() {
		return 4;
	}

	@Override
	protected String getTemplateFilename() {
		return "SelectUpdateStrategyWizardPage.html";
	}

	@Override
	protected Map<String, Object> getFormModel() {
		final List<String> columnNames = new ArrayList<String>();
		for (final ColumnMapping columnMapping : _columnMappings) {
			final Column sourceColumn = columnMapping.getSourceColumn();
			final String columnName = sourceColumn.getName();
			if (sourceColumn.isPrimaryKey()) {
				// add primary keys as the first column
				columnNames.add(0, columnName);
			} else {
				columnNames.add(columnName);
			}
		}

		final Map<String, Object> map = new HashMap<String, Object>();
		map.put("columnNames", columnNames);
		return map;
	}

	@Override
	public WizardPageController nextPageController(
			Map<String, List<String>> formParameters)
			throws DCUserInputException {

		String updateStrategy = formParameters.get("update_strategy").get(0);

		if ("truncate".equals(updateStrategy)) {
			setUpdateStrategyTruncate();
		} else if ("lookup_and_update".equals(updateStrategy)) {
			final String primaryKeyColumnName = formParameters.get(
					"lookup_and_update_column_select").get(0);
			for (final ColumnMapping columnMapping : _columnMappings) {
				final Column sourceColumn = columnMapping.getSourceColumn();
				final String columnName = sourceColumn.getName();
				if (columnName.equals(primaryKeyColumnName)) {
					setUpdateStrategyPrimaryKeyLookup(columnMapping);
					break;
				}
			}
		} else if ("no_strategy".equals(updateStrategy)) {
			// do nothing
		} else {
			throw new IllegalStateException(
					"Unexpected update strategy value: " + updateStrategy);
		}

		return null;
	}

	private void setUpdateStrategyTruncate() {
		_insert.setConfiguredProperty("Truncate table", true);
	}

	private void setUpdateStrategyPrimaryKeyLookup(
			ColumnMapping primaryKeyColumnMapping) {
		final TransformerJobBuilder<TableLookupTransformer> tableLookup = buildLookup(primaryKeyColumnMapping);
		final AnalyzerJobBuilder<UpdateTableAnalyzer> update = buildUpdate(
				primaryKeyColumnMapping, _columnMappings);

		// bind UPDATE and INSERT to outcome of a null check on the looked
		// up fields
		final FilterJobBuilder<NullCheckFilter, NullCheckCategory> nullCheck = _analysisJobBuilder
				.addFilter(NullCheckFilter.class);
		nullCheck.addInputColumns(tableLookup.getOutputColumns());
		update.setRequirement(nullCheck, NullCheckCategory.NOT_NULL);
		_insert.setRequirement(nullCheck, NullCheckCategory.NULL);
	}

	private AnalyzerJobBuilder<UpdateTableAnalyzer> buildUpdate(
			final ColumnMapping primaryKeyColumnMapping,
			final List<ColumnMapping> mappings) {

		// set the ID conditions of the UPDATE ... WHERE clause
		final InputColumn<?>[] conditionValues = new InputColumn[1];
		final String[] conditionColumns = new String[1];
		conditionValues[0] = _analysisJobBuilder
				.getSourceColumnByName(primaryKeyColumnMapping
						.getSourceColumn().getQualifiedLabel());
		conditionColumns[0] = primaryKeyColumnMapping.getTargetColumn()
				.getName();

		// UPDATE those colums which are not IDs
		final InputColumn<?>[] values = new InputColumn[mappings.size() - 1];
		final String[] columnNames = new String[mappings.size() - 1];
		int i = 0;
		for (ColumnMapping mapping : mappings) {
			if (!primaryKeyColumnMapping.equals(mapping)) {
				values[i] = _analysisJobBuilder.getSourceColumnByName(mapping
						.getSourceColumn().getQualifiedLabel());
				columnNames[i] = mapping.getTargetColumn().getName();
				i++;
			}
		}

		final AnalyzerJobBuilder<UpdateTableAnalyzer> update = _analysisJobBuilder
				.addAnalyzer(UpdateTableAnalyzer.class);
		update.setConfiguredProperty("Datastore", _targetDatastore);
		update.setConfiguredProperty("Schema name", _targetTable.getSchema()
				.getName());
		update.setConfiguredProperty("Table name", _targetTable.getName());
		update.setConfiguredProperty("Column names", columnNames);
		update.setConfiguredProperty("Values", values);
		update.setConfiguredProperty("Condition column names", conditionColumns);
		update.setConfiguredProperty("Condition values", conditionValues);

		// set an empty array, or else JaxbJobWriter will fail (Ticket #900)
		update.setConfiguredProperty("Additional error log values",
				new InputColumn[0]);
		return update;
	}

	private TransformerJobBuilder<TableLookupTransformer> buildLookup(
			final ColumnMapping primaryKeyColumnMapping) {
		final InputColumn<?>[] conditionValues = new InputColumn[1];
		final String[] conditionColumns = new String[1];
		conditionValues[0] = _analysisJobBuilder
				.getSourceColumnByName(primaryKeyColumnMapping
						.getSourceColumn().getQualifiedLabel());
		conditionColumns[0] = primaryKeyColumnMapping.getTargetColumn()
				.getName();

		// use the target (ANY) column as output of the lookup
		final String[] outputColumns = new String[1];
		outputColumns[0] = primaryKeyColumnMapping.getTargetColumn().getName();

		final TransformerJobBuilder<TableLookupTransformer> tableLookup = _analysisJobBuilder
				.addTransformer(TableLookupTransformer.class);
		tableLookup.setConfiguredProperty("Datastore", _targetDatastore);
		tableLookup.setConfiguredProperty("Schema name", _targetTable
				.getSchema().getName());
		tableLookup.setConfiguredProperty("Table name", _targetTable.getName());
		tableLookup
				.setConfiguredProperty("Condition columns", conditionColumns);
		tableLookup.setConfiguredProperty("Condition values", conditionValues);
		tableLookup.setConfiguredProperty("Output columns", outputColumns);

		tableLookup.getOutputColumns().get(0).setName("lookup_output");

		return tableLookup;
	}
}
