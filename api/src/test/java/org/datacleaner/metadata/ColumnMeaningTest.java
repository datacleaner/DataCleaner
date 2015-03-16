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
package org.datacleaner.metadata;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ColumnMeaningTest {

    @Test
    public void testFind() throws Exception {
        assertEquals(ColumnMeaning.PERSON_NAME_GIVEN, ColumnMeaning.find(ColumnMeaning.PERSON_NAME_GIVEN.name()));
        assertEquals(ColumnMeaning.PERSON_NAME_GIVEN, ColumnMeaning.find("Given name"));
        assertEquals(ColumnMeaning.PERSON_NAME_GIVEN, ColumnMeaning.find("Givenname"));
        assertEquals(ColumnMeaning.PERSON_NAME_GIVEN, ColumnMeaning.find("First name"));
        assertEquals(ColumnMeaning.PERSON_NAME_GIVEN, ColumnMeaning.find("   FIRST_NAME \t"));
        
        assertEquals(ColumnMeaning.EMAIL_ADDRESS, ColumnMeaning.find("Email"));
        assertEquals(ColumnMeaning.EMAIL_ADDRESS, ColumnMeaning.find("e-mail"));
        
        assertEquals(ColumnMeaning.PHONE_PHONENUMBER, ColumnMeaning.find("phone no."));
        assertEquals(ColumnMeaning.PHONE_PHONENUMBER, ColumnMeaning.find("phone number"));
    }
}
