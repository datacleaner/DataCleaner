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
package org.eobjects.analyzer.beans.transform;

import junit.framework.TestCase;

public class WhitespaceTrimmerTransformerTest extends TestCase {

	public void testTransform() throws Exception {
		WhitespaceTrimmerTransformer t = new WhitespaceTrimmerTransformer(false, true, false);
		assertEquals(" hello  world", t.transform(" hello  world "));
		assertNull(t.transform((String) null));

		t = new WhitespaceTrimmerTransformer(true, false, false);
		assertEquals("hello  world ", t.transform(" hello  world "));

		t = new WhitespaceTrimmerTransformer(true, true, true);
		assertEquals("hello world", t.transform(" hello  world "));
		assertEquals("hello world", t.transform(" hello\t\tworld "));
		assertEquals("hello world", t.transform(" hello\tworld "));
		assertEquals("hello world", t.transform(" hello\tworld  "));
	}
}
