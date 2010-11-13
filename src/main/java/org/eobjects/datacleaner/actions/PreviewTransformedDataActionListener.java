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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MetaModelInputRow;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.data.TransformedInputRow;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.lifecycle.CloseCallback;
import org.eobjects.analyzer.lifecycle.InitializeCallback;
import org.eobjects.analyzer.lifecycle.LifeCycleState;
import org.eobjects.datacleaner.windows.DataSetWindow;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.MetaModelHelper;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public final class PreviewTransformedDataActionListener implements ActionListener {

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final TransformerJobBuilder<?> _transformerJobBuilder;

	public PreviewTransformedDataActionListener(AnalysisJobBuilder analysisJobBuilder,
			TransformerJobBuilder<?> transformerJobBuilder) {
		_analysisJobBuilder = analysisJobBuilder;
		_transformerJobBuilder = transformerJobBuilder;
	}

	// TODO: This method was basically just hacked together. Most of it is based
	// on code that also exists in RowProcessingPublisher in AnalyzerBeans, but
	// it's not a complete match and I was too lazy to refactor the
	// RowProcessingPublisher :)
	@Override
	public void actionPerformed(ActionEvent e) {
		List<TransformerJobBuilder<?>> transformerJobs = new ArrayList<TransformerJobBuilder<?>>();
		transformerJobs.add(_transformerJobBuilder);

		List<Column> physicalColumns = new ArrayList<Column>();

		for (InputColumn<?> inputColumn : _transformerJobBuilder.getInputColumns()) {
			buildInputChain(inputColumn, physicalColumns, transformerJobs);
		}

		Collections.sort(physicalColumns);

		// reversing the list of transformers should place them in the correct
		// order
		Collections.reverse(transformerJobs);

		Table[] tables = MetaModelHelper.getTables(physicalColumns);

		if (tables.length != 1) {
			throw new IllegalStateException("Transformer is expected to contain columns originating from 1 table, found "
					+ tables.length);
		}

		Table table = tables[0];

		DataContext dc = _analysisJobBuilder.getDataContextProvider().getDataContext();
		Query q = dc.query().from(table).select(physicalColumns.toArray(new Column[physicalColumns.size()])).toQuery();
		q.setMaxRows(400);

		List<InputRow> result = new ArrayList<InputRow>();

		// initialize
		for (TransformerJobBuilder<?> tjb : transformerJobs) {
			new InitializeCallback().onEvent(LifeCycleState.INITIALIZE, tjb.getConfigurableBean(), tjb.getDescriptor());
		}

		DataSet dataSet = dc.executeQuery(q);
		int rowNumber = 0;
		while (dataSet.next()) {
			Row row = dataSet.getRow();
			InputRow inputRow = new MetaModelInputRow(rowNumber, row);

			for (TransformerJobBuilder<?> tjb : transformerJobs) {
				List<MutableInputColumn<?>> outputColumns = tjb.getOutputColumns();
				Object[] output = tjb.getConfigurableBean().transform(inputRow);

				assert outputColumns.size() == output.length;

				Map<InputColumn<?>, Object> newValues = new HashMap<InputColumn<?>, Object>();
				for (int i = 0; i < output.length; i++) {
					newValues.put(outputColumns.get(i), output[i]);
				}
				inputRow = new TransformedInputRow(inputRow, newValues);
			}

			result.add(inputRow);
			rowNumber++;
		}

		// close
		for (TransformerJobBuilder<?> tjb : transformerJobs) {
			new CloseCallback().onEvent(LifeCycleState.CLOSE, tjb.getConfigurableBean(), tjb.getDescriptor());
		}

		String[] columnNames = new String[_transformerJobBuilder.getInputColumns().size()
				+ _transformerJobBuilder.getOutputColumns().size()];
		int column = 0;
		for (InputColumn<?> col : _transformerJobBuilder.getInputColumns()) {
			columnNames[column] = col.getName();
			column++;
		}
		for (InputColumn<?> col : _transformerJobBuilder.getOutputColumns()) {
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
			for (InputColumn<?> col : _transformerJobBuilder.getOutputColumns()) {
				tableModel.setValueAt(inputRow.getValue(col), row, column);
				column++;
			}

			row++;
		}

		new DataSetWindow("Preview of transformed dataset", tableModel).setVisible(true);
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

}
