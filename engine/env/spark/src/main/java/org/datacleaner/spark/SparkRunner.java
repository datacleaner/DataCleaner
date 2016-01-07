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
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.launcher.SparkLauncher;
import org.datacleaner.spark.utils.HadoopUtils;

/**
 * Prepares job for launching on Spark
 */
public class SparkRunner {
    public final static String DATACLEANER_DIR = "/datacleaner";
    public final static String DATACLEANER_TEMP_DIR = DATACLEANER_DIR + "/temp";
    public final static String DATACLEANER_LIB_DIR = DATACLEANER_DIR + "/lib";

    private final FileSystem _hadoopDefaultFS;
    private final ApplicationDriver _applicationDriver;
    private final URI _configurationFile;
    private final URI _jobFile;
    private final URI _resultFile;
    private final File _hadoopConfigurationDirectory;

    public SparkRunner(String configurationFilePath, String jobFilePath, String resultFilePath) throws IOException {
        try {
            _hadoopConfigurationDirectory = HadoopUtils.getHadoopConfigurationDirectoryToUse();
            _hadoopDefaultFS = HadoopUtils.getFileSystem();
        } catch (IOException e) {
            throw new IllegalStateException("Could not create Hadoop filesystem", e);
        }
        _applicationDriver = new ApplicationDriver(_hadoopDefaultFS.getUri(), DATACLEANER_LIB_DIR);
        _configurationFile = findFile(configurationFilePath, true);
        _jobFile = findFile(jobFilePath, true);
        if(resultFilePath != null) {
            _resultFile = findFile(resultFilePath, false);
        } else {
            _resultFile = null;
        }
    }

    /**
     * Tries to find a file on HDFS, and optionally uploads it if not found.
     *
     * @param filePath Path of the file to find
     * @param upload true if file upload should be attempted.
     * @return Either the resolved URI, or the uploaded file.
     */
    private URI findFile(final String filePath, final boolean upload){
        try {
            URI fileURI;
            try {
                fileURI = _hadoopDefaultFS.getUri().resolve(filePath);
            } catch(Exception e){
                fileURI = null;
            }

            if ((fileURI == null ||!_hadoopDefaultFS.exists(new Path(fileURI))) && upload) {
                File file = new File(filePath);
                if(!file.isFile()){
                    throw new IllegalArgumentException("'" + filePath + " does not exist, or is not a file");
                }

                String fileName = file.toPath().getFileName().toString();
                return _applicationDriver.copyFileToHdfs(file, DATACLEANER_TEMP_DIR + "/" + UUID.randomUUID() + fileName);
            }

            return fileURI;
        } catch(IOException e){
            throw new IllegalArgumentException("Path '" + filePath + "' is not a proper file path");
        }
    }

    public int runJob() throws Exception{
        final SparkLauncher sparkLauncher = _applicationDriver.createSparkLauncher(_hadoopConfigurationDirectory, _configurationFile,
                _jobFile, _resultFile);
        return _applicationDriver.launch(sparkLauncher);
    }
}
