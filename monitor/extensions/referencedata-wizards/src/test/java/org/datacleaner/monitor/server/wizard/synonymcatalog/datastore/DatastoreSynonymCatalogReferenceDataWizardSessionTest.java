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
package org.datacleaner.monitor.server.wizard.synonymcatalog.datastore;

import org.datacleaner.monitor.server.wizard.shared.TestHelper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DatastoreSynonymCatalogReferenceDataWizardSessionTest {

    @Test
    public void testGetPageCount() throws Exception {
        DatastoreSynonymCatalogReferenceDataWizardSession session =
                new DatastoreSynonymCatalogReferenceDataWizardSession(TestHelper.getReferenceDataWizardContextMock());
        assertEquals(new Integer(5), session.getPageCount());
    }

    @Test
    public void testSynonymColumn() throws Exception {
        DatastoreSynonymCatalogReferenceDataWizardSession session =
                new DatastoreSynonymCatalogReferenceDataWizardSession(TestHelper.getReferenceDataWizardContextMock());
        assertNull(session.getSynonymColumn());
        session.setSynonymColumn("synonym-column-value");
        assertNotNull(session.getSynonymColumn());
    }

    @Test
    public void testSynonymColumnList() throws Exception {
        DatastoreSynonymCatalogReferenceDataWizardSession session =
                new DatastoreSynonymCatalogReferenceDataWizardSession(TestHelper.getReferenceDataWizardContextMock());
        assertNull(session.getSynonymColumnList());
        session.addToSynonymColumnList("synonym-column-1");
        session.addToSynonymColumnList("synonym-column-2");
        assertEquals("synonym-column-1,synonym-column-2", session.getSynonymColumnList());
    }

    @Test
    public void testNextSynonymColumn() throws Exception {
        DatastoreSynonymCatalogReferenceDataWizardSession session =
                new DatastoreSynonymCatalogReferenceDataWizardSession(TestHelper.getReferenceDataWizardContextMock());
        assertNull(session.getAddNextSynonymColumn());
        session.setAddNextSynonymColumn("on");
        assertEquals("on", session.getAddNextSynonymColumn());
    }
}