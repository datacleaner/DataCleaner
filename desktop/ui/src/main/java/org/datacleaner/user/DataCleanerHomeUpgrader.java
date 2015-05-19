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
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.Version;
import org.datacleaner.util.ResourceManager;
import org.datacleaner.util.VFSUtils;
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
            final FileSystemManager manager = VFSUtils.getFileSystemManager();

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
                copyFile(target, manager, filePath, true);
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

        List<FileObject> validateVersionFolders = validateVersionFolders(versionFolders);

        if (!validateVersionFolders.isEmpty()) {

            FileObject latestVersion = Collections.max(validateVersionFolders, new Comparator<FileObject>() {

                @Override
                public int compare(FileObject o1, FileObject o2) {
                    String o1BaseName = o1.getName().getBaseName();
                    String o2BaseName = o2.getName().getBaseName();

                    String[] o1Split = o1BaseName.split("\\.");
                    String[] o2Split = o2BaseName.split("\\.");

                    for (int i = 0; i < Math.min(o1Split.length, o2Split.length); i++) {
                        Integer o1Part;
                        if (o1Split[i].endsWith("-SNAPSHOT")) {
                            o1Part = Integer.parseInt(o1Split[i].substring(0, o1Split[i].lastIndexOf("-SNAPSHOT")));
                        } else {
                            o1Part = Integer.parseInt(o1Split[i]);
                        }
                        Integer o2Part;
                        if (o2Split[i].endsWith("-SNAPSHOT")) {
                            o2Part = Integer.parseInt(o2Split[i].substring(0, o2Split[i].lastIndexOf("-SNAPSHOT")));
                        } else {
                            o2Part = Integer.parseInt(o2Split[i]);
                        }

                        int compareTo = o1Part.compareTo(o2Part);
                        if (compareTo == 0) {
                            // check another part
                            continue;
                        } else {
                            return compareTo;
                        }
                    }

                    Integer o1SplitLength = (Integer) o1Split.length;
                    Integer o2SplitLength = (Integer) o2Split.length;
                    return o1SplitLength.compareTo(o2SplitLength);
                }
            });
            return latestVersion;
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

    private static FileObject copyFile(FileObject candidate, FileSystemManager manager, String filename,
            boolean overwriteIfExists) throws FileSystemException {
        // TODO: this method is also in DataCleanerHome - extract a helper or
        // sth

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

}
