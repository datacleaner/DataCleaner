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

import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.job.AnalysisJob;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * An {@link InjectionManagerFactory} implementation suitable for deployments
 * with the Spring framework as dependency provider.
 */
@Component
public class SpringInjectionManagerFactory implements InjectionManagerFactory, ApplicationContextAware {

    private ApplicationContext _applicationContext;

    public SpringInjectionManagerFactory(ApplicationContext applicationContext) {
        _applicationContext = applicationContext;
    }

    public SpringInjectionManagerFactory() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        _applicationContext = applicationContext;
    }

    @Override
    public InjectionManager getInjectionManager(AnalyzerBeansConfiguration configuration, AnalysisJob job) {
        return new SpringInjectionManager(configuration, job, _applicationContext);
    }

}
