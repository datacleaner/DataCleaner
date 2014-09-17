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
package org.eobjects.datacleaner.monitor.shared.model;

import junit.framework.TestCase;

public class JobIdentifierTest extends TestCase {

    public void testFromResultIdSuccess() throws Exception {
        assertEquals("foo", JobIdentifier.fromResultId("foo-1234").getName());
        assertEquals("1234", JobIdentifier.fromResultId("1234-1234").getName());
        assertEquals("Foo Bar-Baz 1234", JobIdentifier.fromResultId("Foo Bar-Baz 1234-1234").getName());
    }

    public void testFromResultIdFail() throws Exception {
        runFromResultIdFailTest(null);
        runFromResultIdFailTest("");
        runFromResultIdFailTest("--");
        runFromResultIdFailTest("-foo");
        runFromResultIdFailTest("-1234");
        runFromResultIdFailTest("1234");
        runFromResultIdFailTest("1234-abc");
        runFromResultIdFailTest("Foo 1234");
        runFromResultIdFailTest("Foo 1234-1234-abc");
    }

    private String runFromResultIdFailTest(String string) {
        try {
            JobIdentifier.fromResultId(string);
            fail("Exception expected");
            return null;
        } catch (IllegalArgumentException e) {
            // good then
            return e.getMessage();
        }
    }
}
