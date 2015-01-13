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
package org.datacleaner.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.ObjectInputStream.GetField;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.datacleaner.connection.UsageAwareDatastore;
import org.datacleaner.reference.AbstractReferenceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder object to make it convenient to implement a readObject(
 * {@link ObjectInputStream}) method in a Serializable class.
 * 
 * The main functionality of this helper is to aid in setting field values of
 * fields that have been moved around in the class hierarchy. This is eg. the
 * case with implementations of {@link UsageAwareDatastore} and
 * {@link AbstractReferenceData} (where the _name fields have been moved to
 * these super classes).
 * 
 * 
 */
public final class ReadObjectBuilder<E extends Serializable> {

    private static final Logger logger = LoggerFactory.getLogger(ReadObjectBuilder.class);

    /**
     * Annotation used to mark fields in classes that have been moved in the
     * class hierarchy, typically from a subclass to a superclass. Such fields
     * will be discovered and treated accordingly during deserialization.
     * 
     * 
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    @Documented
    @Inherited
    public static @interface Moved {
    }

    /**
     * A custom adaptor interface which can be provided externally to do custom
     * field deserialization logic.
     */
    public static interface Adaptor {
        public void deserialize(GetField getField, Serializable serializable) throws IOException;
    }

    public static <E extends Serializable> ReadObjectBuilder<E> create(E serializable, Class<? super E> clazz) {
        logger.debug("Creating ReadObjectBuilder for new object of {}", clazz);
        return new ReadObjectBuilder<E>(serializable, clazz);
    }

    private final E _serializable;
    private final Class<? super E> _clazz;

    private ReadObjectBuilder(E serializable, Class<? super E> clazz) {
        _serializable = serializable;
        _clazz = clazz;
    }

    public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        readObject(stream, null);
    }

    public void readObject(ObjectInputStream stream, Adaptor adaptor) throws IOException, ClassNotFoundException {
        try {
            GetField getField = stream.readFields();

            Field[] fields;
            fields = _clazz.getDeclaredFields();

            deserializeFields(fields, getField);

            fields = ReflectionUtils.getFields(_clazz, Moved.class);
            deserializeFields(fields, getField);

            if (adaptor != null) {
                adaptor.deserialize(getField, _serializable);
            }
        } catch (IOException e) {
            logger.error("Could not deserialize object!", e);
            throw e;
        }
    }

    private void deserializeFields(Field[] fields, GetField getField) throws IOException {
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (!Modifier.isTransient(modifiers) && !Modifier.isStatic(modifiers)) {
                deserializeField(field, getField);
            }
        }
    }

    private void deserializeField(Field field, GetField getField) throws IOException {
        final String fieldName = field.getName();
        try {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            if (fieldType.isPrimitive()) {
                if (fieldType == boolean.class) {
                    final boolean value = getField.get(fieldName, false);
                    field.setBoolean(_serializable, value);
                } else if (fieldType == byte.class) {
                    final byte value = getField.get(fieldName, (byte) 0);
                    field.setByte(_serializable, value);
                } else if (fieldType == short.class) {
                    final short value = getField.get(fieldName, (short) 0);
                    field.setShort(_serializable, value);
                } else if (fieldType == int.class) {
                    final int value = getField.get(fieldName, 0);
                    field.setInt(_serializable, value);
                } else if (fieldType == long.class) {
                    final long value = getField.get(fieldName, 0l);
                    field.setLong(_serializable, value);
                } else if (fieldType == float.class) {
                    final float value = getField.get(fieldName, 0f);
                    field.setFloat(_serializable, value);
                } else if (fieldType == double.class) {
                    final double value = getField.get(fieldName, 0d);
                    field.setDouble(_serializable, value);
                } else if (fieldType == char.class) {
                    final char value = getField.get(fieldName, (char) 0);
                    field.setChar(_serializable, value);
                }
            } else {
                final Object value = getField.get(fieldName, null);
                if (logger.isDebugEnabled()) {
                    logger.debug("{}.{} was  {}", new Object[] { _clazz, field, value });
                }
                if (value != null) {
                    field.set(_serializable, value);
                }
            }
        } catch (IllegalAccessException e) {
            logger.warn("Not allowed to access field: {}", fieldName);
        } catch (IllegalArgumentException e) {
            logger.debug("No such field found in GetFields: {}", fieldName);
        }
    }
}
