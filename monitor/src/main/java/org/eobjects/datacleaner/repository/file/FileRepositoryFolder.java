/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
import java.io.FileFilter;
import java.io.OutputStream;
import java.util.List;

import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Func;

/**
 * {@link RepositoryFolder} implementation based on a local directory.
 */
class FileRepositoryFolder implements RepositoryFolder {

    private static final long serialVersionUID = 1L;

    private final File _file;
    private final RepositoryFolder _parent;

    public FileRepositoryFolder(RepositoryFolder parent, File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }
        _parent = parent;
        _file = file;
    }

    protected File getFile() {
        return _file;
    }

    @Override
    public RepositoryFolder getParent() {
        return _parent;
    }

    @Override
    public String getName() {
        return _file.getName();
    }

    @Override
    public String getQualifiedPath() {
        if (_parent == null || _parent instanceof Repository) {
            return "/" + getName();
        }
        return _parent.getQualifiedPath() + "/" + getName();
    }

    @Override
    public List<RepositoryFolder> getFolders() {
        File[] directories = _file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory() && !file.isHidden()) {
                    return true;
                }
                return false;
            }
        });

        return CollectionUtils.map(directories, new Func<File, RepositoryFolder>() {
            @Override
            public RepositoryFolder eval(File directory) {
                return new FileRepositoryFolder(FileRepositoryFolder.this, directory);
            }
        });
    }

    @Override
    public List<RepositoryFile> getFiles() {
        File[] files = _file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isFile() && !file.isHidden()) {
                    return true;
                }
                return false;
            }
        });

        return CollectionUtils.map(files, new Func<File, RepositoryFile>() {
            @Override
            public RepositoryFile eval(File file) {
                return new FileRepositoryFile(FileRepositoryFolder.this, file);
            }
        });
    }

    @Override
    public RepositoryFile getFile(String name) {
        if (name.indexOf('/') != -1 || name.indexOf('\\') != -1) {
            throw new IllegalArgumentException("File name cannot contain slashes");
        }

        File file = new File(_file, name);

        if (!file.exists()) {
            return null;
        }
        if (file.isHidden()) {
            return null;
        }

        if (!file.isFile()) {
            return null;
        }
        return new FileRepositoryFile(this, file);
    }

    @Override
    public RepositoryFolder getFolder(String name) {
        if (name.indexOf('/') != -1 || name.indexOf('\\') != -1) {
            throw new IllegalArgumentException("Folder name cannot contain slashes");
        }

        File file = new File(_file, name);
        if (!file.exists() || file.isHidden() || !file.isDirectory()) {
            return null;
        }
        return new FileRepositoryFolder(this, file);
    }

    @Override
    public String toString() {
        return getQualifiedPath();
    }

    @Override
    public RepositoryFile createFile(String name, Action<OutputStream> writeCallback) {
        if (name.indexOf('/') != -1 || name.indexOf('\\') != -1) {
            throw new IllegalArgumentException("File name cannot contain slashes");
        }

        final File file = new File(_file, name);
        if (file.exists()) {
            throw new IllegalArgumentException("A file with the name '" + name + "' already exists");
        }

        RepositoryFile repositoryFile = new FileRepositoryFile(this, file);
        repositoryFile.writeFile(writeCallback);

        return repositoryFile;
    }

    @Override
    public void delete() throws IllegalStateException {
        final boolean success = _file.delete();
        if (!success) {
            throw new IllegalStateException("Could not delete directory: " + _file);
        }
    }
}
