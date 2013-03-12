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
import org.eobjects.metamodel.util.FileHelper;
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
 * 
 * @author Kasper Sørensen
 */
public final class DataCleanerHome {

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
        }

        if (!isUsable(candidate)) {
            if (ClassLoaderUtils.IS_WEB_START) {
                // in web start, the default folder will be in user.home
                String userHomePath = System.getProperty("user.home");
                if (userHomePath == null) {
                    throw new IllegalStateException("Could not determine user home directory: " + candidate);
                }

                candidate = manager.resolveFile(userHomePath + File.separatorChar + ".datacleaner" + File.separatorChar
                        + Version.get());

            } else {
                // in normal mode, the default folder will be in the working
                // directory
                candidate = manager.resolveFile(".");
            }
        }

        if ("true".equalsIgnoreCase(System.getProperty(SystemProperties.SANDBOX))) {
            return candidate;
        }

        if (!isUsable(candidate)) {
            if (!candidate.exists()) {
                candidate.createFolder();
            }

            if (candidate.isWriteable()) {
                copyIfNonExisting(candidate, manager, "conf.xml");
                copyIfNonExisting(candidate, manager, "examples/countrycodes.csv");
                copyIfNonExisting(candidate, manager, "examples/employees.analysis.xml");
                copyIfNonExisting(candidate, manager, "examples/duplicate_customer_detection.analysis.xml");
                copyIfNonExisting(candidate, manager, "examples/customer_data_cleansing.analysis.xml");
                copyIfNonExisting(candidate, manager, "examples/write_order_information.analysis.xml");
                copyIfNonExisting(candidate, manager, "examples/customer_data_completeness.analysis.xml");
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

    private static void copyIfNonExisting(FileObject candidate, FileSystemManager manager, String filename)
            throws FileSystemException {
        FileObject file = candidate.resolveFile(filename);
        if (file.exists()) {
            logger.info("File already exists in DATACLEANER_HOME: " + filename);
            return;
        }
        FileObject parentFile = file.getParent();
        if (!parentFile.exists()) {
            parentFile.createFolder();
        }

        final ResourceManager resourceManager = ResourceManager.getInstance();
        final URL url = resourceManager.getUrl("datacleaner-home/" + filename);

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
