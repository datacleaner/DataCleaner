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

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Alias;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.HasLabelAdvice;
import org.datacleaner.api.Validate;
import org.datacleaner.beans.writers.WriteDataResult;
import org.datacleaner.beans.writers.WriteDataResultImpl;
import org.datacleaner.components.categories.WriteSuperCategory;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.output.OutputWriter;
import org.datacleaner.output.datastore.DatastoreCreationDelegate;
import org.datacleaner.output.datastore.DatastoreCreationDelegateImpl;
import org.datacleaner.output.datastore.DatastoreOutputWriterFactory;
import org.datacleaner.user.UserPreferences;

@Named("Create staging table")
@Alias("Write to Datastore")
@Description("Write data to DataCleaner's embedded staging database (based on H2), which provides a convenient "
        + "location for staging data or simply storing data temporarily for further analysis.")
@Categorized(superCategory = WriteSuperCategory.class)
@Distributed(false)
public class CreateStagingTableAnalyzer extends AbstractOutputWriterAnalyzer implements HasLabelAdvice {

    /**
     * Write mode for the datastore output analyzer. Determines if the datastore
     * will be truncated before writing data or if a new/separate table should
     * be created for this output.
     */
    public enum WriteMode {
        TRUNCATE, NEW_TABLE
    }

    static final String H2_DATABASE_CONNECTION_PROTOCOL = "jdbc:h2:";
    static final String H2_DRIVER_CLASS_NAME = "org.h2.Driver";
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
    public void configureForFilterOutcome(final AnalysisJobBuilder ajb, final FilterDescriptor<?, ?> descriptor,
            final String categoryName) {
        final String dsName = ajb.getDatastoreConnection().getDatastore().getName();
        tableName = "output-" + dsName + "-" + descriptor.getDisplayName() + "-" + categoryName;
    }

    @Override
    public void configureForTransformedData(final AnalysisJobBuilder ajb, final TransformerDescriptor<?> descriptor) {
        final String dsName = ajb.getDatastoreConnection().getDatastore().getName();
        tableName = "output-" + dsName + "-" + descriptor.getDisplayName();
    }

    @Override
    public String getSuggestedLabel() {
        if (datastoreName == null || tableName == null) {
            return null;
        }
        return datastoreName + " - " + tableName;
    }

    @Override
    public OutputWriter createOutputWriter() {
        final String[] headers = new String[columns.length];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = columns[i].getName();
        }

        final boolean truncate = (writeMode == WriteMode.TRUNCATE);
        final DatastoreCreationDelegate creationDelegate = new DatastoreCreationDelegateImpl(datastoreCatalog);

        final File saveDatastoreDirectory =
                (userPreferences == null ? new File("datastores") : userPreferences.getSaveDatastoreDirectory());
        final OutputWriter outputWriter = DatastoreOutputWriterFactory
                .getWriter(saveDatastoreDirectory, creationDelegate, datastoreName, tableName, truncate, columns);

        // update the tablename property with the actual name (whitespace
        // escaped etc.)
        tableName = DatastoreOutputWriterFactory.getActualTableName(outputWriter);
        return outputWriter;
    }

    @Override
    protected WriteDataResult getResultInternal(final int rowCount) {
        return new WriteDataResultImpl(rowCount, datastoreName, null, tableName);
    }

    public String getDatastoreName() {
        return datastoreName;
    }

    public void setDatastoreName(final String datastoreName) {
        this.datastoreName = datastoreName;
    }

    @Validate
    public void validate() {
        // The first time this method is invoked, the datastoreCatalog and
        // userPreferences fields haven't been populated yet, therefore we just
        // skip the check, because it is called a little bit later again, and
        // then these fields are populated.
        if (datastoreCatalog != null) {
            // Validate that the datastoreName doesn't conflict with one of the
            // datastores in the datastoreCatalog.
            final Datastore datastore = datastoreCatalog.getDatastore(datastoreName);

            if (datastore != null) {
                if (datastore instanceof JdbcDatastore && ((JdbcDatastore) datastore).getDriverClass()
                        .equals(H2_DRIVER_CLASS_NAME)) {
                    if (!((JdbcDatastore) datastore).getJdbcUrl().startsWith(
                            H2_DATABASE_CONNECTION_PROTOCOL + userPreferences.getSaveDatastoreDirectory().getPath())) {
                        throw new IllegalStateException("Datastore \"" + datastoreName
                                + "\" is not located in \"Written datastores\" directory \"" + userPreferences
                                .getSaveDatastoreDirectory().getPath() + "\".");
                    }
                } else {
                    throw new IllegalStateException("Datastore \"" + datastoreName
                            + "\" is not an H2 database, so it can't be used as a staging database.");
                }
            }
        }
    }
}
