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

import javax.swing.Box;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.SourceColumnChangeListener;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.jdesktop.swingx.VerticalLayout;

public final class SourceColumnsPanel extends DCPanel implements SourceColumnChangeListener {

	private static final long serialVersionUID = 1L;

	private final List<ColumnListTable> _sourceColumnTables = new ArrayList<ColumnListTable>();
	private final DCLabel _hintLabel;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final AnalyzerBeansConfiguration _configuration;
	private final MaxRowsFilterShortcutPanel _maxRowsFilterShortcutPanel;

	public SourceColumnsPanel(AnalysisJobBuilder analysisJobBuilder, AnalyzerBeansConfiguration configuration) {
		super(ImageManager.getInstance().getImage("images/window/source-tab-background.png"), 95, 95,
				WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		_analysisJobBuilder = analysisJobBuilder;
		_configuration = configuration;
		_maxRowsFilterShortcutPanel = new MaxRowsFilterShortcutPanel(_analysisJobBuilder);
		_maxRowsFilterShortcutPanel.setVisible(false);

		_hintLabel = DCLabel.darkMultiLine("Please select the source columns of your job in the tree to the left.\n\n"
				+ "Source columns define where to retrieve the input of your analysis.");
		_hintLabel.setFont(WidgetUtils.FONT_TABLE_HEADER);
		_hintLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
		_hintLabel.setIconTextGap(20);
		_hintLabel.setIcon(ImageManager.getInstance().getImageIcon("images/model/column.png"));

		setOpaque(true);
		_analysisJobBuilder.getSourceColumnListeners().add(this);
		setBorder(WidgetUtils.BORDER_EMPTY);
		setLayout(new VerticalLayout(4));

		add(_hintLabel);
		add(_maxRowsFilterShortcutPanel);
		add(Box.createVerticalStrut(10));

		List<MetaModelInputColumn> sourceColumns = analysisJobBuilder.getSourceColumns();
		for (InputColumn<?> column : sourceColumns) {
			onAdd(column);
		}
	}

	@Override
	public void onAdd(InputColumn<?> sourceColumn) {
		_hintLabel.setVisible(false);
		_maxRowsFilterShortcutPanel.setVisible(true);

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

			if (_analysisJobBuilder.getSourceColumns().isEmpty()) {
				_hintLabel.setVisible(true);
				_maxRowsFilterShortcutPanel.setVisible(false);
			}

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
			sourceColumnTable = new ColumnListTable(table, _configuration, _analysisJobBuilder, true);
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
	
	public MaxRowsFilterShortcutPanel getMaxRowsFilterShortcutPanel() {
		return _maxRowsFilterShortcutPanel;
	}
}
