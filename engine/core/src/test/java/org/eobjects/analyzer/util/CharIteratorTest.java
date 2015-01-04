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
package org.eobjects.analyzer.util;

import junit.framework.TestCase;

public class CharIteratorTest extends TestCase {

	public void testRemove() throws Exception {
		CharIterator it = new CharIterator("1234");
		it.next();
		it.remove();
		assertEquals(-1, it.currentIndex());
		assertTrue(it.hasNext());
		it.next();
		assertTrue(it.is('2'));
		assertEquals("234", it.toString());
		it.next();
		assertTrue(it.hasNext());
		it.next();
		assertFalse(it.hasNext());
	}

	public void testSimpleIteration() throws Exception {
		CharIterator c = new CharIterator("Hi there Kasper Sørensen");

		assertTrue(c.hasNext());
		assertFalse(c.hasPrevious());

		// H
		c.next();
		assertTrue(c.isLetter());
		assertTrue(c.isUpperCase());
		assertFalse(c.isDiacritic());
		assertFalse(c.isWhitespace());
		assertTrue(c.hasNext());
		assertFalse(c.hasPrevious());

		// i
		c.next();
		assertTrue(c.isLetter());
		assertTrue(c.isLowerCase());
		assertFalse(c.isDiacritic());
		assertFalse(c.isWhitespace());
		assertTrue(c.hasNext());
		assertTrue(c.hasPrevious());

		// (space)
		c.next();
		assertFalse(c.isLetter());
		assertFalse(c.isLowerCase());
		assertFalse(c.isDiacritic());
		assertTrue(c.isWhitespace());
		assertTrue(c.hasNext());
		assertTrue(c.hasPrevious());

		// there
		c.next();
		c.next();
		c.next();
		c.next();
		c.next();
		assertTrue(c.is('e'));

		// (space)
		c.next();
		assertFalse(c.isLetter());
		assertFalse(c.isLowerCase());
		assertFalse(c.isDiacritic());
		assertTrue(c.isWhitespace());
		assertTrue(c.hasNext());
		assertTrue(c.hasPrevious());

		// Kasper
		c.next();
		assertTrue(c.isUpperCase());
		c.next();
		c.next();
		c.next();
		c.next();
		c.next();
		assertTrue(c.is('r'));

		// (space)
		c.next();
		assertFalse(c.isLetter());
		assertFalse(c.isLowerCase());
		assertFalse(c.isDiacritic());
		assertTrue(c.isWhitespace());
		assertTrue(c.hasNext());
		assertTrue(c.hasPrevious());

		// S
		c.next();
		assertTrue(c.isLetter());
		assertFalse(c.isLowerCase());
		assertTrue(c.isUpperCase());
		assertFalse(c.isDiacritic());
		assertFalse(c.isWhitespace());
		assertTrue(c.hasNext());
		assertTrue(c.hasPrevious());

		// ø
		c.next();
		assertTrue(c.isLetter());
		assertTrue(c.isLowerCase());
		assertFalse(c.isUpperCase());
		assertTrue(c.isDiacritic());
		assertFalse(c.isWhitespace());
		assertTrue(c.hasNext());
		assertTrue(c.hasPrevious());

		// rense
		c.next();
		c.next();
		c.next();
		c.next();
		assertTrue(c.is('s'));
		c.next();
		assertTrue(c.is('e'));
		assertTrue(c.hasNext());
		assertTrue(c.hasPrevious());

		c.next();
		assertTrue(c.is('n'));
		assertFalse(c.hasNext());
		assertTrue(c.hasPrevious());
	}

	public void testDigits() throws Exception {
		CharIterator c = new CharIterator("a1b2");
		c.next();
		assertTrue(c.isLetter());
		c.next();
		assertTrue(c.isDigit());
		c.next();
		assertTrue(c.isLetter());
		c.next();
		assertTrue(c.isDigit());
		assertFalse(c.hasNext());
	}

	public void testFirstAndLast() throws Exception {
		CharIterator c = new CharIterator("1234");
		assertEquals('1', c.first().charValue());
		assertEquals('4', c.last().charValue());
	}

	public void testEmptyString() throws Exception {
		CharIterator c = new CharIterator("");
		assertFalse(c.hasNext());
	}

	public void testNull() throws Exception {
		CharIterator c = new CharIterator((CharSequence) null);
		assertFalse(c.hasNext());

		c = new CharIterator((char[]) null);
		assertFalse(c.hasNext());
	}

	public void testSubIterator() throws Exception {
		CharIterator c = new CharIterator("hello world");
		assertEquals("hello world", c.toString());

		assertEquals("world", c.subIterator(6, 11).toString());

		c = c.subIterator(2, 4);
		assertEquals("ll", c.toString());
	}
}
