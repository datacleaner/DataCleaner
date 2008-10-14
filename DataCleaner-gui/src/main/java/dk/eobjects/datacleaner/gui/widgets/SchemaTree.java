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
package dk.eobjects.datacleaner.gui.widgets;

import java.awt.event.MouseListener;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.util.WeakObservable;
import dk.eobjects.datacleaner.util.WeakObserver;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.MetaModelException;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

public class SchemaTree extends JTree implements WeakObserver,
		TreeWillExpandListener {

	private static final Log _log = LogFactory.getLog(SchemaTree.class);
	private static final long serialVersionUID = 7763827443642264329L;
	public static final String LOADING_NODE_STRING = "Loading tables...";
	public static final String UNNAMED_SCHEMA_STRING = "(unnamed schema)";
	public static final String ROOT_NODE_STRING = "Schemas";
	private DataContextSelection _dataContextSelection;

	@Override
	public void removeNotify() {
		super.removeNotify();
		_log.debug("removeNotify()");
		_dataContextSelection.deleteObserver(this);
		_dataContextSelection = null;
		MouseListener[] mouseListeners = getMouseListeners();
		for (MouseListener mouseListener : mouseListeners) {
			removeMouseListener(mouseListener);
		}
	}

	public SchemaTree(DataContextSelection dataContextSelection) {
		super();
		_dataContextSelection = dataContextSelection;
		_dataContextSelection.addObserver(this);
		addTreeWillExpandListener(this);
		updateTree();
	}

	private void updateTree() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
		rootNode.setUserObject(ROOT_NODE_STRING);
		DataContext dataContext = _dataContextSelection.getDataContext();
		if (dataContext == null) {
			rootNode.setUserObject("No data selected");
		} else {
			String[] schemaNames = dataContext.getSchemaNames();
			for (int i = 0; i < schemaNames.length; i++) {
				String schemaName = schemaNames[i];
				if (schemaName == null) {
					schemaName = UNNAMED_SCHEMA_STRING;
				}
				DefaultMutableTreeNode schemaNode = new DefaultMutableTreeNode(
						schemaName);
				schemaNode.add(new DefaultMutableTreeNode(LOADING_NODE_STRING));
				rootNode.add(schemaNode);
			}
		}
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		setModel(treeModel);
	}

	public void update(WeakObservable o) {
		if (o instanceof DataContextSelection) {
			updateTree();
		}
	}

	public static Column getColumn(Table table, TreePath path) {
		if (path.getPathCount() >= 4) {
			return table.getColumnByName(path.getPathComponent(3).toString());
		}
		return null;
	}

	public static Table getTable(Schema schema, TreePath path) {
		if (path.getPathCount() >= 3) {
			return schema.getTableByName(path.getPathComponent(2).toString());
		}
		return null;
	}

	public static Schema getSchema(DataContext dc, TreePath path) {
		if (path.getPathCount() >= 2) {
			String schemaName = path.getPathComponent(1).toString();
			if (UNNAMED_SCHEMA_STRING.equals(schemaName)) {
				schemaName = null;
			}
			try {
				Schema schema = dc.getSchemaByName(schemaName);
				return schema;
			} catch (RuntimeException e) {
				GuiHelper.showErrorMessage("Could not open schema",
						"An exception occurred when trying to retrieve schema details of schema '"
								+ schemaName + "'", e);
				throw e;
			}
		}
		return null;
	}

	public void treeWillCollapse(TreeExpansionEvent event)
			throws ExpandVetoException {
		// Do nothing
	}

	public void treeWillExpand(TreeExpansionEvent event)
			throws ExpandVetoException {
		TreePath path = event.getPath();
		DefaultMutableTreeNode lastComponent = (DefaultMutableTreeNode) path
				.getLastPathComponent();
		if (lastComponent.getChildCount() == 1) {
			if (((DefaultMutableTreeNode) lastComponent.getChildAt(0))
					.getUserObject() == LOADING_NODE_STRING) {
				DataContext dc = _dataContextSelection.getDataContext();
				try {
					Schema schema = getSchema(dc, path);
					Table[] tables = schema.getTables();
					for (Table table : tables) {
						DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(
								table.getName());
						Column[] columns = table.getColumns();
						for (Column column : columns) {
							DefaultMutableTreeNode columnNode = new DefaultMutableTreeNode(
									column.getName());
							tableNode.add(columnNode);
						}
						lastComponent.add(tableNode);
					}
				} catch (MetaModelException e) {
					_log.fatal("Error retrieving schema from path: " + path);
					_log.error(e);
					GuiHelper
							.showErrorMessage(
									"Error retrieving schema",
									"A fatal error was encountered retrieving metadata about the selected schema.",
									e);
				} finally {
					lastComponent.remove(0);
					setModel(getModel());
				}
			}
		}
	}
}