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
package org.datacleaner.util.convert;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.inject.Inject;

import org.apache.commons.lang.SerializationUtils;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.api.Alias;
import org.datacleaner.api.Converter;
import org.datacleaner.components.convert.ConvertToDateTransformer;
import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.descriptors.EnumerationValue;
import org.datacleaner.util.ChangeAwareObjectInputStream;
import org.datacleaner.util.FileResolver;
import org.datacleaner.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the {@link Converter} interface. This converter is
 * able to convert single instances (not arrays or collections) of:
 *
 * <ul>
 * <li>Boolean</li>
 * <li>Byte</li>
 * <li>Short</li>
 * <li>Integer</li>
 * <li>Long</li>
 * <li>Float</li>
 * <li>Double</li>
 * <li>Character</li>
 * <li>String</li>
 * <li>enums</li>
 * <li>java.io.File</li>
 * <li>java.util.Date</li>
 * <li>java.sql.Date</li>
 * <li>java.util.Calendar</li>
 * <li>java.util.regex.Pattern</li>
 * </ul>
 *
 * If given a {@link Serializable} type it will also attempt serializing it to a
 * byte-array string.
 */
public class StandardTypeConverter implements Converter<Object> {

    private static final Logger logger = LoggerFactory.getLogger(StandardTypeConverter.class);

    // ISO 8601
    private static final String dateFormatString = "yyyy-MM-dd'T'HH:mm:ss S";

    @Inject
    Converter<Object> _parentConverter;

    @Inject
    DataCleanerConfiguration _configuration;

    public StandardTypeConverter() {
        this(null, null);
    }

    public StandardTypeConverter(final DataCleanerConfiguration configuration,
            final Converter<Object> parentConverter) {
        _configuration = configuration;
        _parentConverter = parentConverter;
    }

    private static Date toDate(final String str) {
        try {
            return new SimpleDateFormat(dateFormatString).parse(str);
        } catch (final ParseException e) {

            final Date date = ConvertToDateTransformer.getInternalInstance().transformValue(str);
            if (date == null) {
                logger.error("Could not parse date: " + str, e);
                throw new IllegalArgumentException(e);
            } else {
                return date;
            }
        }
    }

    @Override
    public Object fromString(final Class<?> type, final String str) {
        if (ReflectionUtils.isString(type)) {
            return str;
        }
        if (ReflectionUtils.isBoolean(type)) {
            return Boolean.valueOf(str);
        }
        if (ReflectionUtils.isCharacter(type)) {
            return Character.valueOf(str.charAt(0));
        }
        if (ReflectionUtils.isInteger(type)) {
            return Integer.valueOf(str);
        }
        if (ReflectionUtils.isLong(type)) {
            return Long.valueOf(str);
        }
        if (ReflectionUtils.isByte(type)) {
            return Byte.valueOf(str);
        }
        if (ReflectionUtils.isShort(type)) {
            return Short.valueOf(str);
        }
        if (ReflectionUtils.isDouble(type)) {
            return Double.valueOf(str);
        }
        if (ReflectionUtils.isFloat(type)) {
            return Float.valueOf(str);
        }
        if (ReflectionUtils.is(type, Class.class)) {
            try {
                return Class.forName(str);
            } catch (final ClassNotFoundException e) {
                throw new IllegalArgumentException("Class not found: " + str, e);
            }
        }
        if (ReflectionUtils.is(type, EnumerationValue.class)) {
            return new EnumerationValue(str);
        }
        if (type.isEnum()) {
            try {
                final Object[] enumConstants = type.getEnumConstants();

                // first look for enum constant matches
                final Method nameMethod = Enum.class.getMethod("name");
                for (final Object e : enumConstants) {
                    final String name = (String) nameMethod.invoke(e);
                    if (name.equals(str)) {
                        return e;
                    }
                }

                // check for aliased enums
                for (final Object e : enumConstants) {
                    final String name = (String) nameMethod.invoke(e);
                    final Field field = type.getField(name);
                    final Alias alias = ReflectionUtils.getAnnotation(field, Alias.class);
                    if (alias != null) {
                        final String[] aliasValues = alias.value();
                        for (final String aliasValue : aliasValues) {
                            if (aliasValue.equals(str)) {
                                return e;
                            }
                        }
                    }
                }
            } catch (final Exception e) {
                throw new IllegalStateException("Unexpected error occurred while examining enum", e);
            }
            throw new IllegalArgumentException("No such enum '" + str + "' in enum class: " + type.getName());
        }
        if (ReflectionUtils.isDate(type)) {
            return toDate(str);
        }
        if (ReflectionUtils.is(type, File.class)) {
            final FileResolver fileResolver = new FileResolver(_configuration);
            return fileResolver.toFile(str);
        }
        if (ReflectionUtils.is(type, Calendar.class)) {
            final Date date = toDate(str);
            final Calendar c = Calendar.getInstance();
            c.setTime(date);
            return c;
        }
        if (ReflectionUtils.is(type, Pattern.class)) {
            try {
                return Pattern.compile(str);
            } catch (final PatternSyntaxException e) {
                throw new IllegalArgumentException("Invalid regular expression syntax in '" + str + "'.", e);
            }
        }
        if (ReflectionUtils.is(type, java.sql.Date.class)) {
            final Date date = toDate(str);
            return new java.sql.Date(date.getTime());
        }
        if (ReflectionUtils.isNumber(type)) {
            return ConvertToNumberTransformer.transformValue(str);
        }
        if (ReflectionUtils.is(type, Serializable.class)) {
            logger.warn("fromString(...): No built-in handling of type: {}, using deserialization", type.getName());
            final byte[] bytes = (byte[]) _parentConverter.fromString(byte[].class, str);
            ChangeAwareObjectInputStream objectInputStream = null;
            try {
                objectInputStream = new ChangeAwareObjectInputStream(new ByteArrayInputStream(bytes));
                objectInputStream.addClassLoader(type.getClassLoader());
                return objectInputStream.readObject();
            } catch (final Exception e) {
                throw new IllegalStateException("Could not deserialize to " + type + ".", e);
            } finally {
                FileHelper.safeClose(objectInputStream);
            }
        }

        throw new IllegalArgumentException("Could not convert to type: " + type.getName());
    }

    @Override
    public String toString(Object o) {
        if (o instanceof Calendar) {
            // will now be picked up by the date conversion
            o = ((Calendar) o).getTime();
        }

        final String result;
        if (o instanceof Boolean || o instanceof Number || o instanceof String || o instanceof Character) {
            result = o.toString();
        } else if (o instanceof File) {
            final File file = (File) o;
            final FileResolver fileResolver = new FileResolver(_configuration);
            return fileResolver.toPath(file);
        } else if (o instanceof Date) {
            if (o instanceof ExpressionDate) {
                // preserve the expression if it is an ExpressionDate
                result = ((ExpressionDate) o).getExpression();
            } else {
                result = new SimpleDateFormat(dateFormatString).format((Date) o);
            }
        } else if (o instanceof Pattern) {
            result = o.toString();
        } else if (o instanceof Enum) {
            return ((Enum<?>) o).name();
        } else if (o instanceof Class) {
            result = ((Class<?>) o).getName();
        } else if (o instanceof EnumerationValue) {
            result = ((EnumerationValue) o).getValue();
        } else if (o instanceof Serializable) {
            logger.info("toString(...): No built-in handling of type: {}, using serialization.",
                    o.getClass().getName());
            final byte[] bytes = SerializationUtils.serialize((Serializable) o);
            result = _parentConverter.toString(bytes);
        } else {
            logger.warn("toString(...): Could not convert type: {}", o.getClass().getName());
            result = o.toString();
        }
        return result;
    }

    @Override
    public boolean isConvertable(final Class<?> type) {
        return ReflectionUtils.is(type, Serializable.class) || type.isPrimitive();
    }
}
