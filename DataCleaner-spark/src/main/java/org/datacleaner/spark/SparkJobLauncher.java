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
package org.datacleaner.spark;

import java.io.InputStream;

import org.apache.metamodel.util.HdfsResource;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.JaxbConfigurationReader;

public class SparkJobLauncher {

    private final DataCleanerConfiguration _dataCleanerConfiguration;
    
    public SparkJobLauncher(String confXmlPath) {
        _dataCleanerConfiguration = readDataCleanerConfiguration(new HdfsResource(confXmlPath));
    }
    
    public void launchJob(String analysisJobXmlPath) {
    }
    
    private static DataCleanerConfiguration readDataCleanerConfiguration(HdfsResource confXmlHdfsResource) {
        final InputStream confXmlInputStream = confXmlHdfsResource.read();
        JaxbConfigurationReader confReader = new JaxbConfigurationReader();
        DataCleanerConfiguration dataCleanerConfiguration = confReader.create(confXmlInputStream);

        return dataCleanerConfiguration;
    }
    
}
