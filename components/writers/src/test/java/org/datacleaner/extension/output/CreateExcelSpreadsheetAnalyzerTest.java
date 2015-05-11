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
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.util.FileResource;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.easymock.EasyMock;
import org.junit.Test;

public class CreateExcelSpreadsheetAnalyzerTest extends TestCase {
    
    private File generatedFile;
    
    @Override
    protected void tearDown() throws Exception {
        if (generatedFile != null) {
            generatedFile.delete();
        }
        super.tearDown();
    }

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
            assertEquals("Sheet name cannot contain '.'", e.getMessage());
        }
    }

    @Test
    public void testFixSheetName() throws Exception {
        final Datastore datastore = EasyMock.createMock(Datastore.class);
        final FilterDescriptor<?, ?> filterDescriptor = EasyMock.createMock(FilterDescriptor.class);

        EasyMock.expect(datastore.openConnection()).andReturn(null);
        EasyMock.expect(datastore.getName()).andReturn("data:store");
        EasyMock.expect(filterDescriptor.getDisplayName()).andReturn("my fil-ter");

        EasyMock.replay(datastore, filterDescriptor);

        final AnalysisJobBuilder ajb = new AnalysisJobBuilder(new DataCleanerConfigurationImpl());
        ajb.setDatastore(datastore);

        final CreateExcelSpreadsheetAnalyzer analyzer = new CreateExcelSpreadsheetAnalyzer();
        analyzer.configureForFilterOutcome(ajb, filterDescriptor, "OUT.COME");

        assertEquals("output-data-store-my fil-ter-OUT-COME", analyzer.sheetName);

        EasyMock.verify(datastore, filterDescriptor);
    }

    @Test
    public void testValidateOverwriteFile() throws Exception {

        CreateExcelSpreadsheetAnalyzer analyzer = new CreateExcelSpreadsheetAnalyzer();

        analyzer.sheetName = "foo";
        analyzer.overwriteSheetIfExists = false;

        assertNotNull(analyzer.file);
        assertFalse(analyzer.file.exists());
        analyzer.validate();

        analyzer.overwriteSheetIfExists = true;
        analyzer.validate();
        assertFalse(analyzer.file.exists());

        analyzer.file = new File("src/test/resources/multiple_Sheets.xlsx");
        assertTrue(analyzer.file.exists());
        analyzer.validate();

        try {
            analyzer.overwriteSheetIfExists = false;
            assertFalse(analyzer.overwriteSheetIfExists);
            analyzer.sheetName = "Sheet1";
            analyzer.validate();
            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("The sheet 'Sheet1' already exists. Please select another sheet name.", e.getMessage());
        }

        analyzer.overwriteSheetIfExists = true;
        assertTrue(analyzer.overwriteSheetIfExists);
        analyzer.sheetName = "Sheet1";
        analyzer.validate();

        analyzer.sheetName = "Bar";
        assertTrue(analyzer.overwriteSheetIfExists);
        analyzer.validate();

        analyzer.overwriteSheetIfExists = false;
        assertFalse(analyzer.overwriteSheetIfExists);
        analyzer.validate();
    }
    
    @Test
    public void testSortNumerical() throws Exception {
        final String filename = "target/exceltest-sortnumerical.xlsx";
        
        CreateExcelSpreadsheetAnalyzer analyzer = new CreateExcelSpreadsheetAnalyzer();

        final InputColumn<String> testColumn = new MockInputColumn<String>("TestColumn");
        // Point of focus: MockInputColumn is of type Input, so it should be sorted as numbers
        final InputColumn<Integer> idColumn = new MockInputColumn<Integer>("IdToSort", Integer.class);

        generatedFile = new File(filename);
        analyzer.file = generatedFile;
        assertNotNull(analyzer.file);
        
        analyzer.sheetName = "foo";

        analyzer.columns = new InputColumn<?>[2];
        analyzer.columns[0] = testColumn;
        analyzer.columns[1] = idColumn;

        analyzer.columnToBeSortedOn = idColumn;

        analyzer.validate();
        
        analyzer.init();

        InputRow[] rows = new InputRow[13];
        rows[0] = new MockInputRow().put(testColumn, "row00").put(idColumn, 7);
        rows[1] = new MockInputRow().put(testColumn, "row01").put(idColumn, 9);
        rows[2] = new MockInputRow().put(testColumn, "row02").put(idColumn, 2);
        rows[3] = new MockInputRow().put(testColumn, "row03").put(idColumn, 3);
        rows[4] = new MockInputRow().put(testColumn, "row04").put(idColumn, 4);
        rows[5] = new MockInputRow().put(testColumn, "row05").put(idColumn, 12);
        rows[6] = new MockInputRow().put(testColumn, "row06").put(idColumn, 6);
        rows[7] = new MockInputRow().put(testColumn, "row07").put(idColumn, 0);
        rows[8] = new MockInputRow().put(testColumn, "row08").put(idColumn, 8);
        rows[9] = new MockInputRow().put(testColumn, "row09").put(idColumn, 1);
        rows[10] = new MockInputRow().put(testColumn, "row10").put(idColumn, 10);
        rows[11] = new MockInputRow().put(testColumn, "row11").put(idColumn, 11);
        rows[12] = new MockInputRow().put(testColumn, "row12").put(idColumn, 5);

        for (int i = 0; i < rows.length; i++) {
            analyzer.run(rows[i], i);
        }

        analyzer.getResult();

        final List<Integer> resultIds = new ArrayList<>(13);
        ExcelDatastore outputDatastore = new ExcelDatastore(filename, new FileResource(analyzer.file), analyzer.file.getAbsolutePath());
        try (UpdateableDatastoreConnection outputDatastoreConnection = outputDatastore.openConnection()) {
            DataContext dataContext = outputDatastoreConnection.getDataContext();
            try (DataSet dataSet = dataContext.query().from("foo").selectAll().execute()) {
                while (dataSet.next()) {
                    Row row = dataSet.getRow();
                    Integer idValue = Integer.parseInt((String) row.getValue(1));
                    resultIds.add(idValue);
                }
            }
        }

        assertEquals("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]", resultIds.toString());
    }

    @Test
    public void testSortLexicographic() throws Exception {
        final String filename = "target/exceltest-sortlexicographic.xlsx";

        CreateExcelSpreadsheetAnalyzer analyzer = new CreateExcelSpreadsheetAnalyzer();

        final InputColumn<String> testColumn = new MockInputColumn<String>("TestColumn");
        // Point of focus: MockInputColumn is of type String, so it should be sorted alphabetically
        final InputColumn<String> idColumn = new MockInputColumn<String>("IdToSort", String.class);

        generatedFile = new File(filename);
        analyzer.file = generatedFile;
        assertNotNull(analyzer.file);
        
        analyzer.sheetName = "foo";

        analyzer.columns = new InputColumn<?>[2];
        analyzer.columns[0] = testColumn;
        analyzer.columns[1] = idColumn;

        analyzer.columnToBeSortedOn = idColumn;

        analyzer.validate();
        
        analyzer.init();

        InputRow[] rows = new InputRow[13];
        rows[0] = new MockInputRow().put(testColumn, "row00").put(idColumn, 7);
        rows[1] = new MockInputRow().put(testColumn, "row01").put(idColumn, 9);
        rows[2] = new MockInputRow().put(testColumn, "row02").put(idColumn, 2);
        rows[3] = new MockInputRow().put(testColumn, "row03").put(idColumn, 3);
        rows[4] = new MockInputRow().put(testColumn, "row04").put(idColumn, 4);
        rows[5] = new MockInputRow().put(testColumn, "row05").put(idColumn, 12);
        rows[6] = new MockInputRow().put(testColumn, "row06").put(idColumn, 6);
        rows[7] = new MockInputRow().put(testColumn, "row07").put(idColumn, 0);
        rows[8] = new MockInputRow().put(testColumn, "row08").put(idColumn, 8);
        rows[9] = new MockInputRow().put(testColumn, "row09").put(idColumn, 1);
        rows[10] = new MockInputRow().put(testColumn, "row10").put(idColumn, 10);
        rows[11] = new MockInputRow().put(testColumn, "row11").put(idColumn, 11);
        rows[12] = new MockInputRow().put(testColumn, "row12").put(idColumn, 5);

        for (int i = 0; i < rows.length; i++) {
            analyzer.run(rows[i], i);
        }

        analyzer.getResult();

        final List<Integer> resultIds = new ArrayList<>(13);
        ExcelDatastore outputDatastore = new ExcelDatastore(filename, new FileResource(analyzer.file), analyzer.file.getAbsolutePath());
        try (UpdateableDatastoreConnection outputDatastoreConnection = outputDatastore.openConnection()) {
            DataContext dataContext = outputDatastoreConnection.getDataContext();
            try (DataSet dataSet = dataContext.query().from("foo").selectAll().execute()) {
                while (dataSet.next()) {
                    Row row = dataSet.getRow();
                    Integer idValue = Integer.parseInt((String) row.getValue(1));
                    resultIds.add(idValue);
                }
            }
        }

        assertEquals("[0, 1, 10, 11, 12, 2, 3, 4, 5, 6, 7, 8, 9]", resultIds.toString());
    }
    
    @Test
    public void testCustomColumnHeaders() throws Exception {
        final String filename = "target/exceltest-customcolumnheaders.xlsx";

        CreateExcelSpreadsheetAnalyzer analyzer = new CreateExcelSpreadsheetAnalyzer();

        final InputColumn<String> stringColumn = new MockInputColumn<String>("StringColumn");
        final InputColumn<Integer> integerColumn = new MockInputColumn<Integer>("IntegerColumn");

        generatedFile = new File(filename);
        analyzer.file = generatedFile;
        analyzer.initTempFile();
        assertNotNull(analyzer.file);
        
        analyzer.sheetName = "foo";

        analyzer.columns = new InputColumn<?>[2];
        analyzer.columns[0] = stringColumn;
        analyzer.columns[1] = integerColumn;
        
//        analyzer.fields = new String[2];
//        analyzer.fields[0] = "CustomNameForStringColumn";
//        analyzer.fields[1] = "CustomNameForIntegerColumn";

        analyzer.init();

        InputRow[] rows = new InputRow[13];
        rows[0] = new MockInputRow().put(stringColumn, "row00").put(integerColumn, 7);
        rows[1] = new MockInputRow().put(stringColumn, "row01").put(integerColumn, 9);
        rows[2] = new MockInputRow().put(stringColumn, "row02").put(integerColumn, 2);
        rows[3] = new MockInputRow().put(stringColumn, "row03").put(integerColumn, 3);
        rows[4] = new MockInputRow().put(stringColumn, "row04").put(integerColumn, 4);
        rows[5] = new MockInputRow().put(stringColumn, "row05").put(integerColumn, 12);
        rows[6] = new MockInputRow().put(stringColumn, "row06").put(integerColumn, 6);
        rows[7] = new MockInputRow().put(stringColumn, "row07").put(integerColumn, 0);
        rows[8] = new MockInputRow().put(stringColumn, "row08").put(integerColumn, 8);
        rows[9] = new MockInputRow().put(stringColumn, "row09").put(integerColumn, 1);
        rows[10] = new MockInputRow().put(stringColumn, "row10").put(integerColumn, 10);
        rows[11] = new MockInputRow().put(stringColumn, "row11").put(integerColumn, 11);
        rows[12] = new MockInputRow().put(stringColumn, "row12").put(integerColumn, 5);

        for (int i = 0; i < rows.length; i++) {
            analyzer.run(rows[i], i);
        }

        analyzer.getResult();

        ExcelDatastore outputDatastore = new ExcelDatastore(filename, new FileResource(analyzer.file), analyzer.file.getAbsolutePath());
        try (UpdateableDatastoreConnection outputDatastoreConnection = outputDatastore.openConnection()) {
            String[] columnNames = outputDatastoreConnection.getSchemaNavigator().getDefaultSchema().getTableByName(analyzer.sheetName).getColumnNames();
            assertEquals(2, columnNames.length);
            assertEquals("CustomNameForStringColumn", columnNames[0]);
            assertEquals("CustomNameForIntegerColumn", columnNames[1]);
        }
    }
    
}
