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
package org.datacleaner.user.upgrade;

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
import org.datacleaner.user.DataCleanerHome;
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

    private static List<FileObject> validateVersionFolders(final List<FileObject> versionFolders) {
        final List<FileObject> validatedVersionFolders = new ArrayList<>();
        final Integer currentMajorVersion = Version.getMajorVersion();
        if (currentMajorVersion == null) {
            return validatedVersionFolders;
        }

        for (final FileObject versionFolder : versionFolders) {
            final String baseName = versionFolder.getName().getBaseName();

            final String[] versionParts = baseName.split("\\.");

            try {
                final int majorVersion = Integer.parseInt(versionParts[0]);
                if (majorVersion != currentMajorVersion) {
                    continue;
                }
            } catch (final NumberFormatException e) {
                continue;
            }

            boolean validated = true;
            for (String versionPart : versionParts) {
                if (versionPart.endsWith("-SNAPSHOT")) {
                    versionPart = versionPart.substring(0, versionPart.lastIndexOf("-SNAPSHOT"));
                }
                try {
                    Integer.parseInt(versionPart);
                } catch (final NumberFormatException e) {
                    logger.warn("Found a version folder in home directory ({}) with a part that could not be parsed "
                                    + "to an integer: {} Removing this folder from potential upgrade candidates.", baseName,
                            versionPart);
                    validated = false;
                }
            }
            if (validated) {
                validatedVersionFolders.add(versionFolder);
            }
        }
        return validatedVersionFolders;
    }

    private static FileObject overwriteFileWithDefaults(final FileObject targetDirectory, final String targetFilename)
            throws FileSystemException {
        final FileObject file = targetDirectory.resolveFile(targetFilename);
        final FileObject parentFile = file.getParent();
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
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            FileHelper.safeClose(in, out);
        }

        return file;
    }

    /**
     * Finds a folder to upgrade from based on the "newFolder" parameter -
     * upgrades are performed only within the same major version.
     *
     * @param newFolder
     *            The folder we want to upgrade to (the new version)
     * @return true if upgrade was successful, false otherwise
     */
    public boolean upgrade(final FileObject newFolder) {
        try {
            if (newFolder.getChildren().length != 0) {
                // if the folder is not new then we don't want to touch it
                return false;
            }

            final FileObject upgradeFromFolderCandidate = findUpgradeCandidate(newFolder);

            if (upgradeFromFolderCandidate == null) {
                logger.info("Did not find a suitable upgrade candidate");
                return false;
            }

            logger.info("Upgrading DATACLEANER_HOME from : {}", upgradeFromFolderCandidate);
            newFolder.copyFrom(upgradeFromFolderCandidate, new AllFileSelector());

            // special handling of userpreferences.dat - we only want to keep
            // the good parts ;-)
            final UserPreferencesUpgrader userPreferencesUpgrader = new UserPreferencesUpgrader(newFolder);
            userPreferencesUpgrader.upgrade();

            // Overwrite example jobs
            final List<String> allFilePaths = DataCleanerHome.getAllInitialFiles();
            for (final String filePath : allFilePaths) {
                overwriteFileWithDefaults(newFolder, filePath);
            }
            return true;
        } catch (final FileSystemException e) {
            logger.warn("Exception occured during upgrading: {}", e);
            return false;
        }
    }

    private FileObject findUpgradeCandidate(final FileObject target) throws FileSystemException {
        final FileObject parentFolder = target.getParent();

        final List<FileObject> versionFolders = new ArrayList<>();
        final FileObject[] allFoldersInParent = parentFolder.findFiles(new FileDepthSelector(1, 1));
        for (final FileObject folderInParent : allFoldersInParent) {
            final String folderInParentName = folderInParent.getName().getBaseName();
            if (folderInParent.getType().equals(FileType.FOLDER) && (!folderInParentName
                    .equals(target.getName().getBaseName())) && (!candidateBlacklist.contains(folderInParentName))) {
                versionFolders.add(folderInParent);
            }
        }

        final List<FileObject> validatedVersionFolders = validateVersionFolders(versionFolders);

        if (!validatedVersionFolders.isEmpty()) {

            final List<String> versions = new ArrayList<>();
            for (final FileObject validatedVersionFolder : validatedVersionFolders) {
                final String baseName = validatedVersionFolder.getName().getBaseName();
                versions.add(baseName);
            }

            final Comparator<String> comp = new VersionComparator();
            final String latestVersion = Collections.max(versions, comp);
            FileObject latestVersionFolder = null;
            for (final FileObject validatedVersionFolder : validatedVersionFolders) {
                if (validatedVersionFolder.getName().getBaseName().equals(latestVersion)) {
                    latestVersionFolder = validatedVersionFolder;
                }
            }
            return latestVersionFolder;
        } else {
            return null;
        }
    }

}
