/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.util.convert;

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
import org.eobjects.analyzer.beans.api.Alias;
import org.eobjects.analyzer.beans.api.Converter;
import org.eobjects.analyzer.beans.convert.ConvertToDateTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.analyzer.util.ChangeAwareObjectInputStream;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.apache.metamodel.util.FileHelper;
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
    Converter<Object> parentConverter;
    
    public StandardTypeConverter() {
        this(null);
    }
    
    public StandardTypeConverter(Converter<Object> parentConverter) {
        this.parentConverter = parentConverter;
    }

    @Override
    public Object fromString(Class<?> type, String str) {
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
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Class not found: " + str, e);
            }
        }
        if (type.isEnum()) {
            try {
                Object[] enumConstants = type.getEnumConstants();
                
                // first look for enum constant matches
                Method nameMethod = Enum.class.getMethod("name");
                for (Object e : enumConstants) {
                    String name = (String) nameMethod.invoke(e);
                    if (name.equals(str)) {
                        return e;
                    }
                }
                
                // check for aliased enums
                for (Object e : enumConstants) {
                    String name = (String) nameMethod.invoke(e);
                    Field field = type.getField(name);
                    Alias alias = ReflectionUtils.getAnnotation(field, Alias.class);
                    if (alias != null) {
                        String[] aliasValues = alias.value();
                        for (String aliasValue : aliasValues) {
                            if (aliasValue.equals(str)) {
                                return e;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException("Unexpected error occurred while examining enum", e);
            }
            throw new IllegalArgumentException("No such enum '" + str + "' in enum class: " + type.getName());
        }
        if (ReflectionUtils.isDate(type)) {
            return toDate(str);
        }
        if (ReflectionUtils.is(type, File.class)) {
            return new File(str.replace('\\', File.separatorChar));
        }
        if (ReflectionUtils.is(type, Calendar.class)) {
            Date date = toDate(str);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            return c;
        }
        if (ReflectionUtils.is(type, Pattern.class)) {
            try {
                return Pattern.compile(str);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Invalid regular expression syntax in '" + str + "'.", e);
            }
        }
        if (ReflectionUtils.is(type, java.sql.Date.class)) {
            Date date = toDate(str);
            return new java.sql.Date(date.getTime());
        }
        if (ReflectionUtils.isNumber(type)) {
            return ConvertToNumberTransformer.transformValue(str);
        }
        if (ReflectionUtils.is(type, Serializable.class)) {
            logger.warn("fromString(...): No built-in handling of type: {}, using deserialization", type.getName());
            byte[] bytes = (byte[]) parentConverter.fromString(byte[].class, str);
            ChangeAwareObjectInputStream objectInputStream = null;
            try {
                objectInputStream = new ChangeAwareObjectInputStream(new ByteArrayInputStream(bytes));
                objectInputStream.addClassLoader(type.getClassLoader());
                Object obj = objectInputStream.readObject();
                return obj;
            } catch (Exception e) {
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
            File file = (File) o;
            if (file.isAbsolute()) {
                result = file.getAbsolutePath().replace('\\','/');
            } else {
                result = file.getPath().replace('\\','/');
            }
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
        } else if (o instanceof Serializable) {
            logger.info("toString(...): No built-in handling of type: {}, using serialization.", o.getClass().getName());
            byte[] bytes = SerializationUtils.serialize((Serializable) o);
            result = parentConverter.toString(bytes);
        } else {
            logger.warn("toString(...): Could not convert type: {}", o.getClass().getName());
            result = o.toString();
        }
        return result;
    }

    private static final Date toDate(String str) {
        try {
            return new SimpleDateFormat(dateFormatString).parse(str);
        } catch (ParseException e) {

            Date date = ConvertToDateTransformer.getInternalInstance().transformValue(str);
            if (date == null) {
                logger.error("Could not parse date: " + str, e);
                throw new IllegalArgumentException(e);
            } else {
                return date;
            }
        }
    }

    @Override
    public boolean isConvertable(Class<?> type) {
        return ReflectionUtils.is(type, Serializable.class) || type.isPrimitive();
    }
}
