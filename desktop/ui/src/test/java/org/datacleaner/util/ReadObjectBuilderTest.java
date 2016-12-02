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

import java.io.FileInputStream;
import java.util.List;

import org.datacleaner.reference.ReferenceData;

import junit.framework.TestCase;

public class ReadObjectBuilderTest extends TestCase {

    public void testDeserializeLegacyReferenceData() throws Exception {
        final ChangeAwareObjectInputStream objectInputStream = new ChangeAwareObjectInputStream(
                new FileInputStream("src/test/resources/analyzerbeans-0.4-reference-data.dat"));
        final Object deserializedObject = objectInputStream.readObject();
        objectInputStream.close();
        assertTrue(deserializedObject instanceof List);

        @SuppressWarnings("unchecked") final List<ReferenceData> list = (List<ReferenceData>) deserializedObject;
        assertEquals(6, list.size());

        assertEquals("DatastoreDictionary[name=datastore_dict]", list.get(0).toString());
        assertEquals(
                "TextFileDictionary[name=textfile_dict, filename=src/test/resources/lastnames.txt, encoding=UTF-8]",
                list.get(1).toString());
        assertEquals("SimpleDictionary[name=valuelist_dict]", list.get(2).toString());
        assertEquals( "TextFileSynonymCatalog[name=textfile_syn, filename=src/test/resources/synonym-countries.txt, "
                + "caseSensitive=false, encoding=UTF-8]", list.get(3).toString());
        assertEquals(
                "RegexStringPattern[name=regex danish email, expression=[a-z]+@[a-z]+\\.dk, matchEntireString=true]",
                list.get(4).toString());
        assertEquals("SimpleStringPattern[name=simple email, expression=aaaa@aaaaa.aa]", list.get(5).toString());
    }
}
