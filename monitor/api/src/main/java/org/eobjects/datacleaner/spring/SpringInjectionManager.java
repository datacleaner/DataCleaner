/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.spring;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.configuration.InjectionManagerImpl;
import org.eobjects.analyzer.configuration.InjectionPoint;
import org.eobjects.analyzer.job.AnalysisJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * A Spring-based {@link InjectionManager} implementation
 */
final class SpringInjectionManager extends InjectionManagerImpl {

    private static final Logger logger = LoggerFactory.getLogger(SpringInjectionManager.class);

    private final ApplicationContext _applicationContext;

    public SpringInjectionManager(AnalyzerBeansConfiguration configuration, AnalysisJob job,
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
                final Object bean = _applicationContext.getBean(baseType);
                logger.debug("Resolved spring bean for injection: {}", bean);
                instance = bean;
            } catch (NoSuchBeanDefinitionException e) {
                logger.warn("Could not resolve sprint bean of type " + baseType + " for injection", e);
            }
        }
        return instance;
    }
}
