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
 * Empty/blank implementation of {@link AnalysisJobMetadata}.
 */
class EmptyAnalysisJobMetadata implements AnalysisJobMetadata {

    @Override
    public String getJobName() {
        return null;
    }

    @Override
    public String getJobVersion() {
        return null;
    }

    @Override
    public String getJobDescription() {
        return null;
    }

    @Override
    public String getAuthor() {
        return null;
    }

    @Override
    public Date getCreatedDate() {
        return null;
    }

    @Override
    public Date getUpdatedDate() {
        return null;
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
        return null;
    }

    @Override
    public Map<String, String> getProperties() {
        return null;
    }

}
