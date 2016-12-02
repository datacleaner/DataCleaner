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
package org.datacleaner.extension.networktools;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;

@Named("Convert number to IP")
@Categorized(NetworkToolsCategory.class)
@Description("Converts a number representation of an IPv4 address to it's regular string representation.")
public class NumberToIpConverter implements Transformer {

    @Configured("IP number column")
    InputColumn<Number> ipColumn;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, ipColumn.getName() + " (IP as number)");
    }

    @Override
    public String[] transform(final InputRow inputRow) {
        final String[] result = new String[1];
        final Number number = inputRow.getValue(ipColumn);

        result[0] = transform(number);

        return result;
    }

    public String transform(final Number number) {
        if (number == null) {
            return null;
        }
        final long l = number.longValue();
        if (l > 0) {

            return ((l >> 24) & 0xFF) + "." + ((l >> 16) & 0xFF) + "." + ((l >> 8) & 0xFF) + "." + (l & 0xFF);
        }
        return null;
    }
}
