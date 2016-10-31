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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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
                        "December"});

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

    @Test
    public void testDenormalizeOrderTotalsAndPresentAsStackedAreaChart() throws Exception {
        final Map<String, String[]> expectedResultSets = new HashMap<>();
        expectedResultSets.put("RESULT: Stacked area plot (13 columns)",
                new String[] {"JavaStackedAreaAnalyzerResult:",
                        "(no metrics)"});

        testJob("Denormalize order totals and present as stacked area chart", "rows processed from table: output", expectedResultSets);
    }

    @Test
    public void testExportOfOrdersDataMart() throws Exception {
        final Map<String, String[]> expectedResultSets = new HashMap<>();
        expectedResultSets.put("RESULT: orders_w_dimensions.csv (10 columns)",
                new String[] {"2996 inserts executed"});

        expectedResultSets.put("RESULT: Lookup customers",
                new String[] {"CategorizationResult:",
                        "- Category count (Match):",
                        "- Category count (Miss): 0",
                        "- Category count (Cached):"});

        // Because the Country standardizer renders its results in a random order, we can't validate the
        // actual number for each country.
        expectedResultSets.put("RESULT: Country standardizer",
                new String[] {"CountryStandardizationResult:",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count (",
                        "- Category count ("});

        expectedResultSets.put("RESULT: Lookup products",
                new String[] {"CategorizationResult:",
                        "- Category count (Match):",
                        "- Category count (Miss): 0",
                        "- Category count (Cached):"});

        testJob("Export of Orders data mart", "rows processed from table: ORDERFACT", expectedResultSets);
    }

    @Test
    public void testJobTitleAnalytics() throws Exception {
        final Map<String, String[]> expectedResultSets = new HashMap<>();

        expectedResultSets.put("RESULT: Unrecognized job titles (Trimmed job title)",
                new String[] {"SingleValueDistributionResult:",
                        "- Distinct count: 172",
                        "- Null count: 0",
                        "- Total count: 1739",
                        "- Unique count: 5",
                        "- Value count (Data architect): 103",
                        "- Value count (Sr.): 61",
                        "- Value count (Software engineer): 59",
                        "- Value count (Data analyst): 58",
                        "- Value count (Business analyst): 57",
                        "- Value count (Project manager): 52",
                        "- Value count (Senior consultant): 45",
                        "- Value count (Bi consultant): 35",
                        "- Value count (It manager): 32",
                        "- Value count (Dr.): 30",
                        "- Value count (Ing.): 30",
                        "- Value count (Ingeniero): 30",
                        "- Value count (Bi architect): 29",
                        "- Value count (N/a): 28",
                        "- Value count (Associate): 24",
                        "- Value count (Managing director): 24",
                        "- Value count (Solution architect): 24",
                        "- Value count (Software developer): 21",
                        "- Value count (Data manager): 20",
                        "- Value count (Product manager): 20",
                        "- Value count (Bi manager): 19",
                        "- Value count (Data warehouse architect): 19",
                        "- Value count (Ms.): 19",
                        "- Value count (Sr. consultant): 19",
                        "- Value count (Enterprise architect): 18",
                        "- Value count (Information architect): 18",
                        "- Value count (Senior data architect): 16",
                        "- Value count (Technical architect): 16",
                        "- Value count (Etl developer): 15",
                        "- Value count (Sql developer): 15",
                        "- Value count (Database manager): 14",
                        "- Value count (Systems engineer): 14",
                        "- Value count (Bi specialist): 13",
                        "- Value count (Data scientist): 13",
                        "- Value count (Development manager): 12",
                        "- Value count (Director of technology): 12",
                        "- Value count (Ing sistemas): 12",
                        "- Value count (It consultant): 12",
                        "- Value count (Mr.): 12",
                        "- Value count (Senior software engineer): 12",
                        "- Value count (System analyst): 12",
                        "- Value count (It director): 11",
                        "- Value count (Web developer): 11",
                        "- Value count (Application developer): 10",
                        "- Value count (Bi developer): 10",
                        "- Value count (Dhr.): 10",
                        "- Value count (It specialist): 10",
                        "- Value count (Sr business analyst): 10",
                        "- Value count (Head of data management): 9",
                        "- Value count (Solutions architect): 9",
                        "- Value count (Chief architect): 8",
                        "- Value count (Contractor): 8",
                        "- Value count (Data consultant): 8",
                        "- Value count (Data specialist): 8",
                        "- Value count (Dq consultant): 8",
                        "- Value count (Employee): 8",
                        "- Value count (Etudiant): 8",
                        "- Value count (General manager): 8",
                        "- Value count (Graphic designer): 8",
                        "- Value count (Marketing specialist): 8",
                        "- Value count (Network admin): 8",
                        "- Value count (Senior developer): 8",
                        "- Value count (Senior director): 8",
                        "- Value count (Software architect): 8",
                        "- Value count (Vp technology): 8",
                        "- Value count (Digital marketing manager): 7",
                        "- Value count (Information systems engineer): 7",
                        "- Value count (Ingeniera): 7",
                        "- Value count (It administrator): 7",
                        "- Value count (It architect): 7",
                        "- Value count (Proprietor): 7",
                        "- Value count (Staff engineer): 7",
                        "- Value count (Technical lead): 7",
                        "- Value count (Avp): 6",
                        "- Value count (Bsa): 6",
                        "- Value count (Crm manager): 6",
                        "- Value count (D): 6",
                        "- Value count (Data quality manager): 6",
                        "- Value count (Database administrator): 6",
                        "- Value count (Eng.): 6",
                        "- Value count (Enginner): 6",
                        "- Value count (Jefe de proyectos): 6",
                        "- Value count (Qq): 6",
                        "- Value count (Senior engineer): 6",
                        "- Value count (Sir): 6",
                        "- Value count (Sqa manager): 6",
                        "- Value count (Sr consultant): 6",
                        "- Value count (Sr. data architect): 6",
                        "- Value count (Sr. software engineer): 6",
                        "- Value count (System engineer): 6",
                        "- Value count (Bi director): 5",
                        "- Value count (Business data analyst): 5",
                        "- Value count (Engneer): 5",
                        "- Value count (It support): 5",
                        "- Value count (Kkk): 5",
                        "- Value count (Marketing director): 5",
                        "- Value count (Monsieur): 5",
                        "- Value count (Principal consultant): 5",
                        "- Value count (Programmer/analyst): 5",
                        "- Value count (Project lead): 5",
                        "- Value count (R): 5",
                        "- Value count (Sr manager): 5",
                        "- Value count (Technical consultant): 5",
                        "- Value count (Admin): 4",
                        "- Value count (Advisor): 4",
                        "- Value count (Analista de sistemas): 4",
                        "- Value count (Data and information quality specialist): 4",
                        "- Value count (Data governance manager): 4",
                        "- Value count (Data warehouse designer): 4",
                        "- Value count (Database architect): 4",
                        "- Value count (Ea): 4",
                        "- Value count (It-consultant): 4",
                        "- Value count (Lead): 4",
                        "- Value count (Lecturer): 4",
                        "- Value count (Leon): 4",
                        "- Value count (Manage): 4",
                        "- Value count (Managing partner): 4",
                        "- Value count (Mgr): 4",
                        "- Value count (Network administrator): 4",
                        "- Value count (Qa analyst): 4",
                        "- Value count (Rd): 4",
                        "- Value count (Research officer): 4",
                        "- Value count (Sdf): 4",
                        "- Value count (Senior analyst): 4",
                        "- Value count (Senior associate): 4",
                        "- Value count (Sr. data analyst): 4",
                        "- Value count (Sr. manager): 4",
                        "- Value count (Sys admin): 4",
                        "- Value count (System admin): 4",
                        "- Value count (Systems architect): 4",
                        "- Value count (Administrador): 3",
                        "- Value count (Assistant manager): 3",
                        "- Value count (Bi analyst): 3",
                        "- Value count (Business development): 3",
                        "- Value count (C): 3",
                        "- Value count (Computer engineer): 3",
                        "- Value count (Data cleanser): 3",
                        "- Value count (Data quality analyst): 3",
                        "- Value count (Developper): 3",
                        "- Value count (Director informatics): 3",
                        "- Value count (Director of business intelligence): 3",
                        "- Value count (Estudante): 3",
                        "- Value count (Gerente): 3",
                        "- Value count (Head of marketing): 3",
                        "- Value count (Manager bi): 3",
                        "- Value count (Market analyst): 3",
                        "- Value count (Master): 3",
                        "- Value count (Miner): 3",
                        "- Value count (Oracle dba): 3",
                        "- Value count (Personal): 3",
                        "- Value count (Program manager): 3",
                        "- Value count (Senior specialist): 3",
                        "- Value count (Sm): 3",
                        "- Value count (Specialist): 3",
                        "- Value count (The man): 3",
                        "- Value count (Abcd): 2",
                        "- Value count (Hello): 2",
                        "- Value count (It analyst): 2",
                        "- Value count (Managing consultant): 2",
                        "- Value count (Mdm analyst): 2",
                        "- Value count (Mrs.): 2",
                        "- Value count (Pepe): 2",
                        "- Value count (Project leader): 2",
                        "- Value count (Sales engineer): 2",
                        "- Value count (Senior business analyst): 2",
                        "- Value count (Solution director): 2",
                        "- Value count (Tech arch): 2"});

        expectedResultSets.put("RESULT: Job title distribution (Recognized job title) (Is job title recognized?=NOT_NULL)",
                new String[] {"SingleValueDistributionResult:",
                        "- Distinct count: 14",
                        "- Null count: 0",
                        "- Total count: 3376",
                        "- Unique count: 0",
                        "- Value count ((dirt)): 585",
                        "- Value count (Consultant): 578",
                        "- Value count (Executive): 548",
                        "- Value count (Student/Intern): 467",
                        "- Value count (Software developer): 439",
                        "- Value count ((honorific)): 247",
                        "- Value count (Manager): 181",
                        "- Value count (Analyst): 147",
                        "- Value count (Database administrator): 105",
                        "- Value count (Academic): 45",
                        "- Value count (Independent): 15",
                        "- Value count (Data warehouse developer): 10",
                        "- Value count (Sales): 5",
                        "- Value count (Marketeer): 4"});

        testJob("Job title analytics", "rows processed from table: customers.csv", expectedResultSets);
    }

    private void testJob(final String jobName, final String resultStartIndicator,
            final Map<String, String[]> expectedResultSets) throws Exception {
        final InputStream resultInputStream = new ByteArrayInputStream(runJob(jobName).getBytes());
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

    private String runJob(final String jobName) throws Exception {
        final ProcessBuilder builder = new ProcessBuilder(JAVA_EXECUTABLE, "-cp", System.getProperty("java.class.path"),
                Main.class.getCanonicalName(), "-job", URLDecoder.decode(new File(
                        "src/main/resources/datacleaner-home/jobs/" + jobName + ".analysis.xml").getPath(), "UTF-8"));

        final Process process = builder.start();

        final StringBuilder result = new StringBuilder();
        new Thread(() -> {
            try {
                final InputStream is = process.getInputStream();
                int c;
                while ((c = is.read()) != -1) {
                    result.append((char) c);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        assertEquals(0, process.waitFor());

        return result.toString();
    }
}
