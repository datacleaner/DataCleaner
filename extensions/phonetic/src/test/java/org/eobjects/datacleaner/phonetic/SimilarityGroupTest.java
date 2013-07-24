/**
 * eobjects.org AnalyzerBeans
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
package org.eobjects.datacleaner.phonetic;


import junit.framework.TestCase;

public class SimilarityGroupTest extends TestCase {

	public void testEqualsAndHashCode() throws Exception {
		SimilarityGroup sv1 = new SimilarityGroup("hello", "world");
		SimilarityGroup sv2 = new SimilarityGroup("hello", "world");
		assertEquals(sv1, sv2);
		assertEquals(sv1.hashCode(), sv2.hashCode());

		sv2 = new SimilarityGroup("world", "hello");
		assertEquals(sv1, sv2);
		assertEquals(sv1.hashCode(), sv2.hashCode());

		assertEquals("SimilarValues[hello,world]", sv1.toString());
		assertEquals("SimilarValues[hello,world]", sv2.toString());

		assertEquals(sv1, sv1);
		assertFalse(sv1.equals(null));
		assertFalse(sv1.equals("hello"));
	}

	public void testContains() throws Exception {
		SimilarityGroup sv = new SimilarityGroup("hello", "world");
		assertTrue(sv.contains("hello"));
		assertTrue(sv.contains("world"));
		assertFalse(sv.contains("helloo"));
		assertFalse(sv.contains("ello"));
		assertFalse(sv.contains(null));
	}
}
