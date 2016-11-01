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
import java.util.Map;

import org.datacleaner.Main;

public class JobTestHelper {
    private static final String JAVA_EXECUTABLE = System.getProperty("java.home") + File.separator + "bin"
            + File.separator + "java";

    public static void testJob(final String jobFileName, final Map<String, String[]> expectedResultSets)
            throws Exception {
        final InputStream resultInputStream = new ByteArrayInputStream(runJob(jobFileName).getBytes());
        final InputStreamReader resultInputStreamReader = new InputStreamReader(resultInputStream);
        final BufferedReader resultReader = new BufferedReader(resultInputStreamReader);

        try {
            String resultLine;

            // Read the output line by line until we see an indicator that the interesting part of the output
            // is coming up.
            while ((resultLine = resultReader.readLine()) != null && !resultLine.equals("SUCCESS!")) {
                // Ignore.
            }

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

    private static String runJob(final String jobFileName) throws Exception {
        final ProcessBuilder builder = new ProcessBuilder(JAVA_EXECUTABLE, "-cp", System.getProperty("java.class.path"),
                Main.class.getCanonicalName(), "-job", jobFileName);

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
