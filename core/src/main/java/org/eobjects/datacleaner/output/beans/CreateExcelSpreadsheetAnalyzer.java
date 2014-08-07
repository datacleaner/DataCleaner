/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.output.beans;

import java.io.File;

import org.eobjects.analyzer.beans.api.Alias;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Distributed;
import org.eobjects.analyzer.beans.api.FileProperty;
import org.eobjects.analyzer.beans.api.Validate;
import org.eobjects.analyzer.beans.api.FileProperty.FileAccessMode;
import org.eobjects.analyzer.beans.writers.WriteDataCategory;
import org.eobjects.analyzer.beans.writers.WriteDataResult;
import org.eobjects.analyzer.beans.writers.WriteDataResultImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.ExcelDatastore;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.output.OutputWriter;
import org.eobjects.datacleaner.output.excel.ExcelOutputWriterFactory;
import org.apache.metamodel.util.FileResource;

@AnalyzerBean("Create Excel spreadsheet")
@Alias("Write to Excel spreadsheet")
@Description("Write data to an Excel spreadsheet, useful for manually editing and inspecting the data in Microsoft Excel.")
@Categorized(WriteDataCategory.class)
@Distributed(false)
public class CreateExcelSpreadsheetAnalyzer extends AbstractOutputWriterAnalyzer {

	@Configured
	@FileProperty(accessMode = FileAccessMode.SAVE, extension = { "xls", "xlsx" })
	File file = new File("DataCleaner-staging.xlsx");

	@Configured
	String sheetName;

	@Validate
	public void validate() {
		if (sheetName.indexOf(".") != -1) {
			throw new IllegalStateException("Sheet name cannot contain dots (.)");
		}
	}

	@Override
	public void configureForFilterOutcome(AnalysisJobBuilder ajb, FilterBeanDescriptor<?, ?> descriptor, String categoryName) {
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