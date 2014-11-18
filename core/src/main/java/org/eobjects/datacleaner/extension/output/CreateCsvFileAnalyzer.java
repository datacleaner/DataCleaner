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
package org.eobjects.datacleaner.extension.output;

import java.io.File;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Alias;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Distributed;
import org.eobjects.analyzer.beans.api.FileProperty;
import org.eobjects.analyzer.beans.api.FileProperty.FileAccessMode;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.beans.writers.WriteDataCategory;
import org.eobjects.analyzer.beans.writers.WriteDataResult;
import org.eobjects.analyzer.beans.writers.WriteDataResultImpl;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.output.OutputWriter;
import org.eobjects.datacleaner.output.csv.CsvOutputWriterFactory;
import org.eobjects.datacleaner.user.UserPreferences;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;

@AnalyzerBean("Create CSV file")
@Alias("Write to CSV file")
@Description("Write data to a CSV file on your harddrive. CSV file writing is extremely fast and the file format is commonly used in many tools. But CSV files do not preserve data types.")
@Categorized(WriteDataCategory.class)
@Distributed(false)
public class CreateCsvFileAnalyzer extends AbstractOutputWriterAnalyzer {

    @Configured(order = 1)
    @FileProperty(accessMode = FileAccessMode.SAVE, extension = { "csv", "tsv", "txt", "dat" })
    File file;

    @Configured(order = 2, required = false)
    Character separatorChar = ',';

    @Configured(order = 3, required = false)
    Character quoteChar = '"';

    @Configured(order = 4, required = false)
    Character escapeChar = '\\';

    @Configured(order = 5, required = false)
    boolean includeHeader = true;

    @Inject
    @Provided
    UserPreferences userPreferences;

    @Override
    public void configureForFilterOutcome(AnalysisJobBuilder ajb, FilterBeanDescriptor<?, ?> descriptor,
            String categoryName) {
        final String dsName = ajb.getDatastore().getName();
        final File saveDatastoreDirectory = userPreferences.getSaveDatastoreDirectory();
        final String displayName = descriptor.getDisplayName();
        file = new File(saveDatastoreDirectory, "output-" + dsName + "-" + displayName + "-" + categoryName + ".csv");
    }

    @Override
    public void configureForTransformedData(AnalysisJobBuilder ajb, TransformerBeanDescriptor<?> descriptor) {
        final String dsName = ajb.getDatastore().getName();
        final File saveDatastoreDirectory = userPreferences.getSaveDatastoreDirectory();
        final String displayName = descriptor.getDisplayName();
        file = new File(saveDatastoreDirectory, "output-" + dsName + "-" + displayName + ".csv");
    }

    @Override
    public OutputWriter createOutputWriter() {
        final String[] headers = new String[columns.length];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = columns[i].getName();
        }
        return CsvOutputWriterFactory.getWriter(file.getPath(), headers, separatorChar, quoteChar, escapeChar,
                includeHeader, columns);
    }

    @Override
    protected WriteDataResult getResultInternal(int rowCount) {
        final Resource resource = new FileResource(file);
        final Datastore datastore = new CsvDatastore(file.getName(), resource, file.getAbsolutePath(), quoteChar,
                separatorChar, escapeChar, FileHelper.DEFAULT_ENCODING, false, true,
                CsvConfiguration.DEFAULT_COLUMN_NAME_LINE);
        return new WriteDataResultImpl(rowCount, datastore, null, null);
    }

    public void setFile(File file) {
        this.file = file;
    }
}