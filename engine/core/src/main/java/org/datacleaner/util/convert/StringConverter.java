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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datacleaner.api.Converter;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.configuration.InjectionPoint;
import org.datacleaner.configuration.SimpleInjectionPoint;
import org.datacleaner.job.AnalysisJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for converting objects to and from string representations as
 * used for example in serialized XML jobs.
 *
 * The string converter currently supports instances and arrays of:
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
 * <li>java.io.File</li>
 * <li>java.util.Date</li>
 * <li>java.sql.Date</li>
 * <li>java.util.Calendar</li>
 * <li>java.util.regex.Pattern</li>
 * <li>org.datacleaner.reference.Dictionary</li>
 * <li>org.datacleaner.reference.SynonymCatalog</li>
 * <li>org.datacleaner.reference.StringPattern</li>
 * <li>org.datacleaner.connection.Datastore</li>
 * <li>org.apache.metamodel.schema.Column</li>
 * <li>org.apache.metamodel.schema.Table</li>
 * <li>org.apache.metamodel.schema.Schema</li>
 * </ul>
 */
public final class StringConverter {

    private static final Logger logger = LoggerFactory.getLogger(StringConverter.class);

    private static final StringConverter SIMPLE_INSTANCE = new StringConverter(new DataCleanerConfigurationImpl());

    private final InjectionManager _injectionManager;
    private final DataCleanerConfiguration _configuration;
    private final DelegatingConverter _baseConverter;

    public StringConverter(final DataCleanerConfiguration configuration, final AnalysisJob job) {
        this(getInjectionManager(configuration, job));
    }

    public StringConverter(final DataCleanerConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("DataCleanerConfiguration cannot be null");
        }
        _configuration = configuration;
        _injectionManager =
                configuration.getEnvironment().getInjectionManagerFactory().getInjectionManager(configuration);
        _baseConverter = createBaseConverter();
    }

    public StringConverter(final InjectionManager injectionManager) {
        if (injectionManager == null) {
            throw new IllegalArgumentException("InjectionManager cannot be null");
        }
        final InjectionPoint<DataCleanerConfiguration> injectionPoint =
                SimpleInjectionPoint.of(DataCleanerConfiguration.class);
        _configuration = injectionManager.getInstance(injectionPoint);
        _injectionManager = injectionManager;
        _baseConverter = createBaseConverter();
    }

    /**
     * Gets a simple instance of {@link StringConverter}. This instance will not
     * be able to convert all types as well as an instance that is bound to a
     * specific {@link DataCleanerConfiguration}, a {@link InjectionManager} or
     * an {@link AnalysisJob}.
     *
     * In other words: The instance will work for simple use-cases but is
     * discouraged when it is possible to provide a bounded context object.
     *
     * @return
     */
    public static StringConverter simpleInstance() {
        return SIMPLE_INSTANCE;
    }

    private static InjectionManager getInjectionManager(final DataCleanerConfiguration configuration,
            final AnalysisJob job) {
        final InjectionManagerFactory injectionManagerFactory =
                configuration.getEnvironment().getInjectionManagerFactory();
        if (job == null) {
            return injectionManagerFactory.getInjectionManager(configuration);
        } else {
            return injectionManagerFactory.getInjectionManager(configuration, job);
        }
    }

    private DelegatingConverter createBaseConverter() {
        final DelegatingConverter baseConverter = new DelegatingConverter();
        baseConverter.addConverter(new ConfigurationItemConverter());
        baseConverter.addConverter(getResourceConverter());
        baseConverter.addConverter(new StandardTypeConverter(_configuration, baseConverter));
        baseConverter.initializeAll(_injectionManager);
        return baseConverter;
    }

    /**
     * Serializes a Java object to a String representation.
     *
     * @param obj
     *            the object to serialize
     * @return a String representation of the Java object
     */
    public String serialize(final Object obj) {
        return serialize(obj, new ArrayList<>(0));
    }

    /**
     * @deprecated by {@link #serialize(Object, Converter)}
     */
    public String serialize(final Object obj, final Class<? extends Converter<?>> converterClass) {
        final Converter<?> converter = converterClass == null ? null : createConverter(converterClass);
        return serialize(obj, converter);
    }

    public String serialize(final Object obj, final Converter<?> converter) {
        final Collection<Converter<?>> col = new ArrayList<>();
        if (converter != null) {
            col.add(converter);
        }
        return serialize(obj, col);
    }

    /**
     * Serializes a Java object to a String representation.
     *
     * @param obj
     *            the object to serialize
     * @param converters
     *            an optional collection of custom converter classes
     * @return a String representation of the Java object
     */
    public String serialize(final Object obj, final Collection<Converter<?>> converters) {
        final DelegatingConverter delegatingConverter = new DelegatingConverter();

        if (converters != null) {
            for (final Converter<?> converter : converters) {
                delegatingConverter.addConverter(converter);
            }
        }

        delegatingConverter.addConverter(new ConfigurationItemConverter());
        delegatingConverter.addConverter(getResourceConverter());
        delegatingConverter.addConverter(new StandardTypeConverter(_configuration, delegatingConverter));

        delegatingConverter.initializeAll(_injectionManager);

        return delegatingConverter.toString(obj);
    }

    private ResourceConverter getResourceConverter() {
        if (_injectionManager == null) {
            return new ResourceConverter(_configuration);
        } else {
            final ResourceConverter converter =
                    _injectionManager.getInstance(SimpleInjectionPoint.of(ResourceConverter.class));
            if (converter == null) {
                return new ResourceConverter(_configuration);
            }
            return converter;
        }
    }

    private Converter<?> createConverter(final Class<? extends Converter<?>> converterClass) {
        try {
            return converterClass.newInstance();
        } catch (final Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Error occurred while using instantiating: " + converterClass, e);
        }
    }

    /**
     * Deserializes a String into a Java object of the particular type.
     *
     * @param str
     *            the serialized string representation
     * @param type
     *            the requested type
     * @return a Java object matching the String representation
     */
    public <E> E deserialize(final String str, final Class<E> type) {
        return deserialize(str, type, new ArrayList<>(0));
    }

    /**
     * @deprecated by {@link #deserialize(String, Class, Converter)}
     */
    public <E> E deserialize(final String str, final Class<E> type,
            final Class<? extends Converter<?>> converterClass) {
        if (converterClass == null) {
            return deserialize(str, type);
        }
        return deserialize(str, type, createConverter(converterClass));
    }

    public <E> E deserialize(final String str, final Class<E> type, final Converter<?> converter) {
        final Collection<Converter<?>> col = new ArrayList<>();
        if (converter != null) {
            col.add(converter);
        }
        return deserialize(str, type, col);
    }

    /**
     * Deserializes a String into a Java object of the particular type.
     *
     * @param str
     *            the serialized string representation
     * @param type
     *            the requested type
     * @param converters
     *            an optional collection of custom converters to apply when
     *            deserializing
     * @return a Java object matching the String representation
     */
    public <E> E deserialize(final String str, final Class<E> type, final Collection<Converter<?>> converters) {
        logger.debug("deserialize(\"{}\", {})", str, type);

        if (converters == null || converters.isEmpty()) {
            // when possible, just reuse the base converter
            @SuppressWarnings("unchecked") final E result = (E) _baseConverter.fromString(type, str);
            return result;
        }

        final DelegatingConverter delegatingConverter = new DelegatingConverter();

        if (converters != null) {
            for (final Converter<?> converter : converters) {
                delegatingConverter.addConverter(converter);
                delegatingConverter.initialize(converter, _injectionManager);
            }
        }

        final List<Converter<?>> baseconverters = _baseConverter.getConverters();
        for (final Converter<?> converter : baseconverters) {
            delegatingConverter.addConverter(converter);
        }

        @SuppressWarnings("unchecked") final E result = (E) delegatingConverter.fromString(type, str);
        return result;
    }
}
