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
package org.datacleaner.server;

import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.metamodel.util.HdfsResource;
import org.datacleaner.util.SystemProperties;

/**
 * Environment based configuration
 */
public class DirectConnectionHadoopClusterInformation extends EnvironmentBasedHadoopClusterInformation
        implements HadoopClusterInformation {
    private static final long serialVersionUID = 1L;
    private final URI _nameNodeUri;

    public DirectConnectionHadoopClusterInformation(final String name, final String description, final URI nameNodeUri) {
        super(name, description);

        _nameNodeUri = nameNodeUri;
    }

    @Override
    public Configuration getConfiguration() {
        final Configuration configuration;
        if(SystemProperties.getBoolean(HdfsResource.SYSTEM_PROPERTY_HADOOP_CONF_DIR_ENABLED, false)){
            configuration = super.getConfiguration();
        } else {
            configuration = new Configuration();
        }

        configuration.set("fs.defaultFS", _nameNodeUri.toString());

        return configuration;
    }

    public URI getNameNodeUri() {
        return _nameNodeUri;
    }
}
