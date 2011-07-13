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

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.output.OutputWriter;
import org.eobjects.datacleaner.output.datastore.DatastoreOutputWriterFactory;

@AnalyzerBean("Write to Datastore")
@OutputWriterAnalyzer
public class DatastoreOutputAnalyzer extends AbstractOutputWriterAnalyzer {

	/**
	 * Write mode for the datastore output analyzer. Determines if the datastore
	 * will be truncated before writing data or if a new/separate table should
	 * be created for this output.
	 */
	public static enum WriteMode {
		TRUNCATE, NEW_TABLE
	}

	@Configured(order = 1)
	String datastoreName = "DataCleaner-staging";

	@Configured(order = 2)
	String tableName;

	@Configured(order = 3)
	@Description("Determines the behaviour in case of there's an existing datastore and table with the given names.")
	WriteMode writeMode = WriteMode.TRUNCATE;

	@Override
	public void configureForFilterOutcome(AnalysisJobBuilder ajb, FilterBeanDescriptor<?, ?> descriptor, String categoryName) {
		final String dsName = ajb.getDataContextProvider().getDatastore().getName();
		tableName = "output-" + dsName + "-" + descriptor.getDisplayName() + "-" + categoryName;
	}
	
	@Override
	public void configureForTransformedData(AnalysisJobBuilder ajb, TransformerBeanDescriptor<?> descriptor) {
		final String dsName = ajb.getDataContextProvider().getDatastore().getName();
		tableName = "output-" + dsName + "-" + descriptor.getDisplayName();
	}

	@Override
	public OutputWriter createOutputWriter() {
		String[] headers = new String[columns.length];
		for (int i = 0; i < headers.length; i++) {
			headers[i] = columns[i].getName();
		}

		boolean truncate = (writeMode == WriteMode.TRUNCATE);
		return DatastoreOutputWriterFactory.getWriter(datastoreName, tableName, truncate, columns);
	}

	@Override
	protected OutputAnalyzerResult getResultInternal(int rowCount) {
		return new DatastoreOutputAnalyzerResult(rowCount, datastoreName, tableName);
	}

	public String getDatastoreName() {
		return datastoreName;
	}

	public void setDatastoreName(String datastoreName) {
		this.datastoreName = datastoreName;
	}
}