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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.MutableRef;
import org.apache.spark.launcher.SparkLauncher;
import org.datacleaner.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class ApplicationDriver {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationDriver.class);

    private static final String[] PRIMARY_JAR_FILENAME_PREFIXES = { "DataCleaner-spark", "DataCleaner-env-spark" };

    private final URI _defaultFs;
    private final DistributedFileSystem _fileSystem;
    private final String _jarDirectoryPath;
    private final String _sparkHome;

    public ApplicationDriver(final URI uri, final String jarDirectoryPath) throws IOException {
        this(uri, jarDirectoryPath, determineSparkHome());
    }

    public ApplicationDriver(final URI defaultFs, final String jarDirectoryPath, final String sparkHome)
            throws IOException {
        _defaultFs = defaultFs;
        _fileSystem = (DistributedFileSystem) FileSystem.newInstance(_defaultFs, new Configuration());
        _jarDirectoryPath = jarDirectoryPath;
        _sparkHome = sparkHome;
    }

    private static String determineSparkHome() {
        String sparkHome = System.getProperty("SPARK_HOME");

        if (Strings.isNullOrEmpty(sparkHome)) {
            sparkHome = System.getenv("SPARK_HOME");
        }

        if (Strings.isNullOrEmpty(sparkHome)) {
            throw new IllegalStateException(
                    "Could not determine SPARK_HOME. Please set the environment variable, system property or provide it as a "
                            + ApplicationDriver.class.getSimpleName() + " constructor argument");
        }

        return sparkHome;
    }

    /**
     * Launches and waits for the execution of a DataCleaner job on Spark.
     *
     * @param configurationHdfsPath
     *            configuration file path (on HDFS)
     * @param jobHdfsPath
     *            job file path (on HDFS)
     * @return the exit code of the spark-submit process
     * @throws Exception
     */
    public int launch(final String configurationHdfsPath, final String jobHdfsPath) throws Exception {
        // create hadoop configuration directory
        final File hadoopConfDir = createTemporaryHadoopConfDir();

        final SparkLauncher sparkLauncher =
                createSparkLauncher(hadoopConfDir, configurationHdfsPath, jobHdfsPath, null);

        return launch(sparkLauncher);
    }

    public int launch(final SparkLauncher sparkLauncher) throws Exception {
        final Process process = launchProcess(sparkLauncher);

        return process.waitFor();
    }

    public Process launchProcess(final SparkLauncher sparkLauncher) throws IOException {
        final Process process = sparkLauncher.launch();

        final InputStream errorStream = process.getErrorStream();
        startLogger(errorStream);

        final InputStream inputStream = process.getInputStream();
        startLogger(inputStream);
        return process;
    }

    private void startLogger(final InputStream stream) {
        new Thread() {
            public void run() {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
                    String line = br.readLine();
                    while (line != null) {
                        logger.info(line);
                        line = br.readLine();
                    }
                    br.close();
                } catch (final Exception e) {
                    logger.warn("Logger thread failure: " + e.getMessage(), e);
                }
            }
        }.start();
    }

    public HdfsResource createResource(final String hdfsPath) {
        return new HdfsResource(_defaultFs.resolve(hdfsPath).toString());
    }

    public SparkLauncher createSparkLauncher(final File hadoopConfDir, final URI configurationHdfsUri,
            final URI jobHdfsUri, final URI resultHdfsUri) throws Exception {
        return createSparkLauncher(hadoopConfDir, configurationHdfsUri.toString(), jobHdfsUri.toString(),
                resultHdfsUri == null ? null : resultHdfsUri.toString());
    }

    public SparkLauncher createSparkLauncher(final File hadoopConfDir, final String configurationHdfsPath,
            final String jobHdfsPath, final String resultHdfsPath) throws Exception {
        // mimic env. variables
        final Map<String, String> env = new HashMap<>();
        env.put("HADOOP_CONF_DIR", hadoopConfDir.getAbsolutePath());
        env.put("YARN_CONF_DIR", hadoopConfDir.getAbsolutePath());

        final SparkLauncher sparkLauncher = new SparkLauncher(env);

        sparkLauncher.setSparkHome(_sparkHome);
        sparkLauncher.setMaster("yarn-cluster");
        sparkLauncher.setAppName("DataCleaner");

        final MutableRef<String> primaryJar = new MutableRef<>();
        final List<String> jars = buildJarFiles(primaryJar);
        logger.info("Using JAR files: {}", jars);

        for (final String jar : jars) {
            sparkLauncher.addJar(jar);
        }

        sparkLauncher.setMainClass(Main.class.getName());
        sparkLauncher.setConf("spark.serializer", "org.apache.spark.serializer.JavaSerializer");

        // the primary jar is always the first argument
        sparkLauncher.addAppArgs(primaryJar.get());

        sparkLauncher.addAppArgs(toHadoopPath(configurationHdfsPath));
        sparkLauncher.addAppArgs(toHadoopPath(jobHdfsPath));

        if (!StringUtils.isNullOrEmpty(resultHdfsPath)) {
            final Properties properties = new Properties();
            properties.setProperty("datacleaner.result.hdfs.path", resultHdfsPath);
            final File tempFile = File.createTempFile("job-", ".properties");
            properties.store(new FileWriter(tempFile), "DataCleaner Spark runner properties");
            final URI uri = copyFileToHdfs(tempFile,
                    _fileSystem.getHomeDirectory().toUri().resolve("temp/" + tempFile.getName()).toString());
            sparkLauncher.addAppArgs(uri.toString());
        }

        return sparkLauncher;
    }

    private String toHadoopPath(final String path) {
        if (URI.create(path).getScheme() != null) {
            return path;
        }

        return _defaultFs.resolve(path).toString();
    }

    private List<String> buildJarFiles(final MutableRef<String> primaryJarRef) throws IOException {
        final List<String> list = new ArrayList<>();

        final Path directoryPath = new Path(_jarDirectoryPath);
        final RemoteIterator<LocatedFileStatus> files = _fileSystem.listFiles(directoryPath, false);
        while (files.hasNext()) {
            final LocatedFileStatus file = files.next();
            final Path path = file.getPath();
            final String filename = path.getName();
            boolean primaryJar = false;
            for (final String prefix : PRIMARY_JAR_FILENAME_PREFIXES) {
                if (filename.startsWith(prefix)) {
                    primaryJarRef.set(path.toString());
                    primaryJar = true;
                    break;
                }
            }
            if (!primaryJar) {
                list.add(path.toString());
            }
        }

        if (primaryJarRef.get() == null) {
            throw new IllegalArgumentException(
                    "Failed to find primary jar (starting with '" + PRIMARY_JAR_FILENAME_PREFIXES[0]
                            + "') in JAR file directory: " + _jarDirectoryPath);
        }

        return list;
    }

    public File createTemporaryHadoopConfDir() throws IOException {
        final File hadoopConfDir =
                new File(FileHelper.getTempDir(), "datacleaner_hadoop_conf_" + UUID.randomUUID().toString());
        final boolean dirCreated = hadoopConfDir.mkdirs();
        assert dirCreated;

        createTemporaryHadoopConfFile(hadoopConfDir, "core-site.xml", "core-site-template.xml");
        createTemporaryHadoopConfFile(hadoopConfDir, "yarn-site.xml", "yarn-site-template.xml");

        logger.debug("Created temporary Hadoop conf dir: {}", hadoopConfDir);

        return hadoopConfDir;
    }

    private void createTemporaryHadoopConfFile(final File hadoopConfDir, final String filename,
            final String templateName) throws IOException {
        final File coreSiteFile = new File(hadoopConfDir, filename);
        try (InputStream inputStream = getClass().getResourceAsStream(templateName)) {
            final BufferedReader reader = FileHelper.getBufferedReader(inputStream, FileHelper.UTF_8_ENCODING);
            try (Writer writer = FileHelper.getWriter(coreSiteFile)) {
                String line = reader.readLine();
                while (line != null) {
                    line = StringUtils.replaceAll(line, "${HDFS_HOSTNAME}", _defaultFs.getHost());
                    line = StringUtils.replaceAll(line, "${HDFS_PORT}", _defaultFs.getPort() + "");
                    writer.write(line);

                    line = reader.readLine();
                }
                writer.flush();
            }
        }
    }

    public URI copyFileToHdfs(final File file, final String hdfsPath) {
        return copyFileToHdfs(file, hdfsPath, true);
    }

    public URI copyFileToHdfs(final File file, final String hdfsPath, final boolean overwrite) {
        final HdfsResource hdfsResource = createResource(hdfsPath);
        final URI uri = hdfsResource.getHadoopPath().toUri();
        final boolean exists = hdfsResource.isExists();
        if (!overwrite && exists) {
            // no need to copy
            logger.debug("Skipping file-copy to {} because file already exists", hdfsPath);
            return uri;
        }

        if (exists) {
            logger.info("Overwriting file on HDFS: {}", hdfsPath);
        } else {
            logger.debug("Copying file to HDFS: {}", hdfsPath);
        }

        hdfsResource.write(out -> {
            final FileInputStream in = new FileInputStream(file);
            FileHelper.copy(in, out);
            in.close();
        });

        return uri;
    }
}
