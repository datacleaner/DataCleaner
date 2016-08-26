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
package org.datacleaner.monitor.server.wizard.stringpattern.regexswap;

import org.junit.Test;

import static org.junit.Assert.*;

public class RegexSwapStringPatternReferenceDataWizardSessionTest {
    
    @Test
    public void testPageCount() {
        final RegexSwapStringPatternReferenceDataWizardSession session = 
                new RegexSwapStringPatternReferenceDataWizardSession(null);
        assertEquals(new Integer(2), session.getPageCount());
    }

    @Test
    public void testName() {
        final RegexSwapStringPatternReferenceDataWizardSession session =
                new RegexSwapStringPatternReferenceDataWizardSession(null);
        final String name = "name-value";
        assertNull(session.getName());
        session.setName(name);
        assertEquals(name, session.getName());
    }

    @Test
    public void testCategory() {
        final RegexSwapStringPatternReferenceDataWizardSession session =
                new RegexSwapStringPatternReferenceDataWizardSession(null);
        final String category = "category-value";
        assertNull(session.getCategory());
        session.setCategory(category);
        assertEquals(category, session.getCategory());
    }
}