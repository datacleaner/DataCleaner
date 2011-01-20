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
package org.eobjects.datacleaner.widgets;

import junit.framework.TestCase;

public class DCProgressBarTest extends TestCase {

	public void testIsHigherAndSignificantBigMax() throws Exception {
		final DCProgressBar pb = new DCProgressBar(0, 1000);
		pb.setSize(100, 10);
		assertEquals(100, pb.getWidth());
		assertFalse(pb.setValueIfHigherAndSignificant(5));
		assertFalse(pb.setValueIfHigherAndSignificant(9));
		assertTrue(pb.setValueIfHigherAndSignificant(10));
		assertTrue(pb.setValueIfHigherAndSignificant(999));
		assertTrue(pb.setValueIfHigherAndSignificant(1000));
		assertFalse(pb.setValueIfHigherAndSignificant(1001));
		assertFalse(pb.setValueIfHigherAndSignificant(2001));
	}

	public void testIsHigherAndSignificantSmallMax() throws Exception {
		final DCProgressBar pb = new DCProgressBar(0, 7);
		pb.setSize(100, 10);
		assertEquals(100, pb.getWidth());
		assertFalse(pb.setValueIfHigherAndSignificant(0));
		assertTrue(pb.setValueIfHigherAndSignificant(1));
		assertTrue(pb.setValueIfHigherAndSignificant(7));
		assertFalse(pb.setValueIfHigherAndSignificant(9));
	}
}
