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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.util.ReadObjectBuilder;

/**
 * Simple implementation of the {@link RowAnnotation} interface which allows
 * incrementing row count by a member method.
 */
public final class RowAnnotationImpl implements RowAnnotation {

    private static final long serialVersionUID = 1L;

    private final AtomicInteger _counter;

    public RowAnnotationImpl() {
        this(0);
    }

    public RowAnnotationImpl(final int initialRowCount) {
        _counter = new AtomicInteger(initialRowCount);
    }

    public void incrementRowCount(final int increment) {
        _counter.addAndGet(increment);
    }

    public void resetRowCount() {
        _counter.set(0);
    }

    @Override
    public int getRowCount() {
        return _counter.get();
    }

    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        try {
            final Field counterField = getClass().getDeclaredField("_counter");
            counterField.setAccessible(true);
            counterField.set(this, new AtomicInteger());
        } catch (final Exception e) {
            throw new IllegalStateException("Could not create counter while deserializing.", e);
        }
        final ReadObjectBuilder<RowAnnotationImpl> builder = ReadObjectBuilder.create(this, RowAnnotationImpl.class);
        final ReadObjectBuilder.Adaptor adaptor = (getField, serializable) -> {
            try {
                final int count = getField.get("_rowCount", 0);
                _counter.set(count);
            } catch (final IllegalArgumentException e) {
                // happens for newer versions of the object type.
            }
        };
        builder.readObject(stream, adaptor);
    }
}
