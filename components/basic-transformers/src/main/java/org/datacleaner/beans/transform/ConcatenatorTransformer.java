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

import org.datacleaner.api.*;
import org.datacleaner.components.categories.StringManipulationCategory;

import javax.inject.Named;

/**
 * Concatenates several values into one String value.
 */
@Named("Concatenator")
@Description("Concatenate several column values into one.")
@Categorized(StringManipulationCategory.class)
@WSStatelessComponent
public class ConcatenatorTransformer implements Transformer {

	@Configured
	InputColumn<?>[] columns;

	@Configured(required = false)
	@Description("A string to separate the concatenated values")
	String separator;

	public ConcatenatorTransformer(String separator, InputColumn<?>[] columns) {
		this.separator = separator;
		this.columns = columns;
	}

	public ConcatenatorTransformer() {
	}

	@Override
	public OutputColumns getOutputColumns() {
		StringBuilder sb = new StringBuilder("Concat of ");
		for (int i = 0; i < columns.length; i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append(columns[i].getName());
			if (i == 4) {
				sb.append("...");
				// only include a preview of columns in the default name
				break;
			}
		}
		return new OutputColumns(String.class, sb.toString());
	}

	@Override
	public String[] transform(InputRow inputRow) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (int i = 0; i < columns.length; i++) {
			InputColumn<?> column = columns[i];
			Object value = inputRow.getValue(column);
			if (value != null && !"".equals(value)) {
				if (!first && separator != null) {
					sb.append(separator);
				}
				sb.append(value);
				first = false;
			}
		}
		return new String[] { sb.toString() };
	}

}
