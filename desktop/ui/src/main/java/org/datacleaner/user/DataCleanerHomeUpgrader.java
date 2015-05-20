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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileDepthSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.Version;
import org.datacleaner.VersionComparator;
import org.datacleaner.util.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks if there is DataCleanerHome set up for previous versions of
 * DataCleaner and upgrades.
 */
public final class DataCleanerHomeUpgrader {
    
    private static final Logger logger = LoggerFactory.getLogger(DataCleanerHomeUpgrader.class);

    private static final List<String> candidateBlacklist = Arrays.asList("log", Version.UNKNOWN_VERSION);

    public boolean upgrade(FileObject target) {
        try {
            FileObject upgradeCandidate = findUpgradeCandidate(target.getParent());

            if (upgradeCandidate == null) {
                logger.info("Did not find a suitable upgrade candidate");
                return false;
            }

            logger.info("Upgrading DATACLEANER_HOME from : {}", upgradeCandidate);
            target.copyFrom(upgradeCandidate, new AllFileSelector());

            // Overwrite example jobs
            final List<String> allFilePaths = DemoConfiguration.getAllFilePaths();
            for (String filePath : allFilePaths) {
                owerwriteFileWithDefaults(target, filePath);
            }
            return true;
        } catch (FileSystemException e) {
            logger.warn("Exception occured during upgrading: {}", e);
            return false;
        }
    }

    private FileObject findUpgradeCandidate(FileObject parentFolder) throws FileSystemException {
        List<FileObject> versionFolders = new ArrayList<>();
        FileObject[] allFoldersInParent = parentFolder.findFiles(new FileDepthSelector(1, 1));
        for (FileObject folderInParent : allFoldersInParent) {
            final String folderInParentName = folderInParent.getName().getBaseName();
            if (folderInParent.getType().equals(FileType.FOLDER) && (!candidateBlacklist.contains(folderInParentName))) {
                versionFolders.add(folderInParent);
            }
        }

        List<FileObject> validatedVersionFolders = validateVersionFolders(versionFolders);

        if (!validatedVersionFolders.isEmpty()) {

            List<String> versions = new ArrayList<>();
            for (FileObject validatedVersionFolder : validatedVersionFolders) {
                String baseName = validatedVersionFolder.getName().getBaseName();
                versions.add(baseName);
            }
            
            final Comparator<String> comp = new VersionComparator();           
            String latestVersion = Collections.max(versions, comp);
            FileObject latestVersionFolder = null; 
            for (FileObject validatedVersionFolder : validatedVersionFolders) {
                if (validatedVersionFolder.getName().getBaseName().equals(latestVersion)) {
                    latestVersionFolder = validatedVersionFolder; 
                }
            }
            return latestVersionFolder;
        } else {
            return null;
        }
    }

    private static List<FileObject> validateVersionFolders(List<FileObject> versionFolders) {
        List<FileObject> validatedVersionFolders = new ArrayList<>();
        Integer currentMajorVersion = Version.getMajorVersion();
        if (currentMajorVersion == null) {
            return validatedVersionFolders;
        }

        for (FileObject versionFolder : versionFolders) {
            String baseName = versionFolder.getName().getBaseName();

            String[] versionParts = baseName.split("\\.");

            try {
                int majorVersion = Integer.parseInt(versionParts[0]);
                if (majorVersion != currentMajorVersion) {
                    continue;
                }
            } catch (NumberFormatException e) {
                continue;
            }

            boolean validated = true;
            for (String versionPart : versionParts) {
                if (versionPart.endsWith("-SNAPSHOT")) {
                    versionPart = versionPart.substring(0, versionPart.lastIndexOf("-SNAPSHOT"));
                }
                try {
                    Integer.parseInt(versionPart);
                } catch (NumberFormatException e) {
                    logger.warn(
                            "Found a version folder in home directory ({}) with a part that could not be parsed to an integer: {} Removing this folder from potential upgrade candidates.",
                            baseName, versionPart);
                    validated = false;
                }
            }
            if (validated) {
                validatedVersionFolders.add(versionFolder);
            }
        }
        return validatedVersionFolders;
    }

    private static FileObject owerwriteFileWithDefaults(FileObject targetDirectory, String targetFilename) throws FileSystemException {
        FileObject file = targetDirectory.resolveFile(targetFilename);
        FileObject parentFile = file.getParent();
        if (!parentFile.exists()) {
            parentFile.createFolder();
        }

        final ResourceManager resourceManager = ResourceManager.get();
        final URL url = resourceManager.getUrl("datacleaner-home/" + targetFilename);
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

}
