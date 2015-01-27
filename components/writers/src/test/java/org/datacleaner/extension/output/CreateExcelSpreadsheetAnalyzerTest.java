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
package org.datacleaner.extension.output;

import java.io.File;

import org.junit.Test;

import junit.framework.TestCase;

public class CreateExcelSpreadsheetAnalyzerTest extends TestCase {

    @Test
    public void testValidateSheetName() throws Exception {
        CreateExcelSpreadsheetAnalyzer analyzer = new CreateExcelSpreadsheetAnalyzer();
        analyzer.sheetName = "foo";
        analyzer.validate();

        analyzer.sheetName = "foo.bar";
        try {
            analyzer.validate();
            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("Sheet name cannot contain dots (.)", e.getMessage());
        }
    }

    @Test
    public void testValidateOverwriteFile() throws Exception {
        CreateExcelSpreadsheetAnalyzer analyzer = new CreateExcelSpreadsheetAnalyzer();

        analyzer.sheetName = "foo";
        analyzer.overwriteFile = false;

        final File file = analyzer.file;
        assertNotNull(file);
        assertFalse(file.exists());
        analyzer.validate();

        try {
            file.createNewFile();
            assertTrue(file.exists()); 
            assertFalse(analyzer.overwriteFile); 
            analyzer.validate();
            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("The file already exits and the columns selected do not match", e.getMessage());
        }

        analyzer.overwriteFile = true;
        analyzer.validate();
        file.delete();
        assertFalse(file.exists());

    }
}
