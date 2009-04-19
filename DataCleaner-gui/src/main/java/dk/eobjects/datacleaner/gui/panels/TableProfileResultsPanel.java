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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import dk.eobjects.datacleaner.export.HtmlResultExporter;
import dk.eobjects.datacleaner.export.IResultExporter;
import dk.eobjects.datacleaner.export.XmlResultExporter;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.widgets.MatrixTable;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.profiler.ProfilerHelper;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Table;
import dk.eobjects.metamodel.util.FileHelper;

public class TableProfileResultsPanel extends JPanel {

	private static final long serialVersionUID = 7504166314384076977L;
	private Table _table;
	private List<IProfileResult> _results;

	public TableProfileResultsPanel(DataContext dataContext, final Table table, final List<IProfileResult> results) {
		super();
		_table = table;
		_results = results;
		setLayout(new BorderLayout());

		JLabel headerLabel = new JLabel("Profiler results for " + table.getName());
		headerLabel.setFont(GuiHelper.FONT_HEADER);

		JButton exportButton = new JButton("Export", GuiHelper.getImageIcon("images/toolbar_save.png"));
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPopupMenu popupMenu = new JPopupMenu();
				JMenuItem xmlMenuItem = new JMenuItem("To XML format",GuiHelper.getImageIcon("images/file_xml.png"));
				xmlMenuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						exportToFile(new XmlResultExporter(), "xml");
					}
				});
				popupMenu.add(xmlMenuItem);

				JMenuItem htmlMenuItem = new JMenuItem("To HTML format", GuiHelper.getImageIcon("images/file_html.png"));
				htmlMenuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						exportToFile(new HtmlResultExporter(), "html");
					}
				});
				popupMenu.add(htmlMenuItem);
				JButton button = (JButton) e.getSource();
				popupMenu.show(button, 0, button.getHeight());
			}

		});

		JToolBar toolBar = GuiHelper.createToolBar();
		toolBar.add(headerLabel);
		toolBar.add(GuiHelper.createSeparator());
		toolBar.add(exportButton);

		add(toolBar, BorderLayout.NORTH);

		JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();
		// Loop through every profile for this table
		Map<IProfileDescriptor, List<IProfileResult>> profiles = ProfilerHelper
				.getProfileResultsByProfileDescriptor(results);
		for (Entry<IProfileDescriptor, List<IProfileResult>> entry : profiles.entrySet()) {
			IProfileDescriptor profileDescriptor = entry.getKey();
			List<IProfileResult> profileResults = entry.getValue();
			String displayName = profileDescriptor.getDisplayName();
			int matrixNumber = 0;

			for (Iterator<IProfileResult> it = profileResults.iterator(); it.hasNext();) {
				IProfileResult profileResult = it.next();
				Exception error = profileResult.getError();
				if (error != null) {
					JXTaskPane taskPane = new JXTaskPane();
					taskPane.setIcon(GuiHelper.getImageIcon(profileDescriptor.getIconPath()));
					taskPane.setAnimated(false);
					taskPane.setCollapsed(false);
					taskPane.setTitle(displayName);
					JLabel errorLabel = new JLabel(error.getMessage());
					errorLabel.setFont(errorLabel.getFont().deriveFont(Font.BOLD));
					taskPane.add(errorLabel);
					taskPane.add(new JLabel("See the log for details."));
					taskPaneContainer.add(taskPane);
				} else {
					IMatrix[] matrices = profileResult.getMatrices();
					for (int j = 0; j < matrices.length; j++) {
						matrixNumber++;
						JXTaskPane taskPane = new JXTaskPane();
						taskPane.setIcon(GuiHelper.getImageIcon(profileDescriptor.getIconPath()));
						taskPane.setAnimated(false);
						taskPane.setCollapsed(false);
						if (matrices.length > 1) {
							taskPane.setSpecial(true);
						}

						IMatrix matrix = matrices[j];
						String[] columnNames = matrix.getColumnNames();
						if (matrices.length > 1) {
							if (columnNames.length == 1) {
								taskPane.setTitle(displayName + " (" + columnNames[0] + ")");
							} else {
								taskPane.setTitle(displayName + " #" + matrixNumber);
							}
						} else {
							taskPane.setTitle(displayName);
						}

						MatrixTable matrixTable = new MatrixTable(matrix, dataContext);
						taskPane.add(matrixTable.toPanel());
						taskPaneContainer.add(taskPane);
					}
				}
			}
		}
		add(new JScrollPane(taskPaneContainer), BorderLayout.CENTER);
	}

	private void exportToFile(IResultExporter resultExporter, String fileExtension) {
		JFileChooser f = new JFileChooser();
		f.setSelectedFile(new File(_table.getName() + "-results." + fileExtension));
		if (JFileChooser.APPROVE_OPTION == f.showSaveDialog(TableProfileResultsPanel.this)) {
			boolean accepted = false;
			File file = f.getSelectedFile();
			if (file.exists()) {
				if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(TableProfileResultsPanel.this,
						"File already exists, overwrite?", "Overwrite", JOptionPane.YES_NO_OPTION)) {
					accepted = true;
				}
			} else {
				accepted = true;
			}
			if (accepted) {
				PrintWriter writer = new PrintWriter(FileHelper.getBufferedWriter(file));
				resultExporter.writeProfileResultHeader(writer);
				for (IProfileResult profileResult : _results) {
					resultExporter.writeProfileResult(_table, profileResult, writer);
				}
				resultExporter.writeProfileResultFooter(writer);
				writer.close();
			}
		}
	}
}