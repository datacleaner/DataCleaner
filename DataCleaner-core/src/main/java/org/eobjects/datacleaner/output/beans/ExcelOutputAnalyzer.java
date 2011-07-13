/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.FileProperty;
import org.eobjects.analyzer.beans.api.FileProperty.FileAccessMode;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.output.OutputWriter;
import org.eobjects.datacleaner.output.excel.ExcelOutputWriterFactory;

@AnalyzerBean("Write to Excel spreadsheet")
@OutputWriterAnalyzer
public class ExcelOutputAnalyzer extends AbstractOutputWriterAnalyzer {

	@Configured
	@FileProperty(accessMode = FileAccessMode.SAVE, extension = { "xls", "xlsx" })
	File file = new File("DataCleaner-staging.xlsx");

	@Configured
	String sheetName;

	@Override
	public void configureForFilterOutcome(AnalysisJobBuilder ajb, FilterBeanDescriptor<?, ?> descriptor, String categoryName) {
		final String dsName = ajb.getDataContextProvider().getDatastore().getName();
		sheetName = "output-" + dsName + "-" + descriptor.getDisplayName() + "-" + categoryName;
	}

	@Override
	public void configureForTransformedData(AnalysisJobBuilder ajb, TransformerBeanDescriptor<?> descriptor) {
		final String dsName = ajb.getDataContextProvider().getDatastore().getName();
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
	protected OutputAnalyzerResult getResultInternal(int rowCount) {
		ExcelOutputAnalyzerResult result = new ExcelOutputAnalyzerResult(file, sheetName, rowCount);
		return result;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
}