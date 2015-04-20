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

import java.util.Collections;
import java.util.Map;

import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;

/**
 * A {@link RowAnnotationFactory} that does not store any row samples. As such
 * it is usually not very useful to the user but can function as a stub for
 * situations where the row data is irrelevant.
 */
public class DummyRowAnnotationFactory implements RowAnnotationFactory {

    @Override
    public RowAnnotation createAnnotation() {
        return new RowAnnotationImpl();
    }

    @Override
    public void annotate(InputRow[] rows, RowAnnotation annotation) {
        RowAnnotationImpl a = (RowAnnotationImpl) annotation;
        a.incrementRowCount(rows.length);
    }

    @Override
    public void annotate(InputRow row, int distinctCount, RowAnnotation annotation) {
        RowAnnotationImpl a = (RowAnnotationImpl) annotation;
        a.incrementRowCount(distinctCount);
    }

    @Override
    public void reset(RowAnnotation annotation) {
        RowAnnotationImpl a = (RowAnnotationImpl) annotation;
        a.resetRowCount();
    }

    @Override
    public InputRow[] getRows(RowAnnotation annotation) {
        return new InputRow[0];
    }

    @Override
    public Map<Object, Integer> getValueCounts(RowAnnotation annotation, InputColumn<?> inputColumn) {
        return Collections.emptyMap();
    }

    @Override
    public void transferAnnotations(RowAnnotation from, RowAnnotation to) {
        RowAnnotationImpl a1 = (RowAnnotationImpl) from;
        RowAnnotationImpl a2 = (RowAnnotationImpl) to;
        a2.incrementRowCount(a1.getRowCount());
    }

}
