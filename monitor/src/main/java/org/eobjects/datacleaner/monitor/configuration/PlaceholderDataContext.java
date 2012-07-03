/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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

import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.metamodel.AbstractDataContext;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.MetaModelException;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.ColumnType;
import org.eobjects.metamodel.schema.ImmutableSchema;
import org.eobjects.metamodel.schema.MutableColumn;
import org.eobjects.metamodel.schema.MutableSchema;
import org.eobjects.metamodel.schema.MutableTable;
import org.eobjects.metamodel.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DataContext} placeholder for lightweight reading of analysis jobs
 * without having to read live metadata from an actual datastore.
 */
public class PlaceholderDataContext extends AbstractDataContext {

    private static final Logger logger = LoggerFactory.getLogger(PlaceholderDataContext.class);

    private final ImmutableSchema _schema;

    public PlaceholderDataContext(List<String> sourceColumnPaths) {
        final String prefix = StringUtils.getLongestCommonToken(sourceColumnPaths, '.');

        final int schemaAndTableDelim = prefix.indexOf('.');
        final String schemaName;
        final String tableName;

        if (schemaAndTableDelim == -1) {
            schemaName = "schema";
            if (prefix.isEmpty()) {
                tableName = "table";
            } else {
                tableName = prefix;
            }
        } else {
            schemaName = prefix.substring(0, schemaAndTableDelim);
            tableName = prefix.substring(schemaAndTableDelim + 1);
        }

        logger.info("Using schema name '{}' and table name '{}'", schemaName, tableName);

        MutableSchema schema = new MutableSchema(schemaName);
        MutableTable table = new MutableTable(tableName).setSchema(schema);
        schema.addTable(table);

        for (String sourceColumnPath : sourceColumnPaths) {
            final String columnName;
            if (prefix.isEmpty()) {
                columnName = sourceColumnPath;
            } else {
                columnName = sourceColumnPath.substring(prefix.length() + 1);
            }

            // TODO: Column type should be same as original column type
            ColumnType columnType = ColumnType.VARCHAR;
            MutableColumn column = new MutableColumn(columnName).setType(columnType).setTable(table);
            table.addColumn(column);
        }

        _schema = new ImmutableSchema(schema);
    }

    @Override
    public DataSet executeQuery(Query arg0) throws MetaModelException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String getDefaultSchemaName() {
        return _schema.getName();
    }

    @Override
    protected Schema getSchemaByNameInternal(String schemaName) {
        if (!getDefaultSchemaName().equals(schemaName)) {
            return null;
        }
        return _schema;
    }

    @Override
    protected String[] getSchemaNamesInternal() {
        return new String[] { getDefaultSchemaName() };
    }
}
