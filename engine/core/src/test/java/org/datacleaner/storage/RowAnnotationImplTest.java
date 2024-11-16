/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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

import org.apache.commons.lang.SerializationUtils;

import junit.framework.TestCase;

public class RowAnnotationImplTest extends TestCase {

	public void testSerializeAndDeserializeCurrentVersion() throws Exception {
		final RowAnnotationImpl annotation1 = new RowAnnotationImpl();
		annotation1.incrementRowCount(20);

		final byte[] bytes = SerializationUtils.serialize(annotation1);
		final RowAnnotationImpl annotation2 = (RowAnnotationImpl) SerializationUtils.deserialize(bytes);

		assertEquals(20, annotation2.getRowCount());
	}
}
