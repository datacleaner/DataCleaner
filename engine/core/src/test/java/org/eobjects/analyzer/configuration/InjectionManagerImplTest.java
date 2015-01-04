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
package org.eobjects.analyzer.configuration;

import java.util.List;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.descriptors.SimpleDescriptorProvider;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.eobjects.analyzer.test.TestHelper;
import org.eobjects.analyzer.util.convert.StringConverter;
import org.apache.metamodel.util.MutableRef;
import org.junit.Ignore;

public class InjectionManagerImplTest extends TestCase {

    private static final MutableRef<List<String>> listRef = new MutableRef<List<String>>();

    @Ignore
    @AnalyzerBean("Fancy transformer")
    public static class FancyTransformer implements Analyzer<AnnotatedRowsResult> {

        @Provided
        List<String> stringList;

        @Configured
        InputColumn<Number> col;

        @Inject
        RowAnnotation rowAnnotation;

        @Inject
        RowAnnotationFactory rowAnnotationFactory;

        @Override
        public void run(InputRow row, int distinctCount) {
            Number value = row.getValue(col);
            if (value.intValue() % 2 == 0) {
                rowAnnotationFactory.annotate(row, distinctCount, rowAnnotation);
            } else {
                stringList.add(value.toString());
            }
        }

        @Override
        public AnnotatedRowsResult getResult() {
            listRef.set(stringList);
            return new AnnotatedRowsResult(rowAnnotation, rowAnnotationFactory, col);
        }
    }

    public void testInjectCustomClass() throws Exception {
        assertNull(listRef.get());

        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(FancyTransformer.class));

        final AnalyzerBeansConfigurationImpl conf = new AnalyzerBeansConfigurationImpl()
                .replace(new DatastoreCatalogImpl(TestHelper.createSampleDatabaseDatastore("orderdb")));

        try (final AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf)) {

            ajb.setDatastore("orderdb");
            ajb.addSourceColumns("PUBLIC.EMPLOYEES.EMPLOYEENUMBER");

            final AnalyzerJobBuilder<FancyTransformer> analyzerBuilder = ajb.addAnalyzer(FancyTransformer.class);
            analyzerBuilder.addInputColumns(ajb.getSourceColumns());

            final AnalysisResultFuture result = new AnalysisRunnerImpl(conf).run(ajb.toAnalysisJob());
            assertTrue(result.isSuccessful());

            AnnotatedRowsResult res = (AnnotatedRowsResult) result.getResults().get(0);
            assertEquals(13, res.getAnnotatedRowCount());
            assertNotNull(listRef.get());
            assertEquals(10, listRef.get().size());

        }
    }

    public void testGetInstanceUsingSimpleInjectionPoint() throws Exception {
        InjectionManagerImpl injectionManager = new InjectionManagerImpl(null);

        InjectionPoint<StringConverter> point = SimpleInjectionPoint.of(StringConverter.class);

        StringConverter instance = injectionManager.getInstance(point);
        assertNotNull(instance);
    }
}
