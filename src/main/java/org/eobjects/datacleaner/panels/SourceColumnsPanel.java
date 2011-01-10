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
package org.eobjects.datacleaner.panels;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.SourceColumnChangeListener;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.VerticalLayout;

import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;

public final class SourceColumnsPanel extends DCPanel implements SourceColumnChangeListener {

	private static final long serialVersionUID = 1L;

	private final List<ColumnListTable> _sourceColumnTables = new ArrayList<ColumnListTable>();
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final AnalyzerBeansConfiguration _configuration;

	public SourceColumnsPanel(AnalysisJobBuilder analysisJobBuilder, AnalyzerBeansConfiguration configuration) {
		super(ImageManager.getInstance().getImage("images/window/source-tab-background.png"), 95, 95,
				WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		setOpaque(true);
		_analysisJobBuilder = analysisJobBuilder;
		_configuration = configuration;
		_analysisJobBuilder.getSourceColumnListeners().add(this);
		setBorder(WidgetUtils.BORDER_EMPTY);
		setLayout(new VerticalLayout(4));
		
		List<MetaModelInputColumn> sourceColumns = analysisJobBuilder.getSourceColumns();
		for (InputColumn<?> column : sourceColumns) {
			onAdd(column);
		}
	}

	@Override
	public void onAdd(InputColumn<?> sourceColumn) {
		Column column = sourceColumn.getPhysicalColumn();
		Table table = column.getTable();

		ColumnListTable sourceColumnTable = getColumnListTable(table);
		sourceColumnTable.addColumn(sourceColumn);
	}

	@Override
	public void onRemove(InputColumn<?> sourceColumn) {
		Column column = sourceColumn.getPhysicalColumn();
		Table table = column.getTable();
		ColumnListTable sourceColumnTable = getColumnListTable(table);
		sourceColumnTable.removeColumn(sourceColumn);
		if (sourceColumnTable.getColumnCount() == 0) {
			this.remove(sourceColumnTable);
			_sourceColumnTables.remove(sourceColumnTable);

			// force UI update because sometimes the removed panel doesn't go
			// away automatically
			updateUI();
		}
	}

	private ColumnListTable getColumnListTable(Table table) {
		ColumnListTable sourceColumnTable = null;
		for (ColumnListTable sct : _sourceColumnTables) {
			if (sct.getTable() == table) {
				sourceColumnTable = sct;
				break;
			}
		}

		if (sourceColumnTable == null) {
			sourceColumnTable = new ColumnListTable(table, _configuration, _analysisJobBuilder);
			this.add(sourceColumnTable);
			_sourceColumnTables.add(sourceColumnTable);
			updateUI();
		}
		return sourceColumnTable;
	}

	@Override
	public void removeNotify() {
		_analysisJobBuilder.getSourceColumnListeners().remove(this);
		super.removeNotify();
	}
}
