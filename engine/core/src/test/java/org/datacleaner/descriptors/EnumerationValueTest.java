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
package org.datacleaner.descriptors;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class EnumerationValueTest {
    enum TestEnum {
        a, b
    }
    
    @Test
    public void testFromArray() {
        assertArrayEquals(new EnumerationValue[] { new EnumerationValue(TestEnum.a), new EnumerationValue(TestEnum.b) },
                EnumerationValue.fromArray(new TestEnum[] { TestEnum.a, TestEnum.b }));

        assertArrayEquals(new EnumerationValue[] { new EnumerationValue(TestEnum.a), null },
                EnumerationValue.fromArray(new TestEnum[] { TestEnum.a, null }));
    }
}
