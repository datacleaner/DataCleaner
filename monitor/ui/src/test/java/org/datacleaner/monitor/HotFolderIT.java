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
package org.datacleaner.monitor;

import static javax.management.timer.Timer.ONE_MINUTE;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class HotFolderIT {
    private static final String IMAGE_NAME = "datacleaner-monitor";
    private String _containerId;

    @Test(timeout = 5 * ONE_MINUTE)
    public void testHotFolder() {
        try {
            final String command = "docker exec " + getContainerId() + " /bin/sh /tmp/generate-hot-folder-input.sh";
            new ProcessBuilder(command.split(" ")).start();

            try {
                Thread.sleep(5 * 1000); // wait for the hot folder trigger and job execution
            } catch (final InterruptedException e) {
                // nothing
            }

            // TODO: some results checking...
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private String getContainerId() throws IOException {
        if (_containerId == null) {
            final List<String> lines = getCommandOutput(new String[] { "docker", "ps" });

            for (final String line : lines) {
                if (line.contains(IMAGE_NAME)) {
                    _containerId = line.replaceFirst("\\ .*$", "");
                }
            }

            if (_containerId == null) {
                fail("Docker container ID is unknown. ");
            }
        }

        return _containerId;
    }

    private List<String> getCommandOutput(final String[] commandParts) {
        final List<String> outputLines = new ArrayList<>();

        try {
            final Process process = new ProcessBuilder(commandParts).start();
            final InputStream inputStream = process.getInputStream();
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                outputLines.add(line);
            }
        } catch (IOException e) {
            // nothing
        }

        return outputLines;
    }
}
