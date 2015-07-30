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
