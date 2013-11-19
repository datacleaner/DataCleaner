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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.FileHelper;
import org.eobjects.metamodel.util.Func;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RepositoryFile} implementation based on a local file.
 */
public final class FileRepositoryFile implements RepositoryFile {

    private static final Logger logger = LoggerFactory.getLogger(FileRepositoryFile.class);

    private static final long serialVersionUID = 1L;

    private final FileRepositoryFolder _parent;
    private final File _file;

    public FileRepositoryFile(FileRepositoryFolder parent, File file) {
        _parent = parent;
        _file = file;
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
    public long getSize() {
        return _file.length();
    }

    @Override
    public String getQualifiedPath() {
        if (_parent == null || _parent instanceof Repository) {
            return "/" + getName();
        }
        return _parent.getQualifiedPath() + "/" + getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public InputStream readFile() {
        try {
            final FileInputStream in = new FileInputStream(_file);
            final BufferedInputStream bin = new BufferedInputStream(in);
            return bin;
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void readFile(Action<InputStream> readCallback) {
        final FileInputStream fileInputStream;
        final InputStream inputStream;
        try {
            fileInputStream = new FileInputStream(_file);
            inputStream = new BufferedInputStream(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }

        try {
            readCallback.run(inputStream);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Error occurred while reading from file", e);
        } finally {
            FileHelper.safeClose(inputStream, fileInputStream);
        }
    }

    @Override
    public <E> E readFile(Func<InputStream, E> readCallback) {
        final FileInputStream fileInputStream;
        final InputStream inputStream;
        try {
            fileInputStream = new FileInputStream(_file);
            inputStream = new BufferedInputStream(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }

        try {
            final E result = readCallback.eval(inputStream);
            return result;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Error occurred while writing to file", e);
        } finally {
            FileHelper.safeClose(inputStream, fileInputStream);
        }
    }

    @Override
    public void writeFile(Action<OutputStream> writeCallback) {
        final FileOutputStream fileOutputStream;
        final OutputStream outputStream;
        try {
            fileOutputStream = new FileOutputStream(_file);
            outputStream = new BufferedOutputStream(fileOutputStream);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }

        try {
            if (writeCallback != null) {
                writeCallback.run(outputStream);
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Error occurred while writing to file", e);
        } finally {
            FileHelper.safeClose(outputStream, fileOutputStream);
        }
    }

    @Override
    public Type getType() {
        if (getName().endsWith(FileFilters.ANALYSIS_XML.getExtension())) {
            return Type.ANALYSIS_JOB;
        } else if (getName().endsWith(FileFilters.ANALYSIS_RESULT_SER.getExtension())) {
            return Type.ANALYSIS_RESULT;
        } else if (getName().endsWith(FileFilters.ANALYSIS_TIMELINE_XML.getExtension())) {
            return Type.TIMELINE_SPEC;
        }

        return Type.OTHER;
    }

    @Override
    public String toString() {
        return getQualifiedPath();
    }

    @Override
    public void delete() throws IllegalStateException {
        final boolean success = _file.delete();
        if (!success) {
            throw new IllegalStateException("Could not delete file: " + _file);
        }
    }

    @Override
    public int hashCode() {
        return getQualifiedPath().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof RepositoryFile) {
            String otherQualifiedPath = ((RepositoryFile) obj).getQualifiedPath();
            return getQualifiedPath().equals(otherQualifiedPath);
        }
        return false;
    }

    @Override
    public long getLastModified() {
        long lastModified = _file.lastModified();
        if (lastModified == 0) {
            if (logger.isWarnEnabled()) {
                logger.warn("File.lastModified() return 0. File.exists()={}, File.getPath()={}",
                        Boolean.valueOf(_file.exists()), _file.getPath());
            }
            return -1;
        }
        return lastModified;
    }
}
