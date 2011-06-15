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
package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MetaModelInputRow;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.data.TransformedInputRow;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.reference.ReferenceData;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPresenter;
import org.eobjects.datacleaner.user.DCConfiguration;
import org.eobjects.datacleaner.windows.DataSetWindow;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.MetaModelHelper;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.Row;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;

/**
 * ActionListener responsible for previewing transformed data in a
 * {@link DataSetWindow}.
 * 
 * @author Kasper Sørensen
 */
public final class PreviewTransformedDataActionListener implements ActionListener, Callable<TableModel> {

	private static final int DEFAULT_PREVIEW_ROWS = 400;

	private final TransformerJobBuilderPresenter _transformerJobBuilderPresenter;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final TransformerJobBuilder<?> _transformerJobBuilder;
	private final LifeCycleHelper _lifeCycleHelper;
	private final WindowContext _windowContext;

	public PreviewTransformedDataActionListener(WindowContext windowContext,
			TransformerJobBuilderPresenter transformerJobBuilderPresenter, AnalysisJobBuilder analysisJobBuilder,
			TransformerJobBuilder<?> transformerJobBuilder) {
		_windowContext = windowContext;
		_transformerJobBuilderPresenter = transformerJobBuilderPresenter;
		_analysisJobBuilder = analysisJobBuilder;
		_transformerJobBuilder = transformerJobBuilder;

		final DatastoreCatalog datastoreCatalog = DCConfiguration.get().getDatastoreCatalog();
		final ReferenceDataCatalog referenceDataCatalog = DCConfiguration.get().getReferenceDataCatalog();

		_lifeCycleHelper = new LifeCycleHelper(datastoreCatalog, referenceDataCatalog);
	}

	// TODO: This method was basically just hacked together. Most of it is based
	// on code that also exists in RowProcessingPublisher in AnalyzerBeans, but
	// it's not a complete match and I was too lazy to refactor the
	// RowProcessingPublisher :)
	@Override
	public void actionPerformed(ActionEvent e) {
		new DataSetWindow("Preview of transformed dataset", this, _windowContext).setVisible(true);
	}

	private void initialize(TransformerJobBuilder<?> tjb) {
		Object bean = tjb.getConfigurableBean();
		TransformerBeanDescriptor<?> descriptor = tjb.getDescriptor();
		_lifeCycleHelper.initialize(descriptor, bean);

		Set<ConfiguredPropertyDescriptor> referenceDataProperties = descriptor.getConfiguredPropertiesByType(
				ReferenceData.class, true);
		for (ConfiguredPropertyDescriptor configuredPropertyDescriptor : referenceDataProperties) {
			Object configuredProperty = tjb.getConfiguredProperty(configuredPropertyDescriptor);
			_lifeCycleHelper.initialize(configuredProperty);
		}
	}

	private void close(TransformerJobBuilder<?> tjb) {
		Object bean = tjb.getConfigurableBean();
		TransformerBeanDescriptor<?> descriptor = tjb.getDescriptor();
		_lifeCycleHelper.close(descriptor, bean);

		Set<ConfiguredPropertyDescriptor> referenceDataProperties = descriptor.getConfiguredPropertiesByType(
				ReferenceData.class, true);
		for (ConfiguredPropertyDescriptor configuredPropertyDescriptor : referenceDataProperties) {
			Object configuredProperty = tjb.getConfiguredProperty(configuredPropertyDescriptor);
			_lifeCycleHelper.close(configuredProperty);
		}
	}

	private void buildInputChain(InputColumn<?> inputColumn, List<Column> physicalColumns,
			List<TransformerJobBuilder<?>> transformerJobs) {
		if (inputColumn.isPhysicalColumn()) {
			physicalColumns.add(inputColumn.getPhysicalColumn());
		} else {
			TransformerJobBuilder<?> tjb = _analysisJobBuilder.getOriginatingTransformer(inputColumn);
			if (!transformerJobs.contains(tjb)) {
				transformerJobs.add(tjb);
				List<InputColumn<?>> tjbInputs = tjb.getInputColumns();
				for (InputColumn<?> tjbInput : tjbInputs) {
					buildInputChain(tjbInput, physicalColumns, transformerJobs);
				}
			}
		}
	}

	@Override
	public TableModel call() throws Exception {
		_transformerJobBuilderPresenter.applyPropertyValues();

		final List<TransformerJobBuilder<?>> transformerJobs = new ArrayList<TransformerJobBuilder<?>>();
		transformerJobs.add(_transformerJobBuilder);

		final List<Column> physicalColumns = new ArrayList<Column>();

		for (InputColumn<?> inputColumn : _transformerJobBuilder.getInputColumns()) {
			buildInputChain(inputColumn, physicalColumns, transformerJobs);
		}

		Collections.sort(physicalColumns);

		// reversing the list of transformers should place them in the correct
		// order
		Collections.reverse(transformerJobs);

		final Table[] tables = MetaModelHelper.getTables(physicalColumns);

		if (tables.length != 1) {
			throw new IllegalStateException("Transformer is expected to contain columns originating from 1 table, found "
					+ tables.length);
		}

		final Table table = tables[0];

		final DataContextProvider dataContextProvider = _analysisJobBuilder.getDataContextProvider();
		final DataContext dc = dataContextProvider.getDataContext();
		final Query q = dc.query().from(table).select(physicalColumns.toArray(new Column[physicalColumns.size()])).toQuery();
		q.setMaxRows(DEFAULT_PREVIEW_ROWS);

		for (TransformerJobBuilder<?> tjb : transformerJobs) {
			initialize(tjb);
		}

		// getting the output columns can be an expensive call, so we do it
		// upfront in stead of for each row.
		final Map<TransformerJobBuilder<?>, List<MutableInputColumn<?>>> outputColumns = new LinkedHashMap<TransformerJobBuilder<?>, List<MutableInputColumn<?>>>();
		for (TransformerJobBuilder<?> tjb : transformerJobs) {
			List<MutableInputColumn<?>> cols = tjb.getOutputColumns();
			outputColumns.put(tjb, cols);
		}

		final List<InputRow> result = new ArrayList<InputRow>();
		final DataSet dataSet = dc.executeQuery(q);
		int rowNumber = 0;
		while (dataSet.next()) {
			Row row = dataSet.getRow();
			InputRow inputRow = new MetaModelInputRow(rowNumber, row);

			TransformedInputRow resultRow;
			if (inputRow instanceof TransformedInputRow) {
				// re-use existing transformed input row.
				resultRow = (TransformedInputRow) inputRow;
			} else {
				resultRow = new TransformedInputRow(inputRow);
			}

			for (TransformerJobBuilder<?> tjb : transformerJobs) {
				List<MutableInputColumn<?>> cols = outputColumns.get(tjb);
				Object[] output = tjb.getConfigurableBean().transform(inputRow);

				assert cols.size() == output.length;

				for (int i = 0; i < output.length; i++) {
					resultRow.addValue(cols.get(i), output[i]);
				}
			}

			result.add(resultRow);
			rowNumber++;
		}

		// close
		for (TransformerJobBuilder<?> tjb : transformerJobs) {
			close(tjb);
		}

		List<MutableInputColumn<?>> ownOutputColumns = outputColumns.get(_transformerJobBuilder);
		String[] columnNames = new String[_transformerJobBuilder.getInputColumns().size() + ownOutputColumns.size()];
		int column = 0;
		for (InputColumn<?> col : _transformerJobBuilder.getInputColumns()) {
			columnNames[column] = col.getName();
			column++;
		}
		for (InputColumn<?> col : outputColumns.get(_transformerJobBuilder)) {
			columnNames[column] = col.getName();
			column++;
		}

		DefaultTableModel tableModel = new DefaultTableModel(columnNames, result.size());
		int row = 0;
		for (InputRow inputRow : result) {
			column = 0;
			for (InputColumn<?> col : _transformerJobBuilder.getInputColumns()) {
				tableModel.setValueAt(inputRow.getValue(col), row, column);
				column++;
			}
			for (InputColumn<?> col : ownOutputColumns) {
				tableModel.setValueAt(inputRow.getValue(col), row, column);
				column++;
			}

			row++;
		}
		return tableModel;
	}

}
