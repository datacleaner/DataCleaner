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
package org.datacleaner.util.convert;

import static org.datacleaner.util.ReflectionUtils.*;

import javax.inject.Inject;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.Converter;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Converter} implementation for DataCleaner configuration items,
 * specifically:
 *
 * <ul>
 * <li>org.datacleaner.reference.Dictionary</li>
 * <li>org.datacleaner.reference.SynonymCatalog</li>
 * <li>org.datacleaner.reference.StringPattern</li>
 * <li>org.datacleaner.connection.Datastore</li>
 * <li>org.apache.metamodel.schema.Column</li>
 * <li>org.apache.metamodel.schema.Table</li>
 * <li>org.apache.metamodel.schema.Schema</li>
 * </ul>
 */
public class ConfigurationItemConverter implements Converter<Object> {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationItemConverter.class);

    @Inject
    DatastoreCatalog datastoreCatalog;

    @Inject
    ReferenceDataCatalog referenceDataCatalog;

    @Inject
    Datastore datastore;

    protected ConfigurationItemConverter() {
    }

    public ConfigurationItemConverter(final DataCleanerConfiguration configuration, final Datastore datastore) {
        this.datastoreCatalog = configuration.getDatastoreCatalog();
        this.referenceDataCatalog = configuration.getReferenceDataCatalog();
        this.datastore = datastore;
    }

    @Override
    public Object fromString(final Class<?> type, final String str) {
        if (ReflectionUtils.isColumn(type)) {
            try (DatastoreConnection connection = datastore.openConnection()) {
                final Column column = connection.getSchemaNavigator().convertToColumn(str);
                if (column == null) {
                    throw new IllegalArgumentException("Column not found: " + str);
                }
                return column;
            }
        }
        if (ReflectionUtils.isTable(type)) {
            try (DatastoreConnection connection = datastore.openConnection()) {
                final Table table = connection.getSchemaNavigator().convertToTable(str);
                if (table == null) {
                    throw new IllegalArgumentException("Table not found: " + str);
                }
                return table;
            }
        }
        if (ReflectionUtils.isSchema(type)) {
            try (DatastoreConnection connection = datastore.openConnection()) {
                final Schema schema = connection.getSchemaNavigator().convertToSchema(str);
                if (schema == null) {
                    throw new IllegalArgumentException("Schema not found: " + str);
                }
                return schema;
            }
        }
        if (ReflectionUtils.is(type, Dictionary.class)) {
            final Dictionary dictionary = referenceDataCatalog.getDictionary(str);
            if (dictionary == null) {
                throw new IllegalArgumentException("Dictionary not found: " + str);
            }
            return dictionary;
        }
        if (ReflectionUtils.is(type, SynonymCatalog.class)) {
            final SynonymCatalog synonymCatalog = referenceDataCatalog.getSynonymCatalog(str);
            if (synonymCatalog == null) {
                throw new IllegalArgumentException("Synonym catalog not found: " + str);
            }
            return synonymCatalog;
        }
        if (ReflectionUtils.is(type, StringPattern.class)) {
            final StringPattern stringPattern = referenceDataCatalog.getStringPattern(str);
            if (stringPattern == null) {
                throw new IllegalArgumentException("String pattern not found: " + str);
            }
            return stringPattern;
        }
        if (ReflectionUtils.is(type, Datastore.class)) {
            if (null != datastoreCatalog) {
                final Datastore datastore = datastoreCatalog.getDatastore(str);
                if (datastore == null) {
                    throw new IllegalArgumentException("Datastore not found: " + str);
                }
                return datastore;
            }
        }
        throw new IllegalArgumentException("Could not convert to type: " + type.getName());
    }

    @Override
    public String toString(final Object o) {
        final String result;
        if (o instanceof Schema) {
            result = ((Schema) o).getName();
        } else if (o instanceof Table) {
            result = ((Table) o).getQualifiedLabel();
        } else if (o instanceof Column) {
            result = ((Column) o).getQualifiedLabel();
        } else if (o instanceof Dictionary) {
            result = ((Dictionary) o).getName();
        } else if (o instanceof SynonymCatalog) {
            result = ((SynonymCatalog) o).getName();
        } else if (o instanceof StringPattern) {
            result = ((StringPattern) o).getName();
        } else if (o instanceof Datastore) {
            result = ((Datastore) o).getName();
        } else {
            logger.warn("Could not convert type: {}", o.getClass().getName());
            result = o.toString();
        }
        return result;
    }

    @Override
    public boolean isConvertable(final Class<?> type) {
        return isSchema(type) || isTable(type) || isColumn(type) || is(type, Dictionary.class) || is(type,
                SynonymCatalog.class) || is(type, StringPattern.class) || is(type, Datastore.class);
    }
}
