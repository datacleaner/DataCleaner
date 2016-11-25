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
package org.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.apache.metamodel.util.CollectionUtils;
import org.datacleaner.api.ExpressionBasedInputColumn;
import org.datacleaner.api.InputColumn;
import org.datacleaner.data.ConstantInputColumn;
import org.datacleaner.data.ELInputColumn;
import org.datacleaner.util.StringUtils;
import org.datacleaner.widgets.properties.PropertyWidget;

/**
 * Action listener for adding an expression based input column
 *
 * @author Kasper Sørensen
 */
public class AddExpressionBasedColumnActionListener implements ActionListener {

    private final PropertyWidget<InputColumn<?>> _singlePropertyWidget;
    private final PropertyWidget<InputColumn<?>[]> _multiplePropertyWidget;

    private AddExpressionBasedColumnActionListener(final PropertyWidget<InputColumn<?>> singlePropertyWidget,
            final PropertyWidget<InputColumn<?>[]> multiplePropertyWidget) {
        _singlePropertyWidget = singlePropertyWidget;
        _multiplePropertyWidget = multiplePropertyWidget;
    }

    public static AddExpressionBasedColumnActionListener forSingleColumn(
            final PropertyWidget<InputColumn<?>> singlePropertyWidget) {
        return new AddExpressionBasedColumnActionListener(singlePropertyWidget, null);
    }

    public static AddExpressionBasedColumnActionListener forMultipleColumns(
            final PropertyWidget<InputColumn<?>[]> multiplePropertyWidget) {
        return new AddExpressionBasedColumnActionListener(null, multiplePropertyWidget);
    }

    public PropertyWidget<?> getPropertyWidget() {
        return _singlePropertyWidget == null ? _multiplePropertyWidget : _singlePropertyWidget;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final String expression = JOptionPane.showInputDialog(getPropertyWidget().getWidget(),
                "In stead of referencing a column you can also enter an expression.\n"
                        + "An expression may either be a constant string or an EL-expression\n"
                        + "that can access the other columns using the #{column_name} syntax.", "");
        addExpressionBasedInputColumn(expression);
    }

    public void addExpressionBasedInputColumn(final String expression) {
        if (!StringUtils.isNullOrEmpty(expression)) {
            final ExpressionBasedInputColumn<?> expressionBasedInputColumn;
            if (expression.indexOf("#{") != -1) {
                expressionBasedInputColumn = new ELInputColumn(expression);
            } else {
                expressionBasedInputColumn = new ConstantInputColumn(expression);
            }

            if (_multiplePropertyWidget != null) {
                InputColumn<?>[] currentValue = _multiplePropertyWidget.getValue();
                if (currentValue == null) {
                    currentValue = new InputColumn[0];
                }

                @SuppressWarnings("unchecked") final InputColumn<?>[] newValue =
                        CollectionUtils.array(currentValue, expressionBasedInputColumn);
                _multiplePropertyWidget.onValueTouched(newValue);
            } else {
                _singlePropertyWidget.onValueTouched(expressionBasedInputColumn);
            }
        }
    }
}
