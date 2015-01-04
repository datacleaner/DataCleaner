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
package org.eobjects.analyzer.beans;

import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.CompletenessAnalyzer.Condition;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.PojoDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;

public class CompletenessAnalyzerTest extends TestCase {

    public void testIsDistributable() throws Exception {
        AnalyzerBeanDescriptor<CompletenessAnalyzer> descriptor = Descriptors.ofAnalyzer(CompletenessAnalyzer.class);
        assertTrue(descriptor.isDistributable());
    }
    
    public void testAllFieldsEvaluationMode() throws Exception {
        final RowAnnotationFactory annotationFactory = new InMemoryRowAnnotationFactory();

        final InputColumn<?> col1 = new MockInputColumn<String>("foo");
        final InputColumn<?> col2 = new MockInputColumn<String>("bar");

        final CompletenessAnalyzer analyzer = new CompletenessAnalyzer();
        analyzer._evaluationMode = CompletenessAnalyzer.EvaluationMode.ALL_FIELDS;
        analyzer._annotationFactory = annotationFactory;
        analyzer._invalidRecords = annotationFactory.createAnnotation();
        analyzer._valueColumns = new InputColumn[] { col1, col2 };
        analyzer._conditions = new CompletenessAnalyzer.Condition[] { CompletenessAnalyzer.Condition.NOT_NULL, CompletenessAnalyzer.Condition.NOT_NULL };

        analyzer.init();

        analyzer.run(new MockInputRow(1001).put(col1, null).put(col2, null), 1);
        analyzer.run(new MockInputRow(1002).put(col1, "hello").put(col2, null), 1);
        analyzer.run(new MockInputRow(1002).put(col1, null).put(col2, "world"), 1);
        analyzer.run(new MockInputRow(1002).put(col1, "hello").put(col2, "world"), 1);

        assertEquals(4, analyzer.getResult().getTotalRowCount());
        assertEquals(1, analyzer.getResult().getInvalidRowCount());
        assertEquals(3, analyzer.getResult().getValidRowCount());
    }

    public void testConfigurableBeanConfiguration() throws Exception {
        AnalysisJobBuilder ajb = new AnalysisJobBuilder(new AnalyzerBeansConfigurationImpl());
        try {
            List<TableDataProvider<?>> tableDataProviders = Collections.emptyList();
            ajb.setDatastore(new PojoDatastore("ds", tableDataProviders));
            ajb.addSourceColumn(new MutableColumn("foo", ColumnType.VARCHAR));
            
            AnalyzerJobBuilder<CompletenessAnalyzer> analyzer = ajb.addAnalyzer(CompletenessAnalyzer.class);
            analyzer.getConfigurableBean().setValueColumns(ajb.getSourceColumns().toArray(new InputColumn[0]));
            analyzer.getConfigurableBean().fillAllConditions(Condition.NOT_BLANK_OR_NULL);
            
            assertTrue(analyzer.isConfigured(true));
        } finally {
            ajb.close();
        }
    }

    public void testSimpleScenario() throws Exception {
        final RowAnnotationFactory annotationFactory = new InMemoryRowAnnotationFactory();

        final InputColumn<?> col1 = new MockInputColumn<String>("foo");
        final InputColumn<?> col2 = new MockInputColumn<String>("bar");
        final InputColumn<?> col3 = new MockInputColumn<String>("baz");

        final CompletenessAnalyzer analyzer = new CompletenessAnalyzer();
        analyzer._annotationFactory = annotationFactory;
        analyzer._invalidRecords = annotationFactory.createAnnotation();
        analyzer._valueColumns = new InputColumn[] { col1, col2, col3 };
        analyzer._conditions = new CompletenessAnalyzer.Condition[] { CompletenessAnalyzer.Condition.NOT_NULL,
                CompletenessAnalyzer.Condition.NOT_BLANK_OR_NULL, CompletenessAnalyzer.Condition.NOT_NULL };

        analyzer.init();

        analyzer.run(new MockInputRow(1001).put(col1, null).put(col2, null).put(col3, null), 1);
        analyzer.run(new MockInputRow(1002).put(col1, "").put(col2, "").put(col3, ""), 1);

        assertEquals(2, analyzer.getResult().getTotalRowCount());
        assertEquals(0, analyzer.getResult().getValidRowCount());
        assertEquals(2, analyzer.getResult().getInvalidRowCount());

        analyzer.run(new MockInputRow(1002).put(col1, "").put(col2, "not blank").put(col3, ""), 1);
        analyzer.run(new MockInputRow(1002).put(col1, "not blank").put(col2, "not blank").put(col3, "not blank"), 1);

        assertEquals(4, analyzer.getResult().getTotalRowCount());
        assertEquals(2, analyzer.getResult().getValidRowCount());
        assertEquals(2, analyzer.getResult().getInvalidRowCount());

        analyzer.run(new MockInputRow(1002).put(col1, null).put(col2, "not blank").put(col3, ""), 1);

        assertEquals(5, analyzer.getResult().getTotalRowCount());
        assertEquals(2, analyzer.getResult().getValidRowCount());
        assertEquals(3, analyzer.getResult().getInvalidRowCount());
    }
}
