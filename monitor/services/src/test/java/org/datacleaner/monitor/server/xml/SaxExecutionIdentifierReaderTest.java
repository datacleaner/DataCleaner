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
package org.datacleaner.monitor.server.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.metamodel.util.DateUtils;
import org.apache.metamodel.util.Month;
import org.datacleaner.monitor.scheduling.model.ExecutionIdentifier;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.scheduling.model.TriggerType;
import org.datacleaner.monitor.server.jaxb.JaxbExecutionLogWriter;
import org.datacleaner.monitor.server.jaxb.SaxExecutionIdentifierReader;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;

import junit.framework.TestCase;

public class SaxExecutionIdentifierReaderTest extends TestCase {

    public void testRead() throws Exception {

        final ScheduleDefinition schedule =
                new ScheduleDefinition(new TenantIdentifier("DC"), new JobIdentifier("job1"), "my_ds");
        schedule.setDependentJob(new JobIdentifier("job2"));
        final ExecutionLog executionLog = new ExecutionLog();
        executionLog.setResultId("my-result");
        executionLog.setSchedule(schedule);
        executionLog.setTriggerType(TriggerType.DEPENDENT);
        executionLog.setJobBeginDate(DateUtils.get(2012, Month.JULY, 2));
        executionLog.setJobEndDate(DateUtils.get(2012, Month.JULY, 3));
        executionLog.setLogOutput("foo\nbar");
        executionLog.setExecutionStatus(ExecutionStatus.SUCCESS);

        final byte[] bytes;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            new JaxbExecutionLogWriter().write(executionLog, out);
            out.flush();
            bytes = out.toByteArray();
        }

        final ByteArrayInputStream in = new ByteArrayInputStream(bytes);

        final ExecutionIdentifier result = SaxExecutionIdentifierReader.read(in, "test file");

        assertEquals("my-result", result.getResultId());
        assertEquals(ExecutionStatus.SUCCESS, result.getExecutionStatus());
        assertEquals(TriggerType.DEPENDENT, result.getTriggerType());

        final Date jobBeginDate = result.getJobBeginDate();
        assertNotNull(jobBeginDate);

        assertEquals("2012-07-02", new SimpleDateFormat("yyyy-MM-dd").format(jobBeginDate));
    }
}
