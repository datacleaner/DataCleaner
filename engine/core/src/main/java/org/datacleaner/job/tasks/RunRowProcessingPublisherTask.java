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
package org.datacleaner.job.tasks;

import org.datacleaner.job.runner.RowProcessingMetrics;
import org.datacleaner.job.runner.RowProcessingPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RunRowProcessingPublisherTask implements Task {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RowProcessingPublisher _publisher;
    private final RowProcessingMetrics _metrics;

    public RunRowProcessingPublisherTask(final RowProcessingPublisher publisher, final RowProcessingMetrics metrics) {
        _publisher = publisher;
        _metrics = metrics;
    }

    @Override
    public void execute() throws Exception {
        logger.debug("execute()");

        _publisher.processRows(_metrics);
    }

}
