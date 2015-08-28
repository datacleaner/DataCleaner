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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.metamodel.util.FileHelper;

import junit.framework.TestCase;

public class TextFileSynonymCatalogTest extends TestCase {

    public void testCountrySynonyms() throws Exception {
        SynonymCatalog cat = new TextFileSynonymCatalog("foobar", "src/test/resources/synonym-countries.txt", true,
                "UTF-8");

        try (SynonymCatalogConnection scConnection = cat.openConnection()) {

            assertNull(scConnection.getMasterTerm("foobar"));
            assertEquals("DNK", scConnection.getMasterTerm("Denmark"));
            assertEquals("GBR", scConnection.getMasterTerm("England"));

            assertEquals("GBR", scConnection.getMasterTerm("GBR"));
            assertEquals("DNK", scConnection.getMasterTerm("DNK"));
        }
    }

    public void testSerializationAndDeserialization() throws Exception {
        SynonymCatalog cat = new TextFileSynonymCatalog("foobar", "src/test/resources/synonym-countries.txt", true,
                "UTF-8");
        try (SynonymCatalogConnection scConnection = cat.openConnection()) {
            assertEquals("DNK", scConnection.getMasterTerm("Denmark"));

            byte[] bytes;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream os = new ObjectOutputStream(baos)) {
                os.writeObject(cat);
                os.flush();
                bytes = baos.toByteArray();
            }

            assertEquals("DNK", scConnection.getMasterTerm("Denmark"));
            cat = null;

            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream is = new ObjectInputStream(bais);
            cat = (SynonymCatalog) is.readObject();

            assertEquals("DNK", scConnection.getMasterTerm("Denmark"));
        }
    }

    public void testModificationsClearCache() throws Exception {
        File file = new File("target/TextBasedSynonymCatalogTest-modification.txt");
        FileHelper.writeStringAsFile(file, "foo,fooo,fo\nbar,baar,br");
        SynonymCatalog cat = new TextFileSynonymCatalog("sc", file, true, "UTF-8");

        try (SynonymCatalogConnection scConnection = cat.openConnection()) {
            assertEquals("foo", scConnection.getMasterTerm("fooo"));
            assertEquals("bar", scConnection.getMasterTerm("br"));
            assertEquals(null, scConnection.getMasterTerm("foob"));

            // sleep for two seconds because some filesystems only support
            // modification dating for the nearest second.
            Thread.sleep(2000);

            FileHelper.writeStringAsFile(file, "foo,fooo,fo\nfoobar,foob");
            assertEquals("foo", scConnection.getMasterTerm("fooo"));
            assertEquals(null, scConnection.getMasterTerm("br"));
            assertEquals("foobar", scConnection.getMasterTerm("foob"));
        }
    }
}
