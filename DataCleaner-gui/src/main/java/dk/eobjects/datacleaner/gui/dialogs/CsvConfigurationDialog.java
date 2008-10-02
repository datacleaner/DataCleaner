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
package dk.eobjects.datacleaner.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.LineBorder;

import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.model.ExtensionFilter;
import dk.eobjects.datacleaner.gui.widgets.DataTable;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Table;

public class CsvConfigurationDialog extends BanneredDialog implements
		ActionListener {

	private static final long serialVersionUID = -8449807287294665745L;
	private static final String COMMA_ACTION_CMD = ",";
	private static final String SEMI_COLON_ACTION_CMD = ";";
	private static final String TAB_ACTION_CMD = "\t";
	private static final String SINGLE_QUOTE_ACTION_CMD = "'";
	private static final String DOUBLE_QUOTE_ACTION_CMD = "\"";
	private JPanel _dataConfigurationPanel;
	private JTextField _sampleText;
	private DataTable _table;
	private char _curSeparator = ',';
	private char _curQuoteChar = '"';
	private DataContextSelection _dataContextSelection;
	private File _file;
	private JRadioButton _tabButton;
	private JPanel _tablePanel;

	public CsvConfigurationDialog(DataContextSelection selection, File file) {
		super(600, 480);
		_file = file;
		_dataContextSelection = selection;

		JButton okButton = new JButton("Open", GuiHelper
				.getImageIcon("images/toolbar_file.png"));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CsvConfigurationDialog.this.setVisible(false);
			}
		});
		JToolBar toolbar = GuiHelper.createToolBar();
		toolbar.add(okButton);
		add(toolbar, BorderLayout.SOUTH);

		String fileExtension = ExtensionFilter.getExtention(file);
		if (DataFileChooser.EXTENSION_TAB_SEPARATED.equals(fileExtension)) {
			_tabButton.setSelected(true);
			_curSeparator = TAB_ACTION_CMD.charAt(0);
		}

		updateContent();
	}

	@Override
	protected Component getContent() {
		ButtonGroup delimitorGroup = new ButtonGroup();
		_dataConfigurationPanel = GuiHelper.createPanel().applyLayout(null)
				.toComponent();
		JLabel delimitorLbl = new JLabel("Select the Delimitor:");
		addToPanel(delimitorLbl, 10, 5, 200, 20);
		JRadioButton commaButton = GuiHelper.createRadio("Use Comma [,]",
				delimitorGroup).toComponent();
		commaButton.setActionCommand(COMMA_ACTION_CMD);
		commaButton.setSelected(true);
		commaButton.addActionListener(this);
		addToPanel(commaButton, 10, 35, 150, 20);

		JRadioButton semiColonButton = GuiHelper.createRadio(
				"Use Semi Colon [;]", delimitorGroup).toComponent();
		semiColonButton.setActionCommand(SEMI_COLON_ACTION_CMD);
		semiColonButton.addActionListener(this);
		addToPanel(semiColonButton, 10, 60, 150, 20);

		_tabButton = GuiHelper.createRadio("Use Tab [   ]", delimitorGroup)
				.toComponent();
		_tabButton.setActionCommand(TAB_ACTION_CMD);
		_tabButton.addActionListener(this);
		addToPanel(_tabButton, 10, 85, 150, 20);

		delimitorGroup.add(_tabButton);

		JLabel quotationLbl = new JLabel("Select the Quotation type:");

		ButtonGroup quoteGroup = new ButtonGroup();
		addToPanel(quotationLbl, 200, 5, 200, 20);
		JRadioButton doubleButton = GuiHelper.createRadio(
				"Use Double Quote [\"sample text\"]", quoteGroup).toComponent();
		doubleButton.setActionCommand(DOUBLE_QUOTE_ACTION_CMD);
		doubleButton.setSelected(true);
		doubleButton.addActionListener(this);
		addToPanel(doubleButton, 200, 35, 200, 20);
		JRadioButton singleButton = GuiHelper.createRadio(
				"Use Single Quote ['sample text']", quoteGroup).toComponent();
		singleButton.setActionCommand(SINGLE_QUOTE_ACTION_CMD);
		singleButton.addActionListener(this);
		addToPanel(singleButton, 200, 60, 200, 20);

		_sampleText = new JTextField("Sample Text");
		_sampleText.setText("\"sample text1\", \"sample text2\", ...");
		_sampleText.setEnabled(false);
		addToPanel(_sampleText, 10, 115, 400, 20);

		JLabel tableHeading = new JLabel("Preview Data:");
		addToPanel(tableHeading, 10, 145, 200, 20);

		_table = new DataTable(new DataSet(new ArrayList<Row>(0)));
		_table.setName("previewTable");
		_tablePanel = _table.toPanel();
		_tablePanel.setBorder(new LineBorder(Color.GRAY, 1));
		_tablePanel.setLocation(10, 170);
		_dataConfigurationPanel.add(_tablePanel);
		// addToPanel(tablePanel, 10, 150, 580, 108);
		return _dataConfigurationPanel;
	}

	private void addToPanel(Component c, int xPos, int yPos, int width,
			int height) {
		c.setLocation(xPos, yPos);
		c.setSize(width, height);
		_dataConfigurationPanel.add(c);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals(COMMA_ACTION_CMD)
				|| command.equals(SEMI_COLON_ACTION_CMD)
				|| command.equals(TAB_ACTION_CMD)) {
			_curSeparator = command.charAt(0);
		}
		if (command.equals(SINGLE_QUOTE_ACTION_CMD)
				|| command.equals(DOUBLE_QUOTE_ACTION_CMD)) {
			_curQuoteChar = command.charAt(0);
		}
		updateContent();
	}

	private void updateContent() {
		_dataContextSelection.selectFile(_file, _curSeparator, _curQuoteChar);

		_sampleText.setText(buildSampleString(_curSeparator, _curQuoteChar));

		DataContext dataContext = _dataContextSelection.getDataContext();
		Table table = dataContext.getSchemas()[0].getTables()[0];

		Query q = new Query();
		q.from(table).select(table.getColumns());
		q.setMaxRows(5);

		_table.updateTable(dataContext.executeQuery(q));
		_tablePanel.setSize(_table.getPanelPreferredSize());
	}

	private String buildSampleString(char seperatorChar, char quoteChar) {
		return quoteChar + "sample text 1" + quoteChar + seperatorChar + "423"
				+ seperatorChar + quoteChar + "sample text 2" + quoteChar;
	}

	protected JTextField getSampleText() {
		return _sampleText;
	}

	@Override
	protected String getDialogTitle() {
		return "Configure Delimitor and Quotations";
	}
}