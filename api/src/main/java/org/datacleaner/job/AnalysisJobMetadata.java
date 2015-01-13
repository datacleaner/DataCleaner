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
package org.datacleaner.job;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.schema.ColumnType;

/**
 * Defines the metadata of a job. This metadata type can be used to present
 * basic information about a job to the user before he/she decides to open the
 * job. Because opening a job may fail the metadata can also be used to do
 * various verifications. In particular it is needed that the datastore name is
 * matched in the current AnalyzerBeansConfiguration and that the source column
 * paths are present in that particular datastore.
 * 
 * 
 */
public interface AnalysisJobMetadata {

    public static final AnalysisJobMetadata EMPTY_METADATA = new EmptyAnalysisJobMetadata();

    public String getJobName();

    public String getJobVersion();

    public String getJobDescription();

    public String getAuthor();

    public Date getCreatedDate();

    public Date getUpdatedDate();

    public String getDatastoreName();

    public List<String> getSourceColumnPaths();

    public List<ColumnType> getSourceColumnTypes();

    public Map<String, String> getVariables();

    public Map<String, String> getProperties();
}
