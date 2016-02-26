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
package org.datacleaner.api;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class UsageMeteringMessageTest {

    @Test
    public void testNoDetails() {
        UsageMeteringMessage msg = new UsageMeteringMessage("myType");
        Assert.assertEquals("myType", msg.getType());
        Assert.assertEquals("", msg.getDetails());
    }

    @Test
    public void testDetailsSimple() {
        UsageMeteringMessage msg = new UsageMeteringMessage("myType", "abcd", "efg");
        Assert.assertEquals("myType", msg.getType());
        Assert.assertEquals("\"abcd\",\"efg\"", msg.getDetails());
    }

    @Test
    public void testDetailsSpecialChars() {
        UsageMeteringMessage msg = new UsageMeteringMessage("myType", "ab,cd", "ef\"g", "h\\i");
        Assert.assertEquals("myType", msg.getType());
        Assert.assertEquals("\"ab,cd\",\"ef\\\"g\",\"h\\\\i\"", msg.getDetails());
    }

    @Test
    public void testEmptyDetailFields() {
        // empty strings
        UsageMeteringMessage msg = new UsageMeteringMessage("myType", "abcd", "");
        Assert.assertEquals("myType", msg.getType());
        Assert.assertEquals("\"abcd\",\"\"", msg.getDetails());
        msg = new UsageMeteringMessage("myType", "", "abcd");
        Assert.assertEquals("myType", msg.getType());
        Assert.assertEquals("\"\",\"abcd\"", msg.getDetails());

        // null values
        msg = new UsageMeteringMessage("myType", "abcd", null);
        Assert.assertEquals("myType", msg.getType());
        Assert.assertEquals("\"abcd\",\"\"", msg.getDetails());
        msg = new UsageMeteringMessage("myType", null, "abcd");
        Assert.assertEquals("myType", msg.getType());
        Assert.assertEquals("\"\",\"abcd\"", msg.getDetails());
    }
}
