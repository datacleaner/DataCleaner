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
package org.datacleaner.monitor.server.wizard.stringpattern.simple;

import static org.junit.Assert.*;

import org.datacleaner.monitor.server.wizard.shared.TestHelper;
import org.junit.Test;

public class SimpleStringPatternReferenceDataWizardSessionTest {

    @Test
    public void testFirstPageController() throws Exception {
        SimpleStringPatternReferenceDataWizardSession session =
                new SimpleStringPatternReferenceDataWizardSession(TestHelper.getReferenceDataWizardContextMock());
        assertNotNull(session.firstPageController());
    }

    @Test
    public void testGetPageCount() throws Exception {
        SimpleStringPatternReferenceDataWizardSession session =
                new SimpleStringPatternReferenceDataWizardSession(TestHelper.getReferenceDataWizardContextMock());
        assertEquals(new Integer(1), session.getPageCount());
    }

    @Test
    public void testGetName() throws Exception {
        SimpleStringPatternReferenceDataWizardSession session =
                new SimpleStringPatternReferenceDataWizardSession(TestHelper.getReferenceDataWizardContextMock());
        assertNull(session.getName());
        session.setName("name-value");
        assertNotNull(session.getName());
    }

    @Test
    public void testGetExpression() throws Exception {
        SimpleStringPatternReferenceDataWizardSession session =
                new SimpleStringPatternReferenceDataWizardSession(TestHelper.getReferenceDataWizardContextMock());
        assertNull(session.getExpression());
        session.setExpression("expression-value");
        assertNotNull(session.getExpression());
    }
}
