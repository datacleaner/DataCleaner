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
package org.datacleaner.test;

import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;

public class TestEnvironment {
    
    public static final int THREAD_COUNT = 16;

    private static final MultiThreadedTaskRunner TASK_RUNNER = new MultiThreadedTaskRunner(THREAD_COUNT);
    private static final DataCleanerEnvironment ENVIRONMENT = new DataCleanerEnvironmentImpl()
            .withTaskRunner(TASK_RUNNER);

    public static MultiThreadedTaskRunner getMultiThreadedTaskRunner() {
        return TASK_RUNNER;
    }

    public static DataCleanerEnvironment getEnvironment() {
        return ENVIRONMENT;
    }
}
