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
package org.datacleaner.repository;

import java.io.OutputStream;
import java.util.List;

import org.apache.metamodel.util.Action;

/**
 * Represents a folder in the {@link Repository}.
 */
public interface RepositoryFolder extends RepositoryNode {

    /**
     * Get (sub)folders of this folder.
     *
     * @return (sub)folders of this folder.
     */
    List<RepositoryFolder> getFolders();

    /**
     * Gets a (sub)folder of this folder, by name.
     *
     * @param name
     *            the name of the (sub)folder.
     * @return a (sub)folder of this folder, or null if it does not exist.
     */
    RepositoryFolder getFolder(String name);

    /**
     * Gets or creates a (sub) folder of this folder.
     *
     * @param name
     *            the name of the (sub)folder.
     * @return a (sub) folder of this folder
     */
    RepositoryFolder getOrCreateFolder(String name);

    /**
     * Gets files in this folder.
     *
     * @return files in this folder.
     */
    List<RepositoryFile> getFiles();

    /**
     * Gets files in this folder which have a particular extension.
     *
     * @param prefix
     *            an (optional, can be null) prefix for file selection.
     * @param extension
     *            the filename extension to look for, eg. ".analysis.xml".
     *
     * @return files in this folder.
     */
    List<RepositoryFile> getFiles(String prefix, String extension);

    /**
     * Gets the latest (newest / latest modified) file with the given prefix and
     * extension.
     *
     * @param prefix
     *            an (optional, can be null) prefix for file selection.
     * @param extension
     *            the filename extension to look for, eg. ".analysis.xml".
     * @return the latest of the files that match the conditions, or null if no
     *         files match.
     */
    RepositoryFile getLatestFile(String prefix, String extension);

    /**
     * Gets a file in this folder, by name.
     *
     * @param name
     *            the name of the file.
     * @return a file in this folder, or null if none exist with the specified
     *         name.
     */
    RepositoryFile getFile(String name);

    /**
     * Creates a new file in this folder.
     *
     * @param name
     *            the name of the file.
     * @param writeCallback
     *            a callback which should define what to write to the file, or
     *            null if nothing should be written.
     * @return the {@link RepositoryFile} reference to the newly created file.
     */
    RepositoryFile createFile(String name, Action<OutputStream> writeCallback);

    /**
     * Creates a new subfolder in this folder.
     *
     * @param name
     *            the name of the folder.
     * @return the resulting {@link RepositoryFolder} object.
     *
     * @throws IllegalArgumentException
     *             if a folder with this name already exists.
     */
    RepositoryFolder createFolder(String name) throws IllegalArgumentException;
}
