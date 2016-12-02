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
package org.datacleaner.monitor.server.wizard.stringpattern.regex;

import static org.junit.Assert.*;

import org.datacleaner.monitor.server.wizard.shared.TestHelper;
import org.junit.Test;

public class RegexStringPatternReferenceDataWizardTest {

    @Test
    public void testIsApplicableTo() throws Exception {
        final RegexStringPatternReferenceDataWizard wizard = new RegexStringPatternReferenceDataWizard();
        assertTrue(wizard.isApplicableTo(null));
    }

    @Test
    public void testGetDisplayName() throws Exception {
        final RegexStringPatternReferenceDataWizard wizard = new RegexStringPatternReferenceDataWizard();
        assertNotEquals("", wizard.getDisplayName());
    }

    @Test
    public void testGetExpectedPageCount() throws Exception {
        final RegexStringPatternReferenceDataWizard wizard = new RegexStringPatternReferenceDataWizard();
        assertEquals(1, wizard.getExpectedPageCount());
    }

    @Test
    public void testStart() throws Exception {
        final RegexStringPatternReferenceDataWizard wizard = new RegexStringPatternReferenceDataWizard();
        assertNotNull(wizard.start(TestHelper.getReferenceDataWizardContextMock()));
    }
}
