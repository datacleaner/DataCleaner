package org.datacleaner.spark;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.HdfsResource;

/**
 * This utility program can be used to prepare simple manual tests. Once run,
 * use command line arguments a la:
 * 
 * <pre>
 * c:\dev\spark-1.3.1-bin-hadoop2.6\bin>spark-submit --master yarn-cluster --class org.datacleaner.spark.Main c:\Users\kaspers\git\DataCleaner\spark\target\DataCleaner-spark-4.1-SNAPSHOT-jar-with-dependencies.jar hdfs://bigdatavm:9000/user/vagrant/conf.xml hdfs://bigdatavm:9000/user/vagrant/vanilla-job.analysis.xml
 * </pre>
 */
public class CopyExampleDataToHdfs {

    private static final String HDFS_BASE_PATH = "hdfs://bigdatavm:9000/user/vagrant";

    public static void main(String[] args) throws Exception {

        // copy test files to the desired location
        copy(new File("src/test/resources/person_names.txt"), new HdfsResource(HDFS_BASE_PATH + "/person_names.txt"));
        copy(new File("src/test/resources/conf_hdfs.xml"), new HdfsResource(HDFS_BASE_PATH + "/conf.xml"));
        copy(new File("src/test/resources/vanilla-job.analysis.xml"), new HdfsResource(HDFS_BASE_PATH
                + "/vanilla-job.analysis.xml"));

        // final SparkLauncher sparkLauncher = new SparkLauncher();
        // sparkLauncher.addAppArgs("hdfs:");
        // sparkLauncher.setMaster("yarn-cluster");
        // sparkLauncher.setAppName("DataCleaner TestMain");
        // sparkLauncher.setMainClass(Main.class.getName());
        //
        // final Process process = sparkLauncher.launch();
        // final int exitCode = process.waitFor();
        // System.out.println("Exit code: " + exitCode);
    }

    private static void copy(final File file, final HdfsResource hdfsResource) {
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
