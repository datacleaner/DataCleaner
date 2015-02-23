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
package org.datacleaner.user;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.extensions.ClassLoaderUtils;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.VFSUtils;
import org.datacleaner.Version;
import org.datacleaner.util.ResourceManager;
import org.datacleaner.util.SystemProperties;
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

    // note: Logger is specified using a string. This is because the logger is
    // to be used also in the static initializer and any error in that code
    // would otherwise be swallowed.
    private static final Logger logger;

    private static final FileObject _dataCleanerHome;

    static {
        logger = LoggerFactory.getLogger("org.datacleaner.user.DataCleanerHome");
        logger.info("Initializing DATACLEANER_HOME");
        try {
            _dataCleanerHome = findDataCleanerHome();
        } catch (Exception e) {
            logger.error("Failed to initialize DATACLEANER_HOME!", e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
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
        } else {
            final String sysProp = System.getProperty("DATACLEANER_HOME");
            if (!StringUtils.isNullOrEmpty(sysProp)) {
                candidate = manager.resolveFile(sysProp);
                logger.info("Resolved system property DATACLEANER_HOME ({}) to: {}", sysProp, candidate);
            }
        }

        if (isUsable(candidate)) {
            // Found a directory with conf.xml already there
            return candidate;
        } else {
            return initializeDataCleanerHome(candidate);
        }

    }

    private static FileObject initializeDataCleanerHome(FileObject candidate) throws FileSystemException {
        final FileSystemManager manager = VFSUtils.getFileSystemManager();

        if (ClassLoaderUtils.IS_WEB_START) {
            // in web start, the default folder will be in user.home
            final String path = getUserHomeCandidatePath();
            candidate = manager.resolveFile(path);
            logger.info("Running in WebStart mode. Attempting to build DATACLEANER_HOME in user.home: {} -> {}", path,
                    candidate);
        } else {
            // in normal mode it is trying to use specified directory first,
            // working directory as a fallback or user home directory as a
            // second fallback.
            if (isWriteable(candidate)) {
                logger.info("Running in standard mode. Attempting to build DATACLEANER_HOME in {}", candidate);
            } else {
                // Workaround: isWritable is not reliable for a non-existent
                // directory. Just create it and check again.
                logger.debug(
                        "DATACLEANER_HOME ({}) appears to be not writable. Applying a workaround: creating this directory and checking again.",
                        candidate);
                if ((candidate != null) && (!candidate.exists())) {
                    candidate.createFolder();
                }
                if (isWriteable(candidate)) {
                    logger.info("Building DATACLEANER_HOME in {}", candidate);
                } else {
                    
                    candidate = manager.resolveFile(".");
                    if (isWriteable(candidate)) {
                        logger.info(
                                "Application directory is not writeable. Attempting to build DATACLEANER_HOME in the working directory: {}",
                                candidate);
                    } else {
                        final String path = getUserHomeCandidatePath();
                        candidate = manager.resolveFile(path);
                        logger.info(
                                "Application directory is not writeable. Attempting to build DATACLEANER_HOME in user.home: {} -> {}",
                                path, candidate);
                    }
                }
            }

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

            if (isWriteable(candidate)) {
                logger.debug("Copying default configuration and examples to DATACLEANER_HOME directory: {}", candidate);
                copyIfNonExisting(candidate, manager, "conf.xml");
                copyIfNonExisting(candidate, manager, DemoConfiguration.DATASTORE_FILE_CONTACTDATA);
                copyIfNonExisting(candidate, manager, DemoConfiguration.JOB_EXPORT_ORDERS_DATA);
                copyIfNonExisting(candidate, manager, DemoConfiguration.JOB_CUSTOMER_PROFILING);
                copyIfNonExisting(candidate, manager, DemoConfiguration.JOB_ADDRESS_CLEANSING);
                copyIfNonExisting(candidate, manager, DemoConfiguration.JOB_PHONE_CLEANSING);
                copyIfNonExisting(candidate, manager, DemoConfiguration.JOB_SFDC_DUPLICATE_DETECTION);
                copyIfNonExisting(candidate, manager, DemoConfiguration.JOB_SFDC_DUPLICATE_TRAINING);
                copyIfNonExisting(candidate, manager, DemoConfiguration.OTHER_DEDUP_MODEL_SFDC_USERS);
                copyIfNonExisting(candidate, manager, DemoConfiguration.JOB_ORDERDB_DUPLICATE_DETECTION);
                copyIfNonExisting(candidate, manager, DemoConfiguration.JOB_ORDERDB_DUPLICATE_TRAINING);
                copyIfNonExisting(candidate, manager, DemoConfiguration.OTHER_DEDUP_MODEL_ORDERDB_CUSTOMERS);
                copyIfNonExisting(candidate, manager, DemoConfiguration.OTHER_DEDUP_REFERENCE_ORDERDB_CUSTOMERS);
                copyIfNonExisting(candidate, manager, DemoConfiguration.JOB_COPY_EMPLOYEES_TO_CUSTOMERS);
                copyIfNonExisting(candidate, manager, DemoConfiguration.JOB_US_CUSTOMER_STATE_ANALYSIS);
            }
        }

        return candidate;
    }

    private static boolean isWriteable(FileObject candidate) throws FileSystemException {
        if (candidate == null) {
            return false;
        }

        if (!candidate.isWriteable()) {
            return false;
        }

        // check with java.nio.Files.isWriteable() - is more detailed in it's
        // check
        final File file = VFSUtils.toFile(candidate);
        final Path path = file.toPath();
        return Files.isWritable(path);
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

    private static String getUserHomeCandidatePath() {
        final String userHomePath = System.getProperty("user.home");
        final String path = userHomePath + File.separatorChar + ".datacleaner" + File.separatorChar
                + Version.getVersion();
        return path;
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
