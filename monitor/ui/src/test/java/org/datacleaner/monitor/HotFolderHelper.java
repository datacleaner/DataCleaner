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

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HotFolderHelper {

    private static final String IMAGE_NAME = "datacleaner-monitor";

    private static final Logger logger = LoggerFactory.getLogger(HotFolderHelper.class);

    public static List<String> getCommandOutput(final String command) {
        final List<String> outputLines = new ArrayList<>();

        try {
            final Process process = getProcess(command);
            final InputStream inputStream = process.getInputStream();
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                outputLines.add(line);
            }
        } catch (IOException e) {
            logger.error("External command execution failed. ", e);
        }

        return outputLines;
    }

    public static Process getProcess(final String command) throws IOException {
        final boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
        final String interpreter;
        final String argument;

        if (isLinux) {
            interpreter = "bash";
            argument = "-c";
        } else {
            interpreter = "cmd";
            argument = "/c";
        }

        return Runtime.getRuntime().exec(new String[] { interpreter, argument, command });
    }

    public static String getContainerId() throws IOException {
        String containerId = null;
        final List<String> lines = HotFolderHelper.getCommandOutput("docker ps");

        for (final String line : lines) {
            if (line.contains(IMAGE_NAME)) {
                containerId = line.replaceFirst("\\ .*$", "");
            }
        }

        if (containerId == null) {
            fail("Docker container ID is unknown. ");
        }

        return containerId;
    }
}
