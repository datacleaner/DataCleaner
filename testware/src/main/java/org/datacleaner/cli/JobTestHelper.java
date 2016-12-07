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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

public class JobTestHelper {
    private static final String DATACLEANER_MAIN_CLASS_NAME = "org.datacleaner.Main";
    private static final String JAVA_EXECUTABLE =
            System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

    public static void testJob(final File repository, final String jobName,
            final Map<String, String[]> expectedResultSets, final String... extraCliArgs) throws Exception {
        final String jobResult = runJob(repository, jobName, extraCliArgs);

        final InputStream resultInputStream = new ByteArrayInputStream(jobResult.getBytes());
        final InputStreamReader resultInputStreamReader = new InputStreamReader(resultInputStream);
        final BufferedReader resultReader = new BufferedReader(resultInputStreamReader);

        try {
            String resultLine;

            // Read the output line by line until we see an indicator that the interesting part of the output
            // is coming up.
            //noinspection StatementWithEmptyBody
            while ((resultLine = resultReader.readLine()) != null && !resultLine.equals("SUCCESS!")) {
                // Ignore.
            }

            // Now iterate over the different expected result sets and see if they're valid.
            while ((resultLine = resultReader.readLine()) != null) {
                final String resultKey = resultLine.trim();
                if (!"".equals(resultKey)) {
                    final String[] expectedResultSet = expectedResultSets.get(resultKey);

                    assertNotNull(expectedResultSet);

                    for (final String expectedResult : expectedResultSet) {
                        // Only check the first part of the line, because numbers at the end may differ based
                        // on the moment in time the test runs at.
                        assertThat(resultReader.readLine(), containsString(expectedResult));
                    }
                    expectedResultSets.remove(resultKey);
                }
            }

            assertEquals("CLI result:" + System.lineSeparator() + jobResult, 0, expectedResultSets.size());
        } finally {
            resultReader.close();
            resultInputStreamReader.close();
            resultInputStream.close();
        }
    }

    private static String runJob(final File repository, final String jobName, final String... extraCliArgs)
            throws Exception {
        final String jobFileName = getAbsoluteFilename(repository, "jobs/" + jobName + ".analysis.xml");
        final String confFileName = getAbsoluteFilename(repository, "conf.xml");

        final String[] processBuilderArguments = ArrayUtils
                .addAll(new String[] { JAVA_EXECUTABLE, DATACLEANER_MAIN_CLASS_NAME, "-job", jobFileName, "-conf",
                        confFileName }, extraCliArgs);
        final ProcessBuilder builder = new ProcessBuilder(processBuilderArguments);
        builder.environment().put("DATACLEANER_HOME", URLDecoder.decode(repository.getAbsolutePath(), "UTF-8"));
        builder.environment().put("CLASSPATH", System.getProperty("java.class.path"));

        final Process process = builder.start();

        final StringBuilder result = new StringBuilder();
        new Thread(() -> {
            try {
                final InputStream is = process.getInputStream();
                int character;
                while ((character = is.read()) != -1) {
                    result.append((char) character);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        assertEquals(0, process.waitFor());

        return result.toString();
    }

    private static String getAbsoluteFilename(final File repository, final String childPath)
            throws UnsupportedEncodingException {
        return URLDecoder.decode(new File(repository, childPath).getPath(), "UTF-8");
    }
}
