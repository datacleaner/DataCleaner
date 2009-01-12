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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.panels.ComparatorSelectionPanel;
import dk.eobjects.datacleaner.gui.widgets.OpenDatabaseButton;
import dk.eobjects.datacleaner.gui.widgets.OpenFileButton;
import dk.eobjects.datacleaner.gui.widgets.SchemaTree;
import dk.eobjects.datacleaner.gui.widgets.SchemaTreeMouseListener;

public class ComparatorWindow extends AbstractWindow {

	private DataContextSelection _leftDataContextSelection;
	private ColumnSelection _leftColumnSelection;
	private DataContextSelection _rightDataContextSelection;
	private ColumnSelection _rightColumnSelection;

	@Override
	public void disposeInternal() {
		if (_leftDataContextSelection != null) {
			_leftDataContextSelection.selectNothing();
			_leftDataContextSelection.deleteObserver(_leftColumnSelection);
		}
		if (_rightDataContextSelection != null) {
			_rightDataContextSelection.selectNothing();
			_rightDataContextSelection.deleteObserver(_rightColumnSelection);
		}
		_leftDataContextSelection = null;
		_leftColumnSelection = null;
		_rightDataContextSelection = null;
		_rightColumnSelection = null;
	}

	public ComparatorWindow() {
		super();
		_panel.setLayout(new BorderLayout());
		_leftDataContextSelection = new DataContextSelection();
		_leftColumnSelection = new ColumnSelection(_leftDataContextSelection);
		JPanel leftPanel = createSchemaPanel(_leftDataContextSelection,
				_leftColumnSelection);
		leftPanel.setName("left");
		_panel.add(leftPanel, BorderLayout.WEST);

		_rightDataContextSelection = new DataContextSelection();
		_rightColumnSelection = new ColumnSelection(_rightDataContextSelection);
		JPanel rightPanel = createSchemaPanel(_rightDataContextSelection,
				_rightColumnSelection);
		rightPanel.setName("right");
		_panel.add(rightPanel, BorderLayout.EAST);

		ComparatorSelectionPanel comparisonSelectionPanel = new ComparatorSelectionPanel(
				_leftDataContextSelection, _rightDataContextSelection,
				_leftColumnSelection, _rightColumnSelection);
		comparisonSelectionPanel.setName("center");
		_panel.add(comparisonSelectionPanel, BorderLayout.CENTER);

		GuiHelper.silentNotification("comparator-window");
	}

	private JPanel createSchemaPanel(DataContextSelection dataContextSelection,
			ColumnSelection dataSelection) {
		JPanel panel = GuiHelper.createPanel().applyBorderLayout()
				.toComponent();
		JToolBar toolbar = GuiHelper.createToolBar();
		toolbar.setRollover(true);
		toolbar.add(new OpenDatabaseButton(dataContextSelection));
		toolbar.add(new OpenFileButton(dataContextSelection));
		panel.add(toolbar, BorderLayout.NORTH);

		SchemaTree schemaTree = new SchemaTree(dataContextSelection);
		schemaTree.addMouseListener(new SchemaTreeMouseListener(schemaTree,
				dataContextSelection, dataSelection));
		JScrollPane scrollSchemaTree = new JScrollPane(schemaTree);
		panel.add(scrollSchemaTree, BorderLayout.CENTER);
		return panel;
	}

	@Override
	public ImageIcon getFrameIcon() {
		return GuiHelper.getImageIcon("images/window_compare.png");
	}

	@Override
	public String getTitle() {
		return "Comparator";
	}
}