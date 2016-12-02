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
package org.datacleaner.monitor.server.job;

import java.io.Serializable;

import org.datacleaner.api.Configured;
import org.datacleaner.api.Validate;

/**
 * A sample {@link CustomJob} used to show examples of DataCleaner's
 * scheduler/job mechanism.
 */
public class SampleCustomJob implements CustomJob {

    @Configured
    int count;

    @Configured("Sleepy time between counts (milliseconds)")
    int sleep = 100;

    @Validate
    public void validate() {
        if (count < 0) {
            throw new IllegalStateException("Count cannot be negative");
        }
    }

    @Override
    public Serializable execute(final CustomJobCallback callback) throws Exception {
        callback.log("Starting count to " + count + "...");
        for (int i = 0; i < count; i++) {
            Thread.sleep(sleep);
            callback.log("Counted to " + (i + 1));
        }
        callback.log("Done!");
        return count;
    }
}
