import junit.framework.TestCase;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.spark.SparkAnalysisRunner;
import org.datacleaner.spark.SparkJobContext;
import org.junit.Test;

public class SparkAnalysisRunnerTest extends TestCase {

    @Test
    public void testVanillaScenario() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest");
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext, "src/test/resources/conf.xml",
                    "src/test/resources/vanilla-job.analysis.xml");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);

            final SparkAnalysisRunner sparkAnalysisRunner = new SparkAnalysisRunner(sparkContext, sparkJobContext);
            sparkAnalysisRunner.run(job);
        } finally {
            sparkContext.close();
        }
    }
}
