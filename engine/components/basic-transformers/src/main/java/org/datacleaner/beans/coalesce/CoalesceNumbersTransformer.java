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
package org.datacleaner.beans.coalesce;

import org.datacleaner.beans.api.Categorized;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Description;
import org.datacleaner.beans.api.OutputColumns;
import org.datacleaner.beans.api.Transformer;
import org.datacleaner.beans.api.TransformerBean;
import org.datacleaner.beans.categories.NumbersCategory;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;

@TransformerBean("Coalesce numbers")
@Description("Returns the first non-null number. Use it to identify the most "
		+ "accurate or most recent number if multiple observations have been recorded in columns.")
@Categorized({ NumbersCategory.class })
@Deprecated
public class CoalesceNumbersTransformer implements Transformer<Number> {

	@Configured
	InputColumn<Number>[] input;

	public CoalesceNumbersTransformer() {
	}

	@SafeVarargs
	public CoalesceNumbersTransformer(InputColumn<Number>... input) {
		this();
		this.input = input;
	}

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns("Coalesced number");
	}

	@Override
	public Number[] transform(InputRow inputRow) {
		for (InputColumn<Number> column : input) {
			Number value = inputRow.getValue(column);
			if (value != null) {
				return new Number[] { value };
			}
		}
		return new Number[1];
	}

}
