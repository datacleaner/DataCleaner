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
package org.datacleaner.cli;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.datacleaner.Main;
import org.junit.Test;

public class SampleJobsIT {
    private static final String JAVA_EXECUTABLE = System.getProperty("java.home") + File.separator + "bin"
            + File.separator + "java";

    @Test
    public void testCopyEmployeesToCustomerTable() throws Exception {
        final Map<String, String[]> expectedResultSets = new HashMap<>();
        expectedResultSets.put("RESULT: orderdb - CUSTOMERS (19 columns)",
                new String[] {"23 inserts executed"});

        testJob("Copy employees to customer table", "rows processed from table: EMPLOYEES", expectedResultSets);
    }

    @Test
    public void testCustomerAgeAnalysis() throws Exception {
        final Map<String, String[]> expectedResultSets = new HashMap<>();
        expectedResultSets.put("RESULT: Birthdate Patterns (birthdate)",
                new String[] {"Match count Sample",
                        "####-##-##        4661",
                        "####.##.##         427",
                        "a/a                 27"});
        expectedResultSets.put("RESULT: Number analyzer (Age in years)",
                new String[] {"Age in years",
                        "Row count                  5115",
                        "Null count                   29",
                        "Highest value",
                        "Lowest value",
                        "Sum",
                        "Mean",
                        "Geometric mean",
                        "Standard deviation        21.04",
                        "Variance",
                        "Second moment",
                        "Sum of squares",
                        "Median",
                        "25th percentile",
                        "75th percentile",
                        "Skewness",
                        "Kurtosis"});
        expectedResultSets.put("RESULT: Month distribution (birthdate (as date))",
                new String[] {"birthdate (as date)",
                        "January                   460",
                        "February                  436",
                        "March                     493",
                        "April                     432",
                        "May                       467",
                        "June                      469",
                        "July                      480",
                        "August                    473",
                        "September                 492",
                        "October                   427",
                        "November                  457",
                        "December               <null>"});

        testJob("Customer age analysis", "rows processed from table: customers.csv", expectedResultSets);
    }

    private void testJob(final String jobName, final String resultStartIndicator,
            final Map<String, String[]> expectedResultSets) throws Exception {
        final InputStream resultInputStream = runJob(jobName);
        final InputStreamReader resultInputStreamReader = new InputStreamReader(resultInputStream);
        final BufferedReader resultReader = new BufferedReader(resultInputStreamReader);

        try {
            String resultLine;

            // Read the output line by line until we see an indicator that the interesting part of the output
            // is coming up.
            while ((resultLine = resultReader.readLine()) != null && !resultLine.endsWith(resultStartIndicator)) {
                // Ignore.
            }

            // First check that the job was run successfully.
            assertEquals("SUCCESS!", resultReader.readLine());

            // Now iterate over the different expected result sets and see if they're valid.
            while ((resultLine = resultReader.readLine()) != null) {
                final String resultKey = resultLine.trim();
                if (!"".equals(resultKey)) {
                    String[] expectedResultSet = expectedResultSets.get(resultKey);

                    assertNotNull(expectedResultSet);

                    for (String expectedResult : expectedResultSet) {
                        // Only check the first part of the line, because numbers at the end may differ based
                        // on the moment in time the test runs at.
                        assertThat(resultReader.readLine().trim(), startsWith(expectedResult.trim()));
                    }
                    expectedResultSets.remove(resultKey);
                }
            }

            assertEquals(0, expectedResultSets.size());
        } finally {
            resultReader.close();
            resultInputStreamReader.close();
            resultInputStream.close();
        }
    }

    private InputStream runJob(final String jobName) throws Exception {
        final ProcessBuilder builder = new ProcessBuilder(JAVA_EXECUTABLE, "-cp", System.getProperty("java.class.path"),
                Main.class.getCanonicalName(), "-job", URLDecoder.decode(new File(
                        "src/main/resources/datacleaner-home/jobs/" + jobName + ".analysis.xml").getPath(), "UTF-8"));

        final Process process = builder.start();

        assertEquals(0, process.waitFor());

        return process.getInputStream();
    }
}
