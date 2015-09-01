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
package org.datacleaner.beans.numbers;

import org.datacleaner.api.*;
import org.datacleaner.components.categories.NumbersCategory;

import javax.inject.Named;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Simple transformer for incrementing a number
 */
@Named("Increment number")
@Description("Increment an id, a version or any other number.")
@Categorized(NumbersCategory.class)
public class IncrementNumberTransformer implements Transformer {

    @Configured
    InputColumn<Number> _number;

    @Configured
    @NumberProperty(zero = false)
    int _increment = 1;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, _number.getName() + " (incremented)");
    }

    @Override
    public Number[] transform(InputRow row) {
        Number number = row.getValue(_number);
        if (number != null) {
            if (number instanceof Integer || number instanceof Short || number instanceof Byte) {
                number = number.intValue() + _increment;
            } else if (number instanceof Long) {
                number = number.longValue() + _increment;
            } else if (number instanceof Float) {
                number = number.floatValue() + _increment;
            } else if (number instanceof BigDecimal) {
                number = ((BigDecimal) number).add(new BigDecimal(_increment));
            } else if (number instanceof BigInteger) {
                number = ((BigInteger) number).add(new BigInteger("" + _increment));
            } else {
                number = number.doubleValue() + _increment;
            }
        }
        return new Number[] { number };
    }

}
