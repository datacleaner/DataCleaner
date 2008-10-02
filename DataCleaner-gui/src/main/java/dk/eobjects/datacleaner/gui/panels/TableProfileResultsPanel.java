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
package dk.eobjects.datacleaner.gui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.widgets.MatrixTable;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.profiler.ProfilerHelper;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Table;

public class TableProfileResultsPanel extends JPanel {

	private static final long serialVersionUID = 7504166314384076977L;

	public TableProfileResultsPanel(DataContext dataContext, Table table,
			List<IProfileResult> results) {
		super();
		setLayout(new BorderLayout());

		JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();

		JLabel label = new JLabel("Profile results for " + table.getName());
		label.setFont(GuiHelper.FONT_HEADER);
		taskPaneContainer.add(label);

		// Loop through every profile for this table
		Map<IProfileDescriptor, List<IProfileResult>> profiles = ProfilerHelper
				.getProfileResultsByProfileDescriptor(results);
		for (Entry<IProfileDescriptor, List<IProfileResult>> entry : profiles
				.entrySet()) {
			IProfileDescriptor profileDescriptor = entry.getKey();
			List<IProfileResult> profileResults = entry.getValue();
			String displayName = profileDescriptor.getDisplayName();
			int matrixNumber = 0;

			for (Iterator<IProfileResult> it = profileResults.iterator(); it
					.hasNext();) {
				IProfileResult profileResult = it.next();
				Exception error = profileResult.getError();
				if (error != null) {
					JXTaskPane taskPane = new JXTaskPane();
					taskPane.setIcon(GuiHelper.getImageIcon(profileDescriptor
							.getIconPath()));
					taskPane.setAnimated(false);
					taskPane.setCollapsed(false);
					taskPane.setTitle(displayName);
					JLabel errorLabel = new JLabel(error.getMessage());
					errorLabel.setFont(errorLabel.getFont().deriveFont(
							Font.BOLD));
					taskPane.add(errorLabel);
					taskPane.add(new JLabel("See the log for details."));
					taskPaneContainer.add(taskPane);
				} else {
					IMatrix[] matrices = profileResult.getMatrices();
					for (int j = 0; j < matrices.length; j++) {
						matrixNumber++;
						JXTaskPane taskPane = new JXTaskPane();
						taskPane.setIcon(GuiHelper
								.getImageIcon(profileDescriptor.getIconPath()));
						taskPane.setAnimated(false);
						taskPane.setCollapsed(false);
						if (matrices.length > 1) {
							taskPane.setSpecial(true);
						}

						IMatrix matrix = matrices[j];
						String[] columnNames = matrix.getColumnNames();
						if (matrices.length > 1) {
							if (columnNames.length == 1) {
								taskPane.setTitle(displayName + " ("
										+ columnNames[0] + ")");
							} else {
								taskPane.setTitle(displayName + " #"
										+ matrixNumber);
							}
						} else {
							taskPane.setTitle(displayName);
						}

						MatrixTable matrixTable = new MatrixTable(matrix,
								dataContext);
						JPanel tableContainerPanel = GuiHelper.createPanel()
								.applyLayout(
										new FlowLayout(FlowLayout.LEFT, 0, 0))
								.toComponent();
						JPanel matrixTablePanel = matrixTable.toPanel();
						int numColumns = matrixTable.getColumnCount();
						if (numColumns < 8) {
							Dimension d = new Dimension();
							Dimension preferredSize = matrixTablePanel
									.getPreferredSize();
							if (numColumns < 4) {
								d.width = numColumns * 200;
							} else {
								d.width = numColumns * 100;
							}
							d.height = preferredSize.height;
							matrixTablePanel.setSize(d);
							matrixTablePanel.setPreferredSize(d);
						}
						tableContainerPanel.add(matrixTablePanel);
						taskPane.add(tableContainerPanel);
						taskPaneContainer.add(taskPane);
					}
				}
			}
		}
		add(taskPaneContainer, BorderLayout.CENTER);
	}
}