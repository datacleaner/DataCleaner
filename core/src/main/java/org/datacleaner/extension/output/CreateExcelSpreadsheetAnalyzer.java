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

import org.datacleaner.beans.api.Alias;
import org.datacleaner.beans.api.AnalyzerBean;
import org.datacleaner.beans.api.Categorized;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Description;
import org.datacleaner.beans.api.Distributed;
import org.datacleaner.beans.api.FileProperty;
import org.datacleaner.beans.api.Validate;
import org.datacleaner.beans.api.FileProperty.FileAccessMode;
import org.datacleaner.beans.writers.WriteDataCategory;
import org.datacleaner.beans.writers.WriteDataResult;
import org.datacleaner.beans.writers.WriteDataResultImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.descriptors.FilterBeanDescriptor;
import org.datacleaner.descriptors.TransformerBeanDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.util.HasLabelAdvice;
import org.datacleaner.output.OutputWriter;
import org.datacleaner.output.excel.ExcelOutputWriterFactory;
import org.apache.metamodel.util.FileResource;

@AnalyzerBean("Create Excel spreadsheet")
@Alias("Write to Excel spreadsheet")
@Description("Write data to an Excel spreadsheet, useful for manually editing and inspecting the data in Microsoft Excel.")
@Categorized(WriteDataCategory.class)
@Distributed(false)
public class CreateExcelSpreadsheetAnalyzer extends AbstractOutputWriterAnalyzer implements HasLabelAdvice {

    @Configured
    @FileProperty(accessMode = FileAccessMode.SAVE, extension = { "xls", "xlsx" })
    File file = new File("DataCleaner-staging.xlsx");

    @Configured
    String sheetName;

    @Override
    public String getSuggestedLabel() {
        if (file == null || sheetName == null) {
            return null;
        }
        return file.getName() + " - " + sheetName;
    }

    @Validate
    public void validate() {
        if (sheetName.indexOf(".") != -1) {
            throw new IllegalStateException("Sheet name cannot contain dots (.)");
        }
    }

    @Override
    public void configureForFilterOutcome(AnalysisJobBuilder ajb, FilterBeanDescriptor<?, ?> descriptor,
            String categoryName) {
        final String dsName = ajb.getDatastoreConnection().getDatastore().getName();
        sheetName = "output-" + dsName + "-" + descriptor.getDisplayName() + "-" + categoryName;
    }

    @Override
    public void configureForTransformedData(AnalysisJobBuilder ajb, TransformerBeanDescriptor<?> descriptor) {
        final String dsName = ajb.getDatastoreConnection().getDatastore().getName();
        sheetName = "output-" + dsName + "-" + descriptor.getDisplayName();
    }

    @Override
    public OutputWriter createOutputWriter() {
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
