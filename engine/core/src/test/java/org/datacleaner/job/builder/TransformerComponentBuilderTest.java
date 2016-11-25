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
package org.datacleaner.job.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.HasNameMapper;
import org.datacleaner.api.InputColumn;
import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.components.mock.TransformerMock;
import org.datacleaner.components.tablelookup.TableLookupTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.data.ConstantInputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.job.IdGenerator;
import org.datacleaner.job.PrefixedIdGenerator;
import org.datacleaner.util.InputColumnComparator;

import junit.framework.TestCase;

public class TransformerComponentBuilderTest extends TestCase {

    private DataCleanerConfiguration configuration;
    private AnalysisJobBuilder ajb;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        configuration = new DataCleanerConfigurationImpl();

        ajb = new AnalysisJobBuilder(configuration);

        ajb.addSourceColumn(new MutableColumn("fooInt", ColumnType.INTEGER));
        ajb.addSourceColumn(new MutableColumn("fooStr", ColumnType.VARCHAR));
    }

    public void testGetOutputColumnsRetainingAndSorting() throws Exception {
        final TransformerComponentBuilder<?> tjb1 = ajb.addTransformer(TransformerMockForOutputColumnChanges.class);
        tjb1.addInputColumn(ajb.getSourceColumnByName("fooStr"));

        final TransformerComponentBuilder<?> tjb2 = ajb.addTransformer(TransformerMockForOutputColumnChanges.class);
        tjb2.addInputColumn(ajb.getSourceColumnByName("fooStr"));

        assertEquals("[foo, bar, foo, bar]", getSortedOutputColumns(tjb1, tjb2));

        // Column ordering is retained when configuration changes output column
        // names
        tjb1.setConfiguredProperty("Output column names", new String[] { "Hello", "There" });
        tjb2.setConfiguredProperty("Output column names", new String[] { "Big", "World" });

        assertEquals("[Hello, There, Big, World]", getSortedOutputColumns(tjb1, tjb2));

        // Column ordering is retained when user changes output column name
        tjb1.getOutputColumns().get(0).setName("Howdy");
        tjb1.getOutputColumns().get(1).setName("There");
        tjb2.getOutputColumns().get(0).setName("Column3");
        tjb2.getOutputColumns().get(1).setName("Column4");

        assertEquals("[Howdy, There, Column3, Column4]", getSortedOutputColumns(tjb1, tjb2));

        // Column names and ordering is reset when configuration changes output
        // columns size
        tjb1.setConfiguredProperty("Output column names", new String[] { "Hello", "To", "You" });
        assertEquals("[Howdy, To, You, Column3, Column4]", getSortedOutputColumns(tjb1, tjb2));
    }

    private String getSortedOutputColumns(final TransformerComponentBuilder<?> tjb1,
            final TransformerComponentBuilder<?> tjb2) {
        final List<MutableInputColumn<?>> cols1 = tjb1.getOutputColumns();
        final List<MutableInputColumn<?>> cols2 = tjb2.getOutputColumns();
        final List<InputColumn<?>> list = new ArrayList<>();
        list.addAll(cols1);
        list.addAll(cols2);

        Collections.sort(list, new InputColumnComparator());

        final List<String> names = CollectionUtils.map(list, new HasNameMapper());
        return names.toString();
    }

    public void testSetInvalidPropertyType() throws Exception {
        final TransformerComponentBuilder<TransformerMock> tjb = ajb.addTransformer(TransformerMock.class);
        try {
            tjb.setConfiguredProperty("Input", "hello");
            fail("Exception expected");
        } catch (final IllegalArgumentException e) {
            assertEquals("Invalid value type: java.lang.String, expected: org.datacleaner.api.InputColumn",
                    e.getMessage());
        }
    }

    public void testIsConfigured() throws Exception {
        final TransformerComponentBuilder<TransformerMock> tjb = ajb.addTransformer(TransformerMock.class);
        assertFalse(tjb.isConfigured());

        tjb.setConfiguredProperty("Some integer", null);

        tjb.addInputColumn(ajb.getSourceColumns().get(1));
        assertFalse(tjb.isConfigured());

        try {
            tjb.isConfigured(true);
            fail("Exception occurred");
        } catch (final UnconfiguredConfiguredPropertyException e) {
            assertEquals("Property 'Some integer' is not properly configured (TransformerComponentBuilder"
                    + "[transformer=Transformer mock,inputColumns=[MetaModelInputColumn[fooStr]]])", e.getMessage());
        }

        tjb.setConfiguredProperty("Some integer", 10);
        assertTrue(tjb.isConfigured());

        tjb.removeInputColumn(ajb.getSourceColumns().get(1));
        assertFalse(tjb.isConfigured());
    }

    public void testClearInputColumnsArray() throws Exception {
        final TransformerComponentBuilder<TransformerMock> tjb = ajb.addTransformer(TransformerMock.class);
        tjb.addInputColumn(ajb.getSourceColumns().get(1));
        tjb.addInputColumn(new ConstantInputColumn("foo"));

        assertEquals(2, tjb.getInputColumns().size());

        tjb.clearInputColumns();

        assertEquals(0, tjb.getInputColumns().size());
    }

    public void testAddNonRequiredColumn() throws Exception {
        final TransformerComponentBuilder<TableLookupTransformer> tjb =
                ajb.addTransformer(TableLookupTransformer.class);

        final Set<ConfiguredPropertyDescriptor> inputProperties =
                tjb.getDescriptor().getConfiguredPropertiesForInput(true);
        assertEquals(1, inputProperties.size());

        final ConfiguredPropertyDescriptor inputProperty = inputProperties.iterator().next();
        assertFalse(inputProperty.isRequired());

        assertNull(tjb.getConfiguredProperty(inputProperty));

        tjb.addInputColumn(ajb.getSourceColumns().get(0));

        assertNotNull(tjb.getConfiguredProperty(inputProperty));
    }

    public void testClearInputColumnsSingle() throws Exception {
        final TransformerComponentBuilder<SingleInputColumnTransformer> tjb =
                ajb.addTransformer(SingleInputColumnTransformer.class);
        tjb.addInputColumn(ajb.getSourceColumns().get(1));

        assertEquals(1, tjb.getInputColumns().size());

        tjb.clearInputColumns();

        assertEquals(0, tjb.getInputColumns().size());
    }

    public void testGetAvailableInputColumns() throws Exception {
        assertEquals(2, ajb.getAvailableInputColumns(Object.class).size());
        assertEquals(2, ajb.getAvailableInputColumns((Class<?>) null).size());
        assertEquals(1, ajb.getAvailableInputColumns(String.class).size());
        assertEquals(0, ajb.getAvailableInputColumns(Date.class).size());
    }

    public void testInvalidInputColumnType() throws Exception {
        final TransformerComponentBuilder<SingleInputColumnTransformer> tjb =
                ajb.addTransformer(SingleInputColumnTransformer.class);
        assertEquals(0, tjb.getInputColumns().size());
        assertFalse(tjb.isConfigured());
        try {
            tjb.addInputColumn(ajb.getSourceColumns().get(0));
            fail("Exception expected");
        } catch (final IllegalArgumentException e) {
            assertEquals("Unsupported InputColumn type: class java.lang.Integer, expected: class java.lang.String",
                    e.getMessage());
        }
        assertFalse(tjb.isConfigured());

        tjb.addInputColumn(ajb.getSourceColumns().get(1));
        assertEquals(1, tjb.getInputColumns().size());
        assertTrue(tjb.isConfigured());
    }

    public void testNoOutputWhenNotConfigured() throws Exception {
        final TransformerComponentBuilder<SingleInputColumnTransformer> tjb =
                ajb.addTransformer(SingleInputColumnTransformer.class);

        // not yet configured
        assertEquals(0, tjb.getOutputColumns().size());

        tjb.addInputColumn(new MockInputColumn<>("email", String.class));

        assertEquals(2, tjb.getOutputColumns().size());
    }

    public void testConfigureByConfigurableBean() throws Exception {
        final IdGenerator IdGenerator = new PrefixedIdGenerator("");

        final TransformerDescriptor<ConvertToNumberTransformer> descriptor =
                Descriptors.ofTransformer(ConvertToNumberTransformer.class);
        final TransformerComponentBuilder<ConvertToNumberTransformer> builder =
                new TransformerComponentBuilder<>(new AnalysisJobBuilder(null), descriptor, IdGenerator);
        assertFalse(builder.isConfigured());

        final ConvertToNumberTransformer configurableBean = builder.getComponentInstance();
        final InputColumn<String> input = new MockInputColumn<>("foo", String.class);
        configurableBean.setInput(input);

        assertTrue(builder.isConfigured(true));
        final ConfiguredPropertyDescriptor propertyDescriptor =
                descriptor.getConfiguredPropertiesForInput().iterator().next();
        final InputColumn<?>[] value = (InputColumn<?>[]) builder.getConfiguredProperties().get(propertyDescriptor);
        assertEquals("[MockInputColumn[name=foo]]", Arrays.toString(value));
    }

    public void testReplaceAutomaticOutputColumnNames() throws Exception {
        final IdGenerator IdGenerator = new PrefixedIdGenerator("");

        final TransformerDescriptor<TransformerMock> descriptor = Descriptors.ofTransformer(TransformerMock.class);

        final TransformerComponentBuilder<TransformerMock> builder =
                new TransformerComponentBuilder<>(new AnalysisJobBuilder(new DataCleanerConfigurationImpl()),
                        descriptor, IdGenerator);

        final MockInputColumn<String> colA = new MockInputColumn<>("A", String.class);
        final MockInputColumn<String> colB = new MockInputColumn<>("B", String.class);
        final MockInputColumn<String> colC = new MockInputColumn<>("C", String.class);
        builder.addInputColumn(colA);
        builder.addInputColumn(colB);
        builder.addInputColumn(colC);

        List<MutableInputColumn<?>> outputColumns = builder.getOutputColumns();
        assertEquals(3, outputColumns.size());
        assertEquals("[TransformedInputColumn[id=trans-0001-0002,name=Transformer mock (1)], "
                + "TransformedInputColumn[id=trans-0001-0003,name=Transformer mock (2)], "
                + "TransformedInputColumn[id=trans-0001-0004,name=Transformer mock (3)]]", outputColumns.toString());

        builder.removeInputColumn(colB);
        outputColumns.get(0).setName("Foo A");

        outputColumns = builder.getOutputColumns();
        assertEquals(2, outputColumns.size());
        assertEquals("[TransformedInputColumn[id=trans-0001-0002,name=Foo A], "
                + "TransformedInputColumn[id=trans-0001-0003,name=Transformer mock (2)]]", outputColumns.toString());

        builder.addInputColumn(colB);
        outputColumns = builder.getOutputColumns();
        assertEquals(3, outputColumns.size());
        assertEquals("[TransformedInputColumn[id=trans-0001-0002,name=Foo A], "
                + "TransformedInputColumn[id=trans-0001-0003,name=Transformer mock (2)], "
                + "TransformedInputColumn[id=trans-0001-0005,name=Transformer mock (3)]]", outputColumns.toString());

        final ConfiguredPropertyDescriptor inputColumnProperty =
                descriptor.getConfiguredPropertiesForInput().iterator().next();
        builder.setConfiguredProperty(inputColumnProperty, new InputColumn[] { colA, colB, colC });
        outputColumns = builder.getOutputColumns();
        assertEquals(3, outputColumns.size());
        assertEquals("[TransformedInputColumn[id=trans-0001-0002,name=Foo A], "
                + "TransformedInputColumn[id=trans-0001-0003,name=Transformer mock (2)], "
                + "TransformedInputColumn[id=trans-0001-0005,name=Transformer mock (3)]]", outputColumns.toString());

        assertEquals("Transformer mock (1)", outputColumns.get(0).getInitialName());
    }
}
