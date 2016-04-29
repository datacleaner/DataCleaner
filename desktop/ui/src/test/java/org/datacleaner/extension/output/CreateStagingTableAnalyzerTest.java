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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.user.UserPreferences;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CreateStagingTableAnalyzerTest {
    private static final String SAVE_DATASTORE_DIRECTORY = "C:\\Users\\tester\\.datacleaner\\5.0\\datastores";

    private static final String DATASTORE_NAME = "DataCleaner-staging";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private JdbcDatastore datastore;

    private DatastoreCatalog datastoreCatalog;

    private UserPreferences userPreferences;

    private CreateStagingTableAnalyzer createStagingTableAnalyzer;

    @Before
    public void setUp() {
        createStagingTableAnalyzer = new CreateStagingTableAnalyzer();

        datastore = mock(JdbcDatastore.class);
        datastoreCatalog = mock(DatastoreCatalog.class);
        userPreferences = mock(UserPreferences.class);

        createStagingTableAnalyzer.datastoreCatalog = datastoreCatalog;
        createStagingTableAnalyzer.userPreferences = userPreferences;

        when(datastoreCatalog.getDatastore(DATASTORE_NAME)).thenReturn(datastore);
        when(userPreferences.getSaveDatastoreDirectory()).thenReturn(new File(SAVE_DATASTORE_DIRECTORY));
        when(datastore.getDriverClass()).thenReturn(CreateStagingTableAnalyzer.H2_DRIVER_CLASS_NAME);
    }

    @Test
    public void testValidateNonExistingDatastore() {
        when(datastoreCatalog.getDatastore(DATASTORE_NAME)).thenReturn(null);
        
        createStagingTableAnalyzer.validate();
    }

    @Test
    public void testValidateCorrectH2Datastore() {
        when(datastore.getJdbcUrl()).thenReturn(CreateStagingTableAnalyzer.H2_DATABASE_CONNECTION_PROTOCOL
                + SAVE_DATASTORE_DIRECTORY + "\\" + DATASTORE_NAME + ";FILE_LOCK=FS");

        createStagingTableAnalyzer.validate();
    }

    @Test
    public void testValidateIncorrectH2Datastore() {
        when(datastore.getJdbcUrl()).thenReturn(CreateStagingTableAnalyzer.H2_DATABASE_CONNECTION_PROTOCOL
                + SAVE_DATASTORE_DIRECTORY.replace("5.0", "4.5") + "\\" + DATASTORE_NAME + ";FILE_LOCK=FS");

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Datastore \"" + DATASTORE_NAME
                + "\" is not located in \"Written datastores\" directory \"" + userPreferences
                .getSaveDatastoreDirectory().getPath() + "\".");
        
        createStagingTableAnalyzer.validate();
    }

    @Test
    public void testValidateNonH2Datastore() {
        when(datastore.getDriverClass()).thenReturn("");

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Datastore \"" + DATASTORE_NAME
                + "\" is not an H2 database, so it can't be used as a staging database.");

        createStagingTableAnalyzer.validate();
    }
}
