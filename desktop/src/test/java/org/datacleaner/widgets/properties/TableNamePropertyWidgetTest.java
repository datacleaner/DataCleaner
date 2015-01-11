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
package org.datacleaner.widgets.properties;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.metamodel.pojo.ArrayTableDataProvider;
import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.components.tablelookup.TableLookupTransformer;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.TransformerBeanDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.TransformerChangeListener;
import org.datacleaner.job.builder.TransformerJobBuilder;

public class TableNamePropertyWidgetTest extends TestCase {

    public void testTwoWidgetsForSameProperty() throws Exception {
        final TransformerBeanDescriptor<TableLookupTransformer> descriptor = Descriptors
                .ofTransformer(TableLookupTransformer.class);

        final ConfiguredPropertyDescriptor datastoreProperty = descriptor.getConfiguredProperty("Datastore");
        final ConfiguredPropertyDescriptor tableProperty = descriptor.getConfiguredProperty("Table name");
        final ConfiguredPropertyDescriptor schemaProperty = descriptor.getConfiguredProperty("Schema name");

        final AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl();
        final AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(configuration);

        final TransformerJobBuilder<TableLookupTransformer> tjb = analysisJobBuilder
                .addTransformer(TableLookupTransformer.class);

        final List<TableDataProvider<?>> tables = new ArrayList<>();
        tables.add(new ArrayTableDataProvider(new SimpleTableDef("foo", new String[] { "col1", "col2" }),
                new ArrayList<Object[]>()));
        tables.add(new ArrayTableDataProvider(new SimpleTableDef("bar", new String[] { "col3", "col4" }),
                new ArrayList<Object[]>()));

        final PojoDatastore ds = new PojoDatastore("myds", tables);

        final PropertyWidgetCollection collection1 = createPropertyWidgetCollection(tjb);
        final PropertyWidgetCollection collection2 = createPropertyWidgetCollection(tjb);

        final DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(ds);

        final SingleDatastorePropertyWidget datastoreWidget1 = new SingleDatastorePropertyWidget(tjb,
                datastoreProperty, datastoreCatalog);
        final SchemaNamePropertyWidget schemaWidget1 = new SchemaNamePropertyWidget(tjb, schemaProperty);
        final TableNamePropertyWidget tableWidget1 = new TableNamePropertyWidget(tjb, tableProperty);
        datastoreWidget1.connectToSchemaNamePropertyWidget(schemaWidget1);
        schemaWidget1.connectToTableNamePropertyWidget(tableWidget1);
        collection1.registerWidget(tableProperty, tableWidget1);

        final SingleDatastorePropertyWidget datastoreWidget2 = new SingleDatastorePropertyWidget(tjb,
                datastoreProperty, datastoreCatalog);
        final SchemaNamePropertyWidget schemaWidget2 = new SchemaNamePropertyWidget(tjb, schemaProperty);
        final TableNamePropertyWidget tableWidget2 = new TableNamePropertyWidget(tjb, tableProperty);
        datastoreWidget2.connectToSchemaNamePropertyWidget(schemaWidget2);
        schemaWidget2.connectToTableNamePropertyWidget(tableWidget2);
        collection2.registerWidget(tableProperty, tableWidget2);

        datastoreWidget1.setValue(ds);
        datastoreWidget2.setValue(ds);

        assertEquals("myds", schemaWidget1.getSchema().getName());
        assertEquals("myds", schemaWidget2.getSchema().getName());

        assertEquals(null, tableWidget1.getTable());
        assertEquals(null, tableWidget2.getTable());

        assertEquals(3, tableWidget1.getComboBox().getItemCount());
        assertEquals(3, tableWidget2.getComboBox().getItemCount());

        assertEquals("Table[name=bar,type=TABLE,remarks=null]", tableWidget1.getComboBox().getItemAt(1).toString());
        assertEquals("Table[name=bar,type=TABLE,remarks=null]", tableWidget2.getComboBox().getItemAt(1).toString());

        tableWidget1.setValue("bar");

        assertEquals("bar", tableWidget1.getTable().getName());
        assertEquals("bar", tableWidget2.getTable().getName());

        analysisJobBuilder.close();
    }

    private PropertyWidgetCollection createPropertyWidgetCollection(TransformerJobBuilder<TableLookupTransformer> tjb) {
        final PropertyWidgetCollection collection = new PropertyWidgetCollection(tjb);
        tjb.addChangeListener(new TransformerChangeListener() {
            @Override
            public void onRequirementChanged(TransformerJobBuilder<?> transformerJobBuilder) {
            }

            @Override
            public void onRemove(TransformerJobBuilder<?> transformerJobBuilder) {
            }

            @Override
            public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder,
                    List<MutableInputColumn<?>> outputColumns) {
            }

            @Override
            public void onConfigurationChanged(TransformerJobBuilder<?> transformerJobBuilder) {
                collection.onConfigurationChanged();
            }

            @Override
            public void onAdd(TransformerJobBuilder<?> transformerJobBuilder) {
            }
        });
        return collection;
    }
}
