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
package org.datacleaner.util.convert;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.datacleaner.configuration.ServerInformationCatalog;
import org.datacleaner.server.HadoopClusterInformation;
import org.datacleaner.util.HadoopResource;

public class HadoopResourceBuilder {
    private final URI _uri;
    private final String _clusterReferenceName;
    private final Configuration _configuration;

    Pattern _pattern = Pattern.compile("(?:[\\w\\+\\-\\.]+://)?\\{([\\w\\.]*)\\}(.*)");

    public HadoopResourceBuilder(ServerInformationCatalog catalog, String templatedUri) {
        final Matcher matcher = _pattern.matcher(templatedUri);
        if (!matcher.matches()) {
            _clusterReferenceName = null;
            final String fixedUri = templatedUri.replace(" ", "%20");
            final HadoopClusterInformation hadoopClusterInformation = (HadoopClusterInformation)
                    catalog.getServer(HadoopResource.DEFAULT_CLUSTERREFERENCE);

            if (hadoopClusterInformation != null) {
                _configuration = hadoopClusterInformation.getConfiguration();
            } else {
                _configuration = new Configuration();
            }
            _configuration.set("fs.defaultFS", fixedUri);
            _uri = URI.create(fixedUri);
        } else {
            _clusterReferenceName = matcher.group(1);
            final HadoopClusterInformation hadoopClusterInformation = (HadoopClusterInformation) catalog.getServer(
                    _clusterReferenceName);
            _configuration = hadoopClusterInformation.getConfiguration();
            _uri = URI.create(matcher.group(2).replace(" ", "%20"));
        }
    }

    public HadoopResource build() {
        return new HadoopResource(_uri, _configuration, _clusterReferenceName);
    }
}
