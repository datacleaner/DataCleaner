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

import java.util.Map;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.configuration.InjectionManagerImpl;
import org.datacleaner.configuration.InjectionPoint;
import org.datacleaner.job.AnalysisJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * A Spring-based {@link InjectionManager} implementation
 */
final class SpringInjectionManager extends InjectionManagerImpl {

    private static final Logger logger = LoggerFactory.getLogger(SpringInjectionManager.class);

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
            Class<?> baseType = injectionPoint.getBaseType();
            try {
                final Map<String, ?> beans = _applicationContext.getBeansOfType(baseType, false, true);
                if (beans.isEmpty()) {
                    logger.warn("No beans resolved of type {}", baseType);
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
