/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.widgets;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.SchemaFactory;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.windows.CouchDbDatastoreDialog;
import org.datacleaner.windows.MongoDbDatastoreDialog;
import org.datacleaner.windows.TableDefinitionDialog;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.HasName;
import org.apache.metamodel.util.SimpleTableDef;

/**
 * A widget used for selecting whether to automatically discover or to manually
 * create table definitions for a particular datastore, eg. in the
 * {@link MongoDbDatastoreDialog} or {@link CouchDbDatastoreDialog}.
 */
public class TableDefinitionOptionSelectionPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	public static enum TableDefinitionOption implements HasName {
		AUTOMATIC, MANUAL;

		@Override
		public String getName() {
			if (this == AUTOMATIC) {
				return "Automatic discovery";
			}
			return "Manual definition";
		}
	}

	private final DCComboBox<TableDefinitionOption> _comboBox;
	private final JButton _configureButton;
	private final DCLabel _label;
	private final WindowContext _windowContext;
	private final Action<SimpleTableDef[]> _saveAction;
	private SimpleTableDef[] _tableDefs;

	public TableDefinitionOptionSelectionPanel(WindowContext windowContext, final SchemaFactory schemaFactory,
			SimpleTableDef[] tableDefs) {
		super();
		_windowContext = windowContext;
		_tableDefs = tableDefs;
		_label = DCLabel.bright("Loading...");

		_saveAction = new Action<SimpleTableDef[]>() {
			@Override
			public void run(SimpleTableDef[] tableDefs) throws Exception {
				setTableDefs(tableDefs);
			}
		};

		_comboBox = new DCComboBox<TableDefinitionOption>(TableDefinitionOption.values());
		_comboBox.setRenderer(new EnumComboBoxListRenderer());
		_configureButton = WidgetFactory.createSmallButton("images/menu/options.png");
		_configureButton.setText("Define");

		_configureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TableDefinitionDialog dialog = new TableDefinitionDialog(_windowContext, schemaFactory, _tableDefs,
						_saveAction);
				dialog.setVisible(true);
			}
		});
		if (tableDefs != null && tableDefs.length > 0) {
			_comboBox.setSelectedItem(TableDefinitionOption.MANUAL);
		} else {
			_comboBox.setSelectedItem(TableDefinitionOption.AUTOMATIC);
		}
		_comboBox.addListener(new Listener<TableDefinitionOptionSelectionPanel.TableDefinitionOption>() {
			@Override
			public void onItemSelected(TableDefinitionOption item) {
				updateComponents();
			}
		});
		updateComponents();

		setLayout(new BorderLayout(4, 0));
		add(_comboBox, BorderLayout.CENTER);
		add(_configureButton, BorderLayout.EAST);
		add(_label, BorderLayout.SOUTH);
	}

	private void updateComponents() {
		if (_comboBox.getSelectedItem() == TableDefinitionOption.AUTOMATIC) {
			_configureButton.setEnabled(false);
			_label.setText("(Tables will be automatically discovered)");
		} else {
			_configureButton.setEnabled(true);
			if (_tableDefs == null || _tableDefs.length == 0) {
				_label.setText("(no tables defined)");
			} else {
				_label.setText("(" + _tableDefs.length + " tables defined)");
			}
		}
	}

	public SimpleTableDef[] getTableDefs() {
		if (_comboBox.getSelectedItem() == TableDefinitionOption.AUTOMATIC) {
			return null;
		}
		return _tableDefs;
	}

	public void setTableDefs(SimpleTableDef[] tableDefs) {
		_tableDefs = (tableDefs == null ? new SimpleTableDef[0] : tableDefs);
		updateComponents();
	}
}
