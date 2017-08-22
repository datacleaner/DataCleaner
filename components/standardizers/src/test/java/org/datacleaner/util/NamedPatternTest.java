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
package org.datacleaner.util;

import java.util.Set;
import java.util.regex.Pattern;

import org.datacleaner.beans.standardize.EmailStandardizerTransformer;
import org.datacleaner.beans.standardize.EmailStandardizerTransformer.EmailPart;

import junit.framework.TestCase;

@SuppressWarnings("deprecation")
public class NamedPatternTest extends TestCase {

    public enum ExamplePatternGroup {
        FOO, BARRR, W00P
    }

    public void testHasGroupLiteral() throws Exception {
        final NamedPattern<EmailPart> emailPattern = EmailStandardizerTransformer.EMAIL_PATTERN;

        final NamedPatternMatch<EmailPart> match = emailPattern.match("kasper@eobjects.dk");
        assertNotNull(match);
        assertEquals("kasper", match.get(EmailPart.USERNAME));
        assertEquals("eobjects.dk", match.get(EmailPart.DOMAIN));

        assertEquals(EmailPart.USERNAME.getGroupLiteral() + "@" + EmailPart.DOMAIN.getGroupLiteral(),
                emailPattern.getPattern().toString());
    }

    public void testToString() throws Exception {
        final NamedPattern<ExamplePatternGroup> namedPattern =
                new NamedPattern<>("FOO-(W00P)", ExamplePatternGroup.class);
        assertEquals("FOO-(W00P)", namedPattern.toString());
    }

    public void testGroupLiteral() throws Exception {
        final String groupLiteral = NamedPattern.DEFAULT_GROUP_LITERAL;
        assertTrue(Pattern.matches(groupLiteral, "hello"));
        assertFalse(Pattern.matches(groupLiteral, "hello world"));
        assertFalse(Pattern.matches(groupLiteral, "hello\nworld"));
        assertFalse(Pattern.matches(groupLiteral, "hello_world"));
        assertEquals("([\\p{Lu}\\p{Ll}]+)", groupLiteral);
    }

    public void testSimpleMatching() throws Exception {
        final NamedPattern<ExamplePatternGroup> namedPattern =
                new NamedPattern<>("FOO-BARRR", ExamplePatternGroup.class);

        final NamedPatternMatch<ExamplePatternGroup> match = namedPattern.match("hello-world");
        assertEquals("hello", match.get(ExamplePatternGroup.FOO));
        assertEquals("world", match.get(ExamplePatternGroup.BARRR));
        assertNull(match.get(ExamplePatternGroup.W00P));
    }

    public void testFillWords() throws Exception {
        final NamedPattern<ExamplePatternGroup> namedPattern =
                new NamedPattern<>("FOO there BARRR", ExamplePatternGroup.class);

        final NamedPatternMatch<ExamplePatternGroup> match = namedPattern.match("hello there world");
        assertNotNull(match);
        assertEquals("hello", match.get(ExamplePatternGroup.FOO));
        assertEquals("world", match.get(ExamplePatternGroup.BARRR));
    }

    public void testOnlyMatchFully() throws Exception {
        final NamedPattern<ExamplePatternGroup> namedPattern =
                new NamedPattern<>("FOO BARRR", ExamplePatternGroup.class);
        assertNotNull(namedPattern.match("hello there"));
        assertNull(namedPattern.match("hello there world"));
        assertNull(namedPattern.match("hello there "));
    }

    public void testDelims() throws Exception {
        final NamedPattern<ExamplePatternGroup> namedPattern =
                new NamedPattern<>("FOO BARRR", ExamplePatternGroup.class);
        assertNull(namedPattern.match("Sørensen, Kasper"));
    }

    public void testGetUsedGroups() throws Exception {
        final NamedPattern<ExamplePatternGroup> namedPattern =
                new NamedPattern<>("FOO BARRR", ExamplePatternGroup.class);
        final Set<ExamplePatternGroup> usedGroups = namedPattern.getUsedGroups();
        assertEquals(2, usedGroups.size());
        assertTrue(usedGroups.contains(ExamplePatternGroup.FOO));
        assertTrue(usedGroups.contains(ExamplePatternGroup.BARRR));
        assertFalse(usedGroups.contains(ExamplePatternGroup.W00P));
    }

    public void testScandnavianChars() throws Exception {
        final NamedPattern<ExamplePatternGroup> namedPattern =
                new NamedPattern<>("FOO BARRR", ExamplePatternGroup.class);
        final NamedPatternMatch<ExamplePatternGroup> match = namedPattern.match("Sørensen Kasper");
        assertNotNull(match);
        assertEquals("Sørensen", match.get(ExamplePatternGroup.FOO));
        assertEquals("Kasper", match.get(ExamplePatternGroup.BARRR));

        assertNotNull(namedPattern.match("æ ø"));
        assertNotNull(namedPattern.match("Ø å"));
        assertNull(namedPattern.match("Halløj æ ø å"));
    }

    public void testDiacritics() throws Exception {
        final NamedPattern<ExamplePatternGroup> namedPattern =
                new NamedPattern<>("FOO BARRR", ExamplePatternGroup.class);
        assertNotNull(namedPattern.match("ö ä"));
        assertNotNull(namedPattern.match("â á"));
        assertNotNull(namedPattern.match("à ä"));
    }

    public void testParanthesis() throws Exception {
        final NamedPattern<ExamplePatternGroup> namedPattern =
                new NamedPattern<>("FOO-(W00P)", ExamplePatternGroup.class);

        NamedPatternMatch<ExamplePatternGroup> match = namedPattern.match("hello-world");
        assertNull(match);

        match = namedPattern.match("hello-(world)");
        assertEquals("hello", match.get(ExamplePatternGroup.FOO));
        assertEquals("world", match.get(ExamplePatternGroup.W00P));
        assertNull(match.get(ExamplePatternGroup.BARRR));
    }
}
