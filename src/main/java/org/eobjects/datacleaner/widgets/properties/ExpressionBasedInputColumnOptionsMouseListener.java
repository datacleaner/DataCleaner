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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.eobjects.analyzer.data.ConstantInputColumn;
import org.eobjects.analyzer.data.ELInputColumn;
import org.eobjects.analyzer.data.ExpressionBasedInputColumn;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.metamodel.util.CollectionUtils;

public class ExpressionBasedInputColumnOptionsMouseListener implements ActionListener {

	private static final String ICON_PATH = "images/model/column_expression.png";
	private final ConfiguredPropertyDescriptor _propertyDescriptor;
	private final AbstractBeanJobBuilder<?, ?, ?> _beanJobBuilder;
	private final JPopupMenu _popup;
	private final JComponent _parent;
	private final JButton _button;
	private volatile boolean _hoveringParent = false;
	private volatile boolean _hoveringChild = false;
	private volatile boolean _hoveringButton = false;
	private volatile InputColumn<?> _hoveringInputColumn = null;

	public ExpressionBasedInputColumnOptionsMouseListener(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> jobBuilder, JComponent parent) {
		_propertyDescriptor = propertyDescriptor;
		_beanJobBuilder = jobBuilder;
		_button = WidgetFactory.createSmallButton(ICON_PATH);
		_button.setToolTipText("Create expression/value based column");
		_button.addActionListener(this);

		_button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				_hoveringButton = true;
			}

			@Override
			public void mouseExited(MouseEvent e) {
				_hoveringButton = false;
				hideIfNescesary();
			}
		});

		_parent = parent;

		_popup = new JPopupMenu();
		_popup.setInvoker(_parent);
		_popup.add(_button);

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

	@Override
	public void actionPerformed(ActionEvent e) {
		final boolean replace;
		final String initialValue;
		if (_hoveringInputColumn instanceof ExpressionBasedInputColumn) {
			replace = true;
			initialValue = ((ExpressionBasedInputColumn<?>) _hoveringInputColumn).getExpression();
		} else {
			replace = false;
			initialValue = "";
		}

		String expression = JOptionPane.showInputDialog(_parent,
				"In stead of referencing a column you can also enter an expression.\n"
						+ "An expression may either be a constant string or an EL-expression\n"
						+ "that can access the other columns using the #{column_name} syntax.", initialValue);

		if (!StringUtils.isNullOrEmpty(expression)) {
			Object newValue;
			if (expression.indexOf("#{") != -1) {
				newValue = new ELInputColumn(expression);
			} else {
				newValue = new ConstantInputColumn(expression);
			}

			if (_propertyDescriptor.isArray()) {
				InputColumn<?>[] currentValue = (InputColumn[]) _beanJobBuilder.getConfiguredProperty(_propertyDescriptor);
				if (currentValue == null) {
					currentValue = new InputColumn[0];
				}
				newValue = CollectionUtils.array(currentValue, newValue);

				if (replace) {
					List<InputColumn<?>> list = new ArrayList<InputColumn<?>>();
					int length = Array.getLength(newValue);
					for (int i = 0; i < length; i++) {
						Object o = Array.get(newValue, i);
						if (!_hoveringInputColumn.equals(o)) {
							list.add((InputColumn<?>) o);
						}
					}
					newValue = list.toArray(new InputColumn[list.size()]);
				}
			}
			_beanJobBuilder.setConfiguredProperty(_propertyDescriptor, newValue);
		}

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
}
