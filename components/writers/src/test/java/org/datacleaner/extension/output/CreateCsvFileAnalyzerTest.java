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

import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.util.FileResource;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.junit.After;
import org.junit.Test;

public class CreateCsvFileAnalyzerTest {

    private CreateCsvFileAnalyzer analyzer;

    @After
    public void tearDown() {
        if ((analyzer != null) && (analyzer.file != null)) {
            ((FileResource) analyzer.file).getFile().delete();
        }
    }

    @Test
    public void test() throws Exception {

        analyzer = new CreateCsvFileAnalyzer();

        analyzer.file = new FileResource("target/csvtest.csv");
        analyzer.initTempFile();
        assertNotNull(analyzer.file);
        // Case 1 - file does not exists
        assertFalse(analyzer.file.isExists());
        assertEquals("csvtest.csv", analyzer.getSuggestedLabel());
        analyzer.overwriteFileIfExists = false;
        analyzer.validate();

        analyzer.overwriteFileIfExists = true;
        analyzer.validate();

        // Case 2 - file exists
        final boolean createNewFile = ((FileResource) analyzer.file).getFile().createNewFile();
        assertTrue(createNewFile);

        try {
            assertTrue(analyzer.file.isExists());
            analyzer.overwriteFileIfExists = false;
            analyzer.validate();
            fail("Exception expected");
        } catch (final Exception e) {
            assertEquals("The file already exists. Please configure the job to overwrite the existing file.",
                    e.getMessage());

        }

        assertTrue(analyzer.file.isExists());
        analyzer.overwriteFileIfExists = true;
        analyzer.validate();

        ((FileResource) analyzer.file).getFile().delete();
        assertFalse(analyzer.file.isExists());

    }

    @Test
    public void testSortNumerical() throws Exception {
        analyzer = new CreateCsvFileAnalyzer();

        final InputColumn<String> testColumn = new MockInputColumn<>("TestColumn");
        final InputColumn<Integer> idColumn = new MockInputColumn<>("IdToSort", Integer.class);

        analyzer.file = new FileResource("target/csvtest-sortnumerical.csv");
        analyzer.initTempFile();
        assertNotNull(analyzer.file);
        final String targetFilename = analyzer.file.getName();

        analyzer.columns = new InputColumn<?>[2];
        analyzer.columns[0] = testColumn;
        analyzer.columns[1] = idColumn;

        analyzer.columnToBeSortedOn = idColumn;

        analyzer.init();

        final InputRow[] rows = new InputRow[13];
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
        final CsvDatastore outputDatastore = new CsvDatastore("csvtest-sortnumerical", analyzer.file);
        try (UpdateableDatastoreConnection outputDatastoreConnection = outputDatastore.openConnection()) {
            final DataContext dataContext = outputDatastoreConnection.getDataContext();
            try (DataSet dataSet = dataContext.query().from(targetFilename).selectAll().execute()) {
                while (dataSet.next()) {
                    final Row row = dataSet.getRow();
                    final Integer idValue = Integer.parseInt((String) row.getValue(1));
                    resultIds.add(idValue);
                }
            }
        }

        assertEquals("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]", resultIds.toString());
    }

    @Test
    public void testSortLexicographic() throws Exception {
        final CreateCsvFileAnalyzer analyzer = new CreateCsvFileAnalyzer();

        final InputColumn<String> testColumn = new MockInputColumn<>("TestColumn");
        final InputColumn<String> idColumn = new MockInputColumn<>("IdToSort", String.class);

        analyzer.file = new FileResource("target/csvtest-sortnumerical.csv");
        analyzer.initTempFile();
        assertNotNull(analyzer.file);
        final String targetFilename = analyzer.file.getName();

        analyzer.columns = new InputColumn<?>[2];
        analyzer.columns[0] = testColumn;
        analyzer.columns[1] = idColumn;

        analyzer.columnToBeSortedOn = idColumn;

        analyzer.init();

        final InputRow[] rows = new InputRow[13];
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

        final List<String> resultIds = new ArrayList<>(13);
        final CsvDatastore outputDatastore = new CsvDatastore("csvtest-sortnumerical", analyzer.file);
        try (UpdateableDatastoreConnection outputDatastoreConnection = outputDatastore.openConnection()) {
            final DataContext dataContext = outputDatastoreConnection.getDataContext();
            try (DataSet dataSet = dataContext.query().from(targetFilename).selectAll().execute()) {
                while (dataSet.next()) {
                    final Row row = dataSet.getRow();
                    final String idValue = (String) row.getValue(1);
                    resultIds.add(idValue);
                }
            }
        }

        assertEquals("[0, 1, 10, 11, 12, 2, 3, 4, 5, 6, 7, 8, 9]", resultIds.toString());
    }

    @Test
    public void testSortLexicographicCaseSensitivity() throws Exception {
        final CreateCsvFileAnalyzer analyzer = new CreateCsvFileAnalyzer();

        final InputColumn<String> sortColumn = new MockInputColumn<>("SortColumn");
        final InputColumn<String> someColumn = new MockInputColumn<>("SomeColumn", String.class);

        analyzer.file = new FileResource("target/csvtest-sortlexicographiccasesensitivity.csv");
        analyzer.initTempFile();
        assertNotNull(analyzer.file);
        final String targetFilename = analyzer.file.getName();

        analyzer.columns = new InputColumn<?>[2];
        analyzer.columns[0] = sortColumn;
        analyzer.columns[1] = someColumn;

        analyzer.columnToBeSortedOn = sortColumn;

        analyzer.init();

        final InputRow[] rows = new InputRow[8];
        rows[0] = new MockInputRow().put(sortColumn, "Claudia").put(someColumn, 1);
        rows[1] = new MockInputRow().put(sortColumn, "Dennis").put(someColumn, 2);
        rows[2] = new MockInputRow().put(sortColumn, "Kasper").put(someColumn, 3);
        rows[3] = new MockInputRow().put(sortColumn, "Tomasz").put(someColumn, 4);
        rows[4] = new MockInputRow().put(sortColumn, "claudia").put(someColumn, 5);
        rows[5] = new MockInputRow().put(sortColumn, "dennis").put(someColumn, 6);
        rows[6] = new MockInputRow().put(sortColumn, "kasper").put(someColumn, 7);
        rows[7] = new MockInputRow().put(sortColumn, "tomasz").put(someColumn, 8);

        for (int i = 0; i < rows.length; i++) {
            analyzer.run(rows[i], i);
        }

        analyzer.getResult();

        final List<String> resultIds = new ArrayList<>(13);
        final CsvDatastore outputDatastore =
                new CsvDatastore("csvtest-sortlexicographiccasesensitivity", analyzer.file);
        try (UpdateableDatastoreConnection outputDatastoreConnection = outputDatastore.openConnection()) {
            final DataContext dataContext = outputDatastoreConnection.getDataContext();
            try (DataSet dataSet = dataContext.query().from(targetFilename).selectAll().execute()) {
                while (dataSet.next()) {
                    final Row row = dataSet.getRow();
                    final String idValue = (String) row.getValue(0);
                    resultIds.add(idValue);
                }
            }
        }

        assertEquals("[Claudia, claudia, Dennis, dennis, Kasper, kasper, Tomasz, tomasz]", resultIds.toString());
    }

    @Test
    public void testCustomColumnHeaders() throws Exception {
        final CreateCsvFileAnalyzer analyzer = new CreateCsvFileAnalyzer();

        final InputColumn<String> stringColumn = new MockInputColumn<>("StringColumn");
        final InputColumn<Integer> integerColumn = new MockInputColumn<>("IntegerColumn");

        analyzer.file = new FileResource("target/csvtest-customcolumnheaders.csv");
        analyzer.initTempFile();
        assertNotNull(analyzer.file);
        final String targetFilename = analyzer.file.getName();

        analyzer.columns = new InputColumn<?>[2];
        analyzer.columns[0] = stringColumn;
        analyzer.columns[1] = integerColumn;

        analyzer.fields = new String[2];
        analyzer.fields[0] = "CustomNameForStringColumn";
        analyzer.fields[1] = "CustomNameForIntegerColumn";

        analyzer.init();

        final InputRow[] rows = new InputRow[13];
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

        final CsvDatastore outputDatastore = new CsvDatastore("csvtest-customcolumnheaders", analyzer.file);
        try (UpdateableDatastoreConnection outputDatastoreConnection = outputDatastore.openConnection()) {
            final List<String> columnNames =
                    outputDatastoreConnection.getSchemaNavigator().getDefaultSchema().getTableByName(targetFilename)
                            .getColumnNames();
            assertEquals(2, columnNames.size());
            assertEquals("CustomNameForStringColumn", columnNames.get(0));
            assertEquals("CustomNameForIntegerColumn", columnNames.get(1));
        }
    }

}
