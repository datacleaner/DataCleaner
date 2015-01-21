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
package org.datacleaner.configuration;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.job.AnalysisJobMetadata;
import org.datacleaner.job.ImmutableAnalysisJobMetadata;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.util.DateUtils;
import org.apache.metamodel.util.Month;

import junit.framework.TestCase;

public class ImmutableAnalysisJobMetadataTest extends TestCase {

    public void testGetters() throws Exception {
        String jobName = "name";
        String jobVersion = "version";
        String jobDescription = "desc";
        String author = "auth";
        Date createdDate = DateUtils.get(2013, Month.JULY, 23);
        Date updatedDate = DateUtils.get(2013, Month.JULY, 24);
        String datastoreName = "ds";
        List<String> sourceColumnPaths = Arrays.asList("foo", "bar");
        List<ColumnType> sourceColumnTypes = Arrays.asList(ColumnType.VARCHAR, ColumnType.BINARY);
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("foo", "bar");

        Map<String, String> properties = new HashMap<String,String>() ;
        properties.put("abc", "def") ;
        
        AnalysisJobMetadata metadata = new ImmutableAnalysisJobMetadata(jobName, jobVersion, jobDescription, author,
                createdDate, updatedDate, datastoreName, sourceColumnPaths, sourceColumnTypes, variables, properties);

        assertEquals(jobName, metadata.getJobName());
        assertEquals(jobDescription, metadata.getJobDescription());
        assertEquals(jobVersion, metadata.getJobVersion());
        assertEquals(author, metadata.getAuthor());
        assertEquals(createdDate, metadata.getCreatedDate());
        assertEquals(updatedDate, metadata.getUpdatedDate());
        assertEquals(datastoreName, metadata.getDatastoreName());
        assertEquals(sourceColumnPaths, metadata.getSourceColumnPaths());
        assertEquals(sourceColumnTypes, metadata.getSourceColumnTypes());
        assertEquals(variables, metadata.getVariables());
        assertEquals(properties, metadata.getProperties());

        AnalysisJobMetadata metadata2 = new ImmutableAnalysisJobMetadata(jobName, jobVersion, jobDescription, author,
                createdDate, updatedDate, datastoreName, sourceColumnPaths, sourceColumnTypes, variables, properties);

        assertEquals(metadata, metadata2);
    }
}
