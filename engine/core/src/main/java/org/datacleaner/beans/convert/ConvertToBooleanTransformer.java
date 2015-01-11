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
package org.datacleaner.beans.convert;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.beans.api.Alias;
import org.datacleaner.beans.api.Categorized;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Description;
import org.datacleaner.beans.api.OutputColumns;
import org.datacleaner.beans.api.Transformer;
import org.datacleaner.beans.categories.ConversionCategory;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;

/**
 * Attempts to convert anything to a Boolean value
 */
@Named("Convert to boolean")
@Description("Converts anything to a boolean (or null).")
@Categorized({ ConversionCategory.class })
public class ConvertToBooleanTransformer implements Transformer {

	public static final String[] DEFAULT_TRUE_TOKENS = new String[] { "true", "yes", "1", "x" };
	public static final String[] DEFAULT_FALSE_TOKENS = new String[] { "false", "no", "0", "-" };

	@Inject
	@Configured
	@Alias("Column")
	InputColumn<?>[] input;

	@Configured(required = false)
	Boolean nullReplacement;

	@Configured
	@Description("Text tokens that will translate to 'true'")
	String[] _trueTokens = DEFAULT_TRUE_TOKENS;

	@Configured
	@Description("Text tokens that will translate to 'false'")
	String[] _falseTokens = DEFAULT_FALSE_TOKENS;

	public ConvertToBooleanTransformer(InputColumn<?>[] input) {
		this.input = input;
	}

	public ConvertToBooleanTransformer() {
		this(null);
	}

	@Override
	public OutputColumns getOutputColumns() {
		String[] names = new String[input.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = input[i].getName() + " (as boolean)";
		}
		return new OutputColumns(Boolean.class, names);
	}

	@Override
	public Object[] transform(InputRow inputRow) {
		Boolean[] result = new Boolean[input.length];
		for (int i = 0; i < input.length; i++) {
			Object value = inputRow.getValue(input[i]);
			Boolean b = transformValue(value, _trueTokens, _falseTokens);
			if (b == null) {
				b = nullReplacement;
			}
			result[i] = b;
		}
		return result;
	}

	public static Boolean transformValue(final Object value) {
		return transformValue(value, DEFAULT_TRUE_TOKENS, DEFAULT_FALSE_TOKENS);
	}

	public static Boolean transformValue(final Object value, final String[] trueTokens, final String[] falseTokens) {
		Boolean b = null;
		if (value != null) {
			if (value instanceof String) {
				String stringValue = (String) value;
				stringValue = stringValue.trim();

				for (String token : trueTokens) {
					if (token.equalsIgnoreCase(stringValue)) {
						b = true;
						break;
					}
				}
				if (b == null) {
					for (String token : falseTokens) {
						if (token.equalsIgnoreCase(stringValue)) {
							b = false;
							break;
						}
					}
				}
			} else if (value instanceof Number) {
				Number numberValue = (Number) value;
				if (numberValue.intValue() == 1) {
					b = true;
				} else if (numberValue.intValue() == 0) {
					b = false;
				}
			} else if (value instanceof Boolean) {
				b = (Boolean) value;
			}
		}
		return b;
	}

	public void setFalseTokens(String[] falseTokens) {
		_falseTokens = falseTokens;
	}

	public String[] getFalseTokens() {
		return _falseTokens;
	}

	public void setInput(InputColumn<?>... input) {
		this.input = input;
	}

	public InputColumn<?>[] getInput() {
		return input;
	}

	public void setNullReplacement(Boolean nullReplacement) {
		this.nullReplacement = nullReplacement;
	}

	public Boolean getNullReplacement() {
		return nullReplacement;
	}

	public void setTrueTokens(String[] trueTokens) {
		_trueTokens = trueTokens;
	}

	public String[] getTrueTokens() {
		return _trueTokens;
	}

}
