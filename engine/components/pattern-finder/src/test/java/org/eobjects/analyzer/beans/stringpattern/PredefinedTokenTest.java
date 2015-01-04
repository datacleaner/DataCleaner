/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.beans.stringpattern;

import junit.framework.TestCase;

public class PredefinedTokenTest extends TestCase {

	public void testEqualsAndHashCode() throws Exception {
		PredefinedToken token1 = new PredefinedToken(new PredefinedTokenDefinition("Salutations", "Mr", "Mrs", "Hr"), "Mrs");

		PredefinedToken token2 = new PredefinedToken(new PredefinedTokenDefinition("Salutations", "Mr", "Mrs", "Hr"), "Mr");
		assertFalse(token1.equals(token2));
		assertFalse(token1.hashCode() == token2.hashCode());

		PredefinedToken token3 = new PredefinedToken(new PredefinedTokenDefinition("Salutations", "Mr", "Mrs", "Hr"), "Mrs");
		assertTrue(token1.equals(token3));
		assertTrue(token1.hashCode() == token3.hashCode());
	}

	public void testTokenMethods() throws Exception {
		PredefinedToken token = new PredefinedToken(new PredefinedTokenDefinition("Salutations", "Mr", "Mrs", "Hr"), "Mrs");
		assertEquals("Token['Mrs' (PREDEFINED Salutations)]", token.toString());
		assertEquals(TokenType.PREDEFINED, token.getType());
		assertEquals("Mrs", token.getString());
		assertEquals('M', token.charAt(0));
	}
}
