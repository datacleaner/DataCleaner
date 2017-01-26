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
package org.datacleaner.monitor.server.dao;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.server.job.DefaultJobEngineManager;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.SimpleDictionary;
import org.datacleaner.reference.SimpleStringPattern;
import org.datacleaner.reference.SimpleSynonym;
import org.datacleaner.reference.SimpleSynonymCatalog;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ReferenceDataDaoImplTest {
    final ReferenceDataDao _dao = new ReferenceDataDaoImpl();
    private TenantContext _tenantContext;

    @Before
    public void before() {
        final ApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("context/application-context.xml");
        final Repository repository = applicationContext.getBean(FileRepository.class);

        final TenantContextFactoryImpl tenantContextFactory =
                new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(),
                        new DefaultJobEngineManager(applicationContext));
        _tenantContext = tenantContextFactory.getContext("tenant1");
    }

    @Test
    public void testAddAndRemoveStringPattern() throws Exception {
        final StringPattern simpleStringPattern = new SimpleStringPattern("simpleStringPattern", "A#");

        _dao.addStringPattern(_tenantContext, simpleStringPattern);
        final StringPattern loadedStringPattern =
                _tenantContext.getConfiguration().getReferenceDataCatalog().getStringPattern("simpleStringPattern");
        assertNotNull(loadedStringPattern);
        assertEquals("simpleStringPattern", loadedStringPattern.getName());
        _dao.removeStringPattern(_tenantContext, "simpleStringPattern");
        assertNull(_tenantContext.getConfiguration().getReferenceDataCatalog().getStringPattern("simpleStringPattern"));
    }

    @Test
    public void testAddAndRemoveDictionary() throws Exception {
        final Dictionary simpleDictionary = new SimpleDictionary("simpleDictionary", "foo", "bar", "baz");

        _dao.addDictionary(_tenantContext, simpleDictionary);
        final Dictionary loadedDictionary =
                _tenantContext.getConfiguration().getReferenceDataCatalog().getDictionary("simpleDictionary");
        assertNotNull(loadedDictionary);
        assertEquals("simpleDictionary", loadedDictionary.getName());
        _dao.removeDictionary(_tenantContext, "simpleDictionary");
        assertNull(_tenantContext.getConfiguration().getReferenceDataCatalog().getDictionary("simpleDictionary"));
    }

    @Test
    public void testAddAndRemoveSynonymCatalog() throws Exception {
        final SynonymCatalog simpleSynonymCatalog =
                new SimpleSynonymCatalog("simpleSynonymCatalog", new SimpleSynonym("Sir, yes, sir!", "no", "I refuse"));

        _dao.addSynonymCatalog(_tenantContext, simpleSynonymCatalog);
        final SynonymCatalog loadedSynonymCatalog =
                _tenantContext.getConfiguration().getReferenceDataCatalog().getSynonymCatalog("simpleSynonymCatalog");
        assertNotNull(loadedSynonymCatalog);
        assertEquals("simpleSynonymCatalog", loadedSynonymCatalog.getName());
        _dao.removeSynonymCatalog(_tenantContext, "simpleSynonymCatalog");
        assertNull(_tenantContext.getConfiguration().getReferenceDataCatalog().getSynonymCatalog("simpleSynonymCatalog"));
    }
}
