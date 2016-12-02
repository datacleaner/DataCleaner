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

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.scheduling.model.TriggerType;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class AlertNotificationServiceImplTest extends TestCase {

    public void testNotify() throws Exception {
        final File targetDir = new File("target/example_repo");
        FileUtils.deleteDirectory(targetDir);
        FileUtils.copyDirectory(new File("src/test/resources/example_repo"), targetDir);

        final AtomicInteger counter = new AtomicInteger();

        try (ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "context/application-context.xml")) {
            final AlertNotificationServiceImpl alertNotificationService =
                    (AlertNotificationServiceImpl) applicationContext.getBean(AlertNotificationService.class);
            alertNotificationService.getAlertNotifiers().add((execution, activeAlerts, result) -> {
                counter.incrementAndGet();

                assertTrue(activeAlerts.get().isEmpty());
            });

            final TenantIdentifier tenant = new TenantIdentifier("tenant1");
            final JobIdentifier job = new JobIdentifier("product_profiling");
            final ScheduleDefinition schedule = new ScheduleDefinition(tenant, job, "orderdb");
            final ExecutionLog execution = new ExecutionLog(schedule, TriggerType.MANUAL);
            execution.setResultId("product_profiling-3.analysis.result.dat");
            alertNotificationService.notifySubscribers(execution);

            assertEquals(1, counter.get());
        }
    }

    public void testIsBeyondThreshold() throws Exception {
        final AlertNotificationServiceImpl service = new AlertNotificationServiceImpl(null, null);

        assertFalse(service.isBeyondThreshold(10, 5, 15));
        assertFalse(service.isBeyondThreshold(10, null, null));

        assertTrue(service.isBeyondThreshold(10, 11, 15));
        assertTrue(service.isBeyondThreshold(10, 5, 9));

        assertTrue(service.isBeyondThreshold(10, null, 9));
        assertTrue(service.isBeyondThreshold(10, 11, null));

        assertFalse(service.isBeyondThreshold(10, null, 11));
        assertFalse(service.isBeyondThreshold(10, 5, null));
    }
}
