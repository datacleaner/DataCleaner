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
package org.eobjects.analyzer.beans.coalesce;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.StringManipulationCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Coalesce strings")
@Description("Returns the first non-null string. Use it to identify the most "
		+ "accurate or most recent observation, if multiple entries "
		+ "have been recorded in separate columns.")
@Categorized({ StringManipulationCategory.class })
@Deprecated
public class CoalesceStringsTransformer implements Transformer<String> {

	@Configured
	InputColumn<String>[] input;

	@Configured
	@Description("Consider empty strings (\"\") as null also?")
	boolean considerEmptyStringAsNull = true;

	public CoalesceStringsTransformer() {
	}

	@SafeVarargs
	public CoalesceStringsTransformer(InputColumn<String>... input) {
		this();
		this.input = input;
	}

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns("Coalesced string");
	}

	@Override
	public String[] transform(InputRow inputRow) {
		for (InputColumn<String> column : input) {
			String value = inputRow.getValue(column);
			if (value != null) {
				if (considerEmptyStringAsNull) {
					if (!"".equals(value)) {
						return new String[] { value };
					}
				} else {
					return new String[] { value };
				}
			}
		}
		return new String[1];
	}

}
