package org.datacleaner.spark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.HdfsResource;
import org.apache.spark.launcher.SparkLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility program can be used to prepare simple manual tests. Once run,
 * use command line arguments a la:
 * 
 * <pre>
 * c:\dev\spark-1.3.1-bin-hadoop2.6\bin>spark-submit --master yarn-cluster --class org.datacleaner.spark.Main 
 * \ c:\Users\kaspers\git\DataCleaner\spark\target\DataCleaner-spark-4.1-SNAPSHOT-jar-with-dependencies.jar
 * \ hdfs://bigdatavm:9000/user/vagrant/conf.xml 
 * \ hdfs://bigdatavm:9000/user/vagrant/vanilla-job.analysis.xml
 * </pre>
 */
public class CopyExampleDataToHdfs {

    private static final Logger logger = LoggerFactory.getLogger(CopyExampleDataToHdfs.class);

    private static final String HDFS_HOSTNAME = "bigdatavm";
    private static final int HDFS_PORT = 9000;
    private static final String HDFS_JAR_LOCATION = "/user/vagrant/dcjars";
    private static final String HDFS_BASE_PATH = "hdfs://" + HDFS_HOSTNAME + ":" + HDFS_PORT + "/user/vagrant";

    public static void main(String[] args) throws Exception {
        // copy test files to the desired location
        copyIfNotExists(new File("src/test/resources/person_names.txt"), new HdfsResource(HDFS_BASE_PATH
                + "/person_names.txt"));
        copyIfNotExists(new File("src/test/resources/conf_hdfs.xml"), new HdfsResource(HDFS_BASE_PATH + "/conf.xml"));
        copyIfNotExists(new File("src/test/resources/vanilla-job.analysis.xml"), new HdfsResource(HDFS_BASE_PATH
                + "/vanilla-job.analysis.xml"));

        // mimic env. variables
        final Map<String, String> env = new HashMap<>();
        env.put("YARN_CONF_DIR", new File("src/test/resources/yarn_conf_dir").getAbsolutePath());

        final SparkLauncher sparkLauncher = new SparkLauncher(env);
        sparkLauncher.setSparkHome("C:\\dev\\spark-1.3.1-bin-hadoop2.6");
        sparkLauncher.setMaster("yarn-cluster");
        sparkLauncher.setAppName("DataCleaner TestMain");

        final List<String> jars = getJarFilesOnHdfs();
        logger.info("Using JAR files: {}", jars);

        for (String jar : jars) {
            sparkLauncher.addJar(jar);
        }
        sparkLauncher.setVerbose(true);
        sparkLauncher.setMainClass(Main.class.getName());

        // sparkLauncher.addAppArgs(HDFS_BASE_PATH + "/datacleaner_full.jar");
        sparkLauncher.addAppArgs(HDFS_BASE_PATH + "/dcjars/DataCleaner-spark-4.1-SNAPSHOT-jar-with-dependencies.jar");
        sparkLauncher.addAppArgs(HDFS_BASE_PATH + "/conf.xml");
        sparkLauncher.addAppArgs(HDFS_BASE_PATH + "/vanilla-job.analysis.xml");

        final Process process = sparkLauncher.launch();
        printProcessOutput(process.getInputStream());
        printProcessOutput(process.getErrorStream());

        final int exitCode = process.waitFor();
        System.out.println("Exit code: " + exitCode);
    }

    private static List<String> getJarFilesOnHdfs() throws IOException {
        final List<String> list = new ArrayList<>();

        final Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://" + HDFS_HOSTNAME + ":" + HDFS_PORT);

        final FileSystem fs = FileSystem.newInstance(conf);
        try {
            final Path directoryPath = new Path(HDFS_JAR_LOCATION);
            final RemoteIterator<LocatedFileStatus> files = fs.listFiles(directoryPath, false);
            while (files.hasNext()) {
                final LocatedFileStatus file = files.next();
                final Path path = file.getPath();
                list.add(path.toString());
            }
        } finally {
            FileHelper.safeClose(fs);
        }
        return list;
    }

    private static void printProcessOutput(final InputStream in) {
        new Thread() {
            public void run() {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String line = br.readLine();
                    while (line != null) {
                        System.out.println(line);
                        line = br.readLine();
                    }
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }.start();
    }

    private static void copyIfNotExists(final File file, final HdfsResource hdfsResource) {
        if (hdfsResource.isExists()) {
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
