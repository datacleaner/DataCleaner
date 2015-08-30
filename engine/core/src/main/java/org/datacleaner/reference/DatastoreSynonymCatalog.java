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
package org.datacleaner.reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.schema.Column;
import org.datacleaner.components.convert.ConvertToStringTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.job.NoSuchColumnException;
import org.datacleaner.job.NoSuchDatastoreException;

public final class DatastoreSynonymCatalog extends AbstractReferenceData implements SynonymCatalog {

    private static final long serialVersionUID = 1L;

    private final String _datastoreName;
    private final String _masterTermColumnPath;
    private final String[] _synonymColumnPaths;
    private final boolean _loadIntoMemory;

    public DatastoreSynonymCatalog(String name, String datastoreName, String masterTermColumnPath,
            String[] synonymColumnPaths) {
        this(name, datastoreName, masterTermColumnPath, synonymColumnPaths, true);
    }

    public DatastoreSynonymCatalog(String name, String datastoreName, String masterTermColumnPath,
            String[] synonymColumnPaths, boolean loadIntoMemory) {
        super(name);
        _datastoreName = datastoreName;
        _masterTermColumnPath = masterTermColumnPath;
        _synonymColumnPaths = synonymColumnPaths;
        _loadIntoMemory = loadIntoMemory;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            final DatastoreSynonymCatalog other = (DatastoreSynonymCatalog) obj;
            return Objects.equals(_datastoreName, other._datastoreName)
                    && Objects.equals(_masterTermColumnPath, other._masterTermColumnPath)
                    && Arrays.equals(_synonymColumnPaths, other._synonymColumnPaths)
                    && Objects.equals(_loadIntoMemory, other._loadIntoMemory);
        }
        return false;
    }

    public String getDatastoreName() {
        return _datastoreName;
    }

    public String getMasterTermColumnPath() {
        return _masterTermColumnPath;
    }

    public String[] getSynonymColumnPaths() {
        return Arrays.copyOf(_synonymColumnPaths, _synonymColumnPaths.length);
    }

    @Override
    public SynonymCatalogConnection openConnection(DataCleanerConfiguration configuration) {
        final Datastore datastore = configuration.getDatastoreCatalog().getDatastore(_datastoreName);
        if (datastore == null) {
            throw new NoSuchDatastoreException(_datastoreName);
        }

        final DatastoreConnection datastoreConnection = datastore.openConnection();

        if (_loadIntoMemory) {
            final SimpleSynonymCatalog simpleSynonymCatalog = loadIntoMemory(datastoreConnection);

            // no need for the connection anymore
            datastoreConnection.close();

            return simpleSynonymCatalog.openConnection(configuration);
        }

        return new DatastoreSynonymCatalogConnection(this, datastoreConnection);
    }

    public Column[] getSynonymColumns(DatastoreConnection datastoreConnection) {
        final Column[] columns = new Column[_synonymColumnPaths.length];
        for (int i = 0; i < columns.length; i++) {
            final String columnPath = _synonymColumnPaths[i];
            columns[i] = datastoreConnection.getDataContext().getColumnByQualifiedLabel(columnPath);
            if (columns[i] == null) {
                throw new NoSuchColumnException(columnPath);
            }
        }
        return columns;
    }

    public Column getMasterTermColumn(final DatastoreConnection datastoreConnection) {
        final DataContext dataContext = datastoreConnection.getDataContext();

        final Column masterTermColumn = dataContext.getColumnByQualifiedLabel(_masterTermColumnPath);
        if (masterTermColumn == null) {
            throw new NoSuchColumnException(_masterTermColumnPath);
        }
        return masterTermColumn;
    }

    public SimpleSynonymCatalog loadIntoMemory(final DatastoreConnection datastoreConnection) {
        final Map<String, String> synonymMap = new HashMap<>();

        final Column masterTermColumn = getMasterTermColumn(datastoreConnection);
        final Column[] columns = getSynonymColumns(datastoreConnection);

        try (DataSet dataSet = datastoreConnection.getDataContext().query().from(masterTermColumn.getTable().getName())
                .select(masterTermColumn).select(columns).execute()) {
            while (dataSet.next()) {
                final Row row = dataSet.getRow();
                final String masterTerm = getMasterTerm(row, masterTermColumn);
                final String[] synonyms = getSynonyms(row, columns);
                for (String synonym : synonyms) {
                    synonymMap.put(synonym, masterTerm);
                }
            }
        }

        final SimpleSynonymCatalog simpleSynonymCatalog = new SimpleSynonymCatalog(getName(), synonymMap);
        return simpleSynonymCatalog;
    }

    protected static String getMasterTerm(Row row, Column column) {
        Object value = row.getValue(column);
        return ConvertToStringTransformer.transformValue(value);
    }

    protected static String[] getSynonyms(Row row, Column[] columns) {
        List<String> synonyms = new ArrayList<String>();
        for (Column synonymColumn : columns) {
            final Object value = row.getValue(synonymColumn);
            if (value != null) {
                final String stringValue = value.toString();
                synonyms.add(stringValue);
            }
        }
        return synonyms.toArray(new String[synonyms.size()]);
    }
}
