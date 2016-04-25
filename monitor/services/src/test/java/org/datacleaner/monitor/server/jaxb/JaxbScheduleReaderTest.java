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
package org.datacleaner.monitor.server.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.junit.Test;

/**
 * This class tests the old xml schedule configuration which doesn't have a 'run on hadoop' setting 
 *
 */
public class JaxbScheduleReaderTest{

    
    @Test
    public void testReader() throws FileNotFoundException{
        final File file = new File("src/test/resources/CustomerCompleteness_example.schedule.xml");
        final FileInputStream fileInputStream = new FileInputStream(file);
        final JaxbScheduleReader reader = new JaxbScheduleReader();
        final JobIdentifier job =new JobIdentifier();
        TenantIdentifier tenant = new TenantIdentifier();
        String groupName="test";
        final ScheduleDefinition schedule = reader.read(fileInputStream, job, tenant, groupName); 
        assertEquals("@daily",schedule.getCronExpression());
        assertFalse(schedule.isDistributedExecution());
        assertFalse(schedule.isRunOnHadoop()); 
        
    }
}
