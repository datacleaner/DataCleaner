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

import org.datacleaner.api.InputRow;

/**
 * Abstract implementation of {@link RowAnnotationSampleContainer}.
 * 
 * This class is the successor of {@link AbstractRowAnnotationFactory} which was
 * found to be too wasteful in terms of memory usage. A new class was added to
 * allow deserialization of old DataCleaner results, yet this class fully
 * replaces the old one functionally.
 */
public abstract class AbstractRowAnnotationFactory2 implements RowAnnotationFactory, RowAnnotationSampleContainer,
        RowAnnotationHandler {

    @Override
    public final RowAnnotation createAnnotation() {
        return new RowAnnotationImpl();
    }

    @Override
    public void resetAnnotation(RowAnnotation annotation) {
        final RowAnnotationImpl annotationImpl = (RowAnnotationImpl) annotation;
        annotationImpl.resetRowCount();
    }

    @Override
    public void transferAnnotations(RowAnnotation from, RowAnnotation to) {
        final RowAnnotationImpl fromImpl = (RowAnnotationImpl) from;
        final RowAnnotationImpl toImpl = (RowAnnotationImpl) to;

        toImpl.incrementRowCount(fromImpl.getRowCount());
        fromImpl.resetRowCount();
    }

    @Override
    public void annotate(InputRow row, RowAnnotation annotation) {
        final RowAnnotationImpl annotationImpl = (RowAnnotationImpl) annotation;
        annotationImpl.incrementRowCount(1);
    }

    @Override
    public void annotate(InputRow row, int distinctCount, RowAnnotation annotation) {
        for (int i = 0; i < distinctCount; i++) {
            annotate(row, annotation);
        }
    }

    @Override
    public boolean hasSampleRows(RowAnnotation annotation) {
        final List<InputRow> sampleRows = getSampleRows(annotation);
        if (sampleRows == null || sampleRows.isEmpty()) {
            return false;
        }
        return true;
    }
}
