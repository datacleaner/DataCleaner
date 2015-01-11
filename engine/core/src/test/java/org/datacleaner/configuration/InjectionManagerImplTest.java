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
package org.datacleaner.configuration;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.apache.metamodel.util.MutableRef;
import org.datacleaner.beans.api.Analyzer;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Provided;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerJobBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.test.TestHelper;
import org.datacleaner.util.convert.StringConverter;
import org.junit.Ignore;

public class InjectionManagerImplTest extends TestCase {

    private static final MutableRef<List<String>> listRef = new MutableRef<List<String>>();

    @Ignore
    @Named("Fancy transformer")
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
