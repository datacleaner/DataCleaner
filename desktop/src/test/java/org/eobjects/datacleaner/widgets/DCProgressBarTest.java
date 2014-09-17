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
package org.eobjects.datacleaner.widgets;

import junit.framework.TestCase;

public class DCProgressBarTest extends TestCase {

	public void testGetBarWidth() throws Exception {
		DCProgressBar bar = new DCProgressBar(50, 150);
		bar.setSize(100, 10);

		assertEquals(0, bar.getBarWidth(50));
		assertEquals(1, bar.getBarWidth(51));
		assertEquals(50, bar.getBarWidth(100));
		assertEquals(0, bar.getBarWidth(-10));
		assertEquals(100, bar.getBarWidth(1064564));
	}
	
	public void testSetValueIfGreater() throws Exception {
	    DCProgressBar bar = new DCProgressBar(0, 150);
	    
	    assertTrue(bar.setValueIfGreater(2));
	    assertFalse(bar.setValueIfGreater(1));
	    assertEquals(2, bar.getValue());
	    
	    assertTrue(bar.setValueIfGreater(10));
	    assertFalse(bar.setValueIfGreater(9));
	    assertFalse(bar.setValueIfGreater(8));
	    assertFalse(bar.setValueIfGreater(1));
	    assertEquals(10, bar.getValue());
    }
}
