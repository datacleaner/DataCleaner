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
package org.eobjects.analyzer.util;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

public class LabelUtilsTest extends TestCase {

    public void testLabelAdvice() throws Exception {
        HasLabelAdvice o = new HasLabelAdvice() {
            @Override
            public String getSuggestedLabel() {
                return "foo";
            }
        };

        final String label = LabelUtils.getValueLabel(o);
        assertEquals("foo", label);
    }

    public void testFormatNull() throws Exception {
        assertEquals("<null>", LabelUtils.getValueLabel(null));
    }

    public void testFormatBlank() throws Exception {
        assertEquals("<blank>", LabelUtils.getValueLabel(""));
    }

    public void testFormatInteger() throws Exception {
        assertEquals("123", LabelUtils.getValueLabel(123));
    }

    public void testFormatDecimal() throws Exception {
        assertEquals(NumberFormat.getNumberInstance().format(123.01), LabelUtils.getValueLabel(123.01));
    }

    public void testFormatString() throws Exception {
        assertEquals("foo", LabelUtils.getValueLabel("foo"));
    }

    public void testFormatDate() throws Exception {
        Date d = new Date();
        assertEquals(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(d), LabelUtils.getValueLabel(d));
    }
}
