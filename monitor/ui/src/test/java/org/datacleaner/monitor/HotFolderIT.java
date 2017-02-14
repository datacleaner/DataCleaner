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
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class HotFolderIT {

    @Test(timeout = 5 * ONE_MINUTE)
    public void testHotFolder() {
        try {

            final String command = "docker exec " + HotFolderHelper.getContainerId()
                    + " /bin/sh /tmp/generate-hot-folder-input.sh";
            HotFolderHelper.getCommandOutput(command);

            try {
                // wait for the hot folder trigger and job execution
                Thread.sleep(20 * 1000);
            } catch (final InterruptedException e) {
                fail("Waiting for the job execution was interrupted. " + e.getMessage());
            }

            assertEquals(2, getResultFilesCount());
            //remove the hot folder
            final String removeHotFolderCommand = "docker exec " + HotFolderHelper.getContainerId()
                    + " /bin/sh /tmp/remove-hot-folder.sh";
            HotFolderHelper.getCommandOutput(removeHotFolderCommand);
        } catch (final IOException e) {
            fail(e.getMessage());
        } 
    }

    private int getResultFilesCount() throws IOException {
        final String command = "docker exec " + HotFolderHelper.getContainerId() + " /bin/sh /tmp/get-results-count.sh";
        final List<String> outputLines = HotFolderHelper.getCommandOutput(command);

        if (outputLines.size() == 1) {
            return Integer.parseInt(outputLines.get(0));
        }

        return -1;
    }

}
