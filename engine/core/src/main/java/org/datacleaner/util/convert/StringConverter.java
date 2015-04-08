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

import org.datacleaner.api.Converter;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.configuration.InjectionManagerFactory;
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

    private final InjectionManager _injectionManager;

    public StringConverter(DataCleanerConfiguration configuration, AnalysisJob job) {
        this(getInjectionManager(configuration, job));
    }

    private static InjectionManager getInjectionManager(DataCleanerConfiguration configuration, AnalysisJob job) {
        final InjectionManagerFactory injectionManagerFactory = configuration.getEnvironment()
                .getInjectionManagerFactory();
        if (job == null) {
            return injectionManagerFactory.getInjectionManager(configuration);
        } else {
            return injectionManagerFactory.getInjectionManager(configuration, job);
        }
    }

    public StringConverter(InjectionManager injectionManager) {
        _injectionManager = injectionManager;
    }

    /**
     * Serializes a Java object to a String representation.
     * 
     * @param o
     *            the object to serialize
     * @return a String representation of the Java object
     */
    public final String serialize(final Object o) {
        return serialize(o, new ArrayList<Class<? extends Converter<?>>>(0));
    }

    public final String serialize(final Object o, final Class<? extends Converter<?>> converterClass) {
        final Collection<Class<? extends Converter<?>>> col = new ArrayList<Class<? extends Converter<?>>>();
        if (converterClass != null) {
            col.add(converterClass);
        }
        return serialize(o, col);
    }

    /**
     * Serializes a Java object to a String representation.
     * 
     * @param o
     *            the object to serialize
     * @param converterClasses
     *            an optional collection of custom converter classes
     * @return a String representation of the Java object
     */
    public final String serialize(final Object o, final Collection<Class<? extends Converter<?>>> converterClasses) {
        final DelegatingConverter delegatingConverter = new DelegatingConverter();

        if (converterClasses != null) {
            for (Class<? extends Converter<?>> converterClass : converterClasses) {
                delegatingConverter.addConverter(createConverter(converterClass));
            }
        }

        delegatingConverter.addConverter(new ConfigurationItemConverter());
        delegatingConverter.addConverter(getResourceConverter());
        delegatingConverter.addConverter(new StandardTypeConverter(delegatingConverter));

        delegatingConverter.initializeAll(_injectionManager);

        return delegatingConverter.toString(o);
    }

    private ResourceConverter getResourceConverter() {
        if (_injectionManager == null) {
            return new ResourceConverter();
        } else {
            ResourceConverter converter = _injectionManager.getInstance(SimpleInjectionPoint
                    .of(ResourceConverter.class));
            if (converter == null) {
                return new ResourceConverter();
            }
            return converter;
        }
    }

    private Converter<?> createConverter(Class<? extends Converter<?>> converterClass) {
        try {
            Converter<?> converter = converterClass.newInstance();
            return converter;
        } catch (Exception e) {
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
    public final <E> E deserialize(String str, Class<E> type) {
        return deserialize(str, type, new ArrayList<Class<? extends Converter<?>>>(0));
    }

    public final <E> E deserialize(String str, Class<E> type, Class<? extends Converter<?>> converterClass) {
        Collection<Class<? extends Converter<?>>> col = new ArrayList<Class<? extends Converter<?>>>();
        if (converterClass != null) {
            col.add(converterClass);
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
     * @param converterClasses
     *            an optional collection of custom converters to apply when
     *            deserializing
     * @return a Java object matching the String representation
     */
    public final <E> E deserialize(String str, Class<E> type, Collection<Class<? extends Converter<?>>> converterClasses) {
        logger.debug("deserialize(\"{}\", {})", str, type);

        final DelegatingConverter delegatingConverter = new DelegatingConverter();

        if (converterClasses != null) {
            for (Class<? extends Converter<?>> converterClass : converterClasses) {
                delegatingConverter.addConverter(createConverter(converterClass));
            }
        }

        delegatingConverter.addConverter(new ConfigurationItemConverter());
        delegatingConverter.addConverter(getResourceConverter());
        delegatingConverter.addConverter(new StandardTypeConverter(delegatingConverter));

        delegatingConverter.initializeAll(_injectionManager);

        @SuppressWarnings("unchecked")
        E result = (E) delegatingConverter.fromString(type, str);
        return result;
    }
}
