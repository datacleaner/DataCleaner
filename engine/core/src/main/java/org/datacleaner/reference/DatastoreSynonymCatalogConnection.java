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

import java.util.Collection;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.query.builder.SatisfiedWhereBuilder;
import org.apache.metamodel.schema.Column;
import org.datacleaner.connection.DatastoreConnection;

final class DatastoreSynonymCatalogConnection implements SynonymCatalogConnection {

    private final DatastoreConnection _datastoreConnection;
    private final DatastoreSynonymCatalog _synonymCatalog;

    public DatastoreSynonymCatalogConnection(DatastoreSynonymCatalog synonymCatalog,
            DatastoreConnection datastoreConnection) {
        _synonymCatalog = synonymCatalog;
        _datastoreConnection = datastoreConnection;
    }

    @Override
    public Collection<Synonym> getSynonyms() {
        final SimpleSynonymCatalog simpleSynonymCatalog = _synonymCatalog.loadIntoMemory(_datastoreConnection);
        return simpleSynonymCatalog.openConnection(null).getSynonyms();
    }

    @Override
    public String getMasterTerm(String term) {
        final DataContext dataContext = _datastoreConnection.getDataContext();

        final Column masterTermColumn = _synonymCatalog.getMasterTermColumn(_datastoreConnection);
        final Column[] columns = _synonymCatalog.getSynonymColumns(_datastoreConnection);

        SatisfiedWhereBuilder<?> queryBuilder = dataContext.query().from(masterTermColumn.getTable())
                .select(masterTermColumn).where(columns[0]).eq(term);
        for (int i = 1; i < columns.length; i++) {
            final Column column = columns[i];
            queryBuilder = queryBuilder.or(column).eq(term);
        }
        queryBuilder.maxRows(1);

        try (DataSet dataSet = queryBuilder.execute()) {
            while (dataSet.next()) {
                final Object value = dataSet.getRow().getValue(0);
                if (value != null) {
                    return value.toString();
                }
            }
        }

        return null;
    }

    @Override
    public void close() {
        _datastoreConnection.close();
    }

}
