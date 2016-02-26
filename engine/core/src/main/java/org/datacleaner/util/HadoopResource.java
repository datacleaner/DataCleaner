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
package org.datacleaner.util;

import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.server.HadoopClusterInformation;

public class HadoopResource extends HdfsResource {
    public static final String DEFAULT_CLUSTERREFERENCE = "org.datacleaner.hadoop.environment";

    private static final long serialVersionUID = 1L;

    private final transient Configuration _configuration;
    private final String _clusterReferenceName;

    public HadoopResource(URI uri, Configuration configuration, String clusterReferenceName) {
        super(uri.toString());
        _configuration = configuration;
        _clusterReferenceName = clusterReferenceName;
    }

    public HadoopResource(Resource resource, Configuration configuration, String clusterReferenceName) {
        super(resource.getQualifiedPath());
        _configuration = configuration;
        _clusterReferenceName = clusterReferenceName;
    }

    public HadoopResource(final URI uri, final HadoopClusterInformation defaultCluster) {
        this(uri, defaultCluster.getConfiguration(), defaultCluster.getName());
    }

    public HadoopResource(String uri, Configuration configuration, String clusterReferenceName) {
        super(uri);
        _configuration = configuration;
        _clusterReferenceName = clusterReferenceName;
    }

    @Override
    public Configuration getHadoopConfiguration() {
        return _configuration;
    }

    @Override
    public String toString() {
        return "HadoopResource[" + getQualifiedPath() + "]";
    }

    public String getClusterReferenceName() {
        return _clusterReferenceName;
    }

    public String getTemplatedPath() {
        // Legacy support.
        if(_clusterReferenceName == null){
            return getQualifiedPath();
        }
        return "hdfs://{" + _clusterReferenceName + "}" + getFilepath();
    }

}
