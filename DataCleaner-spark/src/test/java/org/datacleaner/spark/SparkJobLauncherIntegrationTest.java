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

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.job.NoSuchDatastoreException;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SparkJobLauncherIntegrationTest {

    @Test
    public void testGetInputDatastorePathPositive() {
        String confXmlPath = "hdfs://bigdatavm" + ":" + "9000" + "/user/vagrant/conf.xml";
        String analysisJobXmlPath = "hdfs://bigdatavm" + ":" + "9000" + "/user/vagrant/hdfs-job.analysis.xml";
        SparkJobLauncher sparkJobLauncher = new SparkJobLauncher(confXmlPath);
        
        final DataCleanerConfiguration dataCleanerConfiguration = sparkJobLauncher.getDataCleanerConfiguration();
        assertNotNull(dataCleanerConfiguration);
        assertTrue(dataCleanerConfiguration.getDatastoreCatalog().containsDatastore("Instructions.txt"));
        
        sparkJobLauncher.launchJob(analysisJobXmlPath);
    }
    
    @Test(expected = NoSuchDatastoreException.class)
    public void testGetInputDatastorePathNegative() {
        String confXmlPath = "hdfs://bigdatavm" + ":" + "9000" + "/user/vagrant/conf.xml";
        String analysisJobXmlPath = "hdfs://bigdatavm" + ":" + "9000" + "/user/vagrant/hdfs-job-datastore-not-exist.analysis.xml";
        SparkJobLauncher sparkJobLauncher = new SparkJobLauncher(confXmlPath);
        
        final DataCleanerConfiguration dataCleanerConfiguration = sparkJobLauncher.getDataCleanerConfiguration();
        assertNotNull(dataCleanerConfiguration);
        assertFalse(dataCleanerConfiguration.getDatastoreCatalog().containsDatastore("Non existent name"));
        
        sparkJobLauncher.launchJob(analysisJobXmlPath);
    }

}
