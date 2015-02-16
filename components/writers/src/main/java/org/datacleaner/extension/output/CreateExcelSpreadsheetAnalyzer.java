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

import javax.inject.Named;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.drop.DropTable;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileResource;
import org.datacleaner.api.Alias;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.FileProperty;
import org.datacleaner.api.FileProperty.FileAccessMode;
import org.datacleaner.api.HasLabelAdvice;
import org.datacleaner.api.Validate;
import org.datacleaner.beans.writers.WriteDataResult;
import org.datacleaner.beans.writers.WriteDataResultImpl;
import org.datacleaner.components.categories.WriteSuperCategory;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.output.OutputWriter;
import org.datacleaner.output.excel.ExcelOutputWriterFactory;

@Named("Create Excel spreadsheet")
@Alias("Write to Excel spreadsheet")
@Description("Write data to an Excel spreadsheet, useful for manually editing and inspecting the data in Microsoft Excel.")
@Categorized(superCategory = WriteSuperCategory.class)
@Distributed(false)
public class CreateExcelSpreadsheetAnalyzer extends AbstractOutputWriterAnalyzer implements HasLabelAdvice {

    private static final char[] ILLEGAL_SHEET_CHARS = new char[] { '.', ':' };

    @Configured
    @FileProperty(accessMode = FileAccessMode.SAVE, extension = { "xls", "xlsx" })
    File file = new File("DataCleaner-staging.xlsx");

    @Configured
    String sheetName;

    @Configured
    boolean overwriteSheetIfExists;

    @Override
    public String getSuggestedLabel() {
        if (file == null || sheetName == null) {
            return null;
        }
        return file.getName() + " - " + sheetName;
    }

    @Validate
    public void validate() {
        for (char c : ILLEGAL_SHEET_CHARS) {
            if (sheetName.indexOf(c) != -1) {
                throw new IllegalStateException("Sheet name cannot contain '" + c + "'");
            }
        }

        if (file.exists()) {
            Datastore datastore = new ExcelDatastore(file.getName(), new FileResource(file), file.getAbsolutePath());
            try (final DatastoreConnection connection = datastore.openConnection()) {
                final String[] tableNames = connection.getDataContext().getDefaultSchema().getTableNames();
                for (int i = 0; i < tableNames.length; i++) {
                    if (tableNames[i].equals(sheetName)) {
                        if (!overwriteSheetIfExists) {
                            throw new IllegalStateException("The sheet '" + sheetName
                                    + "' already exists. Please select another sheet name.");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void configureForFilterOutcome(AnalysisJobBuilder ajb, FilterDescriptor<?, ?> descriptor, String categoryName) {
        final String dsName = ajb.getDatastore().getName();
        sheetName = fixSheetName("output-" + dsName + "-" + descriptor.getDisplayName() + "-" + categoryName);
    }

    @Override
    public void configureForTransformedData(AnalysisJobBuilder ajb, TransformerDescriptor<?> descriptor) {
        final String dsName = ajb.getDatastore().getName();
        sheetName = fixSheetName("output-" + dsName + "-" + descriptor.getDisplayName());
    }

    private String fixSheetName(String sheet) {
        for (char c : ILLEGAL_SHEET_CHARS) {
            while (sheet.indexOf(c) != -1) {
                sheet = sheet.replace(c, '-');
            }
        }
        return sheet;
    }

    @Override
    public OutputWriter createOutputWriter() {
        if (file.exists()) {
            ExcelDatastore datastore = new ExcelDatastore(file.getName(), new FileResource(file),
                    file.getAbsolutePath());
            try (final UpdateableDatastoreConnection connection = datastore.openConnection()) {
                final DataContext dataContext = connection.getDataContext();
                final String[] tableNames = dataContext.getDefaultSchema().getTableNames();
                for (int i = 0; i < tableNames.length; i++) {
                    if (tableNames[i].equals(sheetName)) {
                        if (overwriteSheetIfExists) {
                            final Table tableSheet = dataContext.getTableByQualifiedLabel(sheetName);
                            final UpdateableDataContext updateableDataContext = connection.getUpdateableDataContext();
                            updateableDataContext.executeUpdate(new DropTable(tableSheet));
                        }
                    }
                }
            }
        }

        String[] headers = new String[columns.length];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = columns[i].getName();
        }
        return ExcelOutputWriterFactory.getWriter(file.getPath(), sheetName, columns);
    }

    @Override
    protected WriteDataResult getResultInternal(int rowCount) {
        Datastore datastore = new ExcelDatastore(file.getName(), new FileResource(file), file.getAbsolutePath());
        WriteDataResult result = new WriteDataResultImpl(rowCount, datastore, null, sheetName);
        return result;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

}
