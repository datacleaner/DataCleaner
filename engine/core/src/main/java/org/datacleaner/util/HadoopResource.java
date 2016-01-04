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

public class HadoopResource extends HdfsResource {

    private static final long serialVersionUID = 1L;

    private final Configuration _configuration;

    public HadoopResource(URI uri, Configuration configuration) {
        super(uri.toString());
        _configuration = configuration;
    }

    public HadoopResource(Resource resource, Configuration configuration) {
        super(resource.getQualifiedPath());
        _configuration = configuration;
    }

    @Override
    public Configuration getHadoopConfiguration() {
        return _configuration;
    }

    @Override
    public String toString() {
        return "HadoopResource[" + getQualifiedPath() + "]";
    }
}
