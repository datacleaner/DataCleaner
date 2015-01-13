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
package org.datacleaner.monitor.alertnotification;

import java.util.Map;

import org.datacleaner.monitor.configuration.ResultContext;
import org.datacleaner.monitor.scheduling.model.AlertDefinition;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.apache.metamodel.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple {@link AlertNotifier} that logs alerts as WARNINGs in the log.
 */
public class LoggerAlertNotifier implements AlertNotifier {

    private static final Logger logger = LoggerFactory.getLogger(LoggerAlertNotifier.class);

    @Override
    public void onExecutionFinished(ExecutionLog execution, Ref<Map<AlertDefinition, Number>> activeAlerts,
            ResultContext resultContext) {
        if (!logger.isWarnEnabled()) {
            return;
        }

        final Map<AlertDefinition, Number> alertValues = activeAlerts.get();
        if (!alertValues.isEmpty()) {
            logger.info("Active alerts: {}", alertValues.keySet());
            logger.info("Alert metric values: {}", alertValues.values());
        }
    }

}
