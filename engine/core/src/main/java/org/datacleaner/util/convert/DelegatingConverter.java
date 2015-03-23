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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.datacleaner.api.Convertable;
import org.datacleaner.api.Converter;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.configuration.InjectionPoint;
import org.datacleaner.lifecycle.MemberInjectionPoint;
import org.datacleaner.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main converter used by {@link StringConverter}. This converter will delegate
 * and compose conversions based on relevant converter implementations, such as
 * {@link NullConverter}, {@link ArrayConverter}, {@link StandardTypeConverter}
 * and {@link ConfigurationItemConverter}.
 */
public class DelegatingConverter implements Converter<Object> {

    private static final Logger logger = LoggerFactory.getLogger(DelegatingConverter.class);

    private final List<Converter<?>> _converters;
    private final NullConverter _nullConverter;
    private final ArrayConverter _arrayConverter;

    public DelegatingConverter() {
        this(null);
    }

    public DelegatingConverter(Collection<Converter<?>> converters) {
        _converters = new ArrayList<Converter<? extends Object>>();
        _nullConverter = new NullConverter();
        _arrayConverter = new ArrayConverter(this);

        if (converters != null) {
            _converters.addAll(converters);
        }
    }

    public List<Converter<?>> getConverters() {
        return _converters;
    }

    public void addConverter(Converter<?> converter) {
        _converters.add(converter);
    }

    @Override
    public Object fromString(Class<?> type, String serializedForm) {
        if (type == null || serializedForm == null || _nullConverter.isNull(serializedForm)) {
            return _nullConverter.fromString(type, serializedForm);
        }

        if (type.isArray()) {
            return _arrayConverter.fromString(type, serializedForm);
        }

        serializedForm = SerializationStringEscaper.unescape(serializedForm);

        for (Converter<?> converter : _converters) {
            if (converter.isConvertable(type)) {
                Object result = converter.fromString(type, serializedForm);
                return result;
            }
        }

        if (ReflectionUtils.is(type, List.class)) {
            return _arrayConverter.fromString(type, serializedForm);
        }

        Convertable convertable = ReflectionUtils.getAnnotation(type, Convertable.class);
        if (convertable != null) {
            try {
                Class<? extends Converter<?>> converterClass = convertable.value();
                @SuppressWarnings("unchecked")
                Converter<Object> converter = (Converter<Object>) ReflectionUtils.newInstance(converterClass);
                return converter.fromString(type, serializedForm);
            } catch (Exception e) {
                logger.warn("Failed to convert fromString(" + serializedForm
                        + ") using Convertable annotated converter class", e);
            }
        }

        throw new IllegalStateException("Could not find matching converter for type: " + type);
    }

    @Override
    public String toString(Object instance) {
        if (null == instance) {
            return _nullConverter.toString(instance);
        }

        final Class<? extends Object> type = instance.getClass();

        if (_arrayConverter.isConvertable(type)) {
            return _arrayConverter.toString(instance);
        }

        for (Converter<?> converter : _converters) {
            if (converter.isConvertable(type)) {
                @SuppressWarnings("unchecked")
                Converter<Object> castedConverter = (Converter<Object>) converter;
                String serializedForm = castedConverter.toString(instance);

                return SerializationStringEscaper.escape(serializedForm);
            }
        }

        Convertable convertable = ReflectionUtils.getAnnotation(instance.getClass(), Convertable.class);
        if (convertable != null) {
            try {
                Class<? extends Converter<?>> converterClass = convertable.value();
                @SuppressWarnings("unchecked")
                Converter<Object> converter = (Converter<Object>) ReflectionUtils.newInstance(converterClass);
                return SerializationStringEscaper.escape(converter.toString(instance));
            } catch (Exception e) {
                logger.warn("Failed to convert toString(" + instance + ") using Convertable annotated converter class",
                        e);
            }
        }

        throw new IllegalStateException("Could not find matching converter for instance: " + instance);
    }

    @Override
    public boolean isConvertable(Class<?> instance) {
        return true;
    }

    /**
     * Initializes all converters contained with injections
     * 
     * @param injectionManager
     */
    public void initializeAll(InjectionManager injectionManager) {
        if (injectionManager != null) {
            for (Converter<?> converter : _converters) {
                Field[] fields = ReflectionUtils.getFields(converter.getClass(), Inject.class);
                for (Field field : fields) {
                    final Object value;
                    if (field.getType() == Converter.class) {
                        // Injected converters are used as callbacks. They
                        // should be assigned to the outer converter, which is
                        // this.
                        value = this;
                    } else {
                        InjectionPoint<Object> injectionPoint = new MemberInjectionPoint<Object>(field, converter);
                        value = injectionManager.getInstance(injectionPoint);
                    }
                    field.setAccessible(true);
                    try {
                        field.set(converter, value);
                    } catch (Exception e) {
                        throw new IllegalStateException("Could not initialize converter: " + converter, e);
                    }
                }
            }
        }
    }

}
