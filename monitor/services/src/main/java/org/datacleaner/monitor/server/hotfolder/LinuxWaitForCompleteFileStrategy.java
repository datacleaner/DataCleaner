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
package org.datacleaner.monitor.server.hotfolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Linux implementation of {@link WaitForCompleteFileStrategy}.
 */
public class LinuxWaitForCompleteFileStrategy extends AbstractWaitForCompleteFileStrategy {
    /**
     * Uses shell command based on 'lsof' tool to get the list of opened files in 'write' mode.
     * @param file
     * @return true if the file is not opened in 'write' mode, false otherwise
     */
    @Override
    public boolean isReady(final File file) {
        final Process process;

        try {
            final String command = _hotFolderPreferences.getUnixCommandTemplate()
                    .replace(HotFolderPreferences.FILE_PATH_PLACEHOLDER, file.getAbsolutePath());
            process = new ProcessBuilder("/bin/sh", "-c", command).start();

            try (InputStream inputStream = process.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    if (line.toLowerCase().contains("w") || line.contains("u")) { // w=write, u=read&write
                        return false;
                    }
                }
            } catch (final IOException e) {
                return false;
            }
        } catch (final IOException e) {
            return false;
        }

        return true;
    }
}
