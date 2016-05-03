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
import org.datacleaner.util.FileFilters;

/**
 * Prepares job for launching on Spark
 */
public class SparkRunner {
    public interface ProgressListener {
        /**
         * Called when files are ready. This either means that they have
         * been uploaded or already been found.
         */
        void onJobFilesReady();

        /**
         * Called when jobs has been submitted and now awaits completion.
         * This is actually a bit of a lie, as it only tells us that the
         * Spark process has been created, but there is no simple way to
         * verify progress beyond that.
         */
        void onJobSubmitted();
    }

    public final static String DATACLEANER_DIR = "/datacleaner";
    public final static String DATACLEANER_TEMP_DIR = DATACLEANER_DIR + "/temp";
    public final static String DATACLEANER_LIB_DIR = DATACLEANER_DIR + "/lib";
    public static final String DEFAULT_RESULT_PATH = "/datacleaner/results";
    public static final String RESULT_FILE_EXTENSION = FileFilters.ANALYSIS_RESULT_SER.getExtension();
    

    private final FileSystem _hadoopDefaultFS;
    private final ApplicationDriver _applicationDriver;
    private final File _hadoopConfigurationDirectory;
    private final String _configurationFilePath;
    private final String _jobFilePath;
    private final String _resultFilePath;

    public SparkRunner(String configurationFilePath, String jobFilePath, String resultFilePath) throws IOException {
        _configurationFilePath = configurationFilePath;
        _jobFilePath = jobFilePath;
        _resultFilePath = resultFilePath;
        try {
            _hadoopConfigurationDirectory = HadoopUtils.getHadoopConfigurationDirectoryToUse();
            _hadoopDefaultFS = HadoopUtils.getFileSystem();
        } catch (IOException e) {
            throw new IllegalStateException("Could not create Hadoop filesystem", e);
        }
        _applicationDriver = new ApplicationDriver(_hadoopDefaultFS.getUri(), DATACLEANER_LIB_DIR);
    }

    /**
     * Tries to find a file on HDFS, and optionally uploads it if not found.
     *
     * @param filePath Path of the file to find
     * @param upload true if file upload should be attempted.
     * @return Either the resolved URI, or the uploaded file.
     */
    private URI findFile(final String filePath, final boolean upload) {
        try {
            URI fileURI;
            try {
                fileURI = _hadoopDefaultFS.getUri().resolve(filePath);
            } catch (Exception e) {
                fileURI = null;
            }

            if ((fileURI == null || !_hadoopDefaultFS.exists(new Path(fileURI))) && upload) {
                File file = new File(filePath);
                if (!file.isFile()) {
                    throw new IllegalArgumentException("'" + filePath + " does not exist, or is not a file");
                }

                String fileName = file.toPath().getFileName().toString();
                return _applicationDriver
                        .copyFileToHdfs(file, DATACLEANER_TEMP_DIR + "/" + UUID.randomUUID() + fileName);
            }

            return fileURI;
        } catch (IOException e) {
            throw new IllegalArgumentException("Path '" + filePath + "' is not a proper file path");
        }
    }

    public int runJob() throws Exception {
        return runJob(null);
    }

    public int runJob(ProgressListener listener) throws Exception {
        final URI configurationFile = findFile(_configurationFilePath, true);
        final URI jobFile = findFile(_jobFilePath, true);
        final URI resultFile;
        if (_resultFilePath != null) {
            resultFile = findFile(_resultFilePath, false);
        } else {
            resultFile = null;
        }

        if (listener != null) {
            listener.onJobFilesReady();
        }

        final SparkLauncher sparkLauncher = _applicationDriver.createSparkLauncher(_hadoopConfigurationDirectory,
                configurationFile, jobFile, resultFile);
        Process launchProcess = _applicationDriver.launchProcess(sparkLauncher);

        if (listener != null) {
            listener.onJobSubmitted();
        }

        return launchProcess.waitFor();
    }
}
