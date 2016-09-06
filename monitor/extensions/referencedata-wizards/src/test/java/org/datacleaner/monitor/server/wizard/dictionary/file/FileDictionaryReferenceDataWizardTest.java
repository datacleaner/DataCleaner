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
package org.datacleaner.monitor.server.wizard.dictionary.file;

import org.datacleaner.monitor.server.wizard.shared.TestHelper;
import org.junit.Test;

import static org.junit.Assert.*;

public class FileDictionaryReferenceDataWizardTest {
    @Test
    public void testIsApplicableTo() {
        final FileDictionaryReferenceDataWizard wizard = new FileDictionaryReferenceDataWizard();
        assertTrue(wizard.isApplicableTo(null));
    }

    @Test
    public void testGetExpectedPageCount() {
        final FileDictionaryReferenceDataWizard wizard = new FileDictionaryReferenceDataWizard();
        assertEquals(1, wizard.getExpectedPageCount());
    }

    @Test
    public void testGetDisplayName() {
        final FileDictionaryReferenceDataWizard wizard = new FileDictionaryReferenceDataWizard();
        assertNotEquals("", wizard.getDisplayName());
    }

    @Test
    public void testStart() {
        final FileDictionaryReferenceDataWizard wizard = new FileDictionaryReferenceDataWizard();
        assertNotNull(wizard.start(TestHelper.getReferenceDataWizardContextMock()));
    }
}