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
package org.eobjects.datacleaner.monitor.server.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.datacleaner.monitor.configuration.ConfigurationCache;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.file.FileRepository;

public class JobInvocationControllerTest extends TestCase {

    public void testInvoke() throws Throwable {
        final Repository repository = new FileRepository("src/test/resources/example_repo");
        final ConfigurationCache configurationCache = new ConfigurationCache(repository);
        final TenantContextFactory contextFactory = new TenantContextFactoryImpl(repository, configurationCache);
        final JobInvocationController controller = new JobInvocationController();
        controller._contextFactory = contextFactory;

        List<Object[]> sourceRecords = new ArrayList<Object[]>();
        sourceRecords.add(new Object[] { "kasper@eobjects.dk" });
        sourceRecords.add(new Object[] { "kasper.sorensen@humaninference.com" });

        List<Object[]> result = controller.invokeJob("tenant1", "email_standardizer", sourceRecords);
        assertEquals(2, result.size());

        assertEquals("[kasper, eobjects.dk]", Arrays.toString(result.get(0)));
        assertEquals("[kasper.sorensen, humaninference.com]", Arrays.toString(result.get(1)));
    }
}
