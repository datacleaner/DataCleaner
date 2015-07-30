package org.datacleaner.spark;

import org.datacleaner.configuration.DataCleanerConfiguration;
import static org.junit.Assert.*;
import org.junit.Test;

public class SparkJobLauncherIntegrationTest {

    @Test
    public void testReadConfXmlFromHdfs() {
        String confXmlPath = "hdfs://bigdatavm" + ":" + "9000" + "/user/vagrant/conf.xml";
        SparkJobLauncher sparkJobLauncher = new SparkJobLauncher(confXmlPath);
        final DataCleanerConfiguration dataCleanerConfiguration = sparkJobLauncher.getDataCleanerConfiguration();
        assertNotNull(dataCleanerConfiguration);
        assertTrue(dataCleanerConfiguration.getDatastoreCatalog().containsDatastore("Instructions.txt"));
    }

}
