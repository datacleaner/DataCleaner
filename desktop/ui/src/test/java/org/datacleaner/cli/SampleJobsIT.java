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

import static org.hamcrest.Matchers.containsString;
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

        // TODO: Because of https://github.com/datacleaner/DataCleaner/issues/1589 we don't know the exact
        // number produced by the next resultset and therefore check it less strict then desirable. Fill in
        // the exact numbers when fixing that issue.
        expectedResultSets.put("RESULT: Month distribution (birthdate (as date))",
                new String[] {"birthdate (as date)",
                        "January",
                        "February",
                        "March",
                        "April",
                        "May",
                        "June",
                        "July",
                        "August",
                        "September",
                        "October",
                        "November",
                        "December               <null>"});

        testJob("Customer age analysis", "rows processed from table: customers.csv", expectedResultSets);
    }

    @Test
    public void testCustomerFilter() throws Exception {
        final Map<String, String[]> expectedResultSets = new HashMap<>();
        expectedResultSets.put("RESULT: output-Customers-correct-data.csv (15 columns) (70.0 =< Age in years =< 80.0=LOWER)",
                new String[] { "inserts executed" });
        expectedResultSets.put("RESULT: output-Customers-age-null-or-invalid.csv (15 columns) (FilterOutcome[category=HIGHER] OR FilterOutcome[category=NULL])",
                new String[] { "inserts executed" });

        testJob("Customer filtering", "rows processed from table: customers.csv", expectedResultSets);
    }

    @Test
    public void testCustomerProfiling() throws Exception {
        final Map<String, String[]> expectedResultSets = new HashMap<>();
        expectedResultSets.put("RESULT: Unique key check (id)",
                new String[] {"Unique key check result:",
                        "- Row count: 5115",
                        "- Null count: 0",
                        "- Unique count: 5100",
                        "- Non-unique count: 15"});

        expectedResultSets.put("RESULT: Currencies (income_currency)",
                new String[] {"SingleValueDistributionResult:",
                        "- Distinct count: 6",
                        "- Null count: 0",
                        "- Total count: 5115",
                        "- Unique count: 0",
                        "- Value count (GBP): 1771",
                        "- Value count (EUR): 1511",
                        "- Value count (USD): 1271",
                        "- Value count (DKR): 513",
                        "- Value count (<blank>): 0",
                        "- Value count (n/a): 12"});

        expectedResultSets.put("RESULT: Gender matcher (gender)",
               new String[] {"ValueMatchAnalyzerResult:",
                       "- Null count: 0",
                       "- Unexpected value count: 67",
                       "- Value count (M): 2483",
                       "- Value count (F): 2122",
                       "- Value count (U): 443"});

        expectedResultSets.put("RESULT: Address completeness (address_line,post_code,city,country)",
                new String[] {"CompletenessAnalyzerResult:",
                        "- Row count: 5115",
                        "- Valid row count: 5053",
                        "- Invalid row count: 62"});

        expectedResultSets.put("RESULT: Character set distribution (given_name,family_name,company)",
                new String[] {"given_name family_name     company",
                        "Arabic                     0           0           0",
                        "Armenian                   0           0           0",
                        "Bengali                    0           0           0",
                        "Cyrillic                   0           0           0",
                        "Devanagari                 0           0           0",
                        "Georgian                   0           0           0",
                        "Greek                      3           3           0",
                        "Gujarati                   0           0           0",
                        "Gurmukhi                   0           0           0",
                        "Han                        4           4           0",
                        "Hangul                     0           0           0",
                        "Hebrew                     0           0           0",
                        "Hiragana                   0           0           0",
                        "Kannada                    0           0           0",
                        "Katakana                   0           0           0",
                        "Latin, ASCII            5104        5106        5108",
                        "Latin, non-ASCII          59         198          48",
                        "Malayalam                  0           0           0",
                        "Oriya                      0           0           0",
                        "Syriac                     0           0           0",
                        "Tamil                      0           0           0",
                        "Telugu                     0           0           0",
                        "Thaana                     0           0           0",
                        "Thai                       0           0           0"});

        expectedResultSets.put("RESULT: Names completeness (given_name,family_name,company)",
                new String[] {"CompletenessAnalyzerResult:",
                        "- Row count: 5115",
                        "- Valid row count: 5103",
                        "- Invalid row count: 12"});

        testJob("Customer profiling", "rows processed from table: customers.csv", expectedResultSets);
    }

    private static void testJob(final String jobName, final String resultStartIndicator,
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
                        assertThat(resultReader.readLine(), containsString(expectedResult));
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

    private static InputStream runJob(final String jobName) throws Exception {
        final ProcessBuilder builder = new ProcessBuilder(JAVA_EXECUTABLE, "-cp", System.getProperty("java.class.path"),
                Main.class.getCanonicalName(), "-job", URLDecoder.decode(new File(
                        "src/main/resources/datacleaner-home/jobs/" + jobName + ".analysis.xml").getPath(), "UTF-8"));

        final Process process = builder.start();

        assertEquals(0, process.waitFor());

        return process.getInputStream();
    }
}
