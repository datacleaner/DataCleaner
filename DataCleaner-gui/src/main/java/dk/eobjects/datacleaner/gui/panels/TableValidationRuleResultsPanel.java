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
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.apache.commons.lang.ArrayUtils;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import dk.eobjects.datacleaner.export.XmlResultExporter;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.widgets.ColumnHighlighter;
import dk.eobjects.datacleaner.gui.widgets.DataTable;
import dk.eobjects.datacleaner.validator.IValidationRule;
import dk.eobjects.datacleaner.validator.IValidationRuleDescriptor;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;
import dk.eobjects.metamodel.util.FileHelper;

public class TableValidationRuleResultsPanel extends JPanel {

	private static final long serialVersionUID = 78962947201100801L;
	public static final Icon ICON_FAILED = GuiHelper
			.getImageIcon("images/validation_failed.png");
	public static final Icon ICON_SUCCESS = GuiHelper
			.getImageIcon("images/validation_success.png");
	public static final Icon ICON_ERROR = GuiHelper
			.getImageIcon("images/validation_error.png");

	public TableValidationRuleResultsPanel(final Table table,
			Column[] queriedColumns, final List<IValidationRuleResult> results) {
		setLayout(new BorderLayout());

		JLabel headerLabel = new JLabel("Validator results for "
				+ table.getName());
		headerLabel.setFont(GuiHelper.FONT_HEADER);

		JButton exportButton = new JButton("Export", GuiHelper
				.getImageIcon("images/toolbar_save.png"));
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser f = new JFileChooser();
				f.setSelectedFile(new File(table.getName() + "-results.xml"));
				if (JFileChooser.APPROVE_OPTION == f
						.showSaveDialog(TableValidationRuleResultsPanel.this)) {
					boolean accepted = false;
					File file = f.getSelectedFile();
					if (file.exists()) {
						if (JOptionPane.YES_OPTION == JOptionPane
								.showConfirmDialog(
										TableValidationRuleResultsPanel.this,
										"File already exists, overwrite?",
										"Overwrite", JOptionPane.YES_NO_OPTION)) {
							accepted = true;
						}
					} else {
						accepted = true;
					}
					if (accepted) {
						PrintWriter writer = new PrintWriter(FileHelper
								.getBufferedWriter(file));
						XmlResultExporter xmlResultExporter = new XmlResultExporter();
						xmlResultExporter
								.writeValidationRuleResultHeader(writer);
						for (IValidationRuleResult validationRuleResult : results) {
							xmlResultExporter.writeValidationRuleResult(table,
									validationRuleResult, writer);
						}
						xmlResultExporter
								.writeValidationRuleResultFooter(writer);
						writer.close();
					}
				}
			}
		});

		JToolBar toolBar = GuiHelper.createToolBar();
		toolBar.add(headerLabel);
		toolBar.add(GuiHelper.createSeparator());
		toolBar.add(exportButton);

		add(toolBar, BorderLayout.NORTH);

		JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();

		for (IValidationRuleResult result : results) {
			JXTaskPane taskPane = new JXTaskPane();
			taskPane.setAnimated(false);
			String title = "Unnamed validation rule";
			IValidationRuleDescriptor descriptor = result.getDescriptor();
			taskPane.add(new JLabel("Type: " + descriptor.getDisplayName(),
					GuiHelper.getImageIcon(descriptor.getIconPath()),
					JLabel.LEFT));

			Map<String, String> resultProperties = result.getProperties();
			if (resultProperties != null) {
				String validationRuleName = resultProperties
						.get(IValidationRule.PROPERTY_NAME);
				if (validationRuleName != null) {
					title = validationRuleName;
				}
			}
			taskPane.setTitle(title);
			if (result.isValidated()) {
				taskPane.setIcon(ICON_SUCCESS);
				taskPane.setCollapsed(true);
				taskPane.add(new JLabel("All rows where validated."));
			} else {
				Exception error = result.getError();
				taskPane.setCollapsed(false);
				if (error != null) {
					taskPane.setIcon(ICON_ERROR);
					JLabel errorLabel = new JLabel(error.getMessage());
					errorLabel.setFont(errorLabel.getFont().deriveFont(
							Font.BOLD));
					taskPane.add(errorLabel);
					taskPane.add(new JLabel("See the Status tab for details."));
				} else {
					taskPane.setIcon(ICON_FAILED);
					taskPane.add(new JLabel(result.getUnvalidatedRows().size()
							+ " rows did not validate"));
					List<Row> rows = result.getUnvalidatedRows();

					DataSet data = new DataSet(rows);

					DataTable dataTable = new DataTable(data);

					Column[] evaluatedColumns = result.getEvaluatedColumns();
					int[] evaluatedColumnIndexes = new int[evaluatedColumns.length];
					for (int i = 0; i < evaluatedColumnIndexes.length; i++) {
						evaluatedColumnIndexes[i] = ArrayUtils.indexOf(
								queriedColumns, evaluatedColumns[i]);
					}

					dataTable.addHighlighter(new ColumnHighlighter(
							evaluatedColumnIndexes));

					taskPane.add(new JScrollPane(dataTable.toPanel(),
							JScrollPane.VERTICAL_SCROLLBAR_NEVER,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
				}
			}
			taskPaneContainer.add(taskPane);
		}

		add(new JScrollPane(taskPaneContainer), BorderLayout.CENTER);
	}
}