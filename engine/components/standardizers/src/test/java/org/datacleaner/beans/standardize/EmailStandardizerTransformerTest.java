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
package org.datacleaner.beans.standardize;

import org.datacleaner.beans.standardize.EmailStandardizerTransformer;

import junit.framework.TestCase;

public class EmailStandardizerTransformerTest extends TestCase {
	
	public void testNull() throws Exception {
		EmailStandardizerTransformer transformer = new EmailStandardizerTransformer();

		String[] result = transformer.transform((String)null);
		assertEquals(2, result.length);
		assertEquals(null, result[0]);
		assertEquals(null, result[1]);
	}

	public void testTransform() throws Exception {
		EmailStandardizerTransformer transformer = new EmailStandardizerTransformer();

		String[] result = transformer.transform("kasper@eobjects.dk");
		assertEquals(2, result.length);
		assertEquals("kasper", result[0]);
		assertEquals("eobjects.dk", result[1]);

		result = transformer.transform("kasper.sorensen@eobjects.dk");
		assertEquals(2, result.length);
		assertEquals("kasper.sorensen", result[0]);
		assertEquals("eobjects.dk", result[1]);

		result = transformer.transform("kasper.sorensen@eobjects.d");
		assertEquals(2, result.length);
		assertNull(result[0]);
		assertNull(result[1]);

		result = transformer.transform("@eobjects.dk");
		assertEquals(2, result.length);
		assertNull(result[0]);
		assertNull(result[1]);

		result = transformer.transform("kasper.sorensen@eobjects.organization");
		assertEquals(2, result.length);
		assertNull(result[0]);
		assertNull(result[1]);
	}
}
