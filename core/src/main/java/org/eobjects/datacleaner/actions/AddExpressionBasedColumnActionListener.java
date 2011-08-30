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

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.eobjects.analyzer.data.ConstantInputColumn;
import org.eobjects.analyzer.data.ELInputColumn;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.widgets.properties.AbstractPropertyWidget;
import org.eobjects.metamodel.util.CollectionUtils;

/**
 * Action listener for adding an expression based input column
 * 
 * @author Kasper Sørensen
 */
public class AddExpressionBasedColumnActionListener implements ActionListener {

	private final JComponent _parentComponent;
	private final AbstractBeanJobBuilder<?, ?, ?> _beanJobBuilder;
	private final ConfiguredPropertyDescriptor _propertyDescriptor;

	public AddExpressionBasedColumnActionListener(AbstractPropertyWidget<?> propertyWidget) {
		_parentComponent = propertyWidget.getWidget();
		_beanJobBuilder = propertyWidget.getBeanJobBuilder();
		_propertyDescriptor = propertyWidget.getPropertyDescriptor();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String expression = JOptionPane.showInputDialog(_parentComponent,
				"In stead of referencing a column you can also enter an expression.\n"
						+ "An expression may either be a constant string or an EL-expression\n"
						+ "that can access the other columns using the #{column_name} syntax.", "");

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
			}
			_beanJobBuilder.setConfiguredProperty(_propertyDescriptor, newValue);
		}
	}
}
