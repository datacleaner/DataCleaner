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
package org.eobjects.datacleaner.user;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.eobjects.analyzer.util.ClassLoaderUtils;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.analyzer.util.VFSUtils;
import org.eobjects.datacleaner.Version;
import org.eobjects.datacleaner.util.ResourceManager;
import org.eobjects.datacleaner.util.SystemProperties;
import org.apache.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Incapsulation of the DATACLEANER_HOME folder. This folder is resolved using
 * the following ordered approach:
 * 
 * <ol>
 * <li>If a DATACLEANER_HOME environment variable exists, it will be used.</li>
 * <li>If the application is running in Java WebStart mode, a sandbox folder
 * will be used.</li>
 * <li>If none of the above, the current folder "." will be used.</li>
 * </ol>
 */
public final class DataCleanerHome {
    
    public static final String JOB_EXAMPLE_CUSTOMER_PROFILING = "jobs/Customer profiling.analysis.xml";
    public static final String JOB_EXAMPLE_SFDC_DUPLICATE_DETECTION = "jobs/Salesforce duplicate detection.analysis.xml";
    public static final String JOB_EXAMPLE_SFDC_DUPLICATE_TRAINING = "jobs/Salesforce dedup training.analysis.xml";
    public static final String JOB_EXAMPLE_ADDRESS_CLEANSING = "jobs/Address cleansing with EasyDQ.analysis.xml";
    public static final String JOB_EXAMPLE_PHONE_CLEANSING = "jobs/Phone number analysis with EasyDQ.analysis.xml";
    public static final String JOB_EXAMPLE_EXPORT_ORDERS_DATA = "jobs/Export of Orders data mart.analysis.xml";
    public static final String JOB_EXAMPLE_COPY_EMPLOYEES_TO_CUSTOMERS = "jobs/Copy employees to customer table.analysis.xml";

    private static final Logger logger = LoggerFactory.getLogger(DataCleanerHome.class);
    private static final FileObject _dataCleanerHome;

    static {
        try {
            _dataCleanerHome = findDataCleanerHome();
        } catch (FileSystemException e) {
            throw new IllegalStateException(e);
        }
    }

    private static FileObject findDataCleanerHome() throws FileSystemException {
        final FileSystemManager manager = VFSUtils.getFileSystemManager();

        FileObject candidate = null;

        final String env = System.getenv("DATACLEANER_HOME");
        if (!StringUtils.isNullOrEmpty(env)) {
            candidate = manager.resolveFile(env);
            logger.info("Resolved env. variable DATACLEANER_HOME ({}) to: {}", env, candidate);
        }

        if (isUsable(candidate)) {
            return candidate;
        }

        if (ClassLoaderUtils.IS_WEB_START) {
            // in web start, the default folder will be in user.home
            final String userHomePath = System.getProperty("user.home");
            if (userHomePath == null) {
                throw new IllegalStateException("Could not determine user home directory: " + candidate);
            }

            final String path = userHomePath + File.separatorChar + ".datacleaner" + File.separatorChar + Version.getVersion();
            candidate = manager.resolveFile(path);
            logger.info("Running in WebStart mode. Attempting to build DATACLEANER_HOME in user.home: {} -> {}", path,
                    candidate);
        } else {
            // in normal mode, the default folder will be in the working
            // directory
            candidate = manager.resolveFile(".");
            logger.info("Running in standard mode. Attempting to build DATACLEANER_HOME in '.' -> {}", candidate);
        }

        if ("true".equalsIgnoreCase(System.getProperty(SystemProperties.SANDBOX))) {
            logger.info("Running in sandbox mode ({}), setting {} as DATACLEANER_HOME", SystemProperties.SANDBOX,
                    candidate);
            if (!candidate.exists()) {
                candidate.createFolder();
            }
            return candidate;
        }

        if (!isUsable(candidate)) {
            if (!candidate.exists()) {
                logger.debug("DATACLEANER_HOME directory does not exist, creating: {}", candidate);
                candidate.createFolder();
            }

            if (candidate.isWriteable()) {
                logger.debug("Copying default configuration and examples to DATACLEANER_HOME directory: {}", candidate);
                copyIfNonExisting(candidate, manager, "conf.xml");
                copyIfNonExisting(candidate, manager, "datastores/contactdata.txt");
                copyIfNonExisting(candidate, manager, JOB_EXAMPLE_EXPORT_ORDERS_DATA);
                copyIfNonExisting(candidate, manager, JOB_EXAMPLE_CUSTOMER_PROFILING);
                copyIfNonExisting(candidate, manager, JOB_EXAMPLE_ADDRESS_CLEANSING);
                copyIfNonExisting(candidate, manager, JOB_EXAMPLE_PHONE_CLEANSING);
                copyIfNonExisting(candidate, manager, JOB_EXAMPLE_SFDC_DUPLICATE_DETECTION);
                copyIfNonExisting(candidate, manager, JOB_EXAMPLE_SFDC_DUPLICATE_TRAINING);
                copyIfNonExisting(candidate, manager, JOB_EXAMPLE_COPY_EMPLOYEES_TO_CUSTOMERS);
                copyIfNonExisting(candidate, manager, "jobs/sfdc_dupe_model_users.dedupmodel.xml");
            }
        }

        return candidate;
    }

    /**
     * @return a file reference to the DataCleaner home folder.
     */
    public static FileObject get() {
        return _dataCleanerHome;
    }

    private static FileObject copyIfNonExisting(FileObject candidate, FileSystemManager manager, String filename)
            throws FileSystemException {
        FileObject file = candidate.resolveFile(filename);
        if (file.exists()) {
            logger.info("File already exists in DATACLEANER_HOME: " + filename);
            return file;
        }
        FileObject parentFile = file.getParent();
        if (!parentFile.exists()) {
            parentFile.createFolder();
        }

        final ResourceManager resourceManager = ResourceManager.get();
        final URL url = resourceManager.getUrl("datacleaner-home/" + filename);
        if (url == null) {
            return null;
        }

        InputStream in = null;
        OutputStream out = null;
        try {
            in = url.openStream();
            out = file.getContent().getOutputStream();

            FileHelper.copy(in, out);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            FileHelper.safeClose(in, out);
        }
        
        return file;
    }

    private static boolean isUsable(FileObject candidate) throws FileSystemException {
        if (candidate != null) {
            if (candidate.exists() && candidate.getType() == FileType.FOLDER) {
                FileObject conf = candidate.resolveFile("conf.xml");
                if (conf.exists() && conf.getType() == FileType.FILE) {
                    return true;
                }
            }
        }
        return false;
    }
}
