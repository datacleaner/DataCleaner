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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.metamodel.schema.ColumnType;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.bootstrap.SimpleWindowContext;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.configuration.SourceColumnMapping;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.job.AnalysisJobMetadata;
import org.datacleaner.job.ImmutableAnalysisJobMetadata;
import org.datacleaner.test.TestHelper;
import org.datacleaner.util.VFSUtils;
import org.datacleaner.widgets.LoadingIcon;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;
import com.google.inject.util.Providers;

public class OpenAnalysisJobAsTemplateDialogTest {
    private static final Logger logger = LoggerFactory.getLogger(OpenAnalysisJobAsTemplateDialogTest.class);
    private OpenAnalysisJobAsTemplateDialog _dialog;

    @Test
    public void testCreateLoadingIcon() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            assertTrue(OpenAnalysisJobAsTemplateDialog.createLoadingIcon() instanceof LoadingIcon);
        }
    }

    @Test
    public void testRefreshOpenButtonVisibility() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            getDialog().refreshOpenButtonVisibility();
        }
    }

    @Test
    public void testGetSourceColumnMapping() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            assertTrue(getDialog().getSourceColumnMapping() instanceof SourceColumnMapping);
        }
    }

    @Test
    public void testGetBannerTitle() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            assertEquals("Open as template", getDialog().getBannerTitle());
        }
    }

    @Test
    public void testGetDialogWidth() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            assertEquals(600, getDialog().getDialogWidth());
        }
    }

    @Test
    public void testIsWindowResizable() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            assertEquals(true, getDialog().isWindowResizable());
        }
    }

    @Test
    public void testGetDialogContent() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            assertTrue(getDialog().getDialogContent() instanceof JScrollPane);
        }
    }

    @Test
    public void testGetWindowTitle() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            assertEquals("Open analysis job as template", getDialog().getWindowTitle());
        }
    }

    private OpenAnalysisJobAsTemplateDialog getDialog() {
        if (_dialog == null) {
            try {
                final WindowContext windowContext = new SimpleWindowContext();
                final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
                final DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(datastore);
                final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
                final DataCleanerConfiguration configuration =
                        new DataCleanerConfigurationImpl().withDatastoreCatalog(datastoreCatalog).withEnvironment(
                                new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));
                final FileObject file = VFSUtils.getFileSystemManager()
                        .resolveFile("src/test/resources/cli-examples/date_analysis_job.xml");
                final Provider<OpenAnalysisJobActionListener> providerMock = Providers.of(null);

                _dialog = new OpenAnalysisJobAsTemplateDialog(windowContext, configuration, file, createMetadata(),
                        providerMock);
            } catch (final FileSystemException e) {
                logger.error("Problem occurred while creating dialog instance. \n" + e.getMessage());
            }
        }

        return _dialog;
    }

    private AnalysisJobMetadata createMetadata() {
        final String jobName = "job-name";
        final String jobVersion = "job-version";
        final String jobDescription = "job-description";
        final String author = "author";
        final Date createdDate = new Date();
        final Date updatedDate = new Date();
        final String datastoreName = "orderdb";
        final List<String> sourceColumnPaths = new ArrayList<>();
        final List<ColumnType> sourceColumnTypes = new ArrayList<>();
        final Map<String, String> variables = new HashMap<>();
        final Map<String, String> properties = new HashMap<>();

        return new ImmutableAnalysisJobMetadata(jobName, jobVersion, jobDescription, author, createdDate, updatedDate,
                datastoreName, sourceColumnPaths, sourceColumnTypes, variables, properties);
    }
}
