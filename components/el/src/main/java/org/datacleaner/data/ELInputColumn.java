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
package org.datacleaner.data;

import java.util.List;

import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.datacleaner.api.ExpressionBasedInputColumn;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;

/**
 * InputColumn that evaluates an EL expression in order to return a computed
 * value. This can be used as a lightweight alternative to eg. JavaScript
 * transformation.
 *
 *
 */
public class ELInputColumn extends AbstractExpressionBasedInputColumn<String> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ELInputColumn.class);

    private final ExpressionFactory _factory;
    private final String _expression;

    public ELInputColumn(final String expression) {
        _expression = expression;
        _factory = new ExpressionFactoryImpl();
    }

    @Override
    public String evaluate(final InputRow row) {
        final SimpleContext context = new SimpleContext();
        final List<InputColumn<?>> inputColumns = row.getInputColumns();
        for (final InputColumn<?> inputColumn : inputColumns) {
            if (!(inputColumn instanceof ExpressionBasedInputColumn)) {
                final Object value = row.getValue(inputColumn);
                final Class<?> javaType = inputColumn.getDataType();
                final ValueExpression valueExpression = _factory.createValueExpression(value, javaType);
                String variableName = inputColumn.getName();
                variableName = StringUtils.replaceWhitespaces(variableName, "_");
                context.setVariable(variableName, valueExpression);
            }
        }

        try {
            final ValueExpression valueExpression = _factory.createValueExpression(context, _expression, String.class);
            return (String) valueExpression.getValue(context);
        } catch (final ELException e) {
            logger.error("Could not evaluate EL expression", e);
            return null;
        }
    }

    @Override
    public String getExpression() {
        return _expression;
    }

    @Override
    public Class<? extends String> getDataType() {
        return String.class;
    }
}
