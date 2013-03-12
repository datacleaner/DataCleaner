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
package org.eobjects.datacleaner.repository.file;

import java.io.File;

import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.repository.RepositoryNode;

/**
 * {@link Repository} implementation based on the local file system.
 */
public class FileRepository extends FileRepositoryFolder implements Repository {

    private static final long serialVersionUID = 1L;

    public FileRepository(String filename) {
        this(new File(filename));
    }

    public FileRepository(File file) {
        super(null, file);
    }

    @Override
    public RepositoryFolder getParent() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public RepositoryNode getRepositoryNode(String path) {
        RepositoryFolder folder = this;
        String[] pathParts = path.split("/");
        for (int i = 0; i < pathParts.length - 1; i++) {
            String pathPart = pathParts[i];
            if (!pathPart.isEmpty()) {
                folder = folder.getFolder(pathPart);
                if (folder == null) {
                    return null;
                }
            }
        }
        String lastPart = pathParts[pathParts.length - 1];
        if (lastPart.isEmpty()) {
            return folder;
        }
        RepositoryFile file = folder.getFile(lastPart);
        if (file == null) {
            return folder.getFolder(lastPart);
        }
        return file;
    }
}
