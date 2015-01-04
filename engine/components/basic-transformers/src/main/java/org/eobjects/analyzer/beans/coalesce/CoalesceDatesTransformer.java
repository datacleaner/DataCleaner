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

import java.util.Date;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.DateAndTimeCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Coalesce dates")
@Description("Returns the first non-null date out of a set of dates. "
		+ "Use it to identify relative dates such as 'latest activity' date "
		+ "if multiple stages in a process may have been recorded in different columns.")
@Categorized({ DateAndTimeCategory.class })
@Deprecated
public class CoalesceDatesTransformer implements Transformer<Date> {

	@Configured
	InputColumn<Date>[] input;

	public CoalesceDatesTransformer() {
	}

	@SafeVarargs
    public CoalesceDatesTransformer(InputColumn<Date>... input) {
		this();
		this.input = input;
	}

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns("Coalesced date");
	}

	@Override
	public Date[] transform(InputRow inputRow) {
		for (InputColumn<Date> column : input) {
			Date value = inputRow.getValue(column);
			if (value != null) {
				return new Date[] { value };
			}
		}
		return new Date[1];
	}

}
