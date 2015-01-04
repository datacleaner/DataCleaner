/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.beans.numbers;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.NumberProperty;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.NumbersCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * Simple transformer for rounding a number
 */
@TransformerBean("Round number")
@Description("Transformation for rounding a number, typically to the nearest integer, nearest ten, hundred or thousand.")
@Categorized(NumbersCategory.class)
public class RoundNumberTransformer implements Transformer<Integer> {

    @Configured
    InputColumn<Number> _number;

    @Configured
    @Description("Defines the factor of rounding. A factor of 1 will round to the nearest integer. A factor of 1000 will round the number to the nearest thousand.")
    @NumberProperty(zero = false, positive = true, negative = false)
    int _roundFactor = 1;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(_number.getName() + " (rounded)");
    }
    
    @Override
    public Integer[] transform(InputRow row) {
        Number number = row.getValue(_number);
        if (number != null) {
            number = Math.round(number.doubleValue() / _roundFactor) * _roundFactor;
            number = number.intValue();
        }
        return new Integer[] { (Integer) number };
    }

}
