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
package org.datacleaner.spark.utils;

import org.apache.metamodel.util.Resource;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.spark.SparkJobContext;
import org.datacleaner.util.FileFilters;

import com.google.common.base.Strings;

public class ResultFilePathUtils {

    private static final String DEFAULT_RESULT_PATH = "/datacleaner/results";
    private static final String RESULT_FILE_EXTENSION = FileFilters.ANALYSIS_RESULT_SER.getExtension();

    /**
     * Gets the Resource to use for the job's result. The path can be configured
     * in the job properties file with property 'datacleaner.result.hdfs.path'.
     * The path can be absolute(
     * 'hdfs://[hostname]:[port]/myresults/myjob.analysis.result.dat') or
     * relative('/myresults/myjob.analysis.result.dat'). If no path is set in
     * the properties file and the system is configured to save the results, the
     * default path will be saved to
     * '/datacleaner/results/[jobname]-[timestamp].analysis.result.dat
     * 
     * @return a Resource to use
     */
    public static Resource getResultResource(final JavaSparkContext sparkContext,
            final SparkJobContext sparkJobContext) {
        final HdfsHelper hdfsHelper = new HdfsHelper(sparkContext);

        final String resultPath;
        if (Strings.isNullOrEmpty(sparkJobContext.getResultPath())) {
            resultPath = DEFAULT_RESULT_PATH + '/' + generateResultFilename(sparkJobContext);
        } else {
            if (hdfsHelper.isDirectory(sparkJobContext.getResultPath())) {
                if (sparkJobContext.getResultPath().endsWith("/")) {
                    resultPath = sparkJobContext.getResultPath() + generateResultFilename(sparkJobContext);
                } else {
                    resultPath = sparkJobContext.getResultPath() + '/' + generateResultFilename(sparkJobContext);
                }
            } else {
                resultPath = sparkJobContext.getResultPath();
            }
        }

        return hdfsHelper.getResourceToUse(resultPath);
    }

    private static String generateResultFilename(SparkJobContext sparkJobContext) {
        return sparkJobContext.getJobName() + "-" + System.currentTimeMillis() + RESULT_FILE_EXTENSION;
    }

}
