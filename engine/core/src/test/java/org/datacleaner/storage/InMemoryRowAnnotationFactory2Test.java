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
package org.datacleaner.storage;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.apache.commons.lang.SerializationUtils;
import org.apache.metamodel.data.DataSetHeader;
import org.apache.metamodel.data.DefaultRow;
import org.apache.metamodel.data.SimpleDataSetHeader;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.MutableColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.data.MetaModelInputRow;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.data.TransformedInputRow;

public class InMemoryRowAnnotationFactory2Test extends TestCase {

    public void testRunningOutOfStorage() throws Exception {
        final int maxRecords = 5;
        final int maxSets = 2;
        final InMemoryRowAnnotationFactory2 f = new InMemoryRowAnnotationFactory2(maxSets, maxRecords);

        final AtomicInteger idCounter = new AtomicInteger();

        {
            RowAnnotation a1 = f.createAnnotation();
            assertNotNull(a1);
            assertFalse(f.hasSampleRows(a1));

            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a1);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a1);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a1);
            assertTrue(f.hasSampleRows(a1));
            assertEquals(3, f.getSampleRows(a1).size());

            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a1);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a1);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a1);
            assertTrue(f.hasSampleRows(a1));
            assertEquals(5, f.getSampleRows(a1).size());
        }

        {
            RowAnnotation a2 = f.createAnnotation();
            assertNotNull(a2);
            assertFalse(f.hasSampleRows(a2));

            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a2);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a2);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a2);
            assertTrue(f.hasSampleRows(a2));
            assertEquals(3, f.getSampleRows(a2).size());

            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a2);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a2);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a2);
            assertTrue(f.hasSampleRows(a2));
            assertEquals(5, f.getSampleRows(a2).size());
        }

        {
            RowAnnotation a3 = f.createAnnotation();
            assertNotNull(a3);

            assertFalse(f.hasSampleRows(a3));

            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a3);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a3);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a3);
            assertFalse(f.hasSampleRows(a3));
            assertEquals(0, f.getSampleRows(a3).size());
        }
    }

    public static class MyNonSerializableClass {
        // used to demonstrate a non-serializable value in a row
    }

    public void testRecoverFromBadSerializationAttempt() throws Exception {
        final InMemoryRowAnnotationFactory2 rowAnnotationFactory = new InMemoryRowAnnotationFactory2();
        final RowAnnotation annotation = rowAnnotationFactory.createAnnotation();

        final DataSetHeader header = new SimpleDataSetHeader(new Column[] { new MutableColumn("foo") });

        rowAnnotationFactory.annotate(new MetaModelInputRow(1, new DefaultRow(header,
                new Object[] { "serializable string" })), annotation);
        rowAnnotationFactory.annotate(new MetaModelInputRow(2, new DefaultRow(header,
                new Object[] { new MyNonSerializableClass() })), annotation);
        rowAnnotationFactory.annotate(new TransformedInputRow(new MetaModelInputRow(3, new DefaultRow(header,
                new Object[] { "another serializable string" }))), annotation);

        final TransformedInputRow transformedInputRow = new TransformedInputRow(new MetaModelInputRow(2,
                new DefaultRow(header, new Object[] { new MyNonSerializableClass() })));
        transformedInputRow.addValue(new MockInputColumn<>("bar"), new MyNonSerializableClass());
        rowAnnotationFactory.annotate(transformedInputRow, annotation);

        final byte[] bytes = SerializationUtils.serialize(rowAnnotationFactory);
        final InMemoryRowAnnotationFactory2 deserializedRowAnnotationFactory = (InMemoryRowAnnotationFactory2) SerializationUtils
                .deserialize(bytes);

        final RowAnnotation deserializedAnnotation = deserializedRowAnnotationFactory.getSampledRowAnnotations()
                .iterator().next();
        final List<InputRow> sampleRows = deserializedRowAnnotationFactory.getSampleRows(deserializedAnnotation);
        assertEquals(4, sampleRows.size());
        assertEquals("MetaModelInputRow[Row[values=[serializable string]]]", sampleRows.get(0).toString());
        assertEquals("MetaModelInputRow[Row[values=[NON-SERIALIZABLE-VALUE]]]", sampleRows.get(1).toString());
        assertEquals("TransformedInputRow[values={},delegate=MetaModelInputRow[Row[values=[another serializable string]]]]", sampleRows.get(2).toString());
        assertEquals("TransformedInputRow[values={MockInputColumn[name=bar]=NON-SERIALIZABLE-VALUE},delegate=MetaModelInputRow[Row[values=[NON-SERIALIZABLE-VALUE]]]]", sampleRows.get(3).toString());
    }
}
