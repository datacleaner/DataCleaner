/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.configuration;

import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.descriptors.SimpleDescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.reference.ReferenceDataCatalogImpl;

import junit.framework.TestCase;

public class AnalyzerBeansConfigurationImplTest extends TestCase {

    public void testReplaceRetainsInjectionManagerFactory() throws Exception {
        InjectionManagerFactory injectionManagerFactory = new InjectionManagerFactory() {
            @Override
            public InjectionManager getInjectionManager(AnalyzerBeansConfiguration configuration, AnalysisJob job) {
                throw new UnsupportedOperationException("I'm just a mock");
            }
        };

        AnalyzerBeansConfigurationImpl conf = new AnalyzerBeansConfigurationImpl(injectionManagerFactory);
        conf = conf.replace(new DatastoreCatalogImpl());
        conf = conf.replace(new SimpleDescriptorProvider());
        conf = conf.replace(new ReferenceDataCatalogImpl());
        conf = conf.replace(new SingleThreadedTaskRunner());

        try {
            conf.getInjectionManager(null).getInstance(null);
            fail("Exception expected");
        } catch (UnsupportedOperationException e) {
            assertEquals("I'm just a mock", e.getMessage());
        }
    }
}
