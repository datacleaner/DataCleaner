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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.SchemaComparator;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.NamedStructure;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;

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

	private volatile DataContextProvider _dataContextProvider;
	private volatile Table _table;

	public SourceColumnComboBox() {
		super();
		setEditable(false);
		setRenderer(new SourceColumnComboBoxListRenderer());
	}

	public SourceColumnComboBox(Datastore datastore) {
		this();
		setModel(datastore);
	}

	public SourceColumnComboBox(Datastore datastore, Table table) {
		this();
		setModel(datastore, table);
	}

	public void setModel(Datastore datastore, Table table) {
		final Column previousItem = getSelectedItem();

		if (_table == table) {
			return;
		}
		_table = table;

		if (datastore == null) {
			setDataContextProvider(null);
		} else {
			setDataContextProvider(datastore.getDataContextProvider());
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
				if (column == previousItem) {
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

	public void setModel(Datastore datastore, boolean retainSelection) {
		final Column previousItem = getSelectedItem();

		_table = null;

		if (datastore == null) {
			setDataContextProvider(null);
			setModel(new DefaultComboBoxModel(new String[1]));
		} else {

			DataContextProvider dcp = setDataContextProvider(datastore.getDataContextProvider());

			int selectedIndex = 0;

			List<Object> comboBoxList = new ArrayList<Object>();
			comboBoxList.add(null);

			Schema[] schemas = dcp.getSchemaNavigator().getSchemas();
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

	private DataContextProvider setDataContextProvider(DataContextProvider dataContextProvider) {
		if (_dataContextProvider != null) {
			// close the previous data context provider
			_dataContextProvider.close();
		}
		_dataContextProvider = dataContextProvider;
		return _dataContextProvider;
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
		if (_dataContextProvider != null) {
			// close the data context provider when the widget is removed
			_dataContextProvider.close();
		}
	}

	/**
	 * Renderer for the combo box items
	 * 
	 * @author Kasper Sørensen
	 */
	class SourceColumnComboBoxListRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel result = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value instanceof NamedStructure) {
				result.setText(((NamedStructure) value).getName());

				int indent = 0;
				ImageManager imageManager = ImageManager.getInstance();
				Icon icon = null;
				if (value instanceof Schema) {
					icon = imageManager.getImageIcon("images/model/schema.png", IconUtils.ICON_SIZE_SMALL);
					if (SchemaComparator.isInformationSchema((Schema) value)) {
						icon = imageManager.getImageIcon("images/model/schema_information.png", IconUtils.ICON_SIZE_SMALL);
					} else {
						icon = imageManager.getImageIcon("images/model/schema.png", IconUtils.ICON_SIZE_SMALL);
					}
				} else if (value instanceof Table) {
					icon = imageManager.getImageIcon("images/model/table.png", IconUtils.ICON_SIZE_SMALL);
					indent = 10;
				} else if (value instanceof Column) {
					icon = imageManager.getImageIcon("images/model/column.png", IconUtils.ICON_SIZE_SMALL);
					indent = 20;
				}

				if (icon != null) {
					result.setIcon(icon);
				}
				if (_table == null) {
					result.setBorder(new EmptyBorder(0, indent, 0, 0));
				}
			}

			return result;
		}
	}
}