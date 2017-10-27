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
package org.datacleaner.windows;

import java.util.Arrays;
import java.util.List;

import org.apache.metamodel.util.FileResource;
import org.datacleaner.bootstrap.DCWindowContext;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DomConfigurationWriter;
import org.datacleaner.connection.FixedWidthDatastore;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.user.UserPreferencesImpl;
import org.datacleaner.util.LookAndFeelManager;

public class FixedWidthDatastoreDialogTest {

    public static void main(String[] args) {
        LookAndFeelManager.get().init();

        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();
        final DomConfigurationWriter configurationWriter = new DomConfigurationWriter();
        final UserPreferences userPreferences = new UserPreferencesImpl(null);
        final MutableDatastoreCatalog mutableDatastoreCatalog =
                new MutableDatastoreCatalog(configuration.getDatastoreCatalog(), configurationWriter, userPreferences);
        final WindowContext windowContext = new DCWindowContext(configuration, userPreferences);

        final int[] valueWidths = { 6, 4, 12, 12 };
        final List<String> customColumnNames = Arrays.asList("Entry ID", "Period", "Post date", "Account number");

        final FixedWidthDatastore originalDatastore = new FixedWidthDatastore("My datastore",
                new FileResource("src/test/resources/example_fixed_width_file.txt"), "example_fixed_width_file.txt",
                "UTF8", valueWidths, true, true, true, 0, customColumnNames);

        final FixedWidthDatastoreDialog dialog = new FixedWidthDatastoreDialog(originalDatastore,
                mutableDatastoreCatalog, windowContext, configuration, userPreferences);
        dialog.open();
    }
}
