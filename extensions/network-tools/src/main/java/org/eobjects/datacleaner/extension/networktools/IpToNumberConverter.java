/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
import org.eobjects.analyzer.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TransformerBean("Convert IP to number")
@Categorized(NetworkToolsCategory.class)
@Description("Converts an IPv4 string to a number value, which makes it appropriate for eg. persisting in a number column.")
public class IpToNumberConverter implements Transformer<Number> {

	private static final Logger logger = LoggerFactory
			.getLogger(IpToNumberConverter.class);

	@Configured("IP string column")
	InputColumn<String> ipColumn;

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(ipColumn.getName() + " (IP as number)");
	}

	@Override
	public Number[] transform(InputRow inputRow) {
		final Number[] result = new Number[1];
		final String addr = inputRow.getValue(ipColumn);
		if (StringUtils.isNullOrEmpty(addr)) {
			return result;
		}

		try {
			final String[] addrArray = addr.split("\\.");
			if (addrArray.length != 4) {
				throw new IllegalStateException("Found " + addrArray.length
						+ " tokens, expected 4");
			}
			long num = 0;

			for (int i = 0; i < addrArray.length; i++) {
				int power = 3 - i;
				int ipPart = Integer.parseInt(addrArray[i]);
				if (ipPart < 0 || ipPart > 255) {
					throw new IllegalStateException("Illegal IP part: "
							+ ipPart);
				}
				num += (ipPart * Math.pow(256, power));
			}

			result[0] = num;
		} catch (Exception e) {
			logger.warn("Could not convert ip: {} - {}", addr, e.getMessage());
		}

		return result;
	}
}
