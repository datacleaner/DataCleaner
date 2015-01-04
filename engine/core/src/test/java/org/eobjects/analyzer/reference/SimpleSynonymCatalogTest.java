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
package org.eobjects.analyzer.reference;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.commons.lang.SerializationUtils;

import junit.framework.TestCase;

public class SimpleSynonymCatalogTest extends TestCase {

    public void testGetMasterTerm() throws Exception {
        SimpleSynonymCatalog sc = new SimpleSynonymCatalog("countries", Arrays.asList(new Synonym[] {
                new SimpleSynonym("DNK", "Denmark"), new SimpleSynonym("NLD", "The netherlands") }));

        assertEquals("DNK", sc.getMasterTerm("DNK"));
        assertEquals("NLD", sc.getMasterTerm("NLD"));
        assertEquals("DNK", sc.getMasterTerm("Denmark"));
        assertEquals("NLD", sc.getMasterTerm("The netherlands"));
        assertNull(sc.getMasterTerm("Danemark"));
    }

    public void testDeserializePreviousVersion() throws Exception {
        SynonymCatalog sc;
        try (FileInputStream in = new FileInputStream(
                "src/test/resources/analyzerbeans-0.34-simple-synonym-catalog.ser")) {
            sc = (SynonymCatalog) SerializationUtils.deserialize(in);
        }

        assertEquals("DNK", sc.getMasterTerm("DNK"));
        assertEquals("NLD", sc.getMasterTerm("NLD"));
        assertEquals("DNK", sc.getMasterTerm("Denmark"));
        assertEquals("NLD", sc.getMasterTerm("The netherlands"));
        assertNull(sc.getMasterTerm("Danemark"));
    }

    public void testGetSynonyms() throws Exception {
        SimpleSynonymCatalog sc = new SimpleSynonymCatalog("countries", Arrays.asList(new Synonym[] {
                new SimpleSynonym("DNK", "Denmark", "Danmark"), new SimpleSynonym("NLD", "The netherlands") }));

        Collection<? extends Synonym> synonyms = sc.getSynonyms();
        assertEquals(2, synonyms.size());

        Iterator<? extends Synonym> it = synonyms.iterator();
        assertTrue(it.hasNext());
        Synonym s1 = it.next();
        assertNotNull(s1);
        assertEquals("DNK", s1.getMasterTerm());
        assertEquals("[DNK, Danmark, Denmark]", new TreeSet<>(s1.getSynonyms().getValues()).toString());

        assertTrue(it.hasNext());
        Synonym s2 = it.next();
        assertEquals("NLD", s2.getMasterTerm());
        assertEquals("[NLD, The netherlands]", new TreeSet<>(s2.getSynonyms().getValues()).toString());

        assertFalse(it.hasNext());
    }
}
