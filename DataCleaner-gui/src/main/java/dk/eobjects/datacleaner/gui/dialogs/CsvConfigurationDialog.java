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
import java.awt.Component;
import java.awt.GridBagLayout;
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

import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.widgets.DataTable;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class CsvConfigurationDialog extends BanneredDialog implements
		ActionListener {

	private static final long serialVersionUID = -8449807287294665745L;

	private static final String COMMA_ACTION_CMD = ",";
	private static final String SEMI_COLON_ACTION_CMD = ";";
	private static final String TAB_ACTION_CMD = "\t";
	private static final String SINGLE_QUOTE_ACTION_CMD = "'";
	private static final String DOUBLE_QUOTE_ACTION_CMD = "\"";
	private static final int PREVIEW_ROWS = 5;

	private JPanel _panel;
	private JTextField _sampleText;
	private DataTable _table;
	private char _curSeparator = ',';
	private char _curQuoteChar = '"';
	private DataContextSelection _dataContextSelection;
	private File _file;
	private String _fileExtension;
	private JPanel _tablePanel;

	public CsvConfigurationDialog(DataContextSelection selection, File file) {
		super(500, 480);
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

		_fileExtension = DataContextSelection.getExtention(file);

		updateContent();
	}

	@Override
	protected Component getContent() {
		ButtonGroup delimitorGroup = new ButtonGroup();
		_panel = GuiHelper.createPanel().toComponent();

		JLabel delimitorLabel = new JLabel("Select the Delimitor:");
		GuiHelper.addToGridBag(delimitorLabel, _panel, 0, 0);

		JRadioButton commaButton = GuiHelper.createRadio("Use Comma [,]",
				delimitorGroup).toComponent();
		commaButton.setActionCommand(COMMA_ACTION_CMD);
		commaButton.setSelected(true);
		commaButton.addActionListener(this);
		GuiHelper.addToGridBag(commaButton, _panel, 0, 1);

		JRadioButton semiColonButton = GuiHelper.createRadio(
				"Use Semi Colon [;]", delimitorGroup).toComponent();
		semiColonButton.setActionCommand(SEMI_COLON_ACTION_CMD);
		semiColonButton.addActionListener(this);
		GuiHelper.addToGridBag(semiColonButton, _panel, 0, 2);

		JRadioButton tabButton = GuiHelper.createRadio("Use Tab [   ]",
				delimitorGroup).toComponent();
		tabButton.setActionCommand(TAB_ACTION_CMD);
		tabButton.addActionListener(this);
		if (DataContextSelection.EXTENSION_TAB_SEPARATED.equals(_fileExtension)) {
			tabButton.setSelected(true);
			_curSeparator = TAB_ACTION_CMD.charAt(0);
		}
		GuiHelper.addToGridBag(tabButton, _panel, 0, 3);

		delimitorGroup.add(tabButton);

		JLabel quotationLabel = new JLabel("Select the Quotation type:");
		GuiHelper.addToGridBag(quotationLabel, _panel, 1, 0);

		ButtonGroup quoteGroup = new ButtonGroup();
		JRadioButton doubleQuoteButton = GuiHelper.createRadio(
				"Use Double Quote [\"sample text\"]", quoteGroup).toComponent();
		doubleQuoteButton.setActionCommand(DOUBLE_QUOTE_ACTION_CMD);
		doubleQuoteButton.setSelected(true);
		doubleQuoteButton.addActionListener(this);
		GuiHelper.addToGridBag(doubleQuoteButton, _panel, 1, 1);

		JRadioButton singleQuoteButton = GuiHelper.createRadio(
				"Use Single Quote ['sample text']", quoteGroup).toComponent();
		singleQuoteButton.setActionCommand(SINGLE_QUOTE_ACTION_CMD);
		singleQuoteButton.addActionListener(this);
		GuiHelper.addToGridBag(singleQuoteButton, _panel, 1, 2);

		_sampleText = new JTextField("Sample Text");
		_sampleText.setText("\"sample text1\", \"sample text2\", ...");
		_sampleText.setEnabled(false);
		GuiHelper.addToGridBag(_sampleText, _panel, 0, 10, 2, 1);

		JLabel tableHeading = new JLabel("Preview Data:");
		GuiHelper.addToGridBag(tableHeading, _panel, 0, 11, 2, 1);

		ArrayList<Row> initialData = new ArrayList<Row>(PREVIEW_ROWS);
		SelectItem[] selectItems = new SelectItem[] { new SelectItem(
				new Column("")) };
		for (int i = 0; i < PREVIEW_ROWS; i++) {
			initialData.add(new Row(selectItems, new Object[] { "" }));
		}

		_table = new DataTable(new DataSet(initialData));
		_table.setName("previewTable");
		_tablePanel = _table.toPanel();
		GuiHelper.addToGridBag(_tablePanel, _panel, 0, 12, 2, 1);

		GridBagLayout layout = (GridBagLayout) _panel.getLayout();
		layout.columnWidths = new int[] { 240, 240 };

		return _panel;
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
		q.setMaxRows(PREVIEW_ROWS);

		_table.updateTable(dataContext.executeQuery(q));
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