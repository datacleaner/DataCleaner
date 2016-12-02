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

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("Resolve hostname")
@Categorized(NetworkToolsCategory.class)
@Description("Resolves the IP of a hostname")
public class ResolveHostnameTransformer implements Transformer {

    private static final Logger logger = LoggerFactory.getLogger(ResolveHostnameTransformer.class);

    @Configured
    InputColumn<String> hostnameColumn;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, hostnameColumn.getName() + " (ip address)");
    }

    @Override
    public String[] transform(final InputRow inputRow) {
        final String hostname = inputRow.getValue(hostnameColumn);
        String result = null;
        if (!StringUtils.isNullOrEmpty(hostname)) {
            try {
                final InetAddress addr = InetAddress.getByName(hostname);
                final byte[] ip = addr.getAddress();
                assert ip.length == 4;
                result =
                        toUnsigned(ip[0]) + "." + toUnsigned(ip[1]) + "." + toUnsigned(ip[2]) + "." + toUnsigned(ip[3]);
            } catch (final UnknownHostException e) {
                logger.error("Unknown host: " + hostname, e);
            } catch (final Exception e) {
                logger.error("Unexpected error", e);
            }
        }
        return new String[] { result };
    }

    private int toUnsigned(final byte b) {
        if (b < 0) {
            return b + 256;
        }
        return b;
    }

}
