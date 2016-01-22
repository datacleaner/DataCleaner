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
package org.datacleaner.result;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.datacleaner.storage.InMemoryRowAnnotationFactory2;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.storage.RowAnnotationImpl;
import org.junit.Test;

public class CategorizationResultReducerTest {

    @Test
    public void testReduceEmpty() throws Exception {
        final RowAnnotationFactory annotationFactory = new InMemoryRowAnnotationFactory2(4);

        final CategorizationResultReducer reducer = new CategorizationResultReducer();
        reducer._rowAnnotationFactory = annotationFactory;

        final Map<String, RowAnnotation> categories1 = new LinkedHashMap<>();
        final Map<String, RowAnnotation> categories2 = new LinkedHashMap<>();
        final Map<String, RowAnnotation> categories3 = new LinkedHashMap<>();

        final Collection<CategorizationResult> results = new ArrayList<>();
        results.add(new CategorizationResult(annotationFactory, categories1));
        results.add(new CategorizationResult(annotationFactory, categories2));
        results.add(new CategorizationResult(annotationFactory, categories3));

        final CategorizationResult reducedResult = reducer.reduce(results);

        assertEquals(0, reducedResult.getCategoryNames().size());
    }

    @Test
    public void testReduceTypicalScenario() throws Exception {
        final RowAnnotationFactory annotationFactory = new InMemoryRowAnnotationFactory2(4);

        final CategorizationResultReducer reducer = new CategorizationResultReducer();
        reducer._rowAnnotationFactory = annotationFactory;

        final Map<String, RowAnnotation> categories1 = new LinkedHashMap<>();
        categories1.put("Male", new RowAnnotationImpl(50));
        categories1.put("Female", new RowAnnotationImpl(50));

        final Map<String, RowAnnotation> categories2 = new LinkedHashMap<>();
        categories2.put("Male", new RowAnnotationImpl(46));
        categories2.put("Female", new RowAnnotationImpl(50));
        categories2.put("Unknown", new RowAnnotationImpl(3));

        final Map<String, RowAnnotation> categories3 = new LinkedHashMap<>();
        categories3.put("Male", new RowAnnotationImpl(27));
        categories3.put("Female", new RowAnnotationImpl(24));

        final Collection<CategorizationResult> results = new ArrayList<>();
        results.add(new CategorizationResult(annotationFactory, categories1));
        results.add(new CategorizationResult(annotationFactory, categories2));
        results.add(new CategorizationResult(annotationFactory, categories3));

        final CategorizationResult reducedResult = reducer.reduce(results);

        assertEquals(3, reducedResult.getCategoryNames().size());
        assertEquals(123, reducedResult.getCategoryCount("Male"));
        assertEquals(124, reducedResult.getCategoryCount("Female"));
        assertEquals(3, reducedResult.getCategoryCount("Unknown"));
    }
}
