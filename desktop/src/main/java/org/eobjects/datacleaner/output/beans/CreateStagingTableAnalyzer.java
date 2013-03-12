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

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Alias;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Distributed;
import org.eobjects.analyzer.beans.writers.WriteDataCategory;
import org.eobjects.analyzer.beans.writers.WriteDataResult;
import org.eobjects.analyzer.beans.writers.WriteDataResultImpl;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.output.OutputWriter;
import org.eobjects.datacleaner.output.datastore.DatastoreCreationDelegate;
import org.eobjects.datacleaner.output.datastore.DatastoreCreationDelegateImpl;
import org.eobjects.datacleaner.output.datastore.DatastoreOutputWriterFactory;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.user.UserPreferences;

@AnalyzerBean("Create staging table")
@Alias("Write to Datastore")
@Description("Write data to DataCleaner's embedded staging database (based on H2), which provides a convenient location for staging data or simply storing data temporarily for further analysis.")
@Categorized(WriteDataCategory.class)
@Distributed(false)
public class CreateStagingTableAnalyzer extends AbstractOutputWriterAnalyzer {

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

	@Inject
	UserPreferences userPreferences;

	@Inject
	DatastoreCatalog datastoreCatalog;

	@Override
	public void configureForFilterOutcome(AnalysisJobBuilder ajb, FilterBeanDescriptor<?, ?> descriptor, String categoryName) {
		final String dsName = ajb.getDatastoreConnection().getDatastore().getName();
		tableName = "output-" + dsName + "-" + descriptor.getDisplayName() + "-" + categoryName;
	}

	@Override
	public void configureForTransformedData(AnalysisJobBuilder ajb, TransformerBeanDescriptor<?> descriptor) {
		final String dsName = ajb.getDatastoreConnection().getDatastore().getName();
		tableName = "output-" + dsName + "-" + descriptor.getDisplayName();
	}

	@Override
	public OutputWriter createOutputWriter() {
		final String[] headers = new String[columns.length];
		for (int i = 0; i < headers.length; i++) {
			headers[i] = columns[i].getName();
		}

		final boolean truncate = (writeMode == WriteMode.TRUNCATE);
		final DatastoreCreationDelegate creationDelegate = new DatastoreCreationDelegateImpl(
				(MutableDatastoreCatalog) datastoreCatalog);

		final OutputWriter outputWriter = DatastoreOutputWriterFactory.getWriter(
				userPreferences.getSaveDatastoreDirectory(), creationDelegate, datastoreName, tableName, truncate, columns);

		// update the tablename property with the actual name (whitespace
		// escaped etc.)
		tableName = DatastoreOutputWriterFactory.getActualTableName(outputWriter);
		return outputWriter;
	}

	@Override
	protected WriteDataResult getResultInternal(int rowCount) {
		WriteDataResult result = new WriteDataResultImpl(rowCount, datastoreName, null, tableName);
		return result;
	}

	public String getDatastoreName() {
		return datastoreName;
	}

	public void setDatastoreName(String datastoreName) {
		this.datastoreName = datastoreName;
	}
}