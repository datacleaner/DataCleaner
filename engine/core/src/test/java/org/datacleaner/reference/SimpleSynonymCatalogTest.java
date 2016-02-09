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
package org.datacleaner.reference;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import junit.framework.TestCase;

public class SimpleSynonymCatalogTest extends TestCase {

    public void testGetMasterTerm() throws Exception {
        final    SimpleSynonymCatalog sc = new SimpleSynonymCatalog("countries", Arrays.asList(new Synonym[] {
                new SimpleSynonym("DNK", "Denmark"), new SimpleSynonym("NLD", "The netherlands") }));

        try (SynonymCatalogConnection connection = sc.openConnection(null)) {
            assertEquals("DNK", connection.getMasterTerm("DNK"));
            assertEquals("NLD", connection.getMasterTerm("NLD"));
            assertEquals("DNK", connection.getMasterTerm("Denmark"));
            assertEquals("NLD", connection.getMasterTerm("The netherlands"));
            assertNull(connection.getMasterTerm("Danemark"));
        }
    }

    public void testReplaceInline() {
        final SimpleSynonymCatalog sc =
                new SimpleSynonymCatalog("countries", Arrays.asList(new Synonym[] { new SimpleSynonym("Test", "land"),
                        new SimpleSynonym("DNK", "Denmark", "Danmark"), new SimpleSynonym("NLD", "The netherlands") }));

        final SynonymCatalogConnection connection = sc.openConnection(null);
        final SynonymCatalogConnection.Replacement replacement =
                connection.replaceInline("The netherlands, Denmark, Holland, land");
        assertEquals("NLD, DNK, Holland, Test", replacement.getReplacedString());
        assertEquals("The netherlands", replacement.getSynonyms().get(0));
        assertEquals("Denmark", replacement.getSynonyms().get(1));
        assertEquals("land", replacement.getSynonyms().get(2));
        assertEquals("NLD", replacement.getMasterTerms().get(0));
        assertEquals("DNK", replacement.getMasterTerms().get(1));
        assertEquals("Test", replacement.getMasterTerms().get(2));

    }

    public void testGetSynonyms() throws Exception {
        final SimpleSynonymCatalog sc = new SimpleSynonymCatalog("countries", Arrays.asList(new Synonym[] {
                new SimpleSynonym("DNK", "Denmark", "Danmark"), new SimpleSynonym("NLD", "The netherlands") }));

        final SynonymCatalogConnection connection = sc.openConnection(null);
        final Collection<? extends Synonym> synonyms = connection.getSynonyms();
        connection.close();

        assertEquals(2, synonyms.size());

        final Iterator<? extends Synonym> it = synonyms.iterator();
        assertTrue(it.hasNext());
        final Synonym s1 = it.next();
        assertNotNull(s1);
        assertEquals("DNK", s1.getMasterTerm());
        assertEquals("[DNK, Danmark, Denmark]", new TreeSet<>(s1.getSynonyms()).toString());

        assertTrue(it.hasNext());
        final Synonym s2 = it.next();
        assertEquals("NLD", s2.getMasterTerm());
        assertEquals("[NLD, The netherlands]", new TreeSet<>(s2.getSynonyms()).toString());

        assertFalse(it.hasNext());
    }
}
