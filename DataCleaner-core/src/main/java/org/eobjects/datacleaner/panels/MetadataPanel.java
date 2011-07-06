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

import java.awt.BorderLayout;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.border.CompoundBorder;
import javax.swing.table.DefaultTableModel;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.SourceColumnChangeListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.Alignment;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.eobjects.metamodel.schema.Column;

public class MetadataPanel extends DCPanel implements SourceColumnChangeListener {

	private static final long serialVersionUID = 1L;

	private static final String[] columnNames = new String[] { "Table", "Column", "Type", "Native type", "Size",
			"Nullable?", "Indexed?" };

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final DCTable _table;;

	public MetadataPanel(AnalysisJobBuilder analysisJobBuilder) {
		super(ImageManager.getInstance().getImage("images/window/metadata-tab-background.png"), 95, 95,
				WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		_analysisJobBuilder = analysisJobBuilder;
		_analysisJobBuilder.getSourceColumnListeners().add(this);

		_table = new DCTable(columnNames);
		_table.setAlignment(5, Alignment.CENTER);
		_table.setAlignment(6, Alignment.CENTER);

		setLayout(new BorderLayout());
		DCPanel tablePanel = _table.toPanel();
		tablePanel.setBorder(new CompoundBorder(WidgetUtils.BORDER_SHADOW, WidgetUtils.BORDER_THIN));
		add(tablePanel, BorderLayout.CENTER);
		updateComponents();
	}

	private void updateComponents() {
		final SortedSet<InputColumn<?>> sourceColumns = new TreeSet<InputColumn<?>>(_analysisJobBuilder.getSourceColumns());

		final Icon validIcon = ImageManager.getInstance().getImageIcon("images/status/valid.png", IconUtils.ICON_SIZE_SMALL);

		DefaultTableModel model = new DefaultTableModel(columnNames, sourceColumns.size());

		int i = 0;
		for (InputColumn<?> inputColumn : sourceColumns) {
			Column column = inputColumn.getPhysicalColumn();
			model.setValueAt(column.getTable().getName(), i, 0);
			model.setValueAt(column.getName(), i, 1);
			model.setValueAt(inputColumn.getDataTypeFamily(), i, 2);
			model.setValueAt(column.getNativeType(), i, 3);
			model.setValueAt(column.getColumnSize(), i, 4);

			Boolean nullable = column.isNullable();
			if (nullable != null && nullable.booleanValue()) {
				model.setValueAt(validIcon, i, 5);
			} else {
				model.setValueAt("", i, 5);
			}

			boolean indexed = column.isIndexed();
			if (indexed) {
				model.setValueAt(validIcon, i, 6);
			} else {
				model.setValueAt("", i, 6);
			}

			i++;
		}

		_table.setModel(model);
	}

	@Override
	public void onAdd(InputColumn<?> sourceColumn) {
		updateComponents();
	}

	@Override
	public void onRemove(InputColumn<?> sourceColumn) {
		updateComponents();
	}

}
