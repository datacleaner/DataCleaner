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
package org.eobjects.datacleaner.widgets.properties;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.eobjects.analyzer.data.ExpressionBasedInputColumn;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.actions.AddExpressionBasedColumnActionListener;
import org.eobjects.datacleaner.actions.ReorderColumnsActionListener;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Handler class for displaying the accessory buttons (a popup with the
 * "insert expression based column" button and the "reorder columns" button) on
 * input column selection widgets
 * 
 * @author Kasper Sørensen
 */
public class InputColumnPropertyWidgetAccessoryHandler {

	private static final String EXPRESSION_COLUMN_ICON_PATH = "images/model/column_expression.png";
	private static final String REORDER_COLUMN_ICON_PATH = "images/actions/reorder-columns.png";

	private final MouseAdapter buttonMouseListener = new MouseAdapter() {
		@Override
		public void mouseEntered(MouseEvent e) {
			_hoveringButton = true;
		}

		@Override
		public void mouseExited(MouseEvent e) {
			_hoveringButton = false;
			hideIfNescesary();
		}
	};

	private final ConfiguredPropertyDescriptor _propertyDescriptor;
	private final AbstractBeanJobBuilder<?, ?, ?> _beanJobBuilder;
	private final JPopupMenu _popup;
	private final JComponent _parent;
	private volatile boolean _hoveringParent = false;
	private volatile boolean _hoveringChild = false;
	private volatile boolean _hoveringButton = false;
	private volatile InputColumn<?> _hoveringInputColumn = null;

	public InputColumnPropertyWidgetAccessoryHandler(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> jobBuilder, JComponent parent, boolean allowExpressionBasedColumns) {
		_propertyDescriptor = propertyDescriptor;
		_beanJobBuilder = jobBuilder;
		_parent = parent;

		final DCPanel panel = new DCPanel();
		panel.setLayout(new VerticalLayout());

		if (allowExpressionBasedColumns) {
			final JButton expressionColumnButton = WidgetFactory.createSmallButton(EXPRESSION_COLUMN_ICON_PATH);
			expressionColumnButton.setToolTipText("Create expression/value based column");
			expressionColumnButton.addActionListener(new AddExpressionBasedColumnActionListener(this));
			expressionColumnButton.addMouseListener(buttonMouseListener);
			panel.add(expressionColumnButton);
		}

		if (_propertyDescriptor.isArray()) {
			final JButton reorderColumnsButton = WidgetFactory.createSmallButton(REORDER_COLUMN_ICON_PATH);
			reorderColumnsButton.setToolTipText("Reorder columns");
			reorderColumnsButton.addMouseListener(buttonMouseListener);
			reorderColumnsButton.addActionListener(new ReorderColumnsActionListener(_propertyDescriptor, _beanJobBuilder));
			panel.add(reorderColumnsButton);
		}

		_popup = new JPopupMenu();
		_popup.setInvoker(_parent);
		_popup.add(panel);
		_popup.pack();

		_parent.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				_hoveringParent = true;
				showPopup(null);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				_hoveringParent = false;
				hideIfNescesary();
			}
		});
	}

	private void showPopup(final JComponent component) {
		int x = _parent.getLocationOnScreen().x + _parent.getWidth();
		int y;
		if (_hoveringInputColumn instanceof ExpressionBasedInputColumn) {
			if (component == null) {
				return;
			} else {
				y = component.getLocationOnScreen().y;
			}
		} else {
			y = _parent.getLocationOnScreen().y;
		}
		_popup.setLocation(x, y);
		_popup.setVisible(true);
	}

	public void registerListComponent(final JComponent component, final InputColumn<?> inputColumn) {
		component.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				_hoveringChild = true;
				_hoveringInputColumn = inputColumn;

				showPopup(component);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				_hoveringChild = false;
				hideIfNescesary();
			}
		});
	}

	private void hideIfNescesary() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (!_hoveringParent && !_hoveringChild && !_hoveringButton) {
					_popup.setVisible(false);
				}
			}
		});
	}

	public JComponent getParent() {
		return _parent;
	}

	public AbstractBeanJobBuilder<?, ?, ?> getBeanJobBuilder() {
		return _beanJobBuilder;
	}

	public InputColumn<?> getHoveringInputColumn() {
		return _hoveringInputColumn;
	}

	public ConfiguredPropertyDescriptor getPropertyDescriptor() {
		return _propertyDescriptor;
	}
}
