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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.io.Files;

public class GrouperIT {
    private static final String TEST_DATACLEANER_HOME = "src/test/resources/datacleaner-home-test/";
    private static final String RESULT_LINE_PREFIX = "RESULT:";
    private static final String OUTPUT_FILE = "grouper-job-test-output-file.csv";
    private static final int[] EXPECTED_GROUP_LENGTHS = new int[] { 9947, 10052 };
    private File _jobTempRepoFolder;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testGrouperJob() throws Exception {
        final String resourceName = "grouper-job";
        compareResults(resourceName, loadExpectedResult(resourceName));
        checkOutputFileForInconsistencies(_jobTempRepoFolder.getAbsolutePath() + File.separator + OUTPUT_FILE);
    }

    private void checkOutputFileForInconsistencies(final String filePath) {
        try {
            final List<String> lines = Files.readLines(new File(filePath), Charsets.UTF_8);

            for (int i = 1; i < lines.size(); i++) { // skipping header
                final String[] values = lines.get(i).split(";");
                final int grouperCount = Integer.parseInt(StringUtils.strip(values[0], "\""));
                final String[] idList = values[1].split(",");
                final int expectedCount = EXPECTED_GROUP_LENGTHS[i - 1];

                if (grouperCount != idList.length || grouperCount != expectedCount) {
                    fail(String.format("Size inconsistency at line: %d (%d != %d || %d != %d)", i, grouperCount,
                            idList.length, grouperCount, expectedCount));
                }
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private Map<String, String[]> loadExpectedResult(final String resourceName) throws IOException {
        final String filePath = TEST_DATACLEANER_HOME + "/expected-results/" + resourceName + ".txt";
        final List<String> fileLines = FileUtils.readLines(new File(filePath));
        final Map<String, String[]> results = new HashMap<>();
        final List<String> values = new ArrayList<>();
        String key = "";

        for (final String line : fileLines) {
            if (line.startsWith(RESULT_LINE_PREFIX)) {
                if (values.size() > 0) {
                    if (key.startsWith(RESULT_LINE_PREFIX)) {
                        results.put(key, values.toArray(new String[values.size()]));
                    }

                    values.clear();
                }

                key = line;
            } else if (!line.isEmpty()) {
                values.add(line);
            }
        }

        if (key.startsWith(RESULT_LINE_PREFIX) && values.size() > 0) {
            results.put(key, values.toArray(new String[values.size()]));
        }

        return results;
    }

    private void compareResults(final String jobName, final Map<String, String[]> expectedResultSets) throws Exception {
        _jobTempRepoFolder = tempFolder.newFolder();
        FileUtils.copyDirectory(new File(TEST_DATACLEANER_HOME + "/"), _jobTempRepoFolder);
        JobTestHelper.testJob(_jobTempRepoFolder, jobName, expectedResultSets);
    }
}
