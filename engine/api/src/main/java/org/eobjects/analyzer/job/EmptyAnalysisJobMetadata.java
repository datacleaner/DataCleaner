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
package org.eobjects.analyzer.job;

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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getJobDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAuthor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getCreatedDate() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getUpdatedDate() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDatastoreName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getSourceColumnPaths() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ColumnType> getSourceColumnTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getVariables() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

}
