/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.configuration;

import java.util.List;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.PerformanceCharacteristics;
import org.eobjects.analyzer.connection.PerformanceCharacteristicsImpl;
import org.eobjects.metamodel.schema.ColumnType;

/**
 * {@link Datastore} placeholder for lightweight reading of analysis jobs
 * without having to read live metadata from an actual datastore.
 */
public class PlaceholderDatastore implements Datastore {

    private static final long serialVersionUID = 1L;

    private final String _datastoreName;
    private final List<String> _sourceColumnPaths;
    private final List<ColumnType> _sourceColumnTypes;
    private String _description;

    public PlaceholderDatastore(String datastoreName, List<String> sourceColumnPaths, List<ColumnType> sourceColumnTypes) {
        _datastoreName = datastoreName;
        _sourceColumnPaths = sourceColumnPaths;
        _sourceColumnTypes = sourceColumnTypes;
    }

    public List<String> getSourceColumnPaths() {
        return _sourceColumnPaths;
    }
    
    public List<ColumnType> getSourceColumnTypes() {
        return _sourceColumnTypes;
    }

    @Override
    public String getDescription() {
        return _description;
    }

    @Override
    public String getName() {
        return _datastoreName;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(false);
    }

    @Override
    public DatastoreConnection openConnection() {
        return new PlaceholderDatastoreConnection(this);
    }

    @Override
    public void setDescription(String description) {
        _description = description;
    }

}
