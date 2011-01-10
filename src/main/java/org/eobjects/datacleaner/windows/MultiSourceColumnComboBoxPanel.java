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
package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.SourceColumnComboBox;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;

public class MultiSourceColumnComboBoxPanel extends DCPanel {

	private static final long serialVersionUID = 6598553122965748098L;
	private SourceColumnComboBox _sourceColumnComboBox;
	private Datastore _datastore;
	private DCPanel _sourceComboBoxPanel;
	private DCPanel _parentPanel;
	private DCPanel _buttonPanel;
	private List<SourceColumnComboBox> _sourceColumnComboBoxes;
	private Table _table;

	public MultiSourceColumnComboBoxPanel() {
		_sourceColumnComboBoxes = new ArrayList<SourceColumnComboBox>();
		_sourceColumnComboBox = getNewSourceColumnComboBox();
		initializePanels();
	}

	private SourceColumnComboBox getNewSourceColumnComboBox() {
		SourceColumnComboBox sourceColumnComboBox = (_table == null) ? new SourceColumnComboBox(_datastore)
				: new SourceColumnComboBox(_datastore, _table);
		_sourceColumnComboBoxes.add(sourceColumnComboBox);
		return sourceColumnComboBox;
	}

	private void initializePanels() {
		initializeSourceComboBoxPanel();
		initializeParentPanel();
		initializeButtonPanel();
	}

	private void initializeButtonPanel() {

		_buttonPanel = new DCPanel();

		JButton addButton = WidgetFactory.createSmallButton("images/actions/add.png");
		JButton removeButton = WidgetFactory.createSmallButton("images/actions/remove.png");

		_buttonPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
		_buttonPanel.setLayout(new VerticalLayout(2));

		_buttonPanel.add(addButton);
		_buttonPanel.add(removeButton);

		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SourceColumnComboBox sourceColumnComboBox = getNewSourceColumnComboBox();
				_sourceComboBoxPanel.add(sourceColumnComboBox);
				_sourceComboBoxPanel.updateUI();
			}

		});

		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int componentCount = _sourceComboBoxPanel.getComponentCount();
				if (componentCount > 0) {
					_sourceComboBoxPanel.remove(componentCount - 1);
					_sourceComboBoxPanel.updateUI();
				}
			}
		});
	}

	private void initializeSourceComboBoxPanel() {
		_sourceComboBoxPanel = new DCPanel();
		_sourceComboBoxPanel.add(_sourceColumnComboBox);
		_sourceComboBoxPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
		_sourceComboBoxPanel.setLayout(new VerticalLayout());
	}

	private void initializeParentPanel() {
		_parentPanel = new DCPanel();
		_parentPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
		_parentPanel.setLayout(new HorizontalLayout(2));
		_parentPanel.add(_sourceComboBoxPanel);
	}

	/**
	 * Creates a panel containing ButtonPanel and SourceComboboxPanel
	 * 
	 * @return DCPanel
	 */
	public DCPanel createPanel() {
		_parentPanel.setLayout(new BorderLayout());
		_parentPanel.add(_sourceComboBoxPanel, BorderLayout.CENTER);
		_parentPanel.add(_buttonPanel, BorderLayout.EAST);
		return _parentPanel;
	}

	public void setModel(Datastore datastore) {
		_datastore = datastore;
		_sourceColumnComboBox.setModel(_datastore);
	}

	/**
	 * Returns the column names selected as String[].
	 */
	public String[] getColumnNames() {
		List<Column> columns = getColumns();
		return convertToStringArry(columns);
	}

	private String[] convertToStringArry(List<Column> columns) {
		List<String> columnNamesAsString = new ArrayList<String>();
		for (Column column : columns) {
			columnNamesAsString.add(column.getQualifiedLabel());
		}
		return columnNamesAsString.toArray(new String[0]);
	}

	/**
	 * Returns the columns selected as Column[].
	 */
	public List<Column> getColumns() {
		List<Column> columns = new ArrayList<Column>();
		List<Component> components = Arrays.asList(_sourceComboBoxPanel.getComponents());
		for (Component component : components) {
			if (component instanceof SourceColumnComboBox) {
				SourceColumnComboBox sourceColumnComboBox = (SourceColumnComboBox) component;
				columns.add(sourceColumnComboBox.getSelectedItem());
			}
		}
		return columns;
	}

	/**
	 * updates the SourceColumnComboBoxes with the provided datastore and table 
	 */
	public void updateSourceComboBoxes(Datastore datastore, Table table) {
		_datastore = datastore;
		_table = table;
		for (SourceColumnComboBox sourceColComboBox : _sourceColumnComboBoxes) {
			sourceColComboBox.setModel(datastore, table);
		}
	}

}
