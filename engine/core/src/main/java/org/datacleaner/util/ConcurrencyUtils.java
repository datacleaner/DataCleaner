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
/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.datacleaner.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for handling concurrency - mostly the oriented around the
 * java.util.concurrent classes.
 */
public class ConcurrencyUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrencyUtils.class);
    private static final int AWAIT_TIMEOUT_MINUTES = 2;

    private ConcurrencyUtils() {
        // prevent instantiation
    }

    public static void awaitCountDown(CountDownLatch countDownLatch, String countDownLatchId) {
        int iteration = 0;
        try {
            boolean finished = false;
            while (!finished) {
                iteration++;
                finished = countDownLatch.await(AWAIT_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                if (!finished) {
                    logger.info("Awaited completion of '" + countDownLatchId + "' for "
                            + (iteration * AWAIT_TIMEOUT_MINUTES) + " minutes...");
                }
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException("Awaiting completion of '" + countDownLatchId + "' was interrupted!", e);
        }
    }
}
