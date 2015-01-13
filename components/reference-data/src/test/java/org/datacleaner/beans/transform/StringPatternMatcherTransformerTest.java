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
package org.datacleaner.beans.transform;

import java.util.Arrays;

import org.datacleaner.data.MockInputColumn;
import org.datacleaner.reference.RegexStringPattern;
import org.datacleaner.reference.SimpleStringPattern;
import org.datacleaner.reference.StringPattern;

import junit.framework.TestCase;

public class StringPatternMatcherTransformerTest extends TestCase {

	public void testTransform() throws Exception {
		StringPatternMatcherTransformer t = new StringPatternMatcherTransformer();
		StringPattern[] stringPatterns = new StringPattern[3];
		stringPatterns[0] = new RegexStringPattern("lowercase word", "[a-z]+", true);
		stringPatterns[1] = new RegexStringPattern("any word", "[a-zA-Z]+", true);
		stringPatterns[2] = new SimpleStringPattern("capitalized word", "Aaaa");

		t.setStringPatterns(stringPatterns);
		t.setColumn(new MockInputColumn<Object>("Greeting", Object.class));

		assertEquals(3, t.getOutputColumns().getColumnCount());
		assertEquals("Greeting 'lowercase word'", t.getOutputColumns().getColumnName(0));
		assertEquals("Greeting 'any word'", t.getOutputColumns().getColumnName(1));
		assertEquals("Greeting 'capitalized word'", t.getOutputColumns().getColumnName(2));

		assertEquals("[true, true, false]", Arrays.toString(t.doMatching("hello")));
		assertEquals("[false, true, true]", Arrays.toString(t.doMatching("Hello")));
		assertEquals("[false, false, false]", Arrays.toString(t.doMatching("Hello world")));
		assertEquals("[false, false, false]", Arrays.toString(t.doMatching(null)));
		assertEquals("[false, false, false]", Arrays.toString(t.doMatching(1243)));
		assertEquals("[true, true, false]", Arrays.toString(t.doMatching(true)));

	}
}
