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
package org.eobjects.datacleaner.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.datacleaner.util.SchemaComparator;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

/**
 * A combobox that makes it easy to display and select source coumns from a
 * list. The list can either be populated based on a datastore (in which case
 * the list will include all schemas, all tables and all columns) as well as
 * just a single table (in which case it will only include columns from that
 * table).
 * 
 * @author Kasper Sørensen
 */
public class SourceColumnComboBox extends JComboBox {

	private static final long serialVersionUID = 1L;

	private final SchemaStructureComboBoxListRenderer _renderer;
	private volatile DatastoreConnection _datastoreConnection;
	private volatile Table _table;

	public SourceColumnComboBox() {
		super();
		_renderer = new SchemaStructureComboBoxListRenderer();
		setRenderer(_renderer);
		setEditable(false);
	}

	public SourceColumnComboBox(Datastore datastore) {
		this();
		setModel(datastore);
	}

	public SourceColumnComboBox(Datastore datastore, Table table) {
		this();
		setModel(datastore, table);
	}

	public void setEmptyModel() {
		setModel(null, null);
	}

	public void setModel(Datastore datastore, Table table) {
		final String previousColumnName;
		final Column previousItem = getSelectedItem();
		if (previousItem == null) {
			previousColumnName = null;
		} else {
			previousColumnName = previousItem.getName();
		}

		if (getTable() == table) {
			return;
		}
		setTable(table);

		if (datastore == null) {
			setDatastoreConnection(null);
		} else {
			setDatastoreConnection(datastore.openConnection());
		}
		if (table == null) {
			setModel(new DefaultComboBoxModel(new String[1]));
		} else {
			int selectedIndex = 0;

			List<Column> comboBoxList = new ArrayList<Column>();
			comboBoxList.add(null);

			Column[] columns = table.getColumns();
			for (Column column : columns) {
				comboBoxList.add(column);
				if (column.getName().equals(previousColumnName)) {
					selectedIndex = comboBoxList.size() - 1;
				}
			}
			final ComboBoxModel model = new ListComboBoxModel<Column>(comboBoxList);
			setModel(model);
			setSelectedIndex(selectedIndex);
		}
	}

	public void setModel(Datastore datastore) {
		setModel(datastore, true);
	}

	public void setModel(Table table) {
		setModel(null, table);
	}

	public void setModel(Datastore datastore, boolean retainSelection) {
		final Column previousItem = getSelectedItem();

		setTable(null);

		if (datastore == null) {
			setDatastoreConnection(null);
			setModel(new DefaultComboBoxModel(new String[1]));
		} else {

			DatastoreConnection con = setDatastoreConnection(datastore.openConnection());

			int selectedIndex = 0;

			List<Object> comboBoxList = new ArrayList<Object>();
			comboBoxList.add(null);

			Schema[] schemas = con.getSchemaNavigator().getSchemas();
			Arrays.sort(schemas, new SchemaComparator());

			for (Schema schema : schemas) {
				comboBoxList.add(schema);
				if (!SchemaComparator.isInformationSchema(schema)) {
					Table[] tables = schema.getTables();
					for (Table table : tables) {
						comboBoxList.add(table);
						Column[] columns = table.getColumns();
						for (Column column : columns) {
							comboBoxList.add(column);
							if (column == previousItem) {
								selectedIndex = comboBoxList.size() - 1;
							}
						}
					}
				}
			}

			final ComboBoxModel model = new ListComboBoxModel<Object>(comboBoxList);
			setModel(model);
			if (retainSelection) {
				setSelectedIndex(selectedIndex);
			}
		}
	}

	@Override
	public void setSelectedItem(Object value) {
		if (value instanceof String) {
			if (_table == null) {
				// cannot string convert to column without a table.
				value = null;
			} else {
				value = _table.getColumnByName((String) value);
			}
		}
		super.setSelectedItem(value);
	}

	private void setTable(Table table) {
		_table = table;
		setIndentation();
	}

	private void setIndentation() {
		_renderer.setIndentEnabled(_table == null && _datastoreConnection != null);
	}

	public Table getTable() {
		return _table;
	}

	private DatastoreConnection setDatastoreConnection(DatastoreConnection datastoreConnection) {
		if (_datastoreConnection != null) {
			// close the previous data context provider
			_datastoreConnection.close();
		}
		_datastoreConnection = datastoreConnection;
		setIndentation();
		return _datastoreConnection;
	}

	@Override
	public Column getSelectedItem() {
		Object selectedItem = super.getSelectedItem();
		if (selectedItem instanceof Column) {
			return (Column) selectedItem;
		}
		return null;
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		if (_datastoreConnection != null) {
			// close the data context provider when the widget is removed
			_datastoreConnection.close();
		}
	}
}