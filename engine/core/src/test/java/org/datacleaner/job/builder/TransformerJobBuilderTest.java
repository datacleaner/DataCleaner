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

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.components.mock.TransformerMock;
import org.datacleaner.components.tablelookup.TableLookupTransformer;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.data.ConstantInputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.TransformerBeanDescriptor;
import org.datacleaner.job.IdGenerator;
import org.datacleaner.job.PrefixedIdGenerator;
import org.datacleaner.util.InputColumnComparator;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.HasNameMapper;

public class TransformerJobBuilderTest extends TestCase {

    private AnalyzerBeansConfiguration configuration;
    private AnalysisJobBuilder ajb;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        configuration = new AnalyzerBeansConfigurationImpl();

        ajb = new AnalysisJobBuilder(configuration);

        ajb.addSourceColumn(new MutableColumn("fooInt", ColumnType.INTEGER));
        ajb.addSourceColumn(new MutableColumn("fooStr", ColumnType.VARCHAR));
    }

    public void testGetOutputColumnsRetainingAndSorting() throws Exception {
        final TransformerJobBuilder<?> tjb1 = ajb.addTransformer(TransformerMockForOutputColumnChanges.class);
        tjb1.addInputColumn(ajb.getSourceColumnByName("fooStr"));

        final TransformerJobBuilder<?> tjb2 = ajb.addTransformer(TransformerMockForOutputColumnChanges.class);
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

    private String getSortedOutputColumns(TransformerJobBuilder<?> tjb1, TransformerJobBuilder<?> tjb2) {
        List<MutableInputColumn<?>> cols1 = tjb1.getOutputColumns();
        List<MutableInputColumn<?>> cols2 = tjb2.getOutputColumns();
        List<InputColumn<?>> list = new ArrayList<InputColumn<?>>();
        list.addAll(cols1);
        list.addAll(cols2);

        Collections.sort(list, new InputColumnComparator());

        List<String> names = CollectionUtils.map(list, new HasNameMapper());
        return names.toString();
    }

    public void testSetInvalidPropertyType() throws Exception {
        TransformerJobBuilder<TransformerMock> tjb = ajb.addTransformer(TransformerMock.class);
        try {
            tjb.setConfiguredProperty("Input", "hello");
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid value type: java.lang.String, expected: org.datacleaner.api.InputColumn",
                    e.getMessage());
        }
    }

    public void testIsConfigured() throws Exception {
        TransformerJobBuilder<TransformerMock> tjb = ajb.addTransformer(TransformerMock.class);
        assertFalse(tjb.isConfigured());

        tjb.setConfiguredProperty("Some integer", null);

        tjb.addInputColumn(ajb.getSourceColumns().get(1));
        assertFalse(tjb.isConfigured());

        try {
            tjb.isConfigured(true);
            fail("Exception occurred");
        } catch (UnconfiguredConfiguredPropertyException e) {
            assertEquals(
                    "Property 'Some integer' is not properly configured (TransformerJobBuilder[transformer=Transformer mock,inputColumns=[MetaModelInputColumn[fooStr]]])",
                    e.getMessage());
        }

        tjb.setConfiguredProperty("Some integer", 10);
        assertTrue(tjb.isConfigured());

        tjb.removeInputColumn(ajb.getSourceColumns().get(1));
        assertFalse(tjb.isConfigured());
    }

    public void testClearInputColumnsArray() throws Exception {
        TransformerJobBuilder<TransformerMock> tjb = ajb.addTransformer(TransformerMock.class);
        tjb.addInputColumn(ajb.getSourceColumns().get(1));
        tjb.addInputColumn(new ConstantInputColumn("foo"));

        assertEquals(2, tjb.getInputColumns().size());

        tjb.clearInputColumns();

        assertEquals(0, tjb.getInputColumns().size());
    }

    public void testAddNonRequiredColumn() throws Exception {
        final TransformerJobBuilder<TableLookupTransformer> tjb = ajb.addTransformer(TableLookupTransformer.class);

        final Set<ConfiguredPropertyDescriptor> inputProperties = tjb.getDescriptor().getConfiguredPropertiesForInput(
                true);
        assertEquals(1, inputProperties.size());

        final ConfiguredPropertyDescriptor inputProperty = inputProperties.iterator().next();
        assertFalse(inputProperty.isRequired());

        assertNull(tjb.getConfiguredProperty(inputProperty));

        tjb.addInputColumn(ajb.getSourceColumns().get(0));

        assertNotNull(tjb.getConfiguredProperty(inputProperty));
    }

    public void testClearInputColumnsSingle() throws Exception {
        TransformerJobBuilder<SingleInputColumnTransformer> tjb = ajb
                .addTransformer(SingleInputColumnTransformer.class);
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
        TransformerJobBuilder<SingleInputColumnTransformer> tjb = ajb
                .addTransformer(SingleInputColumnTransformer.class);
        assertEquals(0, tjb.getInputColumns().size());
        assertFalse(tjb.isConfigured());
        try {
            tjb.addInputColumn(ajb.getSourceColumns().get(0));
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            assertEquals("Unsupported InputColumn type: class java.lang.Integer, expected: class java.lang.String",
                    e.getMessage());
        }
        assertFalse(tjb.isConfigured());

        tjb.addInputColumn(ajb.getSourceColumns().get(1));
        assertEquals(1, tjb.getInputColumns().size());
        assertTrue(tjb.isConfigured());
    }

    public void testNoOutputWhenNotConfigured() throws Exception {
        TransformerJobBuilder<SingleInputColumnTransformer> tjb = ajb
                .addTransformer(SingleInputColumnTransformer.class);

        // not yet configured
        assertEquals(0, tjb.getOutputColumns().size());

        tjb.addInputColumn(new MockInputColumn<String>("email", String.class));

        assertEquals(2, tjb.getOutputColumns().size());
    }

    public void testConfigureByConfigurableBean() throws Exception {
        IdGenerator IdGenerator = new PrefixedIdGenerator("");

        TransformerBeanDescriptor<ConvertToNumberTransformer> descriptor = Descriptors
                .ofTransformer(ConvertToNumberTransformer.class);
        TransformerJobBuilder<ConvertToNumberTransformer> builder = new TransformerJobBuilder<ConvertToNumberTransformer>(
                new AnalysisJobBuilder(null), descriptor, IdGenerator);
        assertFalse(builder.isConfigured());

        ConvertToNumberTransformer configurableBean = builder.getComponentInstance();
        InputColumn<String> input = new MockInputColumn<String>("foo", String.class);
        configurableBean.setInput(input);

        assertTrue(builder.isConfigured());
        ConfiguredPropertyDescriptor propertyDescriptor = descriptor.getConfiguredPropertiesForInput().iterator()
                .next();
        InputColumn<?>[] value = (InputColumn<?>[]) builder.getConfiguredProperties().get(propertyDescriptor);
        assertEquals("[MockInputColumn[name=foo]]", Arrays.toString(value));
    }

    public void testReplaceAutomaticOutputColumnNames() throws Exception {
        IdGenerator IdGenerator = new PrefixedIdGenerator("");

        TransformerBeanDescriptor<TransformerMock> descriptor = Descriptors.ofTransformer(TransformerMock.class);

        TransformerJobBuilder<TransformerMock> builder = new TransformerJobBuilder<TransformerMock>(
                new AnalysisJobBuilder(new AnalyzerBeansConfigurationImpl()), descriptor, IdGenerator);

        MockInputColumn<String> colA = new MockInputColumn<String>("A", String.class);
        MockInputColumn<String> colB = new MockInputColumn<String>("B", String.class);
        MockInputColumn<String> colC = new MockInputColumn<String>("C", String.class);
        builder.addInputColumn(colA);
        builder.addInputColumn(colB);
        builder.addInputColumn(colC);

        List<MutableInputColumn<?>> outputColumns = builder.getOutputColumns();
        assertEquals(3, outputColumns.size());
        assertEquals(
                "[TransformedInputColumn[id=trans-0001-0002,name=Transformer mock (1)], TransformedInputColumn[id=trans-0001-0003,name=Transformer mock (2)], TransformedInputColumn[id=trans-0001-0004,name=Transformer mock (3)]]",
                outputColumns.toString());

        builder.removeInputColumn(colB);
        outputColumns.get(0).setName("Foo A");

        outputColumns = builder.getOutputColumns();
        assertEquals(2, outputColumns.size());
        assertEquals(
                "[TransformedInputColumn[id=trans-0001-0002,name=Foo A], TransformedInputColumn[id=trans-0001-0003,name=Transformer mock (2)]]",
                outputColumns.toString());

        builder.addInputColumn(colB);
        outputColumns = builder.getOutputColumns();
        assertEquals(3, outputColumns.size());
        assertEquals(
                "[TransformedInputColumn[id=trans-0001-0002,name=Foo A], TransformedInputColumn[id=trans-0001-0003,name=Transformer mock (2)], TransformedInputColumn[id=trans-0001-0005,name=Transformer mock (3)]]",
                outputColumns.toString());

        ConfiguredPropertyDescriptor inputColumnProperty = descriptor.getConfiguredPropertiesForInput().iterator()
                .next();
        builder.setConfiguredProperty(inputColumnProperty, new InputColumn[] { colA, colB, colC });
        outputColumns = builder.getOutputColumns();
        assertEquals(3, outputColumns.size());
        assertEquals(
                "[TransformedInputColumn[id=trans-0001-0002,name=Foo A], TransformedInputColumn[id=trans-0001-0003,name=Transformer mock (2)], TransformedInputColumn[id=trans-0001-0005,name=Transformer mock (3)]]",
                outputColumns.toString());

        assertEquals("Transformer mock (1)", outputColumns.get(0).getInitialName());
    }
}
