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
package org.eobjects.datacleaner.monitor.dashboard.widgets;

import junit.framework.TestCase;

public class DrillToProfilingResultSelectHandlerTest extends TestCase {

    public void testToCamelCase() throws Exception {
        assertEquals("StringAnalyzer", DrillToProfilingResultSelectHandler.toCamelCase(" String analyzer  "));
        assertEquals("Pattern", DrillToProfilingResultSelectHandler.toCamelCase("Pattern"));
        assertEquals("PatternFinder", DrillToProfilingResultSelectHandler.toCamelCase("Pattern finder"));
        assertEquals("PatternFinder", DrillToProfilingResultSelectHandler.toCamelCase("PatternFinder"));
        assertEquals("", DrillToProfilingResultSelectHandler.toCamelCase(""));
        assertEquals("", DrillToProfilingResultSelectHandler.toCamelCase(null));
    }
}
