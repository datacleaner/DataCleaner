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

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class CreateCsvFileAnalyzerTest {

    @Test
    public void test() throws Exception {
        CreateCsvFileAnalyzer analyzer = new CreateCsvFileAnalyzer();

        analyzer.file = new File("csvtest.csv");
        analyzer.file.delete();
        analyzer.initTempFile();
        assertNotNull(analyzer.file);
        // Case 1 - file does not exists
        assertFalse(analyzer.file.exists());
        assertEquals("csvtest.csv", analyzer.getSuggestedLabel());
        analyzer.overwriteFile = false;
        analyzer.validate();

        try {
            analyzer.overwriteFile = true;
            analyzer.validate();
            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("The file can not be overwritten because the file does not exist", e.getMessage());
        }

        // Case 2 - file exists
        final boolean createNewFile = analyzer.file.createNewFile();
        assertTrue(createNewFile);

        try {
            writeFile(analyzer, false);
            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("The file already exists. Please configure the job to overwrite the existing file.",
                    e.getMessage());
        }

        try {
            writeFile(analyzer, true);
            fail("Exception Expected"); 
        } catch (Exception e) {
            // there are no columns to write in the file. Therefore we will get a Null Pointer exception. 
            assertEquals(null, e.getMessage());
        } finally {
            analyzer.file.delete();
        }
    }

    private void writeFile(CreateCsvFileAnalyzer analyzer, boolean overwrite) {
        analyzer.overwriteFile = overwrite;
        analyzer.validate();
        analyzer.createOutputWriter();
    }

}
