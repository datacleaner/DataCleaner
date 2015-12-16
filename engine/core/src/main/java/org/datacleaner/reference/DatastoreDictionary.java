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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.job.NoSuchColumnException;
import org.datacleaner.job.NoSuchDatastoreException;
import org.datacleaner.util.ReadObjectBuilder;
import org.elasticsearch.common.base.Objects;

/**
 * A dictionary backed by a column in a datastore.
 * 
 * Note that even though this datastore <i>is</i> serializable it is not
 * entirely able to gracefully deserialize. The user of the dictionary will have
 * to inject the DatastoreCatalog using the setter method for this.
 * 
 * 
 */
public final class DatastoreDictionary extends AbstractReferenceData implements Dictionary {

    private static final long serialVersionUID = 1L;

    private final String _datastoreName;
    private final String _qualifiedColumnName;
    private final boolean _loadIntoMemory;

    public DatastoreDictionary(String name, String datastoreName, String qualifiedColumnName) {
        this(name, datastoreName, qualifiedColumnName, true);
    }

    public DatastoreDictionary(String name, String datastoreName, String qualifiedColumnName, boolean loadIntoMemory) {
        super(name);
        _datastoreName = datastoreName;
        _qualifiedColumnName = qualifiedColumnName;
        _loadIntoMemory = loadIntoMemory;
    }
    
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ReadObjectBuilder.create(this, DatastoreDictionary.class).readObject(stream);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            final DatastoreDictionary other = (DatastoreDictionary) obj;
            return Objects.equal(_datastoreName, other._datastoreName)
                    && Objects.equal(_qualifiedColumnName, other._qualifiedColumnName)
                    && Objects.equal(_loadIntoMemory, other._loadIntoMemory);
        }
        return false;
    }

    public SimpleDictionary loadIntoMemory(DatastoreConnection datastoreConnection) {
        final DataContext dataContext = datastoreConnection.getDataContext();
        final Column column = getColumn(datastoreConnection);

        final Query query = dataContext.query().from(column.getTable()).select(column).toQuery();
        query.getSelectClause().setDistinct(true);

        final Set<String> values = new HashSet<>();

        try (final DataSet dataSet = dataContext.executeQuery(query)) {
            while (dataSet.next()) {
                final Object value = dataSet.getRow().getValue(0);
                if (value != null) {
                    values.add(value.toString());
                }
            }
        }

        return new SimpleDictionary(getName(), values);
    }

    @Override
    public DictionaryConnection openConnection(DataCleanerConfiguration configuration) {
        final Datastore datastore = configuration.getDatastoreCatalog().getDatastore(_datastoreName);
        if (datastore == null) {
            throw new NoSuchDatastoreException(_datastoreName);
        }

        final DatastoreConnection datastoreConnection = datastore.openConnection();

        if (_loadIntoMemory) {
            final SimpleDictionary simpleDictionary = loadIntoMemory(datastoreConnection);

            // no need for the connection anymore
            datastoreConnection.close();

            return simpleDictionary.openConnection(configuration);
        }

        return new DatastoreDictionaryConnection(this, datastoreConnection);
    }

    public Column getColumn(DatastoreConnection datastoreConnection) {
        try {
            final Column column = datastoreConnection.getDataContext().getColumnByQualifiedLabel(_qualifiedColumnName);
            if (column == null) {
                throw new NoSuchColumnException(_qualifiedColumnName);
            }
            return column;
        } catch (RuntimeException e) {
            datastoreConnection.close();
            throw e;
        }
    }

    public String getDatastoreName() {
        return _datastoreName;
    }

    public String getQualifiedColumnName() {
        return _qualifiedColumnName;
    }
}
