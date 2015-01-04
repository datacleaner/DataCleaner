/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.job;

import java.util.Calendar;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.eobjects.analyzer.job.jaxb.JobMetadataType;

public class JaxbJobMetadataFactoryImplTest extends TestCase {

    public void testCreate() throws Exception {
        JaxbJobMetadataFactory factory = new JaxbJobMetadataFactoryImpl("kasper", "my job", "desc", "1.0");

        AnalysisJob job = EasyMock.createMock(AnalysisJob.class);
        EasyMock.expect(job.getMetadata()).andReturn(AnalysisJobMetadata.EMPTY_METADATA).anyTimes();
        
        EasyMock.replay(job);

        JobMetadataType metadata = factory.create(job);
        int year = Calendar.getInstance().get(Calendar.YEAR);

        assertEquals("kasper", metadata.getAuthor());
        assertEquals("my job", metadata.getJobName());
        assertEquals("desc", metadata.getJobDescription());
        assertEquals("1.0", metadata.getJobVersion());

        assertEquals(year, metadata.getUpdatedDate().getYear());
        assertEquals(null, metadata.getCreatedDate());
    }
}
