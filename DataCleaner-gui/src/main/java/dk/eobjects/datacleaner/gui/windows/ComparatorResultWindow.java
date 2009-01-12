/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.windows;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import dk.eobjects.datacleaner.comparator.ColumnComparator;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.widgets.MatrixTable;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.metamodel.schema.Column;

public class ComparatorResultWindow extends ResultWindow {

	public ComparatorResultWindow() {
		super("Comparison results");
	}

	@Override
	public ImageIcon getFrameIcon() {
		return GuiHelper.getImageIcon("images/task_result.png");
	}

	public void addResults(ColumnComparator columnComparator) {
		Column[] columns = columnComparator.getColumns();
		String title = getTitle(columns);

		JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();
		JLabel label = new JLabel("Comparison results for columns: " + title);
		label.setFont(GuiHelper.FONT_HEADER);
		taskPaneContainer.add(label);
		JPanel panel = GuiHelper.createPanel().applyBorderLayout().toComponent();
		addTab(title, GuiHelper
				.getImageIcon("images/toolbar_preview_data.png"),
				new JScrollPane(panel));

		for (int i = 0; i < columns.length; i++) {
			Column column = columns[i];
			JXTaskPane taskPane = new JXTaskPane();
			taskPane.setTitle("Found in " + column.getName());
			IMatrix matrix = columnComparator.getResultForColumn(column);
			MatrixTable table = new MatrixTable(matrix, null);
			taskPane.add(table.toPanel());
			taskPaneContainer.add(taskPane);
		}
		panel.add(taskPaneContainer, BorderLayout.CENTER);
	}

	private String getTitle(Column[] columns) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < columns.length; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(columns[i].getName());
		}
		return sb.toString();
	}
}