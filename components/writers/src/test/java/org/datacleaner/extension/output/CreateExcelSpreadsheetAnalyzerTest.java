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

import junit.framework.TestCase;

import org.junit.Test;

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
        analyzer.overwriteFileIfExists = false;

        assertNotNull(analyzer.file);
        assertFalse(analyzer.file.exists());
        analyzer.validate();
        
        analyzer.overwriteFileIfExists = true;
        analyzer.validate();
        assertFalse(analyzer.file.exists());

        analyzer.file.createNewFile();
        assertTrue(analyzer.file.exists());
        analyzer.validate();
        
        try {
            analyzer.overwriteFileIfExists = false;
            assertFalse(analyzer.overwriteFileIfExists); 
            analyzer.validate();
            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("The file already exists and the columns selected do not match. Please configure the job to overwrite the existing file.", e.getMessage());
        }finally{
            analyzer.file.delete(); 
        }
    }
}
