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

/**
 * Wraps a standard {@link InjectionManager} and adds support for all Guice based injections (only triggered in case the standard {@link InjectionManager} is not useful 
 * 
 * @author Kasper Sørensen
 */
final class DCInjectionManager extends InjectionManagerImpl {

    private final InjectorBuilder _injectorBuilder;

    public DCInjectionManager(AnalyzerBeansConfiguration configuration, AnalysisJob job, InjectorBuilder injectorBuilder) {
        super(configuration, job);
        _injectorBuilder = injectorBuilder;
    }

    @Override
    public <E> E getInstance(InjectionPoint<E> injectionPoint) {
        E instance = super.getInstance(injectionPoint);
        if (instance == null) {
            Class<E> baseType = injectionPoint.getBaseType();
            instance = _injectorBuilder.getInstance(baseType);
        }
        return instance;
    }

}
