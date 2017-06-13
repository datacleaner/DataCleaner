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
package org.datacleaner.repository.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.repository.AbstractRepositoryNode;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.util.FileFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RepositoryFile} implementation based on a local file.
 */
public final class FileRepositoryFile extends AbstractRepositoryNode implements RepositoryFile {

    private static final Logger logger = LoggerFactory.getLogger(FileRepositoryFile.class);

    private static final long serialVersionUID = 1L;

    private final ReadWriteLock _lock;
    private final FileRepositoryFolder _parent;
    private final File _file;

    public FileRepositoryFile(final FileRepositoryFolder parent, final File file) {
        _parent = parent;
        _file = file;
        _lock = new ReentrantReadWriteLock();
    }

    /**
     * Gets the physical {@link File} that is backing this
     * {@link RepositoryFile} instance.
     *
     * @return
     */
    public File getFile() {
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
    public long getSize() {
        return _file.length();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public InputStream readFile() {
        try {
            final FileInputStream in = new FileInputStream(_file);
            return new BufferedInputStream(in);
        } catch (final FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void readFile(final Action<InputStream> readCallback) {
        final Lock readLock = _lock.readLock();
        readLock.lock();
        try {
            final FileInputStream fileInputStream;
            final InputStream inputStream;
            try {
                fileInputStream = new FileInputStream(_file);
                inputStream = new BufferedInputStream(fileInputStream);
            } catch (final FileNotFoundException e) {
                throw new IllegalStateException(e);
            }

            try {
                readCallback.run(inputStream);
            } catch (final Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new IllegalStateException("Error occurred while reading from file", e);
            } finally {
                FileHelper.safeClose(inputStream, fileInputStream);
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public <E> E readFile(final Function<InputStream, E> readCallback) {
        final Lock readLock = _lock.readLock();
        readLock.lock();
        try {
            final FileInputStream fileInputStream;
            final InputStream inputStream;
            try {
                fileInputStream = new FileInputStream(_file);
                inputStream = new BufferedInputStream(fileInputStream);
            } catch (final FileNotFoundException e) {
                throw new IllegalStateException(e);
            }

            try {
                return readCallback.apply(inputStream);
            } catch (final Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new IllegalStateException("Error occurred while writing to file", e);
            } finally {
                FileHelper.safeClose(inputStream, fileInputStream);
            }
        } finally {
            readLock.unlock();
        }
    }


    @Override
    public OutputStream writeFile(final boolean append) {
        try {
            return new FileOutputStream(_file, append);
        } catch (final FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void writeFile(final Action<OutputStream> writeCallback) {
        writeFile(writeCallback, false);
    }

    @Override
    public void writeFile(final Action<OutputStream> writeCallback, final boolean append) {
        final Lock writeLock = _lock.writeLock();
        writeLock.lock();
        try {
            final FileOutputStream fileOutputStream;
            final OutputStream outputStream;
            try {
                fileOutputStream = new FileOutputStream(_file, append);
                outputStream = new BufferedOutputStream(fileOutputStream);
            } catch (final FileNotFoundException e) {
                throw new IllegalStateException(e);
            }

            try {
                if (writeCallback != null) {
                    writeCallback.run(outputStream);
                }
            } catch (final Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new IllegalStateException("Error occurred while writing to file", e);
            } finally {
                FileHelper.safeClose(outputStream, fileOutputStream);
            }

        } finally {
            writeLock.unlock();
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
    public void delete() throws IllegalStateException {
        final boolean success = _file.delete();
        if (!success) {
            throw new IllegalStateException("Could not delete file: " + _file);
        }
        _parent.onDeleted(_file);
    }

    @Override
    public long getLastModified() {
        final long lastModified = _file.lastModified();
        if (lastModified == 0) {
            if (logger.isWarnEnabled()) {
                logger.warn("File.lastModified() return 0. File.exists()={}, File.getPath()={}",
                        Boolean.valueOf(_file.exists()), _file.getPath());
            }
            return -1;
        }
        return lastModified;
    }

    @Override
    public Resource toResource() {
        return new FileResource(_file);
    }
}
