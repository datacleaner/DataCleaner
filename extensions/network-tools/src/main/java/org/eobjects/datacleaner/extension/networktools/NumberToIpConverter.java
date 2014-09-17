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
package org.eobjects.datacleaner.extension.networktools;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Convert number to IP")
@Categorized(NetworkToolsCategory.class)
@Description("Converts a number representation of an IPv4 address to it's regular string representation.")
public class NumberToIpConverter implements Transformer<String> {

	@Configured("IP number column")
	InputColumn<Number> ipColumn;

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(ipColumn.getName() + " (IP as number)");
	}

	@Override
	public String[] transform(InputRow inputRow) {
		final String[] result = new String[1];
		final Number number = inputRow.getValue(ipColumn);
		
		result[0] = transform(number);

		return result;
	}

	public String transform(Number number) {
		if (number == null) {
			return null;
		}
		long l = number.longValue();
		if (l > 0) {
			final String str = ((l >> 24) & 0xFF) + "." + ((l >> 16) & 0xFF)
					+ "." + ((l >> 8) & 0xFF) + "." + (l & 0xFF);

			return str;
		}
		return null;
	}
}
