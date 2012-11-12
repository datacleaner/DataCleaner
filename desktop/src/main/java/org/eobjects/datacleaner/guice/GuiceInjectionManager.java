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
package org.eobjects.datacleaner.guice;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.configuration.InjectionManagerImpl;
import org.eobjects.analyzer.configuration.InjectionPoint;
import org.eobjects.analyzer.job.AnalysisJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps a standard {@link InjectionManager} and adds support for all Guice
 * based injections (only triggered in case the standard
 * {@link InjectionManager} is not useful
 * 
 * @author Kasper Sørensen
 */
final class GuiceInjectionManager extends InjectionManagerImpl {

    private static final Logger logger = LoggerFactory.getLogger(GuiceInjectionManager.class);

    private final InjectorBuilder _injectorBuilder;

    public GuiceInjectionManager(AnalyzerBeansConfiguration configuration, AnalysisJob job, InjectorBuilder injectorBuilder) {
        super(configuration, job);
        _injectorBuilder = injectorBuilder;
    }

    @Override
    protected Object getInstanceInternal(InjectionPoint<?> injectionPoint) {
        Object instance = super.getInstanceInternal(injectionPoint);
        if (instance == null) {
            Class<?> baseType = injectionPoint.getBaseType();
            try {
                instance = _injectorBuilder.getInstance(baseType);
            } catch (Exception e) {
                logger.warn("Error occurred while getting guice instance for injection point: " + injectionPoint, e);
            }
        }
        return instance;
    }

}
