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
package org.eobjects.analyzer.configuration;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.AnalysisJobMetadata;
import org.eobjects.analyzer.util.SchemaNavigator;

import org.apache.metamodel.schema.Column;

/**
 * A class that represents a mapping between column paths as defined in a
 * serialized/saved job and their actual column objects. Having this in a
 * separate class allows the user to apply the flow defined in a job on to
 * another set of columns that have been mapped to the original column paths.
 */
public final class SourceColumnMapping {

    private final Map<String, Column> _map;
    private Datastore _datastore;

    public SourceColumnMapping(AnalysisJobMetadata metadata) {
        this(metadata.getSourceColumnPaths());
    }

    public SourceColumnMapping(String... originalColumnPaths) {
        _map = new TreeMap<String, Column>();
        for (String path : originalColumnPaths) {
            _map.put(path, null);
        }
    }

    public SourceColumnMapping(List<String> sourceColumnPaths) {
        this(sourceColumnPaths.toArray(new String[sourceColumnPaths.size()]));
    }

    public void setDatastore(Datastore datastore) {
        _datastore = datastore;
    }

    public Datastore getDatastore() {
        return _datastore;
    }

    /**
     * Automatically maps all unmapped paths by looking them up in a datastore.
     * 
     * @param schemaNavigator
     */
    public void autoMap(Datastore datastore) {
        setDatastore(datastore);
        try (final DatastoreConnection con = datastore.openConnection()) {
            final SchemaNavigator schemaNavigator = con.getSchemaNavigator();
            for (final Entry<String, Column> entry : _map.entrySet()) {
                if (entry.getValue() == null) {
                    final String path = entry.getKey();
                    final Column column = schemaNavigator.convertToColumn(path);
                    entry.setValue(column);
                }
            }
        }
    }

    public boolean isSatisfied() {
        if (_datastore == null) {
            return false;
        }
        for (Entry<String, Column> entry : _map.entrySet()) {
            if (entry.getValue() == null) {
                return false;
            }
        }
        return true;
    }

    public Column getColumn(String path) {
        return _map.get(path);
    }

    public void setColumn(String path, Column column) {
        _map.put(path, column);
    }

    public Set<String> getPaths() {
        return _map.keySet();
    }

    public Set<String> getUnmappedPaths() {
        Set<String> result = new TreeSet<String>();
        for (Entry<String, Column> entry : _map.entrySet()) {
            if (entry.getValue() == null) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "SourceColumnMapping[datastore=" + _datastore + ",map=" + _map + "]";
    }
}
