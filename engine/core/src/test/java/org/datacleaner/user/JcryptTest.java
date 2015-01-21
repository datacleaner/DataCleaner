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
package org.datacleaner.user;

import junit.framework.TestCase;

public class JcryptTest extends TestCase {

	// just some examples of crypts to make sure that refactorings etc. to the
	// Jcrypt algorhitm are non-destructive
	public void testCrypt() throws Exception {
		assertEquals("henl.jovt9g8U", Jcrypt.crypt("hello", "world"));
		assertEquals("hiWZrYCQu7uAQ", Jcrypt.crypt("hi", "world"));
		assertEquals("he/1NK2XEqfVo", Jcrypt.crypt("hello", "there"));
		assertEquals("hi89qWRBmF6qs", Jcrypt.crypt("hi", "there"));
		assertEquals("HE2G1BmcTTZTA", Jcrypt.crypt("HELLO", "there"));
		assertEquals("so.Aionqu/AZQ", Jcrypt.crypt("some_password", "_!$@&mrrrh\"()"));
		assertEquals("thggyIaR6Y8NY", Jcrypt.crypt("there", "there"));
	}
}
