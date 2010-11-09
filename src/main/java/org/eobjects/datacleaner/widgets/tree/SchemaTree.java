package org.eobjects.datacleaner.widgets.tree;

import java.awt.Component;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.WrappingIconPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

public class SchemaTree extends JXTree implements TreeWillExpandListener, TreeCellRenderer {

	private static final long serialVersionUID = 7763827443642264329L;

	private static final Logger logger = LoggerFactory.getLogger(SchemaTree.class);

	public static final String LOADING_TABLES_STRING = "Loading tables...";
	public static final String LOADING_COLUMNS_STRING = "Loading columns...";
	public static final String UNNAMED_SCHEMA_STRING = "(unnamed schema)";
	public static final String ROOT_NODE_STRING = "Schemas";

	private final Datastore _datastore;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final TreeCellRenderer _rendererDelegate;

	public SchemaTree(Datastore datastore, AnalysisJobBuilder analysisJobBuilder) {
		super();
		if (datastore == null) {
			throw new IllegalArgumentException("Datastore cannot be null");
		}
		if (analysisJobBuilder == null) {
			throw new IllegalArgumentException("AnalysisJobBuilder cannot be null");
		}
		_rendererDelegate = new DefaultTreeRenderer();
		setCellRenderer(this);
		_datastore = datastore;
		_analysisJobBuilder = analysisJobBuilder;
		setOpaque(false);
		setBorder(WidgetUtils.BORDER_WIDE);
		addTreeWillExpandListener(this);
		addMouseListener(new SchemaMouseListener(this, _analysisJobBuilder));
		addMouseListener(new TableMouseListener(this, _analysisJobBuilder));
		addMouseListener(new ColumnMouseListener(this, _analysisJobBuilder));
		updateTree();
	}

	private void updateTree() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
		rootNode.setUserObject(_datastore);
		Schema[] schemas = _datastore.getDataContextProvider().getDataContext().getSchemas();

		// make sure that information schemas are arranged at the top
		Arrays.sort(schemas, new Comparator<Schema>() {
			@Override
			public int compare(Schema o1, Schema o2) {
				String name1 = o1.getName();
				if ("information_schema".equals(name1.toLowerCase())) {
					return -1;
				}
				String name2 = o2.getName();
				if ("information_schema".equals(name2.toLowerCase())) {
					return 1;
				}
				return name1.compareTo(name2);
			}
		});

		for (Schema schema : schemas) {
			DefaultMutableTreeNode schemaNode = new DefaultMutableTreeNode(schema);
			schemaNode.add(new DefaultMutableTreeNode(LOADING_TABLES_STRING));
			rootNode.add(schemaNode);
		}
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		setModel(treeModel);
	}

	public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
		// Do nothing
	}

	public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
		TreePath path = event.getPath();
		DefaultMutableTreeNode lastTreeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		Runnable runnable = null;
		if (lastTreeNode.getChildCount() == 1) {
			DefaultMutableTreeNode firstChildNode = (DefaultMutableTreeNode) lastTreeNode.getChildAt(0);
			if (firstChildNode.getUserObject() == LOADING_TABLES_STRING) {
				// Load a schema's tables
				runnable = new LoadTablesRunnable(lastTreeNode);
			} else if (firstChildNode.getUserObject() == LOADING_COLUMNS_STRING) {
				// Load a table's columns
				runnable = new LoadColumnsRunnable(path, lastTreeNode);
			}
		}

		if (runnable != null) {
			runnable.run();
		}
	}

	class LoadTablesRunnable implements Runnable {
		private final DefaultMutableTreeNode _schemaNode;

		public LoadTablesRunnable(DefaultMutableTreeNode schemaNode) {
			_schemaNode = schemaNode;
		}

		public void run() {
			Schema schema = (Schema) _schemaNode.getUserObject();
			Table[] tables = schema.getTables();
			for (Table table : tables) {
				DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table);
				DefaultMutableTreeNode loadingColumnsNode = new DefaultMutableTreeNode(LOADING_COLUMNS_STRING);
				tableNode.add(loadingColumnsNode);
				_schemaNode.add(tableNode);
			}
			_schemaNode.remove(0);
			setModel(getModel());
		}
	}

	class LoadColumnsRunnable implements Runnable {
		private final DefaultMutableTreeNode _tableNode;

		public LoadColumnsRunnable(TreePath path, DefaultMutableTreeNode tableNode) {
			_tableNode = tableNode;
		}

		public void run() {
			Table table = (Table) _tableNode.getUserObject();
			Column[] columns = table.getColumns();
			for (Column column : columns) {
				DefaultMutableTreeNode columnNode = new DefaultMutableTreeNode(column);
				_tableNode.add(columnNode);
			}
			_tableNode.remove(0);
			setModel(getModel());
		}
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		MouseListener[] mouseListeners = getMouseListeners();
		for (MouseListener mouseListener : mouseListeners) {
			removeMouseListener(mouseListener);
		}
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		if (value instanceof DefaultMutableTreeNode) {
			value = ((DefaultMutableTreeNode) value).getUserObject();
		}

		Component component = null;
		ImageManager imageManager = ImageManager.getInstance();
		Icon icon = null;

		if (value instanceof Datastore) {
			component = _rendererDelegate.getTreeCellRendererComponent(tree, ((Datastore) value).getName(), selected,
					expanded, leaf, row, hasFocus);
			icon = IconUtils.getDatastoreIcon((Datastore) value, IconUtils.ICON_SIZE_SMALL);
		} else if (value instanceof Schema) {
			String schemaName = ((Schema) value).getName();
			component = _rendererDelegate.getTreeCellRendererComponent(tree, schemaName, selected, expanded, leaf, row,
					hasFocus);
			icon = imageManager.getImageIcon("images/model/schema.png", IconUtils.ICON_SIZE_SMALL);
			if ("information_schema".equalsIgnoreCase(schemaName)) {
				icon = imageManager.getImageIcon("images/model/schema_information.png", IconUtils.ICON_SIZE_SMALL);
			}
		} else if (value instanceof Table) {
			component = _rendererDelegate.getTreeCellRendererComponent(tree, ((Table) value).getName(), selected, expanded,
					leaf, row, hasFocus);
			icon = imageManager.getImageIcon("images/model/table.png", IconUtils.ICON_SIZE_SMALL);
		} else if (value instanceof Column) {
			Column column = (Column) value;
			component = _rendererDelegate.getTreeCellRendererComponent(tree, column.getName(), selected, expanded, leaf,
					row, hasFocus);
			icon = imageManager.getImageIcon("images/model/column.png", IconUtils.ICON_SIZE_SMALL);
		} else if (value instanceof String) {
			component = _rendererDelegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		}

		if (component == null) {
			throw new IllegalArgumentException("Unexpected value: " + value + " of class: "
					+ (value == null ? "<null>" : value.getClass().getName()));
		}

		final boolean opaque = hasFocus || selected;
		if (component instanceof JComponent) {
			((JComponent) component).setOpaque(opaque);
		}

		if (icon != null) {
			if (component instanceof WrappingIconPanel) {
				WrappingIconPanel wip = (WrappingIconPanel) component;
				wip.setIcon(icon);
				wip.getComponent().setOpaque(opaque);
			} else {
				logger.warn("Rendered TreeNode Component was not a WrappingIconPanel, cannot set icon!");
			}
		}

		return component;
	}
}