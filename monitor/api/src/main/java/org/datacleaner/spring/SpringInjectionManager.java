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
package org.datacleaner.spring;

import java.util.Collection;
import java.util.Map;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerHomeFolder;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.configuration.InjectionManagerImpl;
import org.datacleaner.configuration.InjectionPoint;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Sets;

/**
 * A Spring-based {@link InjectionManager} implementation
 */
final class SpringInjectionManager extends InjectionManagerImpl {

    private static final Logger logger = LoggerFactory.getLogger(SpringInjectionManager.class);

    /**
     * Classes that Spring injection should definately ignore (and avoid warn
     * logging).
     */
    private static final Collection<Class<?>> NON_INJECTED_CLASSES = Sets.<Class<?>> newHashSet(
            DataCleanerConfiguration.class, DataCleanerHomeFolder.class, Datastore.class, DatastoreCatalog.class,
            ReferenceDataCatalog.class, Table.class, Column.class);

    private final ApplicationContext _applicationContext;

    public SpringInjectionManager(DataCleanerConfiguration configuration, AnalysisJob job,
            ApplicationContext applicationContext) {
        super(configuration, job);
        _applicationContext = applicationContext;
    }

    @Override
    protected Object getInstanceInternal(InjectionPoint<?> injectionPoint) {
        Object instance = super.getInstanceInternal(injectionPoint);
        if (instance == null) {
            final Class<?> baseType = injectionPoint.getBaseType();
            if (NON_INJECTED_CLASSES.contains(baseType)) {
                logger.debug("Skipping injection of type {} because it is not a candidate for Spring injection",
                        baseType);
                return null;
            }
            try {
                final Map<String, ?> beans = _applicationContext.getBeansOfType(baseType, false, true);
                if (beans.isEmpty()) {
                    logger.warn("No beans resolved of type {} (in {})", baseType, injectionPoint.getInstance());
                } else if (beans.size() == 1) {
                    Object bean = beans.values().iterator().next();
                    logger.debug("Resolved spring bean for injection: {}", bean);
                    instance = bean;
                } else {
                    logger.warn("Multiple beans resolved of type {}: ", baseType, beans);

                    Object bean = beans.values().iterator().next();
                    logger.warn("Picking the first bean candidate for injection: {}", bean);
                    instance = bean;
                }
            } catch (BeansException e) {
                logger.warn("Could not resolve spring bean of type " + baseType + " for injection", e);
            }
        }
        return instance;
    }
}
