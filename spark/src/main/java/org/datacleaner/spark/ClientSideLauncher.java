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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.MutableRef;
import org.apache.spark.launcher.SparkLauncher;
import org.datacleaner.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSideLauncher {

    private static final Logger logger = LoggerFactory.getLogger(ClientSideLauncher.class);

    private static final String PRIMARY_JAR_FILENAME_PREFIX = "DataCleaner-spark";

    private final String _hostname;
    private final int _port;
    private final String _jarDirectoryPath;

    public ClientSideLauncher(String hostname, int port, String jarDirectoryPath) {
        _hostname = hostname;
        _port = port;
        _jarDirectoryPath = jarDirectoryPath;
    }

    public int launch(String configurationHdfsPath, String jobHdfsPath) throws Exception {
        // create hadoop configuration directory
        final File hadoopConfDir = createTemporaryHadoopConfDir();

        final SparkLauncher sparkLauncher = createSparkLauncher(hadoopConfDir, configurationHdfsPath, jobHdfsPath);

        return launch(sparkLauncher);
    }

    public int launch(SparkLauncher sparkLauncher) throws Exception {
        final Process process = sparkLauncher.launch();

        final InputStream errorStream = process.getErrorStream();
        startLogger(errorStream);

        final InputStream inputStream = process.getInputStream();
        startLogger(inputStream);

        return process.waitFor();
    }

    private void startLogger(final InputStream stream) {
        new Thread() {
            public void run() {
                try (final BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
                    String line = br.readLine();
                    while (line != null) {
                        logger.info(line);
                        line = br.readLine();
                    }
                    br.close();
                } catch (Exception e) {
                    logger.warn("Logger thread failure: " + e.getMessage(), e);
                }
            };
        }.start();
    }

    public HdfsResource createResource(String hdfsPath) {
        return new HdfsResource(_hostname, _port, hdfsPath);
    }

    public SparkLauncher createSparkLauncher(File hadoopConfDir, String configurationHdfsPath, String jobHdfsPath)
            throws Exception {
        // mimic env. variables
        final Map<String, String> env = new HashMap<>();
        env.put("YARN_CONF_DIR", hadoopConfDir.getAbsolutePath());

        final SparkLauncher sparkLauncher = new SparkLauncher(env);

        // TODO: Remove this hardcoded stuff
        sparkLauncher.setSparkHome("C:\\dev\\spark-1.3.1-bin-hadoop2.6");
        sparkLauncher.setMaster("yarn-cluster");
        sparkLauncher.setAppName("DataCleaner");

        final MutableRef<String> primaryJar = new MutableRef<>();
        final List<String> jars = buildJarFiles(primaryJar);
        logger.info("Using JAR files: {}", jars);

        for (final String jar : jars) {
            sparkLauncher.addJar(jar);
        }
        sparkLauncher.setMainClass(Main.class.getName());

        // the primary jar is always the first argument
        sparkLauncher.addAppArgs(primaryJar.get());

        sparkLauncher.addAppArgs(toHdfsPath(configurationHdfsPath));
        sparkLauncher.addAppArgs(toHdfsPath(jobHdfsPath));

        return sparkLauncher;
    }

    private String toHdfsPath(String path) {
        if (path.startsWith("hdfs://")) {
            return path;
        }
        return "hdfs://" + _hostname + ":" + _port + path;
    }

    private List<String> buildJarFiles(MutableRef<String> primaryJarRef) throws IOException {
        final List<String> list = new ArrayList<>();

        final Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://" + _hostname + ":" + _port);

        final FileSystem fs = FileSystem.newInstance(conf);
        try {
            final Path directoryPath = new Path(_jarDirectoryPath);
            final RemoteIterator<LocatedFileStatus> files = fs.listFiles(directoryPath, false);
            while (files.hasNext()) {
                final LocatedFileStatus file = files.next();
                final Path path = file.getPath();
                final String filename = path.getName();
                if (filename.startsWith(PRIMARY_JAR_FILENAME_PREFIX)) {
                    primaryJarRef.set(path.toString());
                } else {
                    list.add(path.toString());
                }
            }
        } finally {
            FileHelper.safeClose(fs);
        }

        if (primaryJarRef.get() == null) {
            throw new IllegalArgumentException("Failed to find primary jar (starting with '"
                    + PRIMARY_JAR_FILENAME_PREFIX + "') in JAR file directory: " + _jarDirectoryPath);
        }

        return list;
    }

    public File createTemporaryHadoopConfDir() throws IOException {
        final File hadoopConfDir = new File(FileHelper.getTempDir(), "datacleaner_hadoop_conf_"
                + UUID.randomUUID().toString());
        hadoopConfDir.mkdirs();

        final File coreSiteFile = new File(hadoopConfDir, "core-site.xml");
        try (final InputStream inputStream = getClass().getResourceAsStream("core-site-template.xml")) {
            final BufferedReader reader = FileHelper.getBufferedReader(inputStream, FileHelper.UTF_8_ENCODING);
            try (final Writer writer = FileHelper.getWriter(coreSiteFile)) {
                String line = reader.readLine();
                while (line != null) {
                    line = StringUtils.replaceAll(line, "${HOSTNAME}", _hostname);
                    line = StringUtils.replaceAll(line, "${PORT}", _port + "");
                    writer.write(line);

                    line = reader.readLine();
                }
            }
        }
        return hadoopConfDir;
    }

    public void copyFileToHdfs(File file, String hdfsPath) {
        copyFileToHdfs(file, hdfsPath, true);
    }

    public void copyFileToHdfs(final File file, final String hdfsPath, final boolean overwrite) {
        final HdfsResource hdfsResource = createResource(hdfsPath);
        if (!overwrite && hdfsResource.isExists()) {
            // no need to copy
            return;
        }
        hdfsResource.write(new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                final FileInputStream in = new FileInputStream(file);
                FileHelper.copy(in, out);
                in.close();
            }
        });
    }
}
