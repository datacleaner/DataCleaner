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
package org.datacleaner.beans.standardize;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.TextCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

@Named("URL parser")
@Description("Retrieve the individual parts of an URL, including protocol, domain, port, path and querystring.")
@Categorized({ TextCategory.class })
public class UrlStandardizerTransformer implements Transformer {
    private static final Logger logger = LoggerFactory.getLogger(UrlStandardizerTransformer.class);
    @Configured
    InputColumn<String> inputColumn;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, new String[] { "Protocol", "Domain", "Port", "Path", "Querystring" });
    }

    @Override
    public String[] transform(final InputRow inputRow) {
        final String value = inputRow.getValue(inputColumn);
        return transform(value);
    }

    public String[] transform(final String value) {
        String protocol = null;
        String host = null;
        String port = null;
        String path = null;
        String queryString = null;

        if (value != null) {
            try {
                final URI url = new URI(value);

                protocol = url.getScheme();
                host = url.getHost();

                if (url.getPort() != -1) {
                    port = Integer.toString(url.getPort());
                }

                if (!Strings.isNullOrEmpty(url.getPath())) {
                    path = url.getPath();
                }
                queryString = url.getRawQuery();

            } catch (final URISyntaxException e) {
                logger.info("Throwing away illegal URL \"{}\"", value);
            }
        }

        return new String[] { protocol, host, port, path, queryString };
    }
}
