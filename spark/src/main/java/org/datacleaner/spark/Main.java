package org.datacleaner.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("The number of arguments is incorrect. Usage:\n"
                    + " <path_to_configuration_xml_file_in_hdfs> <path_to_analysis_job_xml_file_in_hdfs>");
        }

        final SparkConf conf = new SparkConf().setAppName("DataCleaner-spark");
        final JavaSparkContext sparkContext = new JavaSparkContext(conf);

        final String confXmlPath = args[0];
        final String analysisJobXmlPath = args[1];

        final SparkJobContext sparkDataCleanerContext = new SparkJobContext(sparkContext, confXmlPath,
                analysisJobXmlPath);

        final SparkAnalysisRunner sparkJobLauncher = new SparkAnalysisRunner(sparkContext, sparkDataCleanerContext);
        sparkJobLauncher.run();

        sparkContext.stop();
    }
}
