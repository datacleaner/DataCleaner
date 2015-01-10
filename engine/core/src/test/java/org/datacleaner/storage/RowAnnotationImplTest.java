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
package org.datacleaner.storage;

import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.lang.SerializationUtils;
import org.datacleaner.util.ChangeAwareObjectInputStream;

public class RowAnnotationImplTest extends TestCase {

    public void testDeserializeOlderVersion() throws Exception {
        Object obj;
        try (InputStream in = new FileInputStream("src/test/resources/old_row_annotation_impl.ser")) {
            ChangeAwareObjectInputStream changeAware = new ChangeAwareObjectInputStream(in);
            obj = changeAware.readObject();
            changeAware.close();
        }

        assertTrue(obj instanceof RowAnnotationImpl);
        RowAnnotationImpl annotation = (RowAnnotationImpl) obj;
        assertEquals(10, annotation.getRowCount());
    }

    public void testSerializeAndDeserializeCurrentVersion() throws Exception {
        RowAnnotationImpl annotation1 = new RowAnnotationImpl();
        annotation1.incrementRowCount(20);

        byte[] bytes = SerializationUtils.serialize(annotation1);
        RowAnnotationImpl annotation2 = (RowAnnotationImpl) SerializationUtils.deserialize(bytes);

        assertEquals(20, annotation2.getRowCount());
    }
}
