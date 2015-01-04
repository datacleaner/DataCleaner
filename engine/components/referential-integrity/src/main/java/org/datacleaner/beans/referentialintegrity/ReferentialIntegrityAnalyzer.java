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
package org.datacleaner.beans.referentialintegrity;

import javax.inject.Inject;

import org.datacleaner.beans.api.Alias;
import org.datacleaner.beans.api.Analyzer;
import org.datacleaner.beans.api.AnalyzerBean;
import org.datacleaner.beans.api.Categorized;
import org.datacleaner.beans.api.Close;
import org.datacleaner.beans.api.ColumnProperty;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Description;
import org.datacleaner.beans.api.Initialize;
import org.datacleaner.beans.api.MappedProperty;
import org.datacleaner.beans.api.Provided;
import org.datacleaner.beans.api.SchemaProperty;
import org.datacleaner.beans.api.TableProperty;
import org.datacleaner.beans.categories.ValidationCategory;
import org.datacleaner.beans.transform.TableLookupTransformer;
import org.datacleaner.connection.Datastore;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

@AnalyzerBean("Referential integrity")
@Description("Check the integrity of a foreign key by checking that every value can be resolved in another table (which may be in a different datastore altogether).")
@Categorized(ValidationCategory.class)
public class ReferentialIntegrityAnalyzer implements Analyzer<ReferentialIntegrityAnalyzerResult> {

    private static final String PROPERTY_NAME_DATASTORE = "Datastore";
    private static final String PROPERTY_NAME_SCHEMA_NAME = "Schema name";
    private static final String PROPERTY_NAME_TABLE_NAME = "Table name";

    @Inject
    @Configured(order = 1)
    InputColumn<?> foreignKey;

    @Inject
    @Configured(order = 2, value = PROPERTY_NAME_DATASTORE)
    Datastore datastore;

    @Inject
    @Configured(order = 3, value = PROPERTY_NAME_SCHEMA_NAME)
    @Alias("Schema")
    @SchemaProperty
    @MappedProperty(PROPERTY_NAME_DATASTORE)
    String schemaName;

    @Inject
    @Configured(order = 4, value = PROPERTY_NAME_TABLE_NAME)
    @Alias("Table")
    @TableProperty
    @MappedProperty(PROPERTY_NAME_SCHEMA_NAME)
    String tableName;

    @Inject
    @Configured(order = 5)
    @ColumnProperty
    @MappedProperty(PROPERTY_NAME_TABLE_NAME)
    String columnName;

    @Inject
    @Configured(required = false)
    @Description("Use a client-side cache to avoid looking up multiple times with same inputs.")
    boolean cacheLookups = true;

    @Inject
    @Configured(required = false)
    @Description("Ignore null values")
    boolean ignoreNullValues = true;

    @Inject
    @Provided
    RowAnnotation annotation;

    @Inject
    @Provided
    RowAnnotationFactory annotationFactory;

    private TableLookupTransformer _tableLookup;

    @Initialize
    public void init() {
        _tableLookup = new TableLookupTransformer(datastore, schemaName, tableName, new String[] { columnName },
                new InputColumn<?>[] { foreignKey }, new String[] { columnName }, cacheLookups);
        _tableLookup.init();
    }

    @Override
    public void run(InputRow row, int distinctCount) {
        Object value = row.getValue(foreignKey);
        if (value == null) {
            if (ignoreNullValues) {
                // skip processing this record - null is fine
                return;
            } else {
                // no need in looking up "null": This one has no integrity
                annotationFactory.annotate(row, distinctCount, annotation);
                return;
            }
        }

        Object[] result = _tableLookup.transform(row);
        assert result.length == 1;

        Object object = result[0];
        if (object == null) {
            annotationFactory.annotate(row, distinctCount, annotation);
        }
    }

    @Override
    public ReferentialIntegrityAnalyzerResult getResult() {
        return new ReferentialIntegrityAnalyzerResult(annotation, annotationFactory,
                new InputColumn<?>[] { foreignKey });
    }

    @Close
    public void close() {
        if (_tableLookup != null) {
            _tableLookup.close();
            _tableLookup = null;
        }
    }

}
