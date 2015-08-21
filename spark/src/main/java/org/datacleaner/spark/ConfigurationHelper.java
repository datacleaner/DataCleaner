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

import org.apache.metamodel.util.Func;
import org.apache.metamodel.util.HdfsResource;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.JaxbConfigurationReader;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.JaxbJobReader;

public class ConfigurationHelper {

    public static DataCleanerConfiguration readDataCleanerConfiguration(HdfsResource confXmlHdfsResource) {
        final InputStream confXmlInputStream = confXmlHdfsResource.read();
        final JaxbConfigurationReader confReader = new JaxbConfigurationReader();
        final DataCleanerConfiguration dataCleanerConfiguration = confReader.create(confXmlInputStream);

        return dataCleanerConfiguration;
    }
    
    public static AnalysisJob readAnalysisJob(final DataCleanerConfiguration dataCleanerConfiguration, HdfsResource analysisJobXmlHdfsResource) {
        final AnalysisJob analysisJob = analysisJobXmlHdfsResource.read(new Func<InputStream, AnalysisJob>() {

            @Override
            public AnalysisJob eval(InputStream in) {
                final JaxbJobReader jobReader = new JaxbJobReader(dataCleanerConfiguration);
                final AnalysisJob analysisJob = jobReader.read(in);
                return analysisJob;
            }
        });
        return analysisJob;
    }
    
}
