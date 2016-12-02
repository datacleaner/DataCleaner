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
package org.datacleaner.beans.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ValueExpression;
import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.StringProperty;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.NumbersCategory;
import org.datacleaner.util.StringUtils;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;

@Named("Math formula")
@Description("Provide a mathematical formula to perform arbitrary calculations.\n"
        + "Formulas support basic operators like plus (+), minus (-), divide (/), multiply (*) and modulus (%).")
@Categorized({ NumbersCategory.class })
public class MathFormulaTransformer implements Transformer {

    @Configured
    InputColumn<Number>[] _input;

    @Configured
    @StringProperty(emptyString = false, mimeType = "text/groovy")
    String _formula = "(col1 + col2) / col3";

    private ExpressionFactoryImpl _factory;
    private Map<String, List<String>> _columnAliases;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(Number.class, "Formula result");
    }

    @Initialize
    public void init() {
        _factory = new ExpressionFactoryImpl();

        _columnAliases = new HashMap<>();
        int i = 1;
        for (final InputColumn<Number> inputColumn : _input) {
            final String name = inputColumn.getName();
            final List<String> list = new ArrayList<>(3);
            final String variableName1 = StringUtils.replaceWhitespaces(name.toLowerCase(), "_");
            final String variableName2 = StringUtils.replaceWhitespaces(name.toLowerCase(), "");
            list.add(variableName1);
            list.add(variableName2);
            list.add("col" + i);
            i++;
            _columnAliases.put(name, list);
        }
    }

    @Override
    public Number[] transform(final InputRow inputRow) {
        final SimpleContext context = new SimpleContext();

        for (final InputColumn<Number> inputColumn : _input) {
            final Number value = inputRow.getValue(inputColumn);
            final ValueExpression valueExpression = _factory.createValueExpression(value, Number.class);
            final List<String> aliases = _columnAliases.get(inputColumn.getName());
            for (final String alias : aliases) {
                context.setVariable(alias, valueExpression);
            }
        }

        final String expression = "#{" + _formula.toLowerCase() + "}";
        final ValueExpression valueExpression = _factory.createValueExpression(context, expression, Number.class);

        try {
            final Object value = valueExpression.getValue(context);
            assert value instanceof Number;

            if (value instanceof Double) {
                final double dbl = ((Double) value).doubleValue();
                if (Double.isNaN(dbl) || Double.isInfinite(dbl)) {
                    // we don't want to present infinity and NaN - null is a
                    // better replacement.
                    return new Number[] { null };
                }
            }

            return new Number[] { (Number) value };
        } catch (final ArithmeticException e) {
            return new Number[] { null };
        }
    }

}
