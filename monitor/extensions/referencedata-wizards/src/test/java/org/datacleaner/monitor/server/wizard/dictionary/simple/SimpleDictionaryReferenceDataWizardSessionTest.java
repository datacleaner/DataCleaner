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
package org.datacleaner.monitor.server.wizard.dictionary.simple;

import org.datacleaner.monitor.server.wizard.shared.TestHelper;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleDictionaryReferenceDataWizardSessionTest {

    @Test
    public void testFirstPageController() throws Exception {
        SimpleDictionaryReferenceDataWizardSession session =
                new SimpleDictionaryReferenceDataWizardSession(TestHelper.getReferenceDataWizardContextMock());
        assertNotNull(session.firstPageController());
    }

    @Test
    public void testGetPageCount() throws Exception {
        SimpleDictionaryReferenceDataWizardSession session =
                new SimpleDictionaryReferenceDataWizardSession(TestHelper.getReferenceDataWizardContextMock());
        assertEquals(new Integer(1), session.getPageCount());
    }

    @Test
    public void testName() throws Exception {
        SimpleDictionaryReferenceDataWizardSession session =
                new SimpleDictionaryReferenceDataWizardSession(TestHelper.getReferenceDataWizardContextMock());
        assertNull(session.getName());
        session.setName("name-value");
        assertNotNull(session.getName());
    }

    @Test
    public void testValues() throws Exception {
        SimpleDictionaryReferenceDataWizardSession session =
                new SimpleDictionaryReferenceDataWizardSession(TestHelper.getReferenceDataWizardContextMock());
        assertNull(session.getValues());
        session.setValues("values-value");
        assertNotNull(session.getValues());
    }

    @Test
    public void testCaseSensitive() throws Exception {
        SimpleDictionaryReferenceDataWizardSession session =
                new SimpleDictionaryReferenceDataWizardSession(TestHelper.getReferenceDataWizardContextMock());
        assertNull(session.getCaseSensitive());
        session.setCaseSensitive("case-sensitive-value");
        assertNotNull(session.getCaseSensitive());
    }
}