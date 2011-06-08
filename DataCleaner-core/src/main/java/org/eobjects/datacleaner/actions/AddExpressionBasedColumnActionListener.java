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
package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.eobjects.analyzer.data.ConstantInputColumn;
import org.eobjects.analyzer.data.ELInputColumn;
import org.eobjects.analyzer.data.ExpressionBasedInputColumn;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.widgets.properties.InputColumnPropertyWidgetAccessoryHandler;
import org.eobjects.metamodel.util.CollectionUtils;

/**
 * Action listener for adding an expression based input column
 * 
 * @author Kasper Sørensen
 */
public class AddExpressionBasedColumnActionListener implements ActionListener {

	private final InputColumnPropertyWidgetAccessoryHandler _accessoryHandler;
	private volatile InputColumn<?> _hoveringInputColumn = null;

	public AddExpressionBasedColumnActionListener(InputColumnPropertyWidgetAccessoryHandler accessoryHandler) {
		_accessoryHandler = accessoryHandler;
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

		String expression = JOptionPane.showInputDialog(_accessoryHandler.getParent(),
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

			final ConfiguredPropertyDescriptor propertyDescriptor = _accessoryHandler.getPropertyDescriptor();
			final AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder = _accessoryHandler.getBeanJobBuilder();
			if (propertyDescriptor.isArray()) {
				InputColumn<?>[] currentValue = (InputColumn[]) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
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
			beanJobBuilder.setConfiguredProperty(propertyDescriptor, newValue);
		}
	}

}
