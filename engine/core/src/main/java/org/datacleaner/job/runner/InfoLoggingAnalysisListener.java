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
package org.datacleaner.job.runner;

import org.datacleaner.api.ComponentMessage;
import org.datacleaner.api.ExecutionLogMessage;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.util.LabelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AnalysisListener used for INFO level logging. This listener will log
 * interesting progress information for each thousands rows being processed.
 */
public class InfoLoggingAnalysisListener extends AnalysisListenerAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(InfoLoggingAnalysisListener.class);

    /**
     * @return whether or not the debug logging level is enabled. Can be used to
     *         find out of it is even feasable to add this listener or not.
     */
    public static boolean isEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void rowProcessingBegin(AnalysisJob job, RowProcessingMetrics metrics) {
        logger.info("Processing of '{}' start", metrics.getTable().getName());
    }
    
    @Override
    public void rowProcessingSuccess(AnalysisJob job, RowProcessingMetrics metrics) {
        logger.info("Processing of '{}' success", metrics.getTable().getName());
    }
    
    @Override
    public void onComponentMessage(AnalysisJob job, ComponentJob componentJob, ComponentMessage message) {
        if (message instanceof ExecutionLogMessage) {
            logger.info(((ExecutionLogMessage) message).getMessage() + " (" + LabelUtils.getLabel(componentJob) + ")");
        }
    }
}
