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
package org.datacleaner.components.convert;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.ConversionCategory;
import org.datacleaner.util.Percentage;
import org.datacleaner.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to convert anything to a Number (Double) value
 */
@Named("Convert to number")
@Description("Converts anything to a number (or null if not possible).")
@Categorized(ConversionCategory.class)
public class ConvertToNumberTransformer implements Transformer {

    private static final Logger logger = LoggerFactory.getLogger(ConvertToNumberTransformer.class);

    @Inject
    @Configured
    InputColumn<?>[] input;

    @Inject
    @Configured
    char decimalSeparator = DecimalFormatSymbols.getInstance().getDecimalSeparator();

    @Inject
    @Configured
    char thousandSeparator = DecimalFormatSymbols.getInstance().getGroupingSeparator();

    @Inject
    @Configured
    char minusSign = DecimalFormatSymbols.getInstance().getMinusSign();

    @Inject
    @Configured(required = false)
    Number nullReplacement;

    // no-args constructor
    public ConvertToNumberTransformer() {
    }

    public ConvertToNumberTransformer(final char decimalSeparator, final char thousandSeparator, final char minusSign) {
        this();
        this.decimalSeparator = decimalSeparator;
        this.thousandSeparator = thousandSeparator;
        this.minusSign = minusSign;
    }

    public static Number transformValue(final Object value) {
        // use java's normal decimal symbols
        final DecimalFormat format = new DecimalFormat();
        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');
        symbols.setMinusSign('-');
        format.setDecimalFormatSymbols(symbols);
        return transformValue(value, format);
    }

    public static Number transformValue(final Object value, final DecimalFormat decimalFormat) {
        Number n = null;
        if (value != null) {
            if (value instanceof Number) {
                n = (Number) value;
            } else if (value instanceof Boolean) {
                if (Boolean.TRUE.equals(value)) {
                    n = 1;
                } else {
                    n = 0;
                }
            } else if (value instanceof Date) {
                final Date d = (Date) value;
                n = d.getTime();
            } else if (value instanceof Character) {
                final Character c = (Character) value;
                if (!Character.isDigit(c)) {
                    // return the integer value of the character
                    n = (int) c;
                }
            } else {
                String stringValue = value.toString();
                stringValue = StringUtils.replaceWhitespaces(stringValue, "");

                if (stringValue.startsWith("+")) {
                    stringValue = stringValue.substring(1);
                }

                try {
                    if (stringValue.indexOf('%') != -1) {
                        n = Percentage.parsePercentage(stringValue);
                    } else {
                        n = decimalFormat.parse(stringValue);
                    }
                } catch (final Exception e) {
                    logger.info("Error occured parsing string as number: {}", stringValue);
                }
            }
        }
        return n;
    }

    public DecimalFormat getDecimalFormat() {
        final DecimalFormat format = new DecimalFormat();
        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(decimalSeparator);
        symbols.setGroupingSeparator(thousandSeparator);
        symbols.setMinusSign(minusSign);
        format.setDecimalFormatSymbols(symbols);
        return format;
    }

    @Override
    public OutputColumns getOutputColumns() {
        final String[] names = new String[input.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = input[i].getName() + " (as number)";
        }
        return new OutputColumns(Number.class, names);
    }

    @Override
    public Number[] transform(final InputRow inputRow) {
        final Number[] result = new Number[input.length];
        for (int i = 0; i < input.length; i++) {
            final Object value = inputRow.getValue(input[i]);
            Number n = transform(value);
            if (n == null) {
                n = nullReplacement;
            }
            result[i] = n;
        }
        return result;
    }

    protected Number transform(final Object value) {
        return transformValue(value, getDecimalFormat());
    }

    public void setInput(final InputColumn<?>... input) {
        this.input = input;
    }

    public void setNullReplacement(final Number nullReplacement) {
        this.nullReplacement = nullReplacement;
    }

    public void setDecimalSeparator(final char decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    public void setMinusSign(final char minusSign) {
        this.minusSign = minusSign;
    }

    public void setThousandSeparator(final char thousandSeparator) {
        this.thousandSeparator = thousandSeparator;
    }
}
