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
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.Version;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerHomeFolder;
import org.datacleaner.configuration.DataCleanerHomeFolderImpl;
import org.datacleaner.extensions.ClassLoaderUtils;
import org.datacleaner.repository.file.FileRepository;
import org.datacleaner.util.ResourceManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.SystemProperties;
import org.datacleaner.util.VFSUtils;
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

        String path = System.getenv("DATACLEANER_HOME");
        if (!StringUtils.isNullOrEmpty(path)) {
            logger.info("Resolved env. variable DATACLEANER_HOME: {}", path);
        } else {
            path = System.getProperty("DATACLEANER_HOME");
            if (!StringUtils.isNullOrEmpty(path)) {
                candidate = manager.resolveFile(path);
                logger.info("Resolved system property DATACLEANER_HOME: {}", path, candidate);
            }
        }

        if (!StringUtils.isNullOrEmpty(path)) {
            if (path.startsWith("~")) {
                String userHomePath = System.getProperty("user.home");
                path = path.replace("~", userHomePath);
            }
            candidate = manager.resolveFile(path);
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
            // in normal mode try to use specified directory first
            logger.info("Running in standard mode.");
            if (isWriteable(candidate)) {
                logger.info("Attempting to build DATACLEANER_HOME in {}", candidate);
            } else {
                // Workaround: isWritable is not reliable for a non-existent
                // directory. Trying to create it, if it does not exist.
                if ((candidate != null) && (!candidate.exists())) {
                    logger.info("Folder {} does not exist. Trying to create it.", candidate);
                    try {
                        candidate.createFolder();
                        logger.info("Folder {} created successfully. Attempting to build DATACLEANER_HOME here.",
                                candidate);
                    } catch (FileSystemException e) {
                        logger.info("Unable to create folder {}. No write permission in that location.", candidate);
                        candidate = initializeDataCleanerHomeFallback();
                    }
                } else {
                    candidate = initializeDataCleanerHomeFallback();
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
            DataCleanerHomeUpgrader upgrader = new DataCleanerHomeUpgrader();
            FileObject upgradeCandidate = upgrader.upgrade(candidate);

            if (upgradeCandidate == null) {
                logger.debug("Copying default configuration and examples to DATACLEANER_HOME directory: {}", candidate);
                copyFile(candidate, manager, "conf.xml", false);

                final List<String> allFilePaths = DemoConfiguration.getAllFilePaths();
                for (String filePath : allFilePaths) {
                    copyFile(candidate, manager, filePath, false);
                }
            }
        }
        return candidate;
    }

    private static FileObject initializeDataCleanerHomeFallback() throws FileSystemException {
        final FileSystemManager manager = VFSUtils.getFileSystemManager();

        FileObject candidate;

        // Fallback to user home directory
        final String path = getUserHomeCandidatePath();
        candidate = manager.resolveFile(path);
        logger.info("Attempting to build DATACLEANER_HOME in user.home: {} -> {}", path, candidate);
        if (!isWriteable(candidate)) {
            // Workaround: isWritable is not reliable for a non-existent
            // directory. Trying to create it, if it does not exist.
            if ((candidate != null) && (!candidate.exists())) {
                logger.info("Folder {} does not exist. Trying to create it.", candidate);
                try {
                    candidate.createFolder();
                    logger.info("Folder {} created successfully. Attempting to build DATACLEANER_HOME here.", candidate);
                } catch (FileSystemException e) {
                    logger.info("Unable to create folder {}. No write permission in that location.", candidate);
                    throw new IllegalStateException("User home directory (" + candidate
                            + ") is not writable. DataCleaner requires write access to its home directory.");
                }
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

    public static DataCleanerHomeFolder getAsDataCleanerHomeFolder() {
        final File file = getAsFile();
        if (file == null) {
            return DataCleanerConfigurationImpl.defaultHomeFolder();
        }
        final FileRepository fileRepository = new FileRepository(file);
        return new DataCleanerHomeFolderImpl(fileRepository);
    }

    public static File getAsFile() {
        return VFSUtils.toFile(_dataCleanerHome);
    }

    private static FileObject copyFile(FileObject candidate, FileSystemManager manager, String filename,
            boolean overwriteIfExists) throws FileSystemException {
        FileObject file = candidate.resolveFile(filename);
        if (file.exists()) {
            if (!overwriteIfExists) {
                logger.info("File already exists in DATACLEANER_HOME: " + filename);
                return file;
            }
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
