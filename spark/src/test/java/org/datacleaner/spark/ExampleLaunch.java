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

import java.io.File;

import org.apache.spark.launcher.SparkLauncher;

public class ExampleLaunch {

    private static final String HDFS_HOSTNAME = "bigdatavm";
    private static final int HDFS_PORT = 9000;
    private static final String HDFS_JAR_LOCATION = "/datacleaner/lib";
    private static final String CONFIGURATION_LOCATION = "/datacleaner/test/conf.xml";
    private static String JOB_LOCATION = "/datacleaner/test/vanilla-job.analysis.xml";
    private static String DATA_LOCATION = "/datacleaner/test/person_names.txt";

    public static void main(String[] args) throws Exception {
        final ApplicationDriver launcher = new ApplicationDriver(HDFS_HOSTNAME, HDFS_PORT, HDFS_JAR_LOCATION);

        // copy test files to the desired location
        launcher.copyFileToHdfs(new File("src/test/resources/person_names.txt"), DATA_LOCATION, false);
        launcher.copyFileToHdfs(new File("src/test/resources/conf_hdfs.xml"), CONFIGURATION_LOCATION, false);
        launcher.copyFileToHdfs(new File("src/test/resources/vanilla-job.analysis.xml"), JOB_LOCATION, false);

        final File hadoopConfDir = launcher.createTemporaryHadoopConfDir();
        final SparkLauncher sparkLauncher = launcher.createSparkLauncher(hadoopConfDir, CONFIGURATION_LOCATION,
                JOB_LOCATION);
        final int exitCode = launcher.launch(sparkLauncher);

        System.out.println("Exit code: " + exitCode);
    }
}
