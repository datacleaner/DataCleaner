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

import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.util.DateUtils;
import org.apache.metamodel.util.Month;
import org.datacleaner.job.AnalysisJobMetadata;
import org.datacleaner.job.ImmutableAnalysisJobMetadata;

import junit.framework.TestCase;

public class ImmutableAnalysisJobMetadataTest extends TestCase {

    public void testGetters() throws Exception {
        final String jobName = "name";
        final String jobVersion = "version";
        final String jobDescription = "desc";
        final String author = "auth";
        final Date createdDate = DateUtils.get(2013, Month.JULY, 23);
        final Date updatedDate = DateUtils.get(2013, Month.JULY, 24);
        final String datastoreName = "ds";
        final List<String> sourceColumnPaths = Arrays.asList("foo", "bar");
        final List<ColumnType> sourceColumnTypes = Arrays.asList(ColumnType.VARCHAR, ColumnType.BINARY);
        final Map<String, String> variables = new HashMap<>();
        variables.put("foo", "bar");

        final Map<String, String> properties = new HashMap<>();
        properties.put("abc", "def");

        final AnalysisJobMetadata metadata =
                new ImmutableAnalysisJobMetadata(jobName, jobVersion, jobDescription, author, createdDate, updatedDate,
                        datastoreName, sourceColumnPaths, sourceColumnTypes, variables, properties);

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

        final AnalysisJobMetadata metadata2 =
                new ImmutableAnalysisJobMetadata(jobName, jobVersion, jobDescription, author, createdDate, updatedDate,
                        datastoreName, sourceColumnPaths, sourceColumnTypes, variables, properties);

        assertEquals(metadata, metadata2);
    }
}
