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

    /**
     * A regular expression {@link Pattern} that matches resource URIs
     * containing template items for the server definition, for instance:
     * 
     * hdfs://{myserver}/foo/bar.txt
     * 
     * <ul>
     * <li>Group 1: The scheme (example 'hdfs')</li>
     * <li>Group 2: The template name (example 'myserver')</li>
     * <li>Group 3: The path (example '/foo/bar.txt')</li>
     * </ul>
     */
    public static final Pattern RESOURCE_SCHEME_PATTERN = Pattern.compile("([\\w\\+\\-\\.]+)://\\{([\\w\\.\\W\\s]*)\\}(.*)");

    public HadoopResourceBuilder(ServerInformationCatalog catalog, String templatedUri) {
        final Matcher matcher = RESOURCE_SCHEME_PATTERN.matcher(templatedUri);
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
            _clusterReferenceName = matcher.group(2);
            final HadoopClusterInformation hadoopClusterInformation = (HadoopClusterInformation) catalog.getServer(
                    _clusterReferenceName);
            _configuration = hadoopClusterInformation.getConfiguration();
            _uri = URI.create(matcher.group(3).replace(" ", "%20"));
        }
    }

    public HadoopResource build() {
        return new HadoopResource(_uri, _configuration, _clusterReferenceName);
    }
}
