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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.util.BaseObject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Immutable implementation of {@link AnalysisJobMetadata}.
 */
public class ImmutableAnalysisJobMetadata extends BaseObject implements AnalysisJobMetadata {

    private final String _jobName;
    private final String _jobVersion;
    private final String _jobDescription;
    private final String _author;
    private final Date _createdDate;
    private final Date _updatedDate;
    private final String _datastoreName;
    private final List<String> _sourceColumnPaths;
    private final Map<String, String> _variables;
    private final List<ColumnType> _sourceColumnTypes;
    private final Map<String, String> _properties;

    public ImmutableAnalysisJobMetadata(String jobName, String jobVersion, String jobDescription, String author,
            Date createdDate, Date updatedDate, String datastoreName, List<String> sourceColumnPaths,
            List<ColumnType> sourceColumnTypes, Map<String, String> variables, Map<String, String> properties) {
        _jobName = jobName;
        _jobVersion = jobVersion;
        _jobDescription = jobDescription;
        _author = author;
        _createdDate = createdDate;
        _updatedDate = updatedDate;
        _datastoreName = datastoreName;

        if (properties == null) {
            _properties = Collections.emptyMap();
        } else {
            _properties = ImmutableMap.copyOf(properties);
        }
        if (sourceColumnPaths == null) {
            _sourceColumnPaths = Collections.emptyList();
        } else {
            _sourceColumnPaths = ImmutableList.copyOf(sourceColumnPaths);
        }
        if (sourceColumnTypes == null) {
            _sourceColumnTypes = new ArrayList<ColumnType>(sourceColumnPaths.size());
        } else {
            // we don't use ImmutableList here because it does not allow nulls
            _sourceColumnTypes = Collections.unmodifiableList(sourceColumnTypes);
        }

        if (variables == null) {
            variables = Collections.emptyMap();
        } else {
            variables = ImmutableMap.copyOf(variables);
        }
        _variables = Collections.unmodifiableMap(variables);
    }

    @Override
    protected void decorateIdentity(List<Object> identifiers) {
        identifiers.add(_jobName);
        identifiers.add(_jobVersion);
        identifiers.add(_jobDescription);
        identifiers.add(_author);
        identifiers.add(_createdDate);
        identifiers.add(_updatedDate);
        identifiers.add(_datastoreName);
        identifiers.add(_sourceColumnPaths);
        identifiers.add(_variables);
        identifiers.add(_properties);
    }

    @Override
    public String getJobName() {
        return _jobName;
    }

    @Override
    public String getJobVersion() {
        return _jobVersion;
    }

    @Override
    public String getJobDescription() {
        return _jobDescription;
    }

    @Override
    public String getAuthor() {
        return _author;
    }

    @Override
    public Date getCreatedDate() {
        return _createdDate;
    }

    @Override
    public Date getUpdatedDate() {
        return _updatedDate;
    }

    @Override
    public String getDatastoreName() {
        return _datastoreName;
    }

    @Override
    public List<String> getSourceColumnPaths() {
        return _sourceColumnPaths;
    }

    @Override
    public Map<String, String> getVariables() {
        return _variables;
    }

    @Override
    public List<ColumnType> getSourceColumnTypes() {
        return _sourceColumnTypes;
    }

    @Override
    public Map<String, String> getProperties() {
        return _properties;
    }

}
