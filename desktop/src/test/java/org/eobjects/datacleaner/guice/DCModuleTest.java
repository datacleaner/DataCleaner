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
package org.eobjects.datacleaner.guice;

import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.lifecycle.MemberInjectionPoint;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class DCModuleTest extends TestCase {

    AnalysisJobBuilder injectedBuilderField = null;

    public void testAnalysisJobBuilderReferenceWithParentModule() throws Exception {
        final Field field = getClass().getDeclaredField("injectedBuilderField");

        final DCModule module1 = new DCModule();
        final Injector injector1 = Guice.createInjector(module1);
        final AnalysisJobBuilder ajb1 = injector1.getInstance(AnalysisJobBuilder.class);
        final AnalyzerBeansConfiguration conf1 = injector1.getInstance(AnalyzerBeansConfiguration.class);
        final AnalysisJobBuilder ajb2 = conf1.getInjectionManager(null).getInstance(
                new MemberInjectionPoint<AnalysisJobBuilder>(field, this));
        
        assertSame(ajb1, ajb2);

        final DCModule module2 = new DCModule(module1, new AnalysisJobBuilder(conf1));
        final Injector injector2 = Guice.createInjector(module2);
        final AnalyzerBeansConfiguration conf2 = injector2.getInstance(AnalyzerBeansConfiguration.class);
        final AnalysisJobBuilder ajb3 = injector2.getInstance(AnalysisJobBuilder.class);
        assertNotSame(ajb1, ajb3);

        final AnalysisJobBuilder ajb4 = conf2.getInjectionManager(null).getInstance(
                new MemberInjectionPoint<AnalysisJobBuilder>(field, this));
        assertNotSame(ajb1, ajb4);
        
        assertSame(ajb3, ajb4);
    }
}
