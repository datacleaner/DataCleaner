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
package org.datacleaner.job.builder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.schema.ColumnType;
import org.datacleaner.job.AnalysisJobMetadata;

/**
 * A mutable {@link AnalysisJobMetadata} object, used by
 * {@link AnalysisJobBuilder} to allow easy setting of metadata items.
 */
public class MutableAnalysisJobMetadata implements AnalysisJobMetadata {

    private String _jobName;
    private String _jobVersion;
    private String _jobDescription;
    private String _author;
    private Date _createdDate;
    private Date _updatedDate;
    private Map<String, String> _variables;
    private Map<String, String> _properties;

    public MutableAnalysisJobMetadata() {
        this(AnalysisJobMetadata.EMPTY_METADATA);
    }

    public MutableAnalysisJobMetadata(AnalysisJobMetadata metadata) {
        _jobName = metadata.getJobName();
        _jobVersion = metadata.getJobVersion();
        _jobDescription = metadata.getJobDescription();
        _author = metadata.getAuthor();
        _createdDate = metadata.getCreatedDate();
        _updatedDate = metadata.getUpdatedDate();
        Map<String, String> variables = metadata.getVariables();
        if (variables == null) {
            _variables = new HashMap<>();
        } else {
            _variables = new HashMap<>(variables);
        }
        final Map<String, String> properties = metadata.getProperties();
        if (properties == null) {
            _properties = new HashMap<>();
        } else {
            _properties = new HashMap<>(properties);
        }
    }

    @Override
    public String getJobName() {
        return _jobName;
    }

    public void setJobName(String jobName) {
        _jobName = jobName;
    }

    @Override
    public String getJobVersion() {
        return _jobVersion;
    }

    public void setJobVersion(String jobVersion) {
        _jobVersion = jobVersion;
    }

    @Override
    public String getJobDescription() {
        return _jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        _jobDescription = jobDescription;
    }

    @Override
    public String getAuthor() {
        return _author;
    }

    public void setAuthor(String author) {
        _author = author;
    }

    @Override
    public Date getCreatedDate() {
        return _createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        _createdDate = createdDate;
    }

    @Override
    public Date getUpdatedDate() {
        return _updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        _updatedDate = updatedDate;
    }

    @Override
    public String getDatastoreName() {
        return null;
    }

    @Override
    public List<String> getSourceColumnPaths() {
        return null;
    }

    @Override
    public List<ColumnType> getSourceColumnTypes() {
        return null;
    }

    @Override
    public Map<String, String> getVariables() {
        return _variables;
    }

    @Override
    public Map<String, String> getProperties() {
        return _properties;
    }

}
