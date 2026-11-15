/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.metamodel.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.Supplier;

/**
 * A serializable reference to a (potentially transient) object.
 */
public final class SerializableRef<E> implements Supplier<E>, Serializable {

    private static final long serialVersionUID = 1L;

    private volatile E _serializableObj;
    private transient volatile E _transientObj;

    public SerializableRef(final E obj) {
        _transientObj = obj;
        if (obj instanceof Serializable) {
            _serializableObj = obj;
        }
    }

    @Override
    public E get() {
        final E obj = _transientObj != null ? _transientObj : _serializableObj;
        return obj;
    }

    public boolean isSerializing() {
        return _transientObj == null && _serializableObj != null;
    }

    private void writeObject(final ObjectOutputStream objectOutputStream) throws IOException {
        _transientObj = null;
        objectOutputStream.defaultWriteObject();
    }

    private void readObject(final ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        _transientObj = _serializableObj;
    }

    @Override
    public int hashCode() {
        final E obj = get();
        return obj == null ? 0 : obj.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof SerializableRef) {
            final SerializableRef<?> otherSerializableRef = (SerializableRef<?>) obj;
            return get().equals(otherSerializableRef.get());
        }
        return false;
    }
}
