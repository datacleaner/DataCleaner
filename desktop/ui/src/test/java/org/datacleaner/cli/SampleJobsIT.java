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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URLDecoder;

import org.datacleaner.Main;
import org.junit.Test;

public class SampleJobsIT {
    @Test
    public void testCopyEmployeesToCustomerTable() throws IOException {
        testJob("Copy employees to customer table.analysis.xml",
                "rows processed from table: EMPLOYEES",
                "SUCCESS!",
                "",
                "RESULT: orderdb - CUSTOMERS (19 columns)",
                "23 inserts executed");
    }

    private void testJob(final String jobFileName, final String resultStartIndicator,
            final String... expectedResultLines) throws IOException {
        final String result = runJob(jobFileName);
        final BufferedReader resultReader = new BufferedReader(new StringReader(result));

        try {
            String resultLine;
            boolean check = false;
            int resultLineIndex = 0;

            while ((resultLine = resultReader.readLine()) != null) {
                if (check) {
                    assertEquals(expectedResultLines[resultLineIndex++], resultLine);
                }

                if (resultLine.endsWith(resultStartIndicator)) {
                    check = true;
                }
            }

            assertTrue(check);
            assertEquals(expectedResultLines.length, resultLineIndex);
        } finally {
            resultReader.close();
        }
    }

    private String runJob(final String jobFileName) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(out));

            Main.main(new String[] { "-job", URLDecoder.decode(new File("src/main/resources/datacleaner-home/jobs/"
                    + jobFileName).getPath(), "UTF-8") });

            return out.toString();
        } finally {
            out.close();
        }
    }
}
